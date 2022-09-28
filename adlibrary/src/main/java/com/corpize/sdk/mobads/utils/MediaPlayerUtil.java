package com.corpize.sdk.mobads.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.text.TextUtils;

import java.io.IOException;
import java.util.List;

/**
 * author ：yh
 * date : 2019-08-02 13:47
 * description : 音频播放工具类
 */
public class MediaPlayerUtil {

    private static MediaPlayerUtil            sAudioRecorderUtil;
    private static MediaPlayer                mMediaPlayer;
    //其他app播放时,需要请求音频焦点
    private        AudioManager               mAudioManager;
    private        OnAudioFocusChangeListener mAudioFocusChangeListener = null;
    private        int                        mCurrentPosition          = 0;//当前播放的List的位置
    private        int                        mSize                     = 0;
    private        List<String>               mVoiceList;
    private        boolean                    mIsRelease                = true;//播放完成后是否释放MediaPlayer(不释放则说明有两个音频连续播放)

    //单例模式
    public static MediaPlayerUtil getInstance () {
        if (sAudioRecorderUtil == null) {
            sAudioRecorderUtil = new MediaPlayerUtil();
        }
        return sAudioRecorderUtil;
    }

    private MediaPlayerUtil () {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
    }

    /**
     * 播放音频文件
     *
     * @param voicePath       地址(本地或者网络)
     * @param mediaOnListener 回调
     */
    public void playVoice (Context context, String voicePath, final MediaOnListener mediaOnListener) {
        playVoice(context, voicePath, true, mediaOnListener);
    }

    /**
     * @param voicePath
     * @param isRelease
     * @param mediaOnListener
     */
    public void playVoice (Context context, String voicePath, boolean isRelease, final MediaOnListener mediaOnListener) {
        mMediaOnListener = mediaOnListener;
        mIsRelease = isRelease;

        if (TextUtils.isEmpty(voicePath)) {
            //LogUtils.e("音频播放地址为空");
            if (mMediaOnListener != null) {
                mMediaOnListener.OnPlayErrorListener(1000, "播放音频地址为空");
            }
            return;
        }

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        } else {
            mMediaPlayer.reset();
        }

        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //请求焦点
        requestAudioFocus(context);

        try {
            String voice = "http://fjdx.sc.chinaz.net/Files/DownLoad/sound1/201501/5394.mp3";
            mMediaPlayer.setDataSource(voicePath);  //指定音频文件的路径
            mMediaPlayer.prepareAsync();            //让mediaplayer进入准备状态
        } catch (IOException e) {
            LogUtils.e("音频播放MediaPlayer初始化失败");
            if (mMediaOnListener != null) {
                mMediaOnListener.OnPlayErrorListener(1001, "播放音频初始化失败");
            }
            stopAndRelease();
            e.printStackTrace();
        }

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared (final MediaPlayer mp) {
                mp.start();//开始播放
                mp.seekTo(0);
                // 在播放完毕被回调
                if (mMediaOnListener != null) {
                    mMediaOnListener.OnPlayStartListener();
                }

            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion (MediaPlayer mp) {
                // 在播放完毕被回调
                if (mMediaOnListener != null) {
                    mMediaOnListener.OnPlayCompletionListener();
                }

                if (mIsRelease) {
                    //LogUtils.d("音频全释放");
                    //播放完毕释放资源
                    stopAndRelease();
                } else {
                    //LogUtils.d("音频只释放");
                    mMediaPlayer.reset();
                }
            }
        });

        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError (MediaPlayer mp, int what, int extra) {

                // 在播放完毕被回调
                if (mMediaOnListener != null) {
                    mMediaOnListener.OnPlayErrorListener(1002, "音频播放错误");
                }
                LogUtils.e("音频播放错误");

                stopAndRelease();
                return true;
            }
        });

    }

    /**
     * 播放音频文件
     *
     * @param voiceList       地址集合(本地或者网络)
     * @param mediaOnListener 回调
     */
    public void playVoiceList (Context context, List<String> voiceList, final MediaMoreOnListener mediaOnListener) {

        mMediaMoreOnListener = mediaOnListener;
        mVoiceList = voiceList;

        //判断播放地址
        if (voiceList == null || voiceList.size() <= 0) {
            if (mMediaMoreOnListener != null) {
                mMediaMoreOnListener.OnPlayErrorListener(1000, "播放音频地址为空");
            }
            return;
        }

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        } else {
            mMediaPlayer.reset();
        }

        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //请求焦点
        requestAudioFocus(context);

        mSize = mVoiceList.size();
        mCurrentPosition = 0;
        String voicePath = mVoiceList.get(mCurrentPosition);

        //监听播放事件
        initMediaListener();

        //设置地址并播放
        startMediaPlay(voicePath);


    }

    //设置地址并播放(多个音频的资源设置)
    private void startMediaPlay (String voicePath) {

        if (TextUtils.isEmpty(voicePath)) {
            if (mMediaMoreOnListener != null) {
                mMediaMoreOnListener.OnPlayErrorListener(1000, "播放音频地址为空");
            }
            return;
        }

        try {
            String voice = "http://fjdx.sc.chinaz.net/Files/DownLoad/sound1/201501/5394.mp3";
            //指定音频文件的路径
            mMediaPlayer.setDataSource(voicePath);
            //让 Mediaplayer 进入准备状态
            mMediaPlayer.prepareAsync();

        } catch (IOException e) {
            LogUtils.e("音频播放MediaPlayer初始化失败");
            if (mMediaOnListener != null) {
                mMediaOnListener.OnPlayErrorListener(1001, "播放音频初始化失败");
            }
            if (mMediaMoreOnListener != null) {
                mMediaMoreOnListener.OnPlayErrorListener(1001, "播放音频初始化失败");
            }
            //stopAndRelease();//多音频播放时,出错后回调会进行错误播放,这时候不需要清理mediaplay,否则错误播放无法进行
            e.printStackTrace();
        }
    }

    //监听播放事件(多个音频的监听回调)
    private void initMediaListener () {
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared (final MediaPlayer mp) {
                mp.start();//开始播放
                mp.seekTo(0);

            }
        });

        //播放的回调
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion (MediaPlayer mp) {
                mCurrentPosition += 1;
                //判断是否所有的音频都播放完毕了
                if (mCurrentPosition < mSize) {
                    mMediaPlayer.reset();
                    startMediaPlay(mVoiceList.get(mCurrentPosition));
                    if (mMediaMoreOnListener != null) {
                        mMediaMoreOnListener.OnPlayPositionListener(mCurrentPosition);
                    }

                } else {
                    // 在播放完毕被回调
                    if (mMediaMoreOnListener != null) {
                        mMediaMoreOnListener.OnPlayCompletionListener();
                    }
                    if (mIsRelease) {
                        //播放完毕释放资源
                        stopAndRelease();
                    } else {
                        mMediaPlayer.reset();
                    }

                }


            }
        });

        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError (MediaPlayer mp, int what, int extra) {

                // 在播放完毕被回调
                if (mMediaMoreOnListener != null) {
                    mMediaMoreOnListener.OnPlayErrorListener(1002, "音频播放错误");
                }
                LogUtils.e("音频播放错误");

                stopAndRelease();
                return true;
            }
        });
    }

    //请求焦点
    private void requestAudioFocus (Context context) {
        //下方是和其他app音乐冲突的处理
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }
        if (mAudioManager != null) {
            //请求焦点
            int ret = mAudioManager.requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (ret != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {//请求成功
                //LogUtils.d("请求焦点成功");
            } else {
                //LogUtils.d("请求焦点失败");
            }
        }

        mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange (int focusChange) {
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    //长时间失去焦点

                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    //获得焦点

                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    //临时丢失焦点
                }
            }
        };
    }

    //释放资源
    public void stopAndRelease () {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();//资源释放 关键
            mMediaPlayer.release();
            mMediaPlayer = null;
            if (mVoiceList != null) {
                mVoiceList.clear();
            }
            mCurrentPosition = 0;
            mSize = 0;
            releaseTheAudioFocus();
        }
    }

    //暂停、播放完成或退到后台释放音频焦点
    private void releaseTheAudioFocus () {
        if (mAudioManager != null && mAudioFocusChangeListener != null) {
            mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
        }
    }

    //接口回调
    private MediaOnListener     mMediaOnListener;
    private MediaMoreOnListener mMediaMoreOnListener;

    public interface MediaOnListener {
        void OnPlayStartListener ();

        void OnPlayCompletionListener ();

        void OnPlayErrorListener (int code, String msg);
    }

    public interface MediaMoreOnListener {
        void OnPlayCompletionListener ();

        void OnPlayPositionListener (int position);

        void OnPlayErrorListener (int code, String msg);
    }

}
