var platform;//平台，0-iOS，1-android
var height = appcan.locStorage.getVal("screenHeight");
var width = appcan.locStorage.getVal("screenWidth");


appcan.button("#openPopover","btn-act", function(){
    uexWindow.openPopover({
        name:"win_popover",
        url:"win_popover.html",
        x:Math.floor(width/4),
        y:Math.floor(height/2),
        w:Math.floor(width/2),
        h:Math.floor(height/2)
    });
});
appcan.button("#setPopoverVisibility1","btn-act", function(){
    uexWindow.setPopoverVisibility('win_popover',1);//显示
});
appcan.button("#setPopoverVisibility2","btn-act", function(){
    uexWindow.setPopoverVisibility('win_popover',0);//不显示
});
appcan.button("#evaluatePopoverScript","btn-act", function(){
    var param = 'pop';
    uexWindow.evaluatePopoverScript({
        windowName:"",
        popName:"win_popover",
        js:"funEvaluatePopover('" + param + "');"
    });
});
appcan.button("#closePopover","btn-act", function(){
    uexWindow.closePopover('win_popover');
});
appcan.button("#setPopoverFrame","btn-act", function(){
    uexWindow.setPopoverFrame({
        name:'win_popover',
        x:Math.floor(width/4),
        y:Math.floor(height/2),
        w:Math.floor(width/2),
        h:Math.floor(height/2-50)
    });
});
appcan.button("#openMultiPopover","btn-act", function(){
    uexWindow.openMultiPopover({
        content: {
            content: [
                {
                     inPageName: "multiPopover1",
                     inUrl: "multiPopover1.html",
                     inData: "",
                     extraInfo: {opaque:true,bgColor:"#011"}
                },
                {
                     inPageName: "multiPopover2",
                     inUrl: "multiPopover2.html",
                     inData: "",
                     extraInfo: {opaque:true,bgColor:"#022"}
                }
            ]
        },
        name: "multiPopover",
        dataType: 0,
        x: Math.floor(width/5),
        y: Math.floor(height/5),
        w: Math.floor(width/2),
        h: Math.floor(height/2),
        flag: 0,
        indexSelected: 1,
        extras:{
            extraInfo:{opaque:true,bgColor:"#011", delayTime:250}
        }
    });
});
appcan.button("#closeMultiPopover","btn-act", function(){
    uexWindow.closeMultiPopover('multiPopover');
});
appcan.button("#setMultilPopoverFlippingEnbaled1","btn-act", function(){
    uexWindow.setMultilPopoverFlippingEnbaled(0);
});
appcan.button("#setMultilPopoverFlippingEnbaled2","btn-act", function(){
    uexWindow.setMultilPopoverFlippingEnbaled(1);
});
appcan.button("#setSelectedPopOverInMultiWindow","btn-act", function(){
    uexWindow.setSelectedPopOverInMultiWindow({
        name:'multiPopover',
        index:0
    });
});
appcan.button("#setMultiPopoverFrame","btn-act", function(){
    uexWindow.setMultiPopoverFrame({
        name:"multiPopover",
        x: Math.floor(width/5),
        y: Math.floor(height/5),
        w: Math.floor(width/2),
        h: Math.floor(height/2 - 20),
    });
});
appcan.button("#evaluateMultiPopoverScript","btn-act", function(){
    uexWindow.evaluateMultiPopoverScript({
        windowName:"",//默认为当前主窗口
        popName:"multiPopover",
        pageName:"multiPopover1",
        js:"funEvaluateMultiPopover('multi')"
    });
});
appcan.button("#insertPopoverAbovePopover","btn-act", function(){
    uexWindow.insertPopoverAbovePopover("win_popover_A","win_popover_C");
});
appcan.button("#insertPopoverBelowPopover","btn-act", function(){
    uexWindow.insertPopoverBelowPopover("win_popover_A","win_popover_C")
});
appcan.button("#bringPopoverToFront","btn-act", function(){
    uexWindow.bringPopoverToFront("win_popover_A");
});
appcan.button("#sendPopoverToBack","btn-act", function(){
    uexWindow.sendPopoverToBack("win_popover_A");
});
appcan.button("#bringToFront","btn-act", function(){
    uexWindow.evaluatePopoverScript({
        windowName:"",
        popName:"win_popover_A",
        js:"sendAToFront();"
    });
});
appcan.button("#sendToBack","btn-act", function(){
    uexWindow.sendToBack();
});
appcan.button("#insertAbove","btn-act", function(){
    uexWindow.evaluatePopoverScript({
        windowName:"",
        popName:"win_popover_B",
        js:"insertBAboveC();"
    });
});
appcan.button("#insertBelow","btn-act", function(){
    uexWindow.insertBelow("win_popover_C");
});



