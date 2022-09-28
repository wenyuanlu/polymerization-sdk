package com.corpize.sdk.mobads.video;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;


import com.corpize.sdk.mobads.utils.LogUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * author: yh
 * date: 2019-10-12 13:41
 * description: TODO:自定义TextureView
 */
public class MyTextureView extends TextureView {

    private Context                mContext;
    private Surface                mSurface;
    private MediaPlayer            mMediaPlayer;
    private boolean                mIsReady  = false;
    private int                    mPosition = 0;//续播时间
    private String                 mUrl      = "";
    private MediaMetadataRetriever mMetadataRetriever;
    private Bitmap                 mBitmap;
    private boolean                isPlay;//是否开始播放

    private int     HAND_SHOW_BIPMAP = 7000;
    @SuppressLint ("HandlerLeak")
    private Handler mHandler         = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            if (what == HAND_SHOW_BIPMAP) {//显示图片
                if (mVideoOnListener != null) {
                    //LogUtils.d("显示图片");
                    mVideoOnListener.OnBitmapListener(mBitmap);
                }
            }
        }
    };

    public MyTextureView (Context context) {
        this(context, null);
    }

    public MyTextureView (Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyTextureView (Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    //初始化
    private void init () {
        //实例化MediaMetadataRetriever对象,为获取指定帧做准备
        mMetadataRetriever = new MediaMetadataRetriever();
        //设置监听
        setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable (SurfaceTexture surface, int width, int height) {
                LogUtils.d("SurfaceTextureAvailable");
                //初始化好SurfaceTexture后回调这个接口
                mSurface = new Surface(surface);
                mIsReady = true;
                //开启一个线程去播放视频
                if (!TextUtils.isEmpty(mUrl)) {
                    new PlayerVideoThread().start();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged (SurfaceTexture surface, int width, int height) {
                //视频尺寸改变后调用
                //LogUtils.d("SurfaceTextureSizeChanged");
            }

            @Override
            public boolean onSurfaceTextureDestroyed (SurfaceTexture surface) {
                //SurfaceTexture即将被销毁时调用
                LogUtils.d("SurfaceTextureDestroyed");
                mIsReady = false;
                if (mVideoOnListener != null) {
                    mVideoOnListener.OnTextureDestroyedListener();
                }
                if (mMediaPlayer != null) {
                    if (mMediaPlayer.isPlaying()) {
                        mPosition = mMediaPlayer.getCurrentPosition();
                        //LogUtils.d("当前播放时间：" + mPosition);
                        mMediaPlayer.stop();
                    }
                }
                if (mMetadataRetriever != null) {
                    mMetadataRetriever.release();
                    //mMetadataRetriever = null;
                }

                return true;
            }

            @Override
            public void onSurfaceTextureUpdated (SurfaceTexture surface) {
                //通过SurfaceTexture.updateteximage()更新指定的SurfaceTexture时调用
                if (mMediaPlayer != null) {
                    mPosition = mMediaPlayer.getCurrentPosition();//给出现播放中断的情况使用
                }
                //LogUtils.d("SurfaceTextureUpdated" + mMediaPlayer.getCurrentPosition());
            }
        });
    }

    /**
     * 定义一个线程，用于播发视频
     */
    private class PlayerVideoThread extends Thread {
        @Override
        public void run () {
            try {
                //初始化
                mMediaPlayer = new MediaPlayer();
                //设置播放资源
                mMediaPlayer.setDataSource(mUrl);
                //设置渲染画板
                mMediaPlayer.setSurface(mSurface);
                //设置播放类型
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                //预加载监听  播放完成监听  播放错误监听  播放警告监听
                mMediaPlayer.setOnPreparedListener(mPreparedListener);
                mMediaPlayer.setOnCompletionListener(mCompletionListener);
                mMediaPlayer.setOnErrorListener(mErrorListener);
                mMediaPlayer.setOnInfoListener(mInfoListener);

                //设置是否保持屏幕常亮
                mMediaPlayer.setScreenOnWhilePlaying(true);
                //异步的方式装载流媒体文件
                mMediaPlayer.prepareAsync();
                isPlay = true;
                //LogUtils.d("MediaPlayer.prepare准备中2");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 定义一个线程，用于获取视频的第一个图像
     */
    private class FirstBitmapGetThread extends Thread {
        @Override
        public void run () {
            if (mMetadataRetriever != null) {
                Map<String, String> map = new HashMap<>();
                try {
                    mMetadataRetriever.setDataSource(mUrl, map);//为获取帧做准备
                    //获得视频第一帧的Bitmap对象(微秒为单位,不是毫秒)
                    mBitmap = mMetadataRetriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST);
                    if (mVideoOnListener != null) {
                        mHandler.sendEmptyMessage(HAND_SHOW_BIPMAP);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

        }
    }

    //设置播放的数据
    public void setVideoPath (String url) {
        mUrl = url;

        //获取视频的第一帧图像
        if (mMetadataRetriever != null) {
            new FirstBitmapGetThread().start();
        }

        if (mIsReady) {
            new PlayerVideoThread().start();
        }
    }

    //开始播放
    public void start () {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            //mMediaPlayer.setVolume(0.45f, 0.45f);
        }
    }

    //暂停播放
    public void pause () {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    //停止播放
    public void stop () {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            isPlay = false;
        }
    }

    //设置播放的声音
    public void setVolume (Boolean haveVolume) {
        if (mMediaPlayer != null) {
            if (haveVolume) {
                mMediaPlayer.setVolume(0.5f, 0.5f);//正常声音模式
            } else {
                mMediaPlayer.setVolume(0f, 0f);//静音模式
            }
        }
    }

    //释放视频播放资源
    public void release () {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void resumeStart () {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            isPlay = false;
        }
        //开启一个线程去播放视频
        if (!TextUtils.isEmpty(mUrl)) {
            new PlayerVideoThread().start();
        }

    }

    //跳转到指定播放的位置
    public void seekTo (int startTime) {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.seekTo(startTime);
        }
    }

    //获取当前播放的位置
    public int getCurrentPosition () {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    /**
     * 当装载流媒体完毕的时候回调
     */
    private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared (MediaPlayer mp) {
            int duration = mMediaPlayer.getDuration();
            LogUtils.d("视频准备中,当前位置=" + mPosition + "||总时长=" + duration + "||剩余时长=");

            if (isPlay) {
                //跳转到指定位置播放
                mMediaPlayer.seekTo(mPosition);
                if (mVideoOnListener != null) {
                    mVideoOnListener.OnPreparedListener(mp, duration);
                }
            }

        }
    };

    /**
     * 流媒体播放结束时回调类
     */
    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion (MediaPlayer mp) {
            if (mVideoOnListener != null) {
                mVideoOnListener.OnCompletionListener(mp);
            }
        }
    };

    /**
     * 流媒体播放错误时回调类
     */
    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {

        @Override
        public boolean onError (MediaPlayer mp, int what, int extra) {
            if (what != 38 && extra != -38 && what != -38 && extra != 38 && extra != -19) {
                if (mVideoOnListener != null) {
                    mVideoOnListener.OnErrorListener(mp, what, extra);
                }
            }
            return true;
        }
    };

    /**
     * 流媒体播放警告时回调类
     */
    private MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo (MediaPlayer mp, int what, int extra) {
            //LogUtils.d("播放警告信息=" + what + "  " + extra);
            if (what == 804 && extra == -1004) {//播放过程中网络中断
                //mPosition = mMediaPlayer.getCurrentPosition();
                //LogUtils.d("警告的时候的位置=" + mPosition);
            }
            if (what == 3 && extra == 0) {//视频缓存完毕可以开始播放
                if (mVideoOnListener != null) {
                    mVideoOnListener.OnVideoPreparedListener(mp);
                }
            }
            if (mVideoOnListener != null) {
                mVideoOnListener.onInfoListener(mp, what, extra);
            }
            return false;
        }
    };

    //继承的接口回调
    private MyTextureViewOnListener mVideoOnListener;

    public interface MyTextureViewOnListener {

        void OnPreparedListener (MediaPlayer mp, int duration);

        void OnVideoPreparedListener (MediaPlayer mp);//视频缓存完成,可以播放的回调

        void OnCompletionListener (MediaPlayer mp);

        void OnErrorListener (MediaPlayer mp, int what, int extra);

        void onInfoListener (MediaPlayer mp, int what, int extra);

        void OnTextureDestroyedListener ();

        void OnBitmapListener (Bitmap bitmap);
    }

    public void setOnVideoListener (MyTextureViewOnListener videoOnListener) {
        mVideoOnListener = videoOnListener;
    }

}
