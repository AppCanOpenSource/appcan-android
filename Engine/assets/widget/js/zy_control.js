var version = '1.6', imgurl = 'http://img.803.com.cn/', serverurl = 'http://d.803.com.cn/', userUrl = serverurl + 'user/'
shopUrl = 'http://d.803.com.cn/shop/';
var sorts = {
    '0': '分类',
    '2': '汽车',
    '3': '丽人',
    '4': '便民',
    '5': '住宿',
    '6': '娱乐',
    '7': '美食',
    '87': '购物',
	'110':'医院',
	'100':'景区',
};
var $support = {
    transform3d: ('WebKitCSSMatrix' in window),
    touch: ('ontouchstart' in window)
};

var $E = {
    start: $support.touch ? 'touchstart' : 'mousedown',
    move: $support.touch ? 'touchmove' : 'mousemove',
    end: $support.touch ? 'touchend' : 'mouseup',
    cancel: $support.touch ? 'touchcancel' : '',
    transEnd: 'webkitTransitionEnd'
};
function getAreaOption(){
    var listHtml = '';
    for (var i = 0; i < data.length; i++) {
        listHtml += '<tr ontouchstart="zy_touch(\'down\')" onclick="jump(' + data[i]['trainID'] + ',\'' + data[i]['trainCode'] + '\',\'' + data[i]['traindeptStation'] + '-' + data[i]['trainArriveStation'] + '\')">' +
        '<td><strong>' +
        data[i]['trainCode'] +
        '</strong><span>' +
        data[i]['trainType'] +
        '</span></td>' +
        '<td><span>' +
        data[i]['trainsearchStation'] +
        '</span><span>' +
        data[i]['traindeptStation'] +
        '-' +
        data[i]['trainArriveStation'] +
        '</span></td>' +
        '<td><span>' +
        data[i]['traindeptTime'] +
        '</span><span>' +
        data[i]['trainarriveTime'] +
        '</span></td>' +
        '<th></th></tr>';
    }
    $$('tab').className = '';
    $$('list').innerHTML = listHtml;
}

function slideJump(types, nid,p){
    if (nid == 0 || nid == '0') 
        return;
    if (types == 'apps') {
		if (types == 'appurl') {
			if (nid.indexOf('http://app.803.com.cn/Weather') == 0 || nid.indexOf('http://app.803.com.cn/weather') == 0) {
				nid = 'weather';
			}
			if (nid.indexOf('http://app.803.com.cn/Tel') == 0 || nid.indexOf('http://app.803.com.cn/tel') == 0) {
				nid = 'tel';
			}
		}
		appJump(nid);
	}
	else {
		if (types == 'coupons') {
			couponsJump(nid);
		}
		else {
			if (types.substring(0, 4) == 'news') {
				setStorJson('params', {
					types: types,
					id: nid,
					filepath: p,
					title: '资讯详情',
					button: 'share'
				});
				if (types == "news_photo") {
					openwin(types, 'news_photo.html', '2');
				}
				else {
					openwin(types, 'head.html', '2');
				}
			}
			else {
				if (types == 'life') {
					setStorJson('params', {
						types: 'shop_item',
						id: nid,
						title: '商家展示'
					});
					openwin('shop_item', 'head.html', '2');
				}
				else {
					if (types == 'url') {
						loadLink(nid);
					}
				}
			}
		}
	}
}

function couponsJump(id){
    appJump('coupons', {
        file: 'item',
        index: 0,
        id: id,
        title: '优惠券'
    });
}

function appJump(name, json){
    if (checkActive(event.currentTarget)) 
        return;
    //file：默认打开文件；index：main是否为默认文件；title：标题；url：如果有url则为远程应用
    var apps = {
        coupons: {
            file: 'index',
            index: 1,
            title: '优惠券',
            bImg: 'coupons/css/res/bg.png'
        },
        movie: {
            file: 'index',
            title: '岳阳影讯'
        },
        mzone: {
            file: 'head',
            index: 1,
            title: '动感地带'
        },
        fund: {
            file: 'head',
            index: 1,
            title: '公积金查询'
        },
        water: {
            file: 'head',
            index: 1,
            title: '水费查询'
        },
        buses: {
            file: 'head',
            index: 1,
            title: '岳阳公交'
        },
        saic: {
            file: 'head',
            index: 1,
            title: '工商查询'
        },
        report: {
            file: 'index',
            index: 1,
            title: '我要爆料'
        },//新加的。
        politics:
		{
			 file: 'index',
            index: 1,
            title: '网络额问政'
		},//新加的问政
        contacts: {
            file: 'head',
            index: 1,
            title: '',
            button: 'settings'
        },
        weather: {
            title: '岳阳天气',
            url: 'http://app.803.com.cn/Weather/'
        },
		medical : {
            file: 'index',
			index: 1,
            title: '医疗'
        },
		wenming : {
            file: 'wenming',
			index: 1,
            title: '网络文明'
        },
        train: {
            file: 'index',
            index: 1,
            title: '火车时刻'
        },
	    bbs: {
            file: 'index',
            index: 1,
            title: '天下岳阳人论坛'
        },
		paper: { 
            file: 'list',
            index: 1,
            title: '岳阳网电子报'
        },
		league: {
            file: 'index',
            index: 1,
            title: '共青频道'
        },
		hnwm2014: {
            file: 'main',
            index: 1,
            title: '网盟高峰论坛'
        },
        tel: {
            file: 'head',
            index: 1,
            title: '常用电话'
        }
        //tel:{file:'index',title:'常用电话',url:'http://app.803.com.cn/tel/'}
    }, params = apps[name];
    if (json) {
        for (i in json) {
            params[i] = json[i];
        }
    }
    var types = 'apps/' + name + (params.file ? '/' + params.file : ''), //窗口名称，唯一
 		index = (params.url) ? 'url' : 'head'; //默认页面
    if (params.index) {
        index = name + '/' + params.file; //设置file为main打开页面
    }
	
    index = (params.list ? '../' : 'apps/') + index;
    params.types = types;
    setStorJson('params', params);
	if (name == 'wenming') {
		openwin("", 'wm_home.html', '2');
	}
	else {
		openwin(types, index + '.html', '2');
	}
    uescript('root', 'appStatistics("' + name + '")'); //统计应用打开次数
}

function shopArea(name){
    if (checkActive(event.currentTarget)) 
        return;
    var shops = {
        express: {
            title: '快递公司',
            cid: 4,
            xid: 40
        },
        takeout: {
            title: '叫外卖',
            cid: 7,
            xid: 21
        },
        ktv: {
            title: 'KTV',
            cid: 6,
            xid: 28
        }
    };
    var params = shops[name];
    var types = 'shop_area'; //窗口名称
    params.types = types;
    setStorJson('params', params);
    openwin(types, 'head.html', '2');
}


function callTel(title, num){
    uexWindow.cbActionSheet = function(opId, dataType, data){
        if (dataType == 2) {
            if (data == 0) {
                uexCall.call(num);
            }
        }
    }
    uexWindow.actionSheet(title, '取消', ['拨打电话']);
    
}

function setStatus(types, id){
    if (types >= 0) {
        if (!$$('status')) {
            var e = ($$(id) || document.body), node = document.createElement('div');
            node.id = 'status';
            e.appendChild(node);//向下插入
        }
        var className = types ? 'nodata' : 'loading';
        setHtml('status', '<div class="' + className + '"></div>');
    }
    else {
        if ($$('status')) {
            $$('status').style.opacity = 0;
            window.setTimeout(function(){
                removeNode('status');
            }, 1000);
        }
    }
    
}

function jumpShop(id){

    if (checkActive(event.currentTarget)) 
        return;
    setStorJson('params', {
        types: 'shop_item',
        id: id,
        title: '商家展示'
    });
    
    openwin('shop_item', 'head.html', '2');
}

function jumpMore(types, title){
    if (!checkLogin()) 
        return;
    var params = {
        'types': types,
        'title': title
    };
    if (types == 'user_favorite') {
        params['button'] = 'manage';
    }
    else 
        if (types == 'user') {
            params['button'] = 'submit';
        }
    setStorJson('params', params);
    openwin(types, 'head.html', '2');
}


function isDefine(para){
    if (typeof para == 'undefined' || para == "" || para == null || para == 'null' || para == undefined) 
        return false;
    else 
        return true;
}

function setJson(str){
    var ret = (typeof(str) == 'object') ? JSON.stringify(str) : JSON.parse(str);
    return ret;
}

function setHtml(id, html){
    if ("string" == typeof(id)) {
        var ele = $$(id);
        if (ele != null) {
            ele.innerHTML = html == null ? "" : html;
        }
    }
    else 
        if (id != null) {
            id.innerHTML = html == null ? "" : html;
        }
}

function getValue(id){
    var e = $$(id);
    if (e) 
        return e.value;
}

function setValue(id, vl){
    var e = $$(id);
    if (e) 
        e.value = vl;
}

function isDefine(para){
    if (typeof para == 'undefined' || para == "" || para == null || para == undefined) 
        return false;
    else 
        return true;
}

function fucCheckLength(strTemp){ //第一种计算字节
    var i, sum;
    sum = 0;
    for (i = 0; i < strTemp.length; i++) {
        if ((strTemp.charCodeAt(i) >= 0) && (strTemp.charCodeAt(i) <= 255)) 
            sum = sum + 1;
        else 
            sum = sum + 2;
    }
    return sum;
}

function strLen(str){ //第二种计算字节
    return str.replace(/[^\x00-\xff]/g, '__').length;
}

function loadBanner(e, url, json){
    var img = new Image();
    img.src = url;
    img.onload = function(){
        var banner = $$(e);
        banner.innerHTML = '<img src="' + this.src + '" ontouchstart="zy_touch()" onclick="slideJump(\'' + json.type + '\',\'' + json.nid + '\',\'' + json.filepath + ')" /><del ontouchstart="zy_touch()" onclick="hideBanner()"></del>';
        banner.style.height = this.height / (this.width / banner.offsetWidth) + 'px';
        banner.style.opacity = 1;
    }
}

function getBanner(id, fun){

    AJAX.get(serverurl + 'ad'+id+'.txt' , function(json){
        var src = json.img;
        zy_imgcache('banner', src, src, function(e, url){
            loadBanner(e, url, json);
        }, function(e){
            loadBanner(e, src, json);
        });
        if (fun) 
            fun(1);
    }, function(){
        if (fun) 
            fun(0);
    }, -10);
}

function hideBanner(){
    if (checkActive(event.currentTarget)) 
        return;
    $$('banner').style.height = '0';
    $$('banner').style.opacity = 0;
}



var rim = (2.5 + 3.125), //顶部和底部的高度
 fontSize = 16, //字体大小
 loadStatus = 0, //加载状态，默认为可加载状态
 maxsize = 0; //是否最大页
function loadScroll(t){
    if (loadStatus) 
        return;
    var scrollTop = document.body.scrollTop || document.documentElement.scrollTop;
    //可见窗口高度+网页被卷去的高
    var downHeight = int(window.screen.availHeight) + int(scrollTop), Height = int(document.body.scrollHeight) - int($$("more").offsetHeight * 3); //body总高度-加载更多的高度
    if (localStorage['device'] == 'ios') 
        downHeight -= rim;
    if (downHeight > Height) {
        Load(1);
        return;
    }
    if (t == 'end') {
        window.setTimeout(function(){
            Scroll('end');
        }, 1000);
    }
}

function bodyTouch(){
    var s = window.getComputedStyle(document.body, null);
    fontSize = int(s.fontSize);
    rim *= fontSize;
    document.body.addEventListener($E.move, function(event){
        loadScroll('move');
    }, false);
    document.body.addEventListener($E.end, function(event){
        loadScroll('end');
    }, false);
}






var isScroll = 0;
function Scroll(){ //是否禁止拖滚动条
    if (isScroll) 
        event.preventDefault();
}

var active = 0;
function checkActive(t){
    if (active) 
        return 1;
    active = 1;
    var arg = (arguments.length == 1);
    if (arg) 
        t.className = ' active';
    window.setTimeout(function(){
        if (arg) 
            t.className = t.className.replace(' active', '');
        active = 0;
    }, 500);
    return 0;
}

function checkActiveNo(t){
    if (active) 
        return 1;
    active = 1;
    window.setTimeout(function(){
        active = 0;
    }, 300);
    return 0;
}

function base64_decode(data){
    var b64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
    var o1, o2, o3, h1, h2, h3, h4, bits, i = 0, ac = 0, dec = "", tmp_arr = [];
    
    if (!data) {
        return data;
    }
    
    data += '';
    
    do { // unpack four hexets into three octets using index points in b64
        h1 = b64.indexOf(data.charAt(i++));
        h2 = b64.indexOf(data.charAt(i++));
        h3 = b64.indexOf(data.charAt(i++));
        h4 = b64.indexOf(data.charAt(i++));
        
        bits = h1 << 18 | h2 << 12 | h3 << 6 | h4;
        
        o1 = bits >> 16 & 0xff;
        o2 = bits >> 8 & 0xff;
        o3 = bits & 0xff;
        
        if (h3 == 64) {
            tmp_arr[ac++] = String.fromCharCode(o1);
        }
        else 
            if (h4 == 64) {
                tmp_arr[ac++] = String.fromCharCode(o1, o2);
            }
            else {
                tmp_arr[ac++] = String.fromCharCode(o1, o2, o3);
            }
    }
    while (i < data.length);
    
    dec = tmp_arr.join('');
    
    return dec;
}

function urlParse(url){
    var params = {};
    var loc = String(url);
    var pieces = loc.substr(loc.indexOf('#') + 1).split('&');
    params.keys = [];
    for (var i = 0; i < pieces.length; i += 1) {
        var keyVal = pieces[i].split('=');
        params[keyVal[0]] = decodeURIComponent(keyVal[1]);
        params.keys.push(keyVal[0]);
    }
    return params;
}

function uescript(wn, scr){
    uexWindow.evaluateScript(wn, '0', scr);
}

function ueppscript(wn, pn, scr){
    uexWindow.evaluatePopoverScript(wn, pn, scr);
}

function openwin(winName, url, anim){
	
    uexWindow.open(winName, "0", url, anim, "", "", "4", "275");
	
}

function closewin(anim){
    var a = '-1';
    if (anim) 
        a = anim;
    uexWindow.close(a);
}

function setstorage(objName, objValue){ //设置字符串类型的本地缓存
    var sto = window.localStorage;
    if (sto) 
        sto.setItem(objName, objValue);
}

function getstorage(objName){ //读取字符串类型的本地缓存
    var ret = '';
    var sto = window.localStorage;
    if (sto) 
        ret = sto.getItem(objName);
    return ret;
}

function clearstorage(objName){ //清除本地缓存，如没指定名称则为清空所有缓存
    var sto = window.localStorage;
    if (sto) {
        if (objName) 
            sto.removeItem(objName);
        else 
            sto.clear();
    }
}

function setStorJson(objName, json){ //设置Json类型的本地缓存
    if (json) 
        setstorage(objName, JSON.stringify(json));
}

function getStorJson(objName){ //读取Json类型的本地缓存
    var ret = {};
    var str = getstorage(objName);
    if (str) 
        ret = JSON.parse(str);
    return ret;
}


window.AJAX = {
    callBack: {},
    index: 1,
    dataType: 'json',
    uri: 'json',
    get: function(url, succCall, errCall, opId, dataType, timeout){
        this.uri = url;
        this.index++;
        var id = (opId || this.index);
        this.callBack[id] = [succCall, errCall];
        this.dataType = (dataType || this.dataType);
        uexXmlHttpMgr.open(id, 'get', url, (timeout || 8000));
        this._send(id);
    },
    post: function(url, data, succCall, errCall, opId, dataType, timeout){
		console.log("POST提交的数据："+JSON.stringify(data)) 
        this.uri = url;
        this.index++;
        var id = (opId || this.index);
        this.callBack[id] = [succCall, errCall];

        this.dataType = (dataType || this.dataType);
        uexXmlHttpMgr.open(id, 'post', url, (timeout || 8000));
        var fileData = null;
        if (data.length == 2) {
            fileData = data[1]; //二进制数据
            data = data[0]; //文字数据
        }
        if (data) {
            for (var k in data) {
                uexXmlHttpMgr.setPostData(id, 0, k, data[k]);
            }
        }
        if (fileData) {
            for (var k in fileData) {
                uexXmlHttpMgr.setPostData(id, 1, k, fileData[k]);
            }
        }
        this._send(id);
    },
    _send: function(id){
		console.log(this.uri) 
        uexXmlHttpMgr.onData = this.onData;
        uexXmlHttpMgr.send(id);
    },
    onData: function(inOpCode, inStatus, inResult){
		//alert(inResult);
        var that = AJAX, callBack = that.callBack[inOpCode] || [];
        if (inStatus == -1) {
            callBack[1] && callBack[1]();
            delete that.callBack[inOpCode];
            uexXmlHttpMgr.close(inOpCode);
        }
        else  if (inStatus == 1) {
                if (that.dataType == 'json') {
					inResult =inResult.replace(/(\n)+|(\r\n)+/g, "");
					inResult =inResult.replace(/@/g,"");
				console.log(inResult) 
					inResult = eval("(" + inResult + ")");
				}else if(that.dataType == 'train'){
					inResult = eval("(" + inResult.replace(/(\n)+|(\r\n)+/g, "").replace(/@/g,"") + ")");
				}else if(that.dataType == 'qiche'){
					inResult = eval("(" + inResult.replace(/(\n)+|(\r\n)+/g, "").replace(/@/g,"") + ")");
				}
					//inResult = eval("(" + inResult.replace("\r","").replace("\n","") + ")");
				
                //inResult= JSON.parse(inResult);
                //inResult= eval("("+inResult+")");
                callBack[0] && callBack[0](inResult);
                delete that.callBack[inOpCode];
                if (that.dataType != 'json') 
                    window.AJAX.dataType = 'json';
                uexXmlHttpMgr.close(inOpCode);
            }
    }
};


var em_focus = 1;
function zy_Switch(t, i){
    var Switch = $$('switch'), Em = Switch.getElementsByTagName('em');
    if (typeof(i) != 'undefined') {
        em_focus = i;
    }
    Switch.querySelector('.focus').className = '';
    if (em_focus == Em.length) {
        em_focus = 0;
    }
    Em[em_focus].className = 'focus';
    t.moveToPoint(em_focus);
    em_focus++;
}

function zy_slide(){
    var switchTime;
    $$('slider').slide = new zySlide('slider', 'H', function(){
        window.clearInterval(switchTime);
        zy_Switch(this, this.currentPoint);
        var t = this;
        switchTime = window.setInterval(function(){
            zy_Switch(t);
        }, 5000);
    }, false, function(e){
    });
    switchTime = window.setInterval(function(){
        zy_Switch($$('slider').slide);
    }, 5000);
}

//调用外部浏览器
function loadLink(url){
    var appInfo = '';
    var filter = '';
    var dataInfo = url;
    //var dataInfo = url.toLowerCase();//全部小写
    if (localStorage['device'] == 'android') {
        appInfo = 'android.intent.action.VIEW';
        filter = 'text/html';
    }
    uexWidget.loadApp(appInfo, filter, dataInfo);
}

function zy_selectmenu(id){
    var sl = document.getElementById(id);
    if (sl) {
        var sp = sl.parentElement; //<span>
        if (sp) {
            var ch = sp.getElementsByTagName("div")[0];
            var t = sl.options[sl.selectedIndex].text;
            if (ch) {
                ch.innerHTML = t;
            }
        }
    }
}

function zy_for(e, cb){
    var ch;
    if (e.currentTarget) 
        ch = e.currentTarget.previousElementSibling;
    else 
        ch = e.previousElementSibling;
    if (ch.nodeName == "INPUT") {
        if (ch.type == "checkbox") 
            ch.checked = !ch.checked;
        if (ch.type == "radio" && !ch.checked) 
            ch.checked = "checked";
        
    }
    if (cb) 
        cb(e, ch.checked);
}


function zy_fold(e, col){
    var a = e.currentTarget.nextElementSibling;
    if (a.nodeName == "DIV") {
        if (col) 
            a.className = a.className.replace("col-c", "");
        else 
            a.className += ' col-c';
    }
}

function zy_touch(c, f){
    var t = event.currentTarget;
    if (!t.zTouch) {
        t.zTouch = new zyClick(t, f, c);
        t.zTouch._touchStart(event);
    }
}


function zy_Bounce(){
    var t = event.currentTarget;
    if (!t.zTouch) {
        t.zTouch = new zyBounce(t);
        t.zTouch._touchStart(event);
    }
}

function zy_parse(){
    var params = {};
    var loc = String(document.location);
    if (loc.indexOf("?") > 0) 
        loc = loc.substr(loc.indexOf('?') + 1);
    else 
        loc = uexWindow.getUrlQuery();
    var pieces = loc.split('&');
    params.keys = [];
    for (var i = 0; i < pieces.length; i += 1) {
        var keyVal = pieces[i].split('=');
        params[keyVal[0]] = decodeURIComponent(keyVal[1]);
        params.keys.push(keyVal[0]);
    }
    return params;
}

function $$(id){
    return document.getElementById(id);
}

function int(s){
    return parseInt(s);
}

function Int(s){
    return int(s);
}

function zy_con(id, url, x, y){
    var s = window.getComputedStyle($$(id), null);
    uexWindow.openPopover(id, "0", url, "", int(x), int(y), int(s.width), int(s.height), int(s.fontSize), "4");
}

function zy_resize(id, x, y){
    var s = window.getComputedStyle($$(id), null);
    uexWindow.setPopoverFrame(id, int(x), int(y), int(s.width), int(s.height));
}

function zy_init(){
    if (window.navigator.platform == "Win32") {
        document.body.style.fontSize = window.localStorage["defaultfontsize"];
    }
    if (int(localStorage['screen']) > 1000) {
        //document.body.style.fontSize='16px';
    }
}

function zy_cc(t){
    if (!t.cancelClick) {
        t.cancelClick = true;
        t.addEventListener("click", function(){
            event.stopPropagation();
        }, true);
    }
}

function removeNode(id){
    var e = $$(id);
    if (e) 
        e.parentElement.removeChild(e);
}

function Trim(str){
    return str.replace(/(^\s*)|(\s*$)/g, "")
}

function LTrim(str){
    return str.replace(/(^\s*)/g, "")
}

function RTrim(str){
    return str.replace(/(\s*$)/g, "")
}

function AllTrim(str){
    return str.replace(/\s*/g, '')
}



function substr(str, len){
    if (!str || !len) {
        return '';
    }
    // 预期计数：中文2字节，英文1字节
    var a = 0;
    // 循环计数
    var i = 0;
    // 临时字串
    var temp = '';
    for (i = 0; i < str.length; i++) {
        if (str.charCodeAt(i) > 255) {
            // 按照预期计数增加2
            a += 2;
        }
        else {
            a++;
        }
        // 如果增加计数后长度大于限定长度，就直接返回临时字符串
        if (a > len) {
            return temp;
        }
        // 将当前内容加到临时字符串
        temp += str.charAt(i);
    }
    // 如果全部是单字节字符，就直接返回源字符串
    return str;
}


function Now(){
    var myDate = new Date()
    return myDate.getFullYear() + '-' + (myDate.getMonth() + 1) + '-' + (myDate.getDate() < 10 ? '0' : '') + myDate.getDate() + ' ' + (myDate.getHours() < 10 ? '0' : '') + myDate.getHours() + ':' + (myDate.getMinutes() < 10 ? '0' : '') + myDate.getMinutes() + ':' + (myDate.getSeconds() < 10 ? '0' : '') + myDate.getSeconds();
}
