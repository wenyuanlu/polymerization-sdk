package com.corpize.sdk.mobads.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import com.bun.miitmdid.core.ErrorCode;
import com.bun.miitmdid.core.MdidSdkHelper;
import com.bun.miitmdid.interfaces.IIdentifierListener;
import com.bun.miitmdid.interfaces.IdSupplier;
import com.corpize.sdk.mobads.common.Constants;

/**
 * Created by zheng on 2019/8/22.
 */
public class MiitHelper implements IIdentifierListener {

    private        AppIdsUpdater _listener;
    private static MiitHelper    mMiitHelper;

    public static MiitHelper getInstanse () {
        if (mMiitHelper == null) {
            mMiitHelper = new MiitHelper();
        }
        return mMiitHelper;
    }

    public MiitHelper () {
    }

    /**
     * 获取deviceid(主要是oaid)
     *
     * @param cxt
     * @param callback
     */
    public void getDeviceIds (Context cxt, AppIdsUpdater callback) {
        // JLibrary.InitEntry(cxt);
        _listener = callback;
        long timeb  = System.currentTimeMillis();
        int  nres   = CallFromReflect(cxt);//反射调用
        long timee  = System.currentTimeMillis();
        long offset = timee - timeb;
        if (nres == ErrorCode.INIT_ERROR_DEVICE_NOSUPPORT) {//不支持的设备

        } else if (nres == ErrorCode.INIT_ERROR_LOAD_CONFIGFILE) {//加载配置文件出错

        } else if (nres == ErrorCode.INIT_ERROR_MANUFACTURER_NOSUPPORT) {//不支持的设备厂商

        } else if (nres == ErrorCode.INIT_ERROR_RESULT_DELAY) {//获取接口是异步的，结果会在回调中返回，回调执行的回调可能在工作线程

        } else if (nres == ErrorCode.INIT_HELPER_CALL_ERROR) {//反射调用出错

        }
        Log.e(getClass().getSimpleName(), "return value: " + String.valueOf(nres));
    }

    /**
     * 通过反射调用，解决android 9以后的类加载升级，导至找不到so中的方法
     */
    private int CallFromReflect (Context cxt) {
        return MdidSdkHelper.InitSdk(cxt, true, this);
    }

    /**
     * 直接java调用，如果这样调用，在android 9以前没有问题，在android 9以后会抛找不到so方法的异常
     * 解决办法是和JLibrary.InitEntry(cxt)，分开调用，比如在A类中调用JLibrary.InitEntry(cxt)，在B类中调用MdidSdk的方法
     * A和B不能存在直接和间接依赖关系，否则也会报错
     */
    private int DirectCall (Context cxt) {
       /* MdidSdk sdk = new MdidSdk();
        return sdk.InitSdk(cxt,this);*/
        return 0;
    }

    @Override
    public void OnSupport (boolean isSupport, IdSupplier _supplier) {
        if (_supplier == null) {
            return;
        }
        String oaid = _supplier.getOAID();
        String vaid = _supplier.getVAID();
        String aaid = _supplier.getAAID();

        //本地保存oaid
        SpUtils.saveString(Constants.OAID, oaid);
        StringBuilder builder = new StringBuilder();
        builder.append("support: ").append(isSupport ? "true" : "false").append("\n");
        builder.append("OAID: ").append(oaid).append("\n");
        builder.append("VAID: ").append(vaid).append("\n");
        builder.append("AAID: ").append(aaid).append("\n");
        String idstext = builder.toString();
        if (_listener != null) {
            _listener.OnIdsAvalid(idstext);
        }
    }

    public interface AppIdsUpdater {
        void OnIdsAvalid (@NonNull String ids);
    }

}
