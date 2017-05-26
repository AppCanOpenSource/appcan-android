var platform;//平台，0-iOS，1-android

appcan.button("#startWidget","btn-act", function(){
    var data = {
      appId:'123321',
      animId:'1',
      funcName:'widgetStartFinish',
      info:'我来打开一个子widget',
      animDuration:300
    }
    uexWidget.startWidget(data,function(error){
        if(!error){
            $("#startWidget_result_display").html("widget 123321 加载成功!");
        }
    });
});
appcan.button("#removeWidget","btn-act", function(){
    var result=uexWidget.removeWidget("123321");
    if(result){
        $("#removeWidget_result_display").html("widget 123321 删除成功!");
    }else{
        $("#removeWidget_result_display").html("widget 123321 删除失败!");
    }
});
appcan.button("#checkUpdate","btn-act", function(){
    uexWidget.checkUpdate(function(error,data){
        //error 为对象,!error表示请求成功
        if(!error){
            if(data.result==0){
                //需要更新
                $("#checkUpdate_result_display").html("有更新!");
            }else{
                //不需要更新
                $("#checkUpdate_result_display").html("没有更新!");
            }
        }else{
            $("#checkUpdate_result_display").html("请求失败:" + JSON.stringify(error));
        }
    });
});
appcan.button("#loadApp","btn-act", function(){
    if(platform == 0){
        alert("即将打开微信应用");
        uexWidget.loadApp("weixin");
    }else{
        alert("该方法为iOS专用");
    }
});
appcan.button("#installApp","btn-act", function(){
    if(platform == 0){//iOS
        alert("该方法为Android专用");
    }else{//Android
        uexWidget.installApp("res://widgettesttool.apk");
    }
});
appcan.button("#startApp0","btn-act", function(){
    if(platform == 0){//iOS
        alert("该方法为Android专用");
    }else{//Android
        var packageName = "appcan.plugin.android.widgettesttool";
        uexWidget.startApp(0, packageName);//启动入口页面
    }
});
appcan.button("#startApp1","btn-act", function(){
    if(platform == 0){//iOS
        alert("该方法为Android专用");
    }else{//Android
        var action = "com.appcan.test.engine";
        uexWidget.startApp(1, action);//启动MainActivity
    }
});
appcan.button("#isAppInstalled","btn-act", function(){
    var appData = "";
    if(platform == 0){//iOS
        appData = "mqq";
    }else{//Android
        appData = "com.tencent.mobileqq";
    }
    var param1 = {
        appData:appData//判断手机上是否安装qq应用
    };
    var data1 = JSON.stringify(param1);
    var result = uexWidget.isAppInstalled(data1);
    if(result){
        $("#isAppInstalled_result_display").html("已经安装QQ");
    }else{
        $("#isAppInstalled_result_display").html("未安装QQ");
    }
});
appcan.button("#moveToBack","btn-act", function(){
    if(platform == 0){//iOS
        alert("该方法为Android专用");
    }else{//Android
        uexWidget.moveToBack();
    }
});
appcan.button("#setKeyboardMode","btn-act", function(){
    if(platform == 0){//iOS
        alert("该方法为Android专用");
    }else{//Android
        uexWidget.setKeyboardMode({
            mode:0
        });
    }
});
appcan.button("#setPushState0","btn-act", function(){
    uexWidget.setPushState(1);
    $("#setPushState_result_display").html("已开启推送");
});
appcan.button("#setPushState1","btn-act", function(){
    uexWidget.setPushState(0);
    $("#setPushState_result_display").html("已关闭推送");
});
appcan.button("#getPushState","btn-act", function(){
    var result = uexWidget.getPushState();
    $("#getPushState_result_display").html(result);
});
appcan.button("#setPushNotifiCallback","btn-act", function(){
    uexWidget.setPushNotifiCallback("pushNotifyCallback");
});
appcan.button("#setPushInfo","btn-act", function(){
    uexWidget.setPushInfo('123', 'appcan_user');
});
appcan.button("#getPushInfo","btn-act", function(){
    var result = uexWidget.getPushInfo();
    $("#getPushInfo_result_display").html(result);
});

function pushNotifyCallback(data) {
    $("#getPushState_result_display").html("收到消息:" + data);
}
