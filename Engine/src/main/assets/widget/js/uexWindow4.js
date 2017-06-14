var width = appcan.locStorage.getVal("screenWidth");

appcan.button("#setSlidingWindow","btn-act", function(){
    uexWindow.setSlidingWindow({
        leftSliding:{
            width:Math.floor(width/3),
            url:"sliding_win_left.html"
        },
        rightSliding:{
            width:Math.floor(width/3),
            url:"sliding_win_right.html"
        },
        animationId:1,
        bg:"res://girl.jpg"
    });
});
appcan.button("#setSlidingWindowEnabled","btn-act", function(){
    uexWindow.setSlidingWindowEnabled(1);
});
appcan.button("#toggleSlidingWindow","btn-act", function(){
    var params  = {
        mark:0,
        reload:1
    };
    uexWindow.toggleSlidingWindow(params);
uexWindow.onSlidingWindowStateChanged = function(state){
                    console.log("onSlidingWindowStateChanged-state:" + state);
                }
});

function getSlidingWindowState(){
    var state = uexWindow.getSlidingWindowState();
    switch(state){//0:左侧菜单显示;1:主界面显示;2:右侧菜单显示
        case 0:
            $("#getSlidingWindowState_result_display").html("左侧菜单显示");
        break;
        case 1:
            $("#getSlidingWindowState_result_display").html("主界面显示");
        break;
        case 2:
            $("#getSlidingWindowState_result_display").html("右侧菜单显示");
        break;
    }
}

appcan.button("#getSlidingWindowState","btn-act", function(){
    getSlidingWindowState();
});