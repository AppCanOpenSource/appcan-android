/**
 * Created by ylt on 2017/1/11.
 */

function uexWindowAlert(alertTitle,alertMsg,alertbutton){
    uexWindow.alert({
        title:alertTitle,
        message:alertMsg,
        buttonLabel:alertbutton
    });
}


function showConfirm() {
    var value = document.getElementById('confirmbutton').value;
    if (value == null || value.length == 0) {
        alert("参数不能为空");
        return;
    }
    var mycars = value.split(";");
    uexWindow.confirm({
        title:document.getElementById('confirmTitle').value,
        message:document.getElementById('confirmMsg').value,
        buttonLabels:value
    },function(index){
        alert("用户点击了第 " + index + "个按钮");
    });
}

function selectList(opId, dataType, data) {
    alert("用户点击了第 " + "" + "个按钮");
}

function showPrompt() {
    var value = document.getElementById('promptbutton').value;
    if (value == null || value.length == 0) {
        alert("参数不能为空");
        return;
    }
    var mycars = value.split(";");
    uexWindow.prompt({
        title:document.getElementById('promptTitle').value,
        message:document.getElementById('promptMsg').value,
        defaultValue:document.getElementById('defaultMsg').value,
        buttonLabels:value
    },function(index,data){
        alert("用户的输入：" +data + ",点击了第" + index + "个按钮");
    });

}

function showActionSheet() {
    var value = document.getElementById('actionSheetButton').value;
    if (value == null || value.length == 0) {
        alert("参数不能为空");
        return;
    }
    uexWindow.actionSheet(document.getElementById('actionSheetTitle').value, document.getElementById('cancelMsg').value, value);
}

function showSelectList() {
    var value = document.getElementById('list_item').value;
    if (value == null || value.length == 0) {
        alert("参数不能为空");
        return;
    }
    var mycars = value.split("*");
    uexWindow.selectList(mycars);
}

function actionSheetSuccess(opId, dataType, data) {
    alert("用户点击了第 " + data + "个按钮");
}

function msg() {
    var type = document.getElementById('t_type').value;
    var loc = document.getElementById('t_loc').value;
    var msg = document.getElementById('t_msg').value;
    var del = document.getElementById('t_del').value;
    uexWindow.toast(type, loc, msg, del);
    uexWindow.toast({
        type:type,
        location:loc,
        msg:msg,
        duration:del
    });
}

function closeToast() {
    uexWindow.closeToast();
}

var flag_normal = 0x0; //标准不加密
var flag_dialog = 0x1; //对话框
var flag_ob = 0x2; //加密

function openn() {
    var anim = document.getElementById('opp').value;
    var dizhi = document.getElementById('dizhi').value;
    var time = document.getElementById('tim').value;
    uexWindow.open('dd', '0', dizhi, anim, '', '', flag_normal | 64, time);
}

function openPage(name, page) {
    uexWindow.open(name, '0', page, '0', '', '', 0);
}

function openxll() {
    var anim = document.getElementById('opp').value;
    var time = document.getElementById('tim').value;
    uexWindow.open('dd', '0', 'window/vancl/helloWorld.html', anim, '', '', flag_normal | 64, time);
}

function opennn(arg) {
    uexWindow.open('ddd', '0', 'xx.html', arg, '', '', (flag_normal | flag_ob));
}

function openh(arg) {
    uexWindow.open('exc', '0', 'window/hidden.html', '5', '', '', arg);
}

function openMenu() {
    var as = new Array(3);
    as[0] = document.getElementById('menu_list_0').value;
    as[1] = document.getElementById('menu_list_1').value;
    as[2] = document.getElementById('menu_list_2').value;

    uexWindow.actionSheet({
        title:document.getElementById('menu_title').value,
        cancel:document.getElementById('menu_button').value,
        buttons:as.toString()
    },function(index){
        alert("点击了第"+(index+1)+"个按钮");
    });
}

function openpop() {
    var x = document.getElementById("xx").value;
    var y = document.getElementById("yy").value;
    var w = document.getElementById("ww").value;
    var h = document.getElementById("hh").value;
    uexWindow.openPopover("sss", "0", "window/popoverPage.html", "", x, y, w, h, "5", "130");
}

function closepop() {
    uexWindow.closePopover("sss");
}

function openpopOauth() {
    var x = document.getElementById("xx1").value;
    var y = document.getElementById("yy1").value;
    var w = document.getElementById("ww1").value;
    var h = document.getElementById("hh1").value;
    uexWindow.openPopover("sss", "0", "window/pop.html", "", x, y, w, h, "10", "1");
}

function onOAuth(name, url) {
    document.getElementById('ot').innerHTML = "popOver Name：" + name + " , 改变后的url:" + url;
}

function openad() {
    var lo = document.getElementById("loca").value;
    uexWindow.openAd(lo, '0', '5', '8');
}

function get() {
    uexWindow.getWindowUrl("aaa");
}

function getSuc(windName, url) {
    alert("name:" + windName + ",url:" + url);
}

function stateChange(state) {

    if (state == 0) {
        //alert("state:前台");
    } else {
        //alert("state:后台");
    }
}

function getCurState(opcode, datatype, data) {
    alert(data);
}

function getsyn() {
    alert('Query参数值(android3.0及以上专用,其余版本和Iphone上均取不到值):' + uexWindow.getUrlQuery());
}

function getQuery(opcode, datatype, data) {
    alert(data);
}

function preOpen() {
    uexWindow.open('preopen', '0', 'window/vancl/index.html', '5', '', '', '64');
}

function preOpen1() {
    uexWindow.open('preopen1', '0', 'window/popindex.html', '5', '', '', '64');
}

function createProgressDialog() {
    var title = document.getElementById("t_title").value;
    var content = document.getElementById("t_message").value;
    var cancel = document.getElementById("t_cancel").value;

    uexWindow.createProgressDialog({
        title:title,
        msg:content,
        canCancel:cancel
    });
}

window.uexOnload = function (type) {
    if (type == 0) {
        uexWindow.onStateChange = stateChange;
        uexWindow.closeSlibing("1");
        uexWindow.closeSlibing("2");
        uexWindow.cbGetState = getCurState;
        uexWindow.cbPrompt = function (opCode, errorCode, errorInfo) {
            alert("" + errorInfo);
        };
        uexWindow.cbGetUrlQuery = getQuery;
        uexWindow.cbActionSheet = actionSheetSuccess;
        uexWindow.onKeyPressed = function (keyCode) {
            document.getElementById('key').innerHTML = "点击了：" + keyCode + " 键";
        }
        uexWidgetOne.cbError = function (opCode, errorCode, errorInfo) {
            alert("errorCode:" + errorCode + "\n" + "errorInfo:" + errorInfo);
        }
        uexWindow.onWindowUrlChange = getSuc;
        uexWindow.onOAuthInfo = onOAuth;
    }
}

function winBack() {
    uexWindow.evaluateScript({
        name:"uexWindow",
        type:0,
        js:"winBack()"
    });
}


/////最新测试

function funClose() {

    //如果uexWindow 是浮动窗口，将关闭浮动窗口
    uexWindow.close({
        animID:-1,
        animDuration:300
    });
}


function testForwardWin() {

    //打开一个窗口

    var name = "uexWindow_Forward";
    uexWindow.open({
        name:name,
        data:name + ".html",
        animID:2
    });

}


function winOpenPop2() {

    uexWindow.evaluateScript({
        name:"wintestNormal/uexWindow",
        type:0,
        js:"winPopOpen2()"
    });

}

function closeOpenPop2() {

    uexWindow.evaluateScript({
        name:"wintestNormal/uexWindow",
        type:0,
        js:"closeOpenPop2()"
    });

}

function winOpenMultiPop() {

    uexWindow.evaluateScript({
        name:"wintestNormal/uexWindow",
        type:0,
        js:"winOpenMultiPop()"
    });
}

function winCloseMultiPop() {

    uexWindow.evaluateScript({
        name:"wintestNormal/uexWindow",
        type:0,
        js:"winCloseMultiPop()"
    });

}
