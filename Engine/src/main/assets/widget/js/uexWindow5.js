appcan.button("#setBounce","btn-act", function(){
    uexWindow.setBounce(1);
});
appcan.button("#getBounce","btn-act", function(){
    var status =  uexWindow.getBounce();
    $("#getBounce_result_display").html(status == 0 ? "不支持" : "支持");
});
appcan.button("#notifyBounceEvent","btn-act", function(){
    uexWindow.notifyBounceEvent(0,1);
});
appcan.button("#showBounceView","btn-act", function(){
    uexWindow.showBounceView({
      type:0,
      color:"#ffffff",
      flag:1
    });
});
appcan.button("#resetBounceView","btn-act", function(){
    uexWindow.resetBounceView(0);
});
appcan.button("#setBounceParams","btn-act", function(){
    var json={
        "textColor":"#000",
        "imagePath":"res://refresh_icon.png",
        "levelText":"更新日期",
        "pullToReloadText":"拖动到底部",
        "releaseToReloadText":"释放回原处",
        "loadingText":"更新中..."
    };
    uexWindow.setBounceParams(0, json);
});
appcan.button("#hiddenBounceView","btn-act", function(){
    uexWindow.hiddenBounceView(0);
});
appcan.button("#topBounceViewRefresh","btn-act", function(){
    uexWindow.topBounceViewRefresh();
});