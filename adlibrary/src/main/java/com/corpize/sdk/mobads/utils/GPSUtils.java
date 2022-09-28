package com.corpize.sdk.mobads.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;


import com.corpize.sdk.mobads.bean.AppUserBean;

import java.util.List;

/**
 * author: yh
 * date: 2019-08-20 09:25
 * description: 原生定位方法,定位不准确
 */
@SuppressLint ("MissingPermission")
public class GPSUtils {

    private static GPSUtils        instance;
    private        Context         mContext;
    private        LocationManager mLocationManager;
    private        double          latitude  = 0.0;
    private        double          longitude = 0.0;
    private        int             mTimeOut  = 300 * 1000;

    private GPSUtils (Context context) {
        this.mContext = context;
    }

    public static GPSUtils getInstance (Context context) {
        if (instance == null) {
            instance = new GPSUtils(context);
        }
        return instance;
    }

    /**
     * 经纬度信息的获取及保存
     */
    public void initLngAndLat () {
        getLngAndLat(new GPSUtils.OnLocationResultListener() {
            @Override
            public void onLocationResult (Location location) {
                AppUserBean.getInstance().setLat(location.getLatitude());
                AppUserBean.getInstance().setLon(location.getLongitude());
                new GetAddressUtil(mContext).getAddress(location.getLatitude(), location.getLongitude());
                //LogUtils.d("定位信息获取 Latitude = " + location.getLatitude() + " Longitude = " + location.getLongitude());
            }

            @Override
            public void OnLocationChange (Location location) {
                AppUserBean.getInstance().setLat(location.getLatitude());
                AppUserBean.getInstance().setLon(location.getLongitude());
                new GetAddressUtil(mContext).getAddress(location.getLatitude(), location.getLongitude());
                //LogUtils.d("定位信息变更 Latitude = " + location.getLatitude() + " Longitude = " + location.getLongitude());

            }
        });
    }

    /**
     * 获取经纬度,回调接口返回经纬度
     */
    public void getLngAndLat (OnLocationResultListener onLocationResultListener) {

        mOnLocationListener = onLocationResultListener;

        String locationProvider = null;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        // 查找到服务信息
        Criteria criteria = new Criteria();
        // 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // 设置是否要求速度
        criteria.setSpeedRequired(false);
        // 设置是否需要海拔信息
        criteria.setAltitudeRequired(false);
        // 设置是否需要方位信息
        criteria.setBearingRequired(false);
        // 设置是否允许运营商收费
        criteria.setCostAllowed(true);
        // 设置对电源的需求
        criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗

        // 为获取地理位置信息时设置查询条件(获取系统推荐的)
        //locationProvider = mLocationManager.getBestProvider(criteria, true); // 获取GPS信息

        //获取所有可用的位置提供器
        List<String> providers = mLocationManager.getProviders(true);
        if (providers.contains(LocationManager.GPS_PROVIDER)) {             //如果是GPS
            locationProvider = LocationManager.GPS_PROVIDER;

        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {  //如果是Network
            locationProvider = LocationManager.NETWORK_PROVIDER;
        }

        //LogUtils.d("定位信息获取 定位方式=" + locationProvider);

        if (!TextUtils.isEmpty(locationProvider)) {
            //获取默认方式的Location,一般是Gps
            Location locationDefault = mLocationManager.getLastKnownLocation(locationProvider);
            if (locationDefault != null) {
                //不为空,显示地理位置经纬度
                if (mOnLocationListener != null) {
                    mOnLocationListener.onLocationResult(locationDefault);
                }

            } else {
                //默认的走不通的时候,走NetWork
                if (locationProvider.equals(LocationManager.GPS_PROVIDER)) {
                    locationProvider = LocationManager.NETWORK_PROVIDER;
                    //LogUtils.d("定位信息修改 定位方式=" + locationProvider);

                    Location locationNetWork = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    if (mOnLocationListener != null && locationNetWork != null) {
                        mOnLocationListener.onLocationResult(locationNetWork);
                    } else {
                        //QcHttpUtil.getGaodeGPRS();
                    }

                }
            }

            //监视地理位置变化
            mLocationManager.requestLocationUpdates(locationProvider, mTimeOut, 1f, mLocationListener);
        }

    }

    public LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged (Location location) { //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
            //LogUtils.d("位置改变");
            if (mOnLocationListener != null && location != null) {
                mOnLocationListener.OnLocationChange(location);
            }
        }

        @Override
        public void onStatusChanged (String provider, int status, Bundle extras) { // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
            //LogUtils.d("位置提供器状态：改变");
        }

        @Override
        public void onProviderEnabled (String provider) { // Provider被enable时触发此函数，比如GPS被打开
            //LogUtils.d("位置提供器：启用");
            Location location = mLocationManager.getLastKnownLocation(provider);
            if (mLocationListener != null && location != null) {
                mLocationListener.onLocationChanged(location);
            }
        }

        @Override
        public void onProviderDisabled (String provider) {// Provider被disable时触发此函数，比如GPS被关闭
            //LogUtils.d("位置提供器：关闭");
        }

    };

    public void removeListener () {
        mLocationManager.removeUpdates(mLocationListener);
    }

    private OnLocationResultListener mOnLocationListener;

    public interface OnLocationResultListener {
        void onLocationResult (Location location);

        void OnLocationChange (Location location);
    }
}
