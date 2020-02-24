-keepattributes SourceFile,LineNumberTable

-optimizationpasses 5
# 混淆时不使用大小写混合，混淆后的类名为小写。用于兼容Windows等不区分大小写的情况。
-dontusemixedcaseclassnames
# 用于告诉ProGuard，不要跳过对非公开类的处理。默认情况下是跳过的，因为程序中不会引用它们，有些情况下人们编写的代码与类库中的类在同一个包下，并且对包中内容加以引用，此时需要加入此条声明。
#-dontskipnonpubliclibraryclasses
#-dontskipnonpubliclibraryclassmembers
# 不做预校验，preverify是proguard的4个步骤之一
# Android不需要preverify，去掉这一步可加快混淆速度
-dontpreverify
# 有了verbose这句话，混淆后就会生成映射文件
# 包含有类名->混淆后类名的映射关系
# 然后使用printmapping指定映射文件的名称
-verbose
-ignorewarnings
## note-20200224-zhangyipeng: 全面升级了v4v7库到28之后，混淆时总会出错，暂时无法找到问题原因，只能使用下面的选项，禁用了优化。
-dontoptimize
# 指定混淆时采用的算法，后面的参数是一个过滤器
# 这个过滤器是谷歌推荐的算法，一般不改变
#-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-libraryjars libs/wmqtt.jar
-libraryjars libs/httpmime-4.1.3.jar
#-libraryjars libs/android-support-v4.jar
-libraryjars libs/commons-io-2.4.jar
-libraryjars libs/aceimageloader.jar
#-libraryjars libs/gson-2.2.4.jar

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keep public class org.zywx.wbpalmstar.platform.mam.SlidePaneLayout
-keep public class org.zywx.wbpalmstar.platform.mam.WheelView

-dontwarn com.google.gson.**
-keep class com.google.gson.** { *; }
-keep interface com.google.gson.** { *; }
-keep public class * extends com.google.gson.**

-dontwarn android.support.annotation.**
-keep class android.support.annotation.** { *; }
-keep interface android.support.annotation.** { *; }
-keep public class * extends android.support.annotation.**

-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }
-keep public class * extends android.support.v4.**

-keep public class * extends android.app.Fragment
-keep public class * extends android.support.v4.app.FragmentActivity

-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

-dontwarn android.arch.lifecycle.**
-keep class android.arch.lifecycle.** { *; }
-keep interface android.arch.lifecycle.** { *; }

-keep public class * extends org.xwalk.core.XWalkView
-dontwarn org.chromium.**
-dontwarn javax.annotation.**

-keep class org.xwalk.core.** {
    *;
}
-keep class org.chromium.** {
    *;
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keep class org.zywx.wbpalmstar.platform.push.**{
*;
}
-keep class org.zywx.wbpalmstar.platform.certificates.**{
*;
}

-keep class org.zywx.wbpalmstar.plugin.**{
    *;
}

-keep class org.zywx.wbpalmstar.widgetone.uex.**{
    *;
}

-keep class org.zywx.wbpalmstar.base.** {
    <fields>;
    <methods>;
}

-keep class org.zywx.wbpalmstar.widgetone.WidgetOneApplication {
    <fields>;
    <methods>;
}
-keep class org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData {
    <fields>;
    <methods>;
}
-keep class org.zywx.wbpalmstar.engine.AppCan {
    <fields>;
    <methods>;
}
-keep class org.zywx.wbpalmstar.engine.universalex.EUExBase {
    <fields>;
    <methods>;
}

-keep class org.zywx.wbpalmstar.engine.universalex.c{
    <fields>;
    <methods>;
}

-keep class org.zywx.wbpalmstar.engine.universalex.EUExUtil{
    <fields>;
    <methods>;
}

-keep class org.zywx.wbpalmstar.engine.ESystemInfo {
    <fields>;
    <methods>;
}

-keep class org.zywx.wbpalmstar.engine.EBrowserView {
    <fields>;
    <methods>;
}


-keep class org.zywx.wbpalmstar.engine.webview.ACEWebView {
    <fields>;
    <methods>;
}

-keep class org.zywx.wbpalmstar.engine.EUtil {
    <fields>;
    <methods>;
}

-keep class org.zywx.wbpalmstar.engine.EWgtResultInfo {
    <fields>;
    <methods>;
}

-keep class org.zywx.wbpalmstar.engine.universalex.EUExEventListener{
    <fields>;
    <methods>;
}
-keep class org.zywx.wbpalmstar.platform.mam.SlidePaneLayout {
    <fields>;
    <methods>;
}
-keep class org.zywx.wbpalmstar.platform.mam.SlidePaneLayout$* {
    <fields>;
    <methods>;
}
-keepclasseswithmembers,allowshrinking class * {
    public <init>(android.content.Context,android.util.AttributeSet);
}

-keep class org.zywx.wbpalmstar.engine.universalex.EUExCallback {
    <fields>;
    <methods>;
}
-keepclasseswithmembers,allowshrinking class * {
    public <init>(android.content.Context,android.util.AttributeSet,int);
}

-keepclasseswithmembers,allowshrinking class * {
    native <methods>;
}
-keep class org.zywx.wbpalmstar.engine.EngineEventListener {
    <fields>;
    <methods>;
}

-keep class org.zywx.wbpalmstar.engine.DataHelper {
    <fields>;
    <methods>;
}

-keep class org.zywx.wbpalmstar.base.ACEImageLoader {
    <fields>;
    <methods>;
}

-keep class com.baidu.mapapi.** {*;}
-keep class com.baidu.mobads.** {*;}
-keep class com.baidu.location.** {*;}
-keep class org.zywx.wbpalmstar.acedes.** {*;}

#-libraryjars libs/BaiduLBS_Android.jar
-keep class com.baidu.** { *; }
-keep class vi.com.gdi.bgl.android.**{*;}
-keep class pvi.com.gdi.bgl.android.**{*;}

-keepclassmembers class * extends android.webkit.WebChromeClient {
    public void openFileChooser(...);
}

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keep class com.google.gson.** {*;}
-keep class * implements java.io.Serializable {*;}
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson

##---------------End: proguard configuration for Gson  ----------

-keepclassmembers class * {
    @org.zywx.wbpalmstar.base.util.AppCanAPI *;
}

-keep,allowobfuscation @interface android.support.annotation.Keep
-keep @android.support.annotation.Keep class *
-keepclassmembers class * {
    @android.support.annotation.Keep *;
}

-keepclassmembers public class * {
    public void open(java.lang.String[]);
    public void close(java.lang.String[]);
    public void openSlibing(java.lang.String[]);
    public void showSlibing(java.lang.String[]);
    public void closeSlibing(java.lang.String[]);
    public void evaluateScript(java.lang.String[]);
    public void preOpenStart(java.lang.String[]);
    public void preOpenFinish(java.lang.String[]);
    public void openPopover(java.lang.String[]);
    public void closePopover(java.lang.String[]);
    public void setPopoverFrame(java.lang.String[]);
    public void openMultiPopover(java.lang.String[]);
    public void closeMultiPopover(java.lang.String[]);
    public void setSelectedPopOverInMultiWindow(java.lang.String[]);
    public void evaluatePopoverScript(java.lang.String[]);
    public void bringToFront(java.lang.String[]);
    public void sendToBack(java.lang.String[]);
    public void insertAbove(java.lang.String[]);
    public void insertBelow(java.lang.String[]);
    public void insertPopoverAbovePopover(java.lang.String[]);
    public void insertPopoverBelowPopover(java.lang.String[]);
    public void bringPopoverToFront(java.lang.String[]);
    public void sendPopoverToBack(java.lang.String[]);
    public void beginAnimition(java.lang.String[]);
    public void setAnimitionDelay(java.lang.String[]);
    public void setAnimitionDuration(java.lang.String[]);
    public void setAnimitionCurve(java.lang.String[]);
    public void setAnimitionRepeatCount(java.lang.String[]);
    public void setAnimitionAutoReverse(java.lang.String[]);
    public void makeTranslation(java.lang.String[]);
    public void makeScale(java.lang.String[]);
    public void makeRotate(java.lang.String[]);
    public void makeAlpha(java.lang.String[]);
    public void commitAnimition(java.lang.String[]);
    public void openAd(java.lang.String[]);
    public void loadObfuscationData(java.lang.String[]);
    public void back(java.lang.String[]);
    public void forward(java.lang.String[]);
    public void pageBack(java.lang.String[]);
    public void pageForward(java.lang.String[]);
    public void setReportKey(java.lang.String[]);
    public void windowBack(java.lang.String[]);
    public void windowForward(java.lang.String[]);
    public void setBounce(java.lang.String[]);
    public void notifyBounceEvent(java.lang.String[]);
    public void showBounceView(java.lang.String[]);
    public void resetBounceView(java.lang.String[]);
    public void setBounceParams(java.lang.String[]);
    public void hiddenBounceView(java.lang.String[]);
    public void alert(java.lang.String[]);
    public void confirm(java.lang.String[]);
    public void prompt(java.lang.String[]);
    public void toast(java.lang.String[]);
    public void closeToast(java.lang.String[]);
    public int getState(java.lang.String[]);
    public java.lang.String getUrlQuery(java.lang.String[]);
    public java.lang.String getWindowName(java.lang.String[]);
    public void actionSheet(java.lang.String[]);
    public void statusBarNotification(java.lang.String[]);
    public void setWindowFrame(java.lang.String[]);
    public void setSwipeRate(java.lang.String[]);
    public void postGlobalNotification(java.lang.String[]);
    public void subscribeChannelNotification(java.lang.String[]);
    public void publishChannelNotification(java.lang.String[]);
    public void showSoftKeyboard(java.lang.String[]);
    public void hideSoftKeyboard(java.lang.String[]);
    public void closeAboveWndByName(java.lang.String[]);
    public void setSlidingWindow(java.lang.String[]);
    public void toggleSlidingWindow(java.lang.String[]);
    public void setOrientation(java.lang.String[]);
    public void setMultiPopoverFrame(java.lang.String[]);
    public void evaluateMultiPopoverScript(java.lang.String[]);
    public void refresh(java.lang.String[]);
    public void getBounce(java.lang.String[]);
    public void setMultilPopoverFlippingEnbaled(java.lang.String[]);
    public void createProgressDialog(java.lang.String[]);
    public void destroyProgressDialog(java.lang.String[]);
    public void openPresentWindow(java.lang.String[]);
    public void setSlidingWindowEnabled(java.lang.String[]);
    public void getSlidingWindowState(java.lang.String[]);
    public void dispatch(java.lang.String,java.lang.String,java.lang.String[]);
    public void setIsSupportSlideCallback(java.lang.String[]);
    public void setHardwareEnable(java.lang.String[]);
    public void setPopHardwareEnable(java.lang.String[]);
    public void setPageInContainer(java.lang.String[]);
    public void showPluginViewContainer(java.lang.String[]);
    public void hidePluginViewContainer(java.lang.String[]);
    public void share(java.lang.String[]);
    public void setAutorotateEnable(java.lang.String[]);
    public int getHeight(java.lang.String[]);
    public int getWidth(java.lang.String[]);
    public void putLocalData(java.lang.String[]);
    public java.lang.String getLocalData(java.lang.String[]);
    public void disturbLongPressGesture(java.lang.String[]);
    public void setIsSupportSwipeCallback(java.lang.String[]);
    public void publishChannelNotificationForJson(java.lang.String[]);
    public void getMBaaSHost(java.lang.String[]);
    public void topBounceViewRefresh(java.lang.String[]);
    public void setAutorotateEnable(java.lang.String[]);
    public void getId(java.lang.String[]);
    public void getWidgetNumber(java.lang.String[]);
    public void getWidgetInfo(java.lang.String[]);
    public void getCurrentWidgetInfo(java.lang.String[]);
    public void getVersion(java.lang.String[]);
    public int getPlatform(java.lang.String[]);
    public void cleanCache(java.lang.String[]);
    public void exit(java.lang.String[]);
    public void getMainWidgetId(java.lang.String[]);

    public void startWidget(java.lang.String[]);
    public void startWidgetWithPath(java.lang.String[]);
    public void finishWidget(java.lang.String[]);
    public void removeWidget(java.lang.String[]);
    public void loadApp(java.lang.String[]);
    public void isAppInstalled(java.lang.String[]);
    public void installApp(java.lang.String[]);
    public void setMySpaceInfo(java.lang.String[]);
    public void getOpenerInfo(java.lang.String[]);
    public void setPushNotifyCallback(java.lang.String[]);
    public void checkUpdate(java.lang.String[]);
    public void setPushInfo(java.lang.String[]);
    public void setPushState(java.lang.String[]);
    public void getPushState(java.lang.String[]);
    public void getPushInfo(java.lang.String[]);
    public void setLogServerIp(java.lang.String[]);
    public void setSpaceEnable(java.lang.String[]);
    public void delPushInfo(java.lang.String[]);
    public void setKeyboardMode(java.lang.String[]);
    public void reload(java.lang.String[]);
    public void reloadWidgetByAppId(java.lang.String[]);
    public void closeLoading(java.lang.String[]);
    public void moveToBack(java.lang.String[]);
    public void setSwipeCloseEnable(java.lang.String[]);
    public void setLoadingImagePath(java.lang.String[]);

    public void setEvent(java.lang.String[]);
    public void beginEvent(java.lang.String[]);
    public void endEvent(java.lang.String[]);
    public void updateParams(java.lang.String[]); 
    public void setErrorReport(java.lang.String[]);
    public void refreshGetAuthorizeID(java.lang.String[]);
    public void getAuthorizeID(java.lang.String[]);
    public void getDisablePlugins(java.lang.String[]);
    public void getDisableWindows(java.lang.String[]);
    public void getUserInfo(java.lang.String[]);

    public void getSessionKey(java.lang.String[]);
    public void appCenterLoginResult(java.lang.String[]);
    public void downloadApp(java.lang.String[]);
    public void loginOut(java.lang.String[]);

    public void insertWindowAboveWindow(java.lang.String[]);
    public void insertWindowBelowWindow(java.lang.String[]);
    public void setWindowHidden(java.lang.String[]);

    public void registerAppEventListener(org.zywx.wbpalmstar.engine.universalex.EUExEventListener);
    public void unRegisterAppEventListener(org.zywx.wbpalmstar.engine.universalex.EUExEventListener);
    public void uexOnAuthorize(java.lang.String);

    public void setWindowScrollbarVisible(java.lang.String[]);
    protected boolean clean();
}
