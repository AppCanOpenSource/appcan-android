
appcan.button("#close","btn-act", function(){
    closeCurrentWin();
});
appcan.button("#open","btn-act", function(){
    appcan.locStorage.setVal("test_desc", "这是通过open接口打开的主窗口");
    openWindow("descTest");
});
appcan.button("#windowBack","btn-act", function(){
    uexWindow.windowBack({
        animID:-1,
        animDuration:300
    });
});
appcan.button("#windowForward","btn-act", function(){
    uexWindow.windowForward({
        animID:2,
        animDuration:300
    });
});
appcan.button("#forward","btn-act", function(){
    uexWindow.forward();
});
appcan.button("#back","btn-act", function(){
    uexWindow.back();
});
appcan.button("#pageBack","btn-act", function(){
    uexWindow.pageBack();
});
appcan.button("#pageForward","btn-act", function(){
    uexWindow.pageForward();
});
appcan.button("#openSlibing","btn-act", function(){
    uexWindow.openSlibing({
        type:2,
        dataType:"0",
        url:"slibing_window.html",
        h:Math.floor(height/10),
        data:""
    });
});
appcan.button("#closeSlibing","btn-act", function(){
    uexWindow.closeSlibing(2);
});
appcan.button("#showSlibing","btn-act", function(){
    uexWindow.showSlibing(2);
});
appcan.button("#evaluateScript","btn-act", function(){
    openWindow("descTest");
});
appcan.button("#setWindowHidden","btn-act", function(){
    uexWindow.evaluateScript({
        name:"descTest",
        type:0,
        js:"setWindowHidden(0);"
    });
});

appcan.button("#insertWindowAboveWindow","btn-act", function(){
    uexWindow.insertWindowAboveWindow("win_B","win_A");
});
appcan.button("#insertWindowBelowWindow","btn-act", function(){
    uexWindow.insertWindowBelowWindow("win_B","win_A");
});

appcan.button("#setWindowFrame","btn-act", function(){
    uexWindow.setWindowFrame({
       x:50,
       y:0,
       animDuration:1000
    });
});
appcan.button("#setWindowScrollbarVisible","btn-act", function(){
    uexWindow.setWindowScrollbarVisible("false");
});
appcan.button("#openPresentWindow","btn-act", function(){
    uexWindow.openPresentWindow({
        name:"presentWindow",
        data:"presentWindow.html",
        w:100,
        h:100,
        flag:1024
    });
});
appcan.button("#closeAboveWndByName","btn-act", function(){
    uexWindow.closeAboveWndByName("root");
});