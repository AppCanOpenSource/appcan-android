var platform;//平台，0-iOS，1-android

appcan.button("#platformName","btn-act", function(){
    var result=uexWidgetOne.platformName;
    $("#platformName_result_display").html(result);
});
appcan.button("#platformVersion","btn-act", function(){
    var result=uexWidgetOne.platformVersion;
    $("#platformVersion_result_display").html(result);
});
appcan.button("#isFullScreen","btn-act", function(){
    var result = uexWidgetOne.isFullScreen;
    if(result == 0){
        $("#isFullScreen_result_display").html("非全屏(显示状态栏)");
    }else{
        $("#isFullScreen_result_display").html("全屏(不显示状态栏)");
    }
});
appcan.button("#getPlatform","btn-act", function(){
    platform = uexWidgetOne.getPlatform();
    if(platform == 0){
        $("#getPlatform_result_display").html("iOS");
    }else{
        $("#getPlatform_result_display").html("Android");
    }
});
appcan.button("#getCurrentWidgetInfo","btn-act", function(){
    var widgetInfo=uexWidgetOne.getCurrentWidgetInfo();
    $("#getCurrentWidgetInfo_result_display").html(widgetInfo.name);
});
appcan.button("#cleanCache","btn-act", function(){
    uexWidgetOne.cleanCache();
});
appcan.button("#getMainWidgetId","btn-act", function(){
    var result=uexWidgetOne.getMainWidgetId();
    $("#getMainWidgetId_result_display").html(result);
});
appcan.button("#getEngineVersion","btn-act", function(){
    var result=uexWidgetOne.getEngineVersion();
    $("#getEngineVersion_result_display").html(result);
});
appcan.button("#getEngineVersionCode","btn-act", function(){
    var result=uexWidgetOne.getEngineVersionCode();
    $("#getEngineVersionCode_result_display").html(result);
});
appcan.button("#restart","btn-act", function(){
    uexWidgetOne.restart();
});
appcan.button("#exit","btn-act", function(){
    uexWidgetOne.exit(1);
});