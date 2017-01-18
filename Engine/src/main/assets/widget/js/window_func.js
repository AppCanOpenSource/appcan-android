/**
 * Created by ylt on 2017/1/17.
 */

function showConfirm() {
    var value = document.getElementById('confirmbutton').value;
    if (value == null || value.length == 0) {
        alert("参数不能为空");
        return;
    }
    var mycars = value.split(";");
    uexWindow.confirm(document.getElementById('confirmTitle').value, document.getElementById('confirmMsg').value, mycars);
}
window.uexOnload = function (type) {
    if (type == 0) {

    }
}

function winBack() {
    uexWindow.evaluateScript("uexWindow", "0", "winBack()");
}

function share() {
    var imgs = new Array("res://1_1.jpg");
    var params = {
        type: 0,
        text: "test text...text",
        title: "title",
        desc: "desc",
        imgPaths: imgs
    };
    uexWindow.share(params);
}


function putLocalData(data) {
    uexWindow.putLocalData("test_key",data);
}

function getLocalData() {
    alert("获取到的数据是: "+uexWindow.getLocalData("test_key"))
}

function hideStatusBar() {
    uexWindow.hideStatusBar();
}

function showStatusBar() {
    uexWindow.showStatusBar();
}