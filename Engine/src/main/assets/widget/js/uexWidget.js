/**
 * Created by ylt on 2017/1/17.
 */


function isAppInstalled() {
    var appData = document.getElementById('isAppInstalled').value;
    var params = {
        appData: appData
    };
    var data = JSON.stringify(params);
    uexWidget.isAppInstalled(data);
}

function startApp() {
    var appData = document.getElementById('isAppInstalled').value;
    uexWidget.startApp(0, appData);
}
window.uexOnload = function (type) {
    if (type == 0) {
        uexWidget.cbIsAppInstalled = cbIsAppInstalled;
        uexWidget.cbStartApp = cbStartApp;
        uexWindow.onNotification = onNotification;
    }
}
function cbIsAppInstalled(info) {
    var result = JSON.parse(info);
    if (result.installed == 0) {
        alert('installed');
    } else {
        alert('not installed');
    }
}

function cbStartApp(info) {
    alert("cbStartApp:" + info);
}
function moveToBack() {
    uexWidget.moveToBack();
}

function openQQBrowser() {
    var optInfo = "{'key1':'value1'},{'key2':'value1'}";
    var extra = '{data:"http://www.cnblogs.com"}';
    uexWidget.startApp(0, "com.tencent.mtt", "com.tencent.mtt.MainActivity", optInfo, extra);
}

function startWidget() {
    uexWindow.subscribeChannelNotification("emailFresh","onNotification");

    uexWidget.startWidget({
            appId:"123456",
            animId:"10",
            info:"这是从主widget传过来的info",
            animDuration:300
        }
    ,function (error) {
        if(!error){
            alert("加载成功");
        }else{
            alert("加载失败");
        }
    });
}

function onNotification() {
    alert("发送成功！！!!!!！");
}