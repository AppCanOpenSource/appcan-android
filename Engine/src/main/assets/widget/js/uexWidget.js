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

}