package com.corpize.sdk.mobads.utils;


import android.content.Context;
import android.text.TextUtils;

import com.corpize.sdk.mobads.QcAd;
import com.corpize.sdk.mobads.bean.AdBaseBean;
import com.corpize.sdk.mobads.bean.AdResponseBean;
import com.corpize.sdk.mobads.bean.AdidBean;
import com.corpize.sdk.mobads.bean.AppUserBean;
import com.corpize.sdk.mobads.bean.UserBean;
import com.corpize.sdk.mobads.bean.VoiceBean;
import com.corpize.sdk.mobads.common.Constants;
import com.corpize.sdk.mobads.http.MyHttpUtils;
import com.corpize.sdk.mobads.http.callback.JsonCallback;
import com.corpize.sdk.mobads.http.callback.JsonSerializator;
import com.corpize.sdk.mobads.http.callback.StringCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author ：yh
 * date : 2019-08-02 14:44
 * description : 网络编辑类
 */
public class QcHttpUtil {

    private static String mBaseUrl   = Constants.getBaseUrl();          //基础的url,获取广告配置
    private static String mBaseAdUrl = Constants.getBaseAdUrl();        //基础的url,获取广告信息
    private static String mAdidUrl   = mBaseUrl + "sdk";                //通过adid获取广告配置
    private static String mGetAdUrl  = mBaseAdUrl + "ssp/corpize";      //获取广告详情

    //**************************************  下方是网络请求  **************************************//
    //**************************************  下方是网络请求  **************************************//
    //**************************************  下方是网络请求  **************************************//

    /**
     * 获取广告位信息
     */
    public static void getAdid (String adid, final QcHttpOnListener qcHttpOnListener) {
        Map<String, Object> map = new HashMap<>();
        map.put("adid", adid);
        map.put("os", "Android");
        map.put("versionName", DeviceUtil.getVersionName());
        map.put("versionCode", DeviceUtil.getVersionCode());
        String bean = GsonUtil.GsonString(map);
        MyHttpUtils.postAsyn(mAdidUrl)
                .content(bean)
                .execute(new JsonCallback<AdidBean>(new JsonSerializator()) {
                    @Override
                    public void onResponse (AdidBean response) {
//                        if (response != null) {
//                            //校验包名
//                            String bundle      = response.getBundle();
//                            String packageName = DeviceUtil.getPackageName();
//                            if (TextUtils.isEmpty(bundle) || !bundle.equals(packageName)) {
//                                if (qcHttpOnListener != null) {
//                                    qcHttpOnListener.OnQcErrorListener("adid与应用不匹配,请查看包名是否正确", 1003);
//                                }
//                                return;
//                            }
//                        }

                        if (qcHttpOnListener != null) {
                            qcHttpOnListener.OnQcCompletionListener(response);
                        }
                    }

                    @Override
                    public void onError (int code, Exception e) {
                        if (qcHttpOnListener != null) {
                            qcHttpOnListener.OnQcErrorListener(e.getMessage(), code);
                        }
                    }
                });
    }

    /**
     * 获取正式的广告
     */
    public static void getAd (Context context, AdidBean adsSdk, String adId, final QcHttpOnListener qcHttpOnListener) {
        if (adsSdk != null) {
            String bean = setBaseBean(context, adsSdk, adId, false);
            MyHttpUtils.postAsyn(mGetAdUrl)
                    .content(bean)
                    .execute(new JsonCallback<AdResponseBean>(new JsonSerializator()) {
                        @Override
                        public void onResponse (AdResponseBean response) {
                            if (qcHttpOnListener != null) {
                                qcHttpOnListener.OnQcCompletionListener(response);
                            }
                        }

                        @Override
                        public void onError (int code, Exception e) {
                            if (qcHttpOnListener != null) {
                                qcHttpOnListener.OnQcErrorListener(e.getMessage(), code);
                            }
                        }
                    });

        }


    }

    /**
     * 获取正式的音频广告
     */
    public static void getAudioAd (Context context, AdidBean adsSdk, String adId, final QcHttpOnListener qcHttpOnListener) {
        if (adsSdk != null) {
            String bean = setBaseBean(context, adsSdk, adId, true);
            MyHttpUtils.postAsyn(mGetAdUrl)
                    .content(bean)
                    .execute(new JsonCallback<AdResponseBean>(new JsonSerializator()) {
                        @Override
                        public void onResponse (AdResponseBean response) {
                            if (qcHttpOnListener != null) {
                                qcHttpOnListener.OnQcCompletionListener(response);
                            }
                        }

                        @Override
                        public void onError (int code, Exception e) {
                            if (qcHttpOnListener != null) {
                                qcHttpOnListener.OnQcErrorListener(e.getMessage(), code);
                            }
                        }
                    });

        }

    }

    /**
     * 广告曝光请求 (企创广告) 检测曝光
     */
    public static void sendAdExposure (List<String> exposureUrlList) {
        if (exposureUrlList != null && exposureUrlList.size() > 0) {
            for (String url : exposureUrlList) {
                MyHttpUtils.getAsyn(url)
                        .execute(new StringCallback() {
                            @Override
                            public void onResponse (String response) {
                            }

                            @Override
                            public void onError (int code, Exception e) {
                            }
                        });
            }
        }

    }

    /**
     * 广告曝光请求 (企创广告) 检测曝光
     */
    public static void sendAdExposure (String exposureUrl) {
        MyHttpUtils.getAsyn(exposureUrl)
                .execute(new StringCallback() {
                    @Override
                    public void onResponse (String response) {
                    }

                    @Override
                    public void onError (int code, Exception e) {
                    }
                });

    }

    /**
     * 广告点击请求 (企创广告) 检测点击广告
     */
    public static void sendAdClick (List<String> clickUrlList) {
        if (clickUrlList != null) {
            for (String url : clickUrlList) {
                MyHttpUtils.getAsyn(url)
                        .execute(new StringCallback() {
                            @Override
                            public void onResponse (String response) {
                            }

                            @Override
                            public void onError (int code, Exception e) {
                            }
                        });
            }
        }

    }

    /**
     * 组装广告基础参数
     */
    private static String setBaseBean (Context context, AdidBean adsSdk, String adId, boolean isAudio) {
        //获取下发的参数
        int       version  = TextUtils.isEmpty(adsSdk.getVersion()) ? 0 : Integer.valueOf(adsSdk.getVersion());
        String    storeurl = adsSdk.getStoreurl();
        int       pos      = TextUtils.isEmpty(adsSdk.getPos()) ? 0 : Integer.valueOf(adsSdk.getPos());
        int       width    = adsSdk.getWidth();
        int       height   = adsSdk.getHeight();
        final int adtype   = TextUtils.isEmpty(adsSdk.getAdtype()) ? 0 : Integer.valueOf(adsSdk.getAdtype());
        String    appname  = adsSdk.getAppname();
        String    bundle   = adsSdk.getBundle();
        String    adid     = adId;

        //获取本地保存的数据
        AppUserBean instance = AppUserBean.getInstance();

        //初始化数据
        AdBaseBean adUpBean = new AdBaseBean();

        //设置下发的参数
        adUpBean.setVersion(version);                           //协议版本号
        adUpBean.setAdid(adid);                                 //adid
        adUpBean.setStoreurl(storeurl);                         //APP应用市场下载链接
        adUpBean.setPos(pos);                                   //广告位在屏幕的第N屏 参数格式问题
        adUpBean.setWidth(width);                               //广告宽度
        adUpBean.setHeight(height);                             //广告高度
        adUpBean.setAdtype(adtype);                             //广告类型
        adUpBean.setAppname(appname);                           //App名称
        adUpBean.setBundle(bundle);                             //开发包名

        //adUpBean.setBundle(DeviceUtil.getPackageName());        //开发包名
        //adUpBean.setAppname(DeviceUtil.getAppName());           //App名称

        //设置本地的参数(必填)
        adUpBean.setUa(DeviceUtil.getUserAgent(context));       //本地的ua的信息
        adUpBean.setVer(DeviceUtil.getVersionName());           //App的版本
        adUpBean.setSdkver(Constants.getSdkVer());              //SDK的版本号
        adUpBean.setOsv(DeviceUtil.getSdkVersion());            //设备操作系统版本
        adUpBean.setMake(DeviceUtil.getManufacturer());         //设备制造商
        adUpBean.setModel(DeviceUtil.getPhoneModel());          //设备型号
        adUpBean.setLanguage(DeviceUtil.getLanguage());         //设备语言
        adUpBean.setImei(DeviceUtil.getIMEI());                 //手机的imei
        adUpBean.setOaid(DeviceUtil.getOaid());                 //正对于AndroidQ需要获取的oaid
        adUpBean.setAndroidid(DeviceUtil.getAndroidId());       //手机的ID明文
        adUpBean.setSw(DeviceUtil.getDeviceWidth());            //设备屏幕分辨率宽度
        adUpBean.setSh(DeviceUtil.getDeviceHeight());           //设备屏幕分辨率高度
        adUpBean.setIp(NetUtil.getIpAddress());                 //ip地址

        //设置本地的参数(选填)
        adUpBean.setCarrier(DeviceUtil.getOperatorName());      //运营商类型
        adUpBean.setConnectiontype(NetUtil.getNetworkState());  //设备网络类型
        adUpBean.setDensity(DeviceUtil.getDeviceDensity());     //屏幕密度
        adUpBean.setMac(DeviceUtil.getMAC());                   //设备mac地址

        double lat = instance.getLat();
        double lon = instance.getLon();
        if (lat != 0) {
            adUpBean.setLat(lat + "");                          //设置经度
        }
        if (lon != 0) {
            adUpBean.setLon(lon + "");                          //设置纬度
        }

        if (isAudio) {
            VoiceBean voice = new VoiceBean();
            voice.setFormat("mp3");
            voice.setMike(true);
            adUpBean.setVoice(voice);
        }

        //设置app传递过来的用户信息
        UserBean user = QcAd.get().getUserInfo();
        if (user != null) {
            adUpBean.setUser(user);
        }

        String bean = GsonUtil.GsonString(adUpBean);
        return bean;

    }

    //接口回调
    public interface QcHttpOnListener<T> {
        void OnQcCompletionListener (T response);

        void OnQcErrorListener (String erro, int code);
    }


}
