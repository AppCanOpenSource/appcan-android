var isPhone = (window.navigator.platform != "Win32");
var isAndroid = (window.navigator.userAgent.indexOf('Android')>=0)?true : false;

/*
*判断IOS版本号是否大于7，大于7在顶部加20
*为了避免在每个页面都调用这个方法，可以在zy_control.js文件的zy_init()方法中调用。在使用这种方法时，在应用的首页就不要使用zy_init方法了。
*而是改为直接调用该方法，并且传入一个回调函数，在回调函数中在打开浮动窗口，否则就会出现打开的浮动窗口位置错误的问题
*/
function add20ToHeader(cb){
    if (isAndroid){
                if(cb) cb();
                return;
        }
    if (getLocVal('IOS7Plus')) {
        try {
            if (getLocVal('IOS7Plus') == 2) {
                if ($$('header').style.paddingTop) {
                    $$('header').style.paddingTop = (parseInt($$('header').style.paddingTop) + 20) + 'px';
                }
                else {
                    $$('header').style.paddingTop = '20px';
                }
            }
        }
        catch (e) {
        }
                if(cb) cb();
    }
    else {
        uexDevice.cbGetInfo = function(opId, dataType, data){
            if (data) {
                var device = JSON.parse(data);
                var os = parseInt(device.os);
                if (os >= 7) {
                    setLocVal('IOS7Plus',2);
                    try {
                        if ($$('header').style.paddingTop) {
                            $$('header').style.paddingTop = (parseInt($$('header').style.paddingTop) + 20) + 'px';
                        }
                        else {
                            $$('header').style.paddingTop = '20px';
                        }
                    }
                    catch (e) {
                    }
                }else{
                                        setLocVal('IOS7Plus', 1);
                                }
            }
                        if(cb) cb();
        };
        uexDevice.getInfo('1');
    }
}