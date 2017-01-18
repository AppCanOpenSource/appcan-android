/**
 * Created by ylt on 2017/1/17.
 */


window.uexOnload = function (type) {

    uexWidget.onLoadByOtherApp = onLoadByOtherApp;
    uexWindow.onStateChange=onStateChange;
    var name = uexWidgetOne.platformName;

//            alert(name);

    var w_1 = 240;      /// just for testing

    if (name == "android") {

        var ratio = window.devicePixelRatio;

//                alert(ratio);

        if (ratio >= 3) {
            w_1 = 720;    /// just for testing
        } else if (ratio >= 2) {
            w_1 = 360;
        }


    }

    uexWindow.setSlidingWindow({
        leftSliding:{
            width:w_1,
            url:"wintestNav/uexWindow_left.html"
        },
        rightSliding:{
            width:w_1,
            url:"wintestNav/uexWindow_right.html"
        },
        animationId:1,
        bg:"res://girl.jpg"
    });
};

function onLoadByOtherApp(data) {
    var last = JSON.stringify(data);
    alert("onLoadByOtherApp:" + last);
}

function openTest(name) {

}

function onStateChange(state) {
    console.log("onStateChange: "+state);
    if(state==0){
        //回到前台
        uexWindow.setSlidingWindowEnabled(1);
    }else{
        //压入后台
        uexWindow.setSlidingWindowEnabled(0);
    }


}

function openTestController(name) {
    uexWindow.openWithController("ssss", '0', "system/system.html");
}

function openWindow(name) {
    uexWindow.open({
        name: name,
        data: name + ".html",
        animID: 2,
        flag: 1024
    });
}

function openWinInMainWithSliding() {
    var params = {
        mark: 0

    };

    var paramStr = JSON.stringify(params);
    uexWindow.toggleSlidingWindow(paramStr);
    openTest('wintestNav/uexWindow');
}

function openWinInMainWithRightSliding() {
    alert('aaaaa');

    var params = {
        mark: 1

    };

    var paramStr = JSON.stringify(params);
    uexWindow.toggleSlidingWindow(paramStr);
    openTest('wintestNav/uexWindow');
}

function openLeftSlidingWin() {
    var params = {
        mark: 0

    };

    var paramStr = JSON.stringify(params);
    uexWindow.toggleSlidingWindow(paramStr);
}