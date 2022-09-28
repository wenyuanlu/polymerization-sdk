package com.corpize.sdk.mobads.http;

/**
 * author ：yh
 * date : 2019-08-02 16:45
 * description :
 */
public enum QcErrorCode {

    /**
     * Media播放音频文件错误
     */
    MEDIA_ERROR_NOADDRESS(1010, "播放音频地址为空", -1),
    MEDIA_ERROR_START(1011, "播放音频初始化失败", -1),
    MEDIA_ERROR(1012, "播放音频错误", -1),
    MEDIA_NO_VOICE(1013, "媒体音量为0", -1),

    /**
     * Audio录音错误
     */
    AUDIO_ERROR_START(2010, "录音开启失败", -1),
    AUDIO_ERROR_START_STATUE(2011, "录音开启成功,状态获取失败", -1),
    AUDIO_ERROR_NOFILE(2012, "录音文件找不到", -1),
    AUDIO_ERROR_IO(2013, "保存pcm的IO流有误", -1),
    AUDIO_ERROR_NOFILE_PCM(2014, "pcm文件找不到", -1),
    AUDIO_ERROR_PCMTOWAVA_IO(2015, "pcm转Wave流失败", -1),
    AUDIO_ERROR_WEBSOCKET_UNCONNECT(2016, "WebSocket连接失败", -1),

    /**
     * 操作错误
     */
    ERROR_HAND_STOP(3011, "请求成功,手动中断", -1),
    ERROR_HAND_STOP2(3012, "手动停止广告", -1),

    /**
     * 权限相关
     */
    PERMISSION_ERROR_RECORDER(4011, "录音权限未开启", -1),
    PERMISSION_ERROR_WRITER(4012, "读写权限未开启", -1),

    /**
     * 网络相关
     */
    NET_ERROR_NO_CONNECT(5010, "网络不稳定，请稍后再试", -1),
    NET_ERROR_NO_ADID_MID(5011, "adid或mid验证失败", -1);

    public final int    errorCode;  //错误码
    public final String error;      //错误原因
    public final int    extro;      //其他错误提示

    QcErrorCode (int errorCode, String error, int extro) {
        this.errorCode = errorCode;
        this.error = error;
        this.extro = extro;
    }

}
