package com.chanling.haohai;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.chanling.haohai.R;
import com.corpize.sdk.mobads.QcAd;
import com.chanling.haohai.utils.PermissionUtil;

import java.util.HashMap;
import java.util.Map;


/**
 * 首页
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtBanner;
    private Button mBtAudio;
    private Button mBtInsert;
    private Button mBtInfoSingle;
    private Button mBtInfoMore;
    private Button mBtJili;
    private Button mBtSplash;
    private Button mBtVideoNative;

    private static String[] PERMISSION_RECORD_WRITE = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION
    };//录音,读写,状态,定位权限

    private static int PERMISSION_RECORD_CODE = 1001;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtSplash = (Button) findViewById(R.id.bt_splash);
        mBtBanner = (Button) findViewById(R.id.bt_banner);
        mBtInsert = (Button) findViewById(R.id.bt_gdt_cha);
        mBtAudio = (Button) findViewById(R.id.bt_audio);
        mBtInfoSingle = (Button) findViewById(R.id.bt_single_info);
        mBtInfoMore = (Button) findViewById(R.id.bt_more_info);
        mBtJili = (Button) findViewById(R.id.bt_voido_ji);
        mBtVideoNative = (Button) findViewById(R.id.bt_voido_native);

        mBtBanner.setOnClickListener(this);
        mBtAudio.setOnClickListener(this);
        mBtInsert.setOnClickListener(this);
        mBtInfoSingle.setOnClickListener(this);
        mBtInfoMore.setOnClickListener(this);
        mBtJili.setOnClickListener(this);
        mBtSplash.setOnClickListener(this);
        mBtVideoNative.setOnClickListener(this);

        checkAndStartAd(this);

        //拼装用户参数,可选
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", "张三");             //姓名
        userMap.put("yob", "1990-10-10");       //年龄
        userMap.put("gender", "M");             //性别 M - Male , F - Female , O - Other , U - Unknow
        userMap.put("phone", "13313131313");    //用户当前手机号码
        userMap.put("marriage", 1);             //婚姻状况  1(未婚)  2(已婚)
        userMap.put("hobby", "女装,美食,包包");   //用户在APP上的喜好标签,例如："女装,美食,包包"用逗号分隔
        userMap.put("edu", "大学");              //用户学历 1、小学及以下 2、初中 3、高中 4、大学 5、硕士及以上

        //传递用户信息,精准开启广告,可选参数
        QcAd.get().setUserInfo(userMap);

    }

    /**
     * 检查权限 开启广告
     */
    public static void checkAndStartAd (Context context) {
        PermissionUtil.checkAndRequestMorePermissions(context, PERMISSION_RECORD_WRITE, PERMISSION_RECORD_CODE, new PermissionUtil.PermissionRequestSuccessCallBack() {
            @Override
            public void onHasPermission () {
            }
        });
    }

    @Override
    public void onClick (View v) {
        int id = v.getId();
        if (id == R.id.bt_banner) {//banner广告
            startActivity(new Intent(this, BannerAdActivity.class));
        } else if (id == R.id.bt_gdt_cha) {//插屏广告
            startActivity(new Intent(this, InsertAdActivity.class));
        } else if (id == R.id.bt_audio) {//音频广告
            startActivity(new Intent(this, AudioAdActivity.class));
        } else if (id == R.id.bt_single_info) {//单图信息流
            startActivity(new Intent(this, InfoOneAdActivity.class));
        } else if (id == R.id.bt_more_info) {//多图信息流
            startActivity(new Intent(this, InfoThreeAdActivity.class));
        } else if (id == R.id.bt_voido_ji) {//激励视频
            startActivity(new Intent(this, VideoRewardAdActivity.class));
        } else if (id == R.id.bt_voido_native) {//贴片视频
            startActivity(new Intent(this, VideoNativeAdActivity.class));
        } else if (id == R.id.bt_splash) {//开屏广告
            startActivity(new Intent(this, SplashAdActivity.class));
        }
    }
}
