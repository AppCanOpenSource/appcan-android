var platform;//平台，0-iOS，1-android

appcan.button("#showSoftKeyboard","btn-act", function(){
    if(platform == 0){//iOS
        alert("该方法为Android专用");
    }else{//Android
        uexWindow.showSoftKeyboard();
    }
});
appcan.button("#hideSoftKeyboard","btn-act", function(){
    if(platform == 0){//iOS
        alert("该方法为Android专用");
    }else{//Android
        uexWindow.hideSoftKeyboard();
    }
});
appcan.button("#getHeight","btn-act", function(){
    var height=uexWindow.getHeight();
    $("#getHeight_result_display").html("高度:" + height);
});
appcan.button("#getWidth","btn-act", function(){
    var width=uexWindow.getWidth();
    $("#getWidth_result_display").html("宽度:" + width);
});
appcan.button("#setReportKey","btn-act", function(){
    if(platform == 0){//iOS
        alert("该方法为Android专用");
    }else{//Android
        uexWindow.setReportKey(0,1);
        uexWindow.setReportKey(1,1);
    }
});
appcan.button("#postGlobalNotification","btn-act", function(){
    uexWindow.postGlobalNotification("postGlobalNotification发出的消息");
    uexWindow.alert({
        title:"提示",
        message:"消息发送成功",
        buttonLabel:"OK"
    });
});
appcan.button("#publishChannelNotification","btn-act", function(){
    uexWindow.publishChannelNotification("1","publishChannelNotification");
    uexWindow.alert({
        title:"提示",
        message:"消息发送成功",
        buttonLabel:"OK"
    });
});
appcan.button("#publishChannelNotificationForJson","btn-act", function(){
    var json = {
          key : "value"
    }
    uexWindow.publishChannelNotificationForJson("2",JSON.stringify(json));
    uexWindow.alert({
        title:"提示",
        message:"消息发送成功，可关闭该页面，查看前两个页面",
        buttonLabel:"OK"
    });
});
appcan.button("#share","btn-act", function(){
    var params = {
        text:"需要分享的文字"
    };
    var paramStr = JSON.stringify(params);
    uexWindow.share(paramStr);
});
appcan.button("#putLocalData","btn-act", function(){
    uexWindow.putLocalData("platform", "appcan");
});
appcan.button("#getLocalData","btn-act", function(){
    $("#getLocalData_result_display").html("通过platform关键字获取对应的值:" + uexWindow.getLocalData("platform"));
});