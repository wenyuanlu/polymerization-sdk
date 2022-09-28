
# ------------------------------------------通用区域----------------------------------------------------
#----------------------基本指令------------------------
#代码混淆压缩比，在0和7之间，默认为5，一般不需要改
-optimizationpasses 5
#混淆时不使用大小写混合，混淆后的类名为小写
-dontusemixedcaseclassnames
#指定不去忽略非公共的库的类
-dontskipnonpubliclibraryclasses
#指定不去忽略非公共的库的类的成员
-dontskipnonpubliclibraryclassmembers
#不做预校验，preverify是proguard的4个步骤之一
#Android不需要preverify，去掉这一步可加快混淆速度
-dontpreverify
#有了verbose这句话，混淆后就会生成映射文件
#包含有类名->混淆后类名的映射关系
#然后使用printmapping指定映射文件的名称
-verbose
-printmapping proguardMapping.txt
#指定混淆时采用的算法，后面的参数是一个过滤器
#这个过滤器是谷歌推荐的算法，一般不改变
-optimizations !code/simplification/cast,!field/*,!class/merging/*
#保护代码中的Annotation不被混淆，这在JSON实体映射时非常重要，比如fastJson
-keepattributes *Annotation*,InnerClasses
#避免混淆泛型，这在JSON实体映射时非常重要，比如fastJson
-keepattributes Signature
#抛出异常时保留代码行号，在异常分析中可以方便定位
-keepattributes SourceFile,LineNumberTable
#-----------------------全局混淆-----------------------
#除了项目目录，其他都不混淆，这种写法只能有一行，请自行改成自己的包路径，如果再加一行com.li.*会导致全部都不混淆②
#-keep class !com.corpize.sdk.mobads.** {*;}
#-keep class !com.li.** {*;}写2行会导致全部不混淆
-dontwarn **
#---------------------默认保留-------------------------
#基础保留
-keep class * extends android.app.Activity
-keep class * extends android.app.Application
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider
-keep class * extends android.app.backup.BackupAgentHelper
-keep class * extends android.preference.Preference
-keep class * extends android.view.View {
    <init>(...);
}

#序列化
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
#EventBus的注解
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
#WebView
-keepclassmembers class * extends android.webkit.WebView {*;}
-keepclassmembers class * extends android.webkit.WebViewClient {*;}
-keepclassmembers class * extends android.webkit.WebChromeClient {*;}
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# ------------------------------------------------------------ 动态混淆 ------------------------------------------------------------
# ------------------------------------------------------------ 动态混淆 ------------------------------------------------------------
# ------------------------------------------------------------ 动态混淆 ------------------------------------------------------------

# gdt
-keep class com.qq.e.** {
    public protected *;
}
-keep class android.support.v4.**{
    public *;
}
-keep class android.support.v7.**{
    public *;
}

# csj
-keep class com.bytedance.sdk.openadsdk.** { *; }
-keep public interface com.bytedance.sdk.openadsdk.downloadnew.** {*;}
-keep class com.pgl.sys.ces.* {*;}

# bqt
-keepclassmembers class * extends android.app.Activity { public void *(android.view.View);
}
-keepclassmembers enum * {
public static **[] values();
public static ** valueOf(java.lang.String);
}
-keep class com.baidu.mobads.*.** { *; }
-keep class com.baidu.mobad.** { *; }

# 移动联盟
-keep class com.bun.miitmdid.core.** {*;}

-keep class com.bun.** {*;}
-keep class com.asus.msa.** {*;}
-keep class com.heytap.openid.** {*;}
-keep class com.huawei.android.hms.pps.** {*;}
-keep class com.meizu.flyme.openidsdk.** {*;}
-keep class com.samsung.android.deviceidservice.** {*;}
-keep class com.zui.** {*;}
-keep class com.huawei.hms.ads.** {*; }
-keep interface com.huawei.hms.ads.** {*; }
-keepattributes *Annotation*
-keep @androidx.annotation.Keep class **{
@androidx.annotation.Keep <fields>;
    @androidx.annotation.Keep <methods>;
}

# 移动联盟 新
-keep, includedescriptorclasses class com.asus.msa.SupplementaryDID.** { *; }
    -keepclasseswithmembernames class com.asus.msa.SupplementaryDID.**{ *; }
    -keep, includedescriptorclasses class com.asus.msa.sdid.** { *; }
    -keepclasseswithmembernames class com.asus.msa.sdid.** { *; }
    -keep public class com.netease.nis.sdkwrapper.Utils {public <methods>;}
-keep class com.bun.miitmdid.**{*;}
-keep class com.bun.lib.**{*;}
-keep class com.samsung.android.deviceidservice.**{*;}
-keep class a.**{*;}

# ------------------------------------------------------------ 自定义不混淆的类 ------------------------------------------------------------
# ------------------------------------------------------------ 自定义不混淆的类 ------------------------------------------------------------
# ------------------------------------------------------------ 自定义不混淆的类 ------------------------------------------------------------
# bean网络获取数据 http为网络层 防混淆 TODO：包的路径要自己修改
#-keep class com.corpize.sdk.mobads.** { *; }
-keep class com.corpize.sdk.mobads.bean.** { *; }
-keep class com.corpize.sdk.mobads.http.** { *; }
-keep class com.corpize.sdk.mobads.listener.** {*;}
-keep class com.corpize.sdk.mobads.admanager.** {*;}
-keep class com.corpize.sdk.mobads.utils.downloadinstaller.** {*;}
-keep class com.corpize.sdk.mobads.QcAd {*;}