package com.corpize.sdk.mobads.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.corpize.sdk.mobads.bean.AppUserBean;

import java.io.IOException;
import java.util.List;

/**
 * author: yh
 * date: 2019-11-14 14:38
 * description: TODO:根据经纬度查询地址,无网时查询不到
 */
public class GetAddressUtil {
    Context context;

    public GetAddressUtil (Context context) {
        this.context = context;
    }

    public void getAddress (final double lat, final double lnt) {
        new Thread(new Runnable() {
            @Override
            public void run () {
                Geocoder geocoder = new Geocoder(context);
                boolean  falg     = geocoder.isPresent();
                //LogUtils.d("the falg is " + falg);
                StringBuilder stringBuilder = new StringBuilder();
                try {

                    //根据经纬度获取地理位置信息---这里会获取最近的几组地址信息，具体几组由最后一个参数决定
                    List<Address> addresses = geocoder.getFromLocation(lat, lnt, 1);

                    if (addresses.size() > 0) {
                        Address address = addresses.get(0);
                        for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                            //每一组地址里面还会有许多地址。这里我取的前2个地址。xxx街道-xxx位置(可能获取不到)
                            if (i == 0) {
                                stringBuilder.append(address.getAddressLine(i)).append("-");
                            }

                            if (i == 1) {
                                stringBuilder.append(address.getAddressLine(i)).append("-");
                                break;
                            }
                        }

                        //本地保存的信息
                        AppUserBean.getInstance().setProvince(address.getAdminArea());
                        AppUserBean.getInstance().setCity(address.getLocality());
                        AppUserBean.getInstance().setStreet(address.getSubLocality() + address.getThoroughfare());//区+道路

                        //这里的信息才是正式能获取到的信息
                        stringBuilder.append(address.getCountryName()).append("_");     // 国家
                        stringBuilder.append(address.getAdminArea()).append("_");       // 省份
                        stringBuilder.append(address.getLocality()).append("_");        // 市
                        stringBuilder.append(address.getSubLocality()).append("_");     // 洲区
                        stringBuilder.append(address.getThoroughfare());                // 道路

                        //LogUtils.d("地址信息--->" + stringBuilder);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        //return stringBuilder.toString();

    }
}

