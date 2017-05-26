/*
 author:dushaobin
 email:shaobin.du@3g2win.com
 description:appcan常用插件
 created:2014,08.18
 update:chenxue:xue.chen3g2win.com

 */
/*global appcan,window */
appcan.use("detect", function($, detect) {
    if (detect.os.ios) {
        var viewport = document.getElementsByName("viewport");
        var scale = window.devicePixelRatio ? (1 / window.devicePixelRatio) : 1;
        $("[name='viewport']").attr("content", "width=device-width,target-densitydpi=device-dpi,initial-scale=" + scale + ", minimum-scale=" + scale + ", maximum-scale=" + scale);
        var fontsize = $("body").css("font-size");
        $("body").css("font-size", parseInt(fontsize) * window.devicePixelRatio + "px");
    }
})

appcan.extend(function(app, exports, module) {
    var $ = appcan.require('dom');
    var appWin = appcan.require('window');
    var locStorage = appcan.require('locStorage');
    var view = appcan.require('view');
    /*
     * 兼容pc，移动设备的封装
     * @param String className 要更改的class
     * @param Functon fun 执行结束后的回调
     *
     *
     */
    function isWindows() {
        if(window.navigator.platform == "Win32") return true;
        if (!('ontouchstart' in window))
            return true;
    }

    function button(selector, className, fun) {
        $(selector).on(isWindows() ? "mousedown" : "touchstart", function() {
            appcan.touch(className, fun);
        })
    }

    function updateSwitch(obj) {
        var value = obj.attr("data-checked") || 'false';
        value = (value == 'false' ? 'true' : 'false');
        obj.attr("data-checked", value);
        value == 'false' ? obj.addClass("switch-active") : obj.removeClass("switch-active");
        value == 'false' ? obj.removeClass("bc-head") : obj.addClass("bc-head");

        
    }

    function switchBtn(selector, css, cb) {
        if ( typeof css == 'function') {
            cb = css;
            css = null;
        }
        var obj = $(selector);
        for (var i = 0; i < obj.length; i++) {
            var value = $(obj[i]).attr("data-checked") || 'false';
            value == 'false' ? $(obj[i]).removeClass( css ? css : "bc-head") : $(obj[i]).addClass( css ? css : "bc-head");
        }
        $(selector).on("tap", function(evt) {
            var obj = $(evt.currentTarget);
            var value = obj.attr("data-checked") || 'false';
            value = (value == 'false' ? 'true' : 'false');
            obj.attr("data-checked", value);
            value == 'false' ? obj.removeClass( css ? css : "bc-head") : obj.addClass( css ? css : "bc-head");
            value = value == 'false'?false:true;
            cb(obj, value);
        })
    }

    function select(sel, cb) {
        $("select",$(sel))[0].selectedIndex = -1;
        var tl =$(sel).find("div[class=text]");
        var sl =$(sel).find('select');
        var op =sl.find('option');
        var index =parseInt(sl.attr('selectedIndex'));
        if(index != -1){
            tl.html(sl[0].options[index].text);
        }

        $("select", $(sel)).on("change", function(evt) {
            var ele = $(evt.currentTarget);
            appcan.selectChange(evt.currentTarget);
            cb && cb(ele, ele.val());
        });
    }


    function touch(className, fun) {
        var ele = window.event.currentTarget || window.event.srcElement;
        var eventType = window.event.type;
        var hasTouch = ('ontouchstart' in window);
        var $ele = $(ele);
        var ed = $ele.data('inline_event_data');
        if (ed) {
            return;
        }
        ed = {};
        ed.clickFun = $ele.attr('onclick');
        ed.startFun = $ele.attr('ontouchstart') || $ele.attr('onmousedown');
        ed.endFun = $ele.attr('ontouchend') || $ele.attr('onmouseup');
        ed.startClassName = className;
        ed.startCallFun = fun;
        ed.endClassName = ed.startClassName;
        ed.endCallFun = ed.startCallFun;
        $ele.data('inline_event_data', ed);
        $ele.attr('onclick', '');
        $ele.attr('ontouchstart', '');
        $ele.attr('onmousedown', '');
        $ele.attr('ontouchend', '');
        $ele.attr('onmouseup', '');
        if (hasTouch) {
            //set click and change css
            $ele.on('touchstart', function() {
                if (!ed.startClassName) {
                    return;
                }
                $(this).addClass(ed.startClassName);
            });
            $ele.on('touchend', function() {
                if (!ed.endClassName) {
                    return;
                }
                $(this).removeClass(ed.endClassName);
            });
            $ele.on('touchcancel', function() {
                if (!ed.endClassName) {
                    return;
                }
                $(this).removeClass(ed.endClassName);
            });
            
            $ele.on('tap', function() {
                if (appcan.isFunction(ed.startCallFun)) {
                    ed.startCallFun.apply(this, [].slice.call(arguments));
                }
                var f = new Function(ed.clickFun);
                f();
            });
        } else {
            //just change css
            $ele.on('mousedown', function() {
                if (!ed.startClassName) {
                    return;
                }
                var that = this;
                $(this).addClass(ed.startClassName);
                setTimeout(function(){
                    $(that).removeClass(ed.endClassName);
                },300);
            });
            $ele.on('mouseup', function() {
                if (!ed.endClassName) {
                    return;
                }
                $(this).removeClass(ed.endClassName);
            });
            $ele.on('click', function() {
                if (appcan.isFunction(ed.startCallFun)) {
                    ed.startCallFun.apply(this, [].slice.call(arguments));
                }
                var f = new Function(ed.clickFun);
                f();
            });
        }
        //bind webkitTransitionEnd event
        $ele.addClass(ed.startClassName);
        $ele.on('webkitTransitionEnd', function() {

        });
    }

    /*
     * 设置相关check元素选中
     * @param Event e 点击该元素的事件
     * @param Function callback 执行结束后事件回调 todo:同步执行函数没有必要用回调
     *
     *
     */

    function elementFor(e, callback) {
        var forEle;
        e = e || window.event;
        if (e.currentTarget) {
            forEle = e.currentTarget.previousElementSibling;
        } else {
            forEle = e.previousElementSibling;
        }
        var $forEle = $(forEle);
        if ($forEle.prop('tagName') === 'INPUT') {
            if ($forEle.attr('type') === 'checkbox') {
                forEle.checked = !forEle.checked;
                $forEle.trigger("change");
            }
            if ($forEle.attr('type') === 'radio' && !forEle.checked) {
                forEle.checked = 'checked';
                $forEle.trigger("change");
            }
        }
        if (appcan.isFunction(callback)) {
            callback(e, forEle.checked);
        }
    }

    /*
     * 如果是win平太设置body 默认字体
     *
     *
     */

    function initFontsize() {
        var platform = window.navigator.platform;
        if (platform.toLowerCase().indexOf('win') > -1 || platform.toLowerCase().indexOf('wow') > -1) {
            var fs = locStorage.getVal('defaultfontsize');
            if (fs) {
                $('body').css('font-size', fs);
            }
        }
    }

    /*
     下拉列表控件
     @param ELE id 下拉列表select标签的对象

     */

    var selectChange = function(sel) {
        var sl = $(sel)[0];
        if (sl) {
            var sp = sl.parentElement;
            //<span>
            if (sp) {
                var ch = sp.getElementsByTagName('div')[0];
                var t = sl.options[sl.selectedIndex].text;
                if (ch) {
                    $(ch).html(t);
                }
            }
        }
    };
    /*
     字符组去除后面空格
     @param Object e event对象
     @param Boolean col 布尔值
     */
    var fold = function(e, col) {
        e = e || window.event;
        var a = e.currentTarget.nextElementSibling;
        if (a.nodeName == 'DIV') {
            var $a = $(a);
            if (col) {
                $a.removeClass('col-c');
            } else {
                $a.addClass('col-c');
            }
        }
    };
    /*
     parseInt
     @param String s 字符串
     */
    var toInt = function(s) {
        return parseInt(s);
    };
    /*
     停止事件的传播
     @param Object t
     */
    var stopPropagation = function(t) {
        if (!t.cancelClick) {
            t.cancelClick = true;
            t.addEventListener('click', function() {
                event.stopPropagation();
            }, true);
        }
    };
    /*
     * 用指定元素的样式打开一个弹框
     * @param String eleId 指定元素的id
     * @param String url 要打开的连接地址
     * @param Int left 距左边的距离
     * @param Int top 距上边的距离
     * @param String name 窗口名称，默认为id
     * @param String  
     *
     */
    function openPopoverByEle(eleId, url, left, top, type, name) {
        if (!eleId) {
            return;
        }
        var ele = $('#' + eleId);
        appWin.openPopover({
            name : ( name ? name : eleId),
            url : url,
            left : left,
            top : top,
            width : ele.width(),
            height : ele.height(),
            type:(type || 0)
        });
    }

    /*
     * 简单的模版插件
     *
     *
     */

    var tmpl = function() {
        var config = {
            evaluate : /<%([\s\S]+?)%>/g,
            interpolate : /<%=([\s\S]+?)%>/g,
            escape : /\${([\s\S]+?)}/g
        };
        var args = [].slice.call(arguments);
        args.push(config);
        return view.render.apply(this, args);
    };
    var tmplAppend = function() {
        var config = {
            evaluate : /<%([\s\S]+?)%>/g,
            interpolate : /<%=([\s\S]+?)%>/g,
            escape : /\${([\s\S]+?)}/g
        };
        var args = [].slice.call(arguments);
        args.push(config);
        return view.appendRender.apply(this, args);
    };
    /*
     * 打开一个新窗口
     * @param String name 窗口的名字
     * @param String url 要加载的地址
     * @param Int aniId 动画效果
     * @param int tpye 窗口的类型
     0:普通窗口
     1:OAuth 窗口
     2:加密页面窗口
     4:强制刷新
     8:url用系统浏览器打开
     16:view不透明
     32:隐藏的winwdow
     64:等待popOver加载完毕后显示
     128:支持手势
     256:标记opn的window上一个window不隐藏
     512:标记呗open的浮动窗口用友打开wabapp
     @param animDuration 动画时长
     *
     */

    function openWinWithUrl(name, url, aniId, type, animDuration) {
        if (!name) {
            return;
        }
        appcan.window.open({
            name : name,
            data : url,
            aniId : (aniId || 10),
            type : (type || 0),
            animDuration : (animDuration || 300)
        });
    }

    /*
     * 关闭窗口
     *
     *
     */
    function closeWin(aniId) {
        aniId = aniId || -1;
        appWin.close(aniId);
    }

    /*
     * 设置localstorage的值
     *
     *
     */
    function setLocalVal(key, val) {
        return locStorage.setVal(key, val);
    }

    /*
     * 获取localstorage的值
     *
     *
     */
    function getLocalVal(key) {
        return locStorage.getVal(key);
    }

    /*
     *
     * 在窗口中执行脚本
     *
     *
     *
     */

    function execScriptInWin(name, scriptContent) {
        if (!name || !scriptContent) {
            return;
        }
        appWin.evaluateScript({
            name : name,
            scriptContent : scriptContent
        });
    }

    /*
     * 初始弹动，无回调方法
     */
    function initBounce() {
        appWin.setBounceType('0', 'rgba(255,255,255,0)', 0);
        appWin.setBounceType('1', 'rgba(255,255,255,0)', 0);
    }

    /*
     * 获取当前对象内属性的个数
     *
     */
    function objLength(obj) {
        var c = 0;
        if (!obj) {
            return c;
        }
        if (appcan.isArray(obj)) {
            return obj.length;
        } else {
            if (!appcan.isPlainObject(obj)) {
                return c;
            }
            for (var i in obj) {
                c++;
            }
            return c;
        }
    }

    /*
     *
     * 获取模版中的开始结束回调
     *
     * @param String k1 last: 或者 first:
     *
     */
    function getTempCallback(d, c, k1, k2, l) {
        var q = c.match(/(first:|last:)(\"|\'*)([^\"\']*)(\"|\'*)/);
        if (!q) {
            return;
        }
        if (q[1] == k1) {
            if (q[2] == '\"' || q[2] == '\'') {
                return q[3];
            } else {
                return d[q[3]];
            }
        } else if (q[1] == k2 && l > 1) {
            return "";
        }
    }

    /*
     *
     *
     *
     */

    function oldTemp(t, d, i, l, cb) {
        //匹配出 $｛内容｝
        return t.replace(/\$\{([^\}]*)\}/g, function(m, c) {
            if (c.match(/index:/)) {
                return i;
            }
            if (c.match(/cb:/) && cb) {
                return cb(d, c.match(/cb:(.*)/));
            }
            if (i == 0) {
                var s = getTempCallback(d, c, "first:", "last:", l);
                if (s) {
                    return s;
                }
            }
            if (i == (l - 1)) {
                var s = getTempCallback(d, c, "last:", "first:", l);
                if (s) {
                    return s;
                }
            }
            var ar = c.split('.');
            var res = d;
            for (var key in ar) {
                res = res[ar[key]];
            }
            return res || "";
        });
    };

    /*
     *
     * 根据指定的列表返回对应的数据
     *
     */
    var oldTempRenderList = function(t, dd, l, cb, scb) {
        var r = "";
        var index = 0;
        for (var i in dd) {
            if (scb) {
                scb(0, i, dd[i]);
            }
            var rr = oldTemp(t, dd[i], index, l, cb);
            if (scb) {
                scb(1, rr, dd[i]);
            }
            r += rr;
            index++;
        }
        return r;
    };
    /*
     *
     * 返回指定的数据
     *
     */
    function oldTempRender(t, dd, cb) {
        return oldTemp(t, dd, -1, -1, cb);
    }
    
    /*
        
        按顺序执行
        
    */
    function series(tasks,callback){
        if(appcan.isFunction(tasks)){
            callback = tasks;
            tasks = [];
        }
        callback = appcan.isFunction(callback) ? callback : function() {
        };
        var errData = 0;
        var resData = null;
        var taskIdx = 0;
        
        function nextTask(){
            if(taskIdx >= tasks.length){
                callback(0);
                return;
            }
            var tsk = tasks[taskIdx];
            if(tsk && appcan.isFunction(tsk)){
                try{
                    if(tsk(function(err,data){
                        if(err === 0){
                            taskIdx++;
                            nextTask();
                        }else{
                            //结束任务
                            callback(err,data);
                            return;
                        }
                    }) === false){
                        //结束任务
                        callback(0);
                        return;
                    }
                }catch(e){
                    callback(e);
                }
            }
        }
        
        nextTask();
        
    }
    

    //for emulator
    $(document).ready(function() {

    });

    initFontsize();

    module.exports = {
        elementFor : elementFor,
        touch : touch,
        initFontsize : initFontsize,
        openPopoverByEle : openPopoverByEle,
        resizePopoverByEle : appWin.resizePopoverByEle,
        tmpl : tmpl,
        tmplAppend : tmplAppend,
        openWinWithUrl : openWinWithUrl,
        closeWin : closeWin,
        closePopover : appWin.closePopover,
        setLocVal : setLocalVal,
        getLocVal : getLocalVal,
        execScriptInWin : execScriptInWin,
        bringPopoverToFront : appWin.bringPopoverToFront,
        initBounce : initBounce,
        evaluatePopoverScript : appWin.evaluatePopoverScript,
        alert : appWin.alert,
        getObjLength : objLength,
        getTempCallback : getTempCallback,
        temp : oldTemp,
        tempRenderList : oldTempRenderList,
        tempRender : oldTempRender,
        stopPropagation : stopPropagation,
        toInt : toInt,
        selectChange : selectChange,
        fold : fold,
        button : button,
        select : select,
        "switch": switchBtn,
        switchBtn : switchBtn,
        series:series,
        updateSwitch:updateSwitch
    };
});

appcan.ready(function(){
    var ios7style=uexWidgetOne.iOS7Style;
    var isFullScreen = uexWidgetOne.isFullScreen;
    if (ios7style == '1' && isFullScreen != '1') {
      $("body").addClass("uh_ios7");
    }
});

