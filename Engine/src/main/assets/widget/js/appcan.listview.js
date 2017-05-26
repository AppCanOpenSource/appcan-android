appcan.define("listview", function($, exports, module) {
var model_thinLine =  '<li class="ubb ub bc-border bc-text ub-ac lis" <%if(group){%>data-group="<%=group%>"<%}%> <%if(data.id){%>id="<%=data.id%>"<%}%> data-index="<%=index%>">\
<%if(option.hasCheckbox && option.align=="left"){%>\
<div class="checkbox umar-r"><input type="checkbox" class="uabs ub-con" ></div>\
<%}%>\
<%if(option.hasRadiobox && option.align=="left"){%>\
<div class="radiobox umar-r" name=""><input type="radio" class="uabs ub-con" name="lv_radio_<%=option.id%>"></div>\
<%}%>\
<%if(option.hasIcon && data.icon){%>\
<div class="lazy <%if(option.hasSmallIcon){%>lis-icon-ss<%}else{%>lis-icon-s<%}%> ub-img" data-original="<%=data.icon%>" style="background-image:url(<%=data.icon%>)"></div>\
<%}%>\
<div class="lv_title ub-f1 marg-l ub ub-ver ut-m line<%=option.multiLine%>">\
<%=data.title%>\
</div>\
<%if(option.hasSubTitle){%>\
<div class="tx-r sc-text ulev-1 umar-r lv_subTitle">\
<%=data.subTitle%>\
</div>\
<%}%>\
<%if(option.hasControl && data.switchBtn){%>\
<div class="switch uba bc-border <%if(data.switchBtn.mini){%>switch-mini<%}%>" data-checked="<%=data.switchBtn.value%>">\
<div class="switch-btn sc-bg-active "></div>\
</div>\
<%}%>\
<%if(option.hasAngle && !(option.hasControl && data.switchBtn)){%>\
<div class="fa fa-angle-right ulev2 sc-text"></div>\
<%}%>\
<%if(option.hasCheckbox && option.align=="right"){%>\
<div class="checkbox  umar-l"><input type="checkbox" class="uabs ub-con"></div>\
<%}%>\
<%if(option.hasRadiobox && option.align=="right"){%>\
<div class="radiobox umar-l"><input type="radio" class="uabs ub-con" name="lv_radio_<%=option.id%>"></div>\
<%}%>\
</li>';

var model_groupLine= '<li id="lv_group_<%=data.groupId%>" class="ubb ub bc-border bc-text sc-bg-active ub-ac lis group" data-index="<%=index%>">\
<%=data.title%>\
</li>';
var model_thickLine = '<li <%if(data.id){%>id="<%=data.id%>"<%}%> class="ubb ub bc-border t-bla ub-ac lis"  data-index="<%=index%>">\
<%if(option.hasCheckbox && option.align=="left"){%>\
<div class="checkbox umar-r"><input type="checkbox" class="uabs ub-con" ></div>\
<%}%>\
<%if(option.hasRadiobox && option.align=="left"){%>\
<div class="radiobox umar-r" name=""><input type="radio" class="uabs ub-con" name="lv_radio_<%=option.id%>"></div>\
<%}%>\
<%if(option.hasIcon && data.icon){%>\
<ul class="ub ub ub-ver">\
<li class="">\
<div class="lazy lis-icon ub-img" data-original="<%=data.icon%>" style="background-image:url(<%=data.icon%>)"></div>\
<div class="ulev-1 bc-text umar-t"><%=data.icontitle%></div>\
</li>\
</ul>\
<%}%>\
<ul class="ub-f1 ub ub-pj ub-ac">\
<ul class="ub-f1 ub ub-ver marg-l">\
<li class="bc-text ub ub-ver ut-m line<%=option.multiLine%>"><%=data.title%></li>\
<%if(data.describe){%><li class="ulev-1 sc-text1 uinn3"><%=data.describe%> </li><%}%>\
<%if(data.note){%><li class="ulev-2 sc-text1 uinn3"><%=data.note%></li><%}%>\
</ul>\
<%if(option.hasSubTitle){%>\
<ul class="ub ub-ver ub-ae umar-r">\
<%if(data.subTitle){%><li class="bc-text lv_subTitle"><%=data.subTitle%></li><%}%>\
<%if(data.subDescribe){%><li class="ulev-1 sc-text1 uinn3"><%=data.subDescribe%></li><%}%>\
<%if(data.subNote){%><li class="ulev-2 sc-text1 uinn3"><%=data.subNote%></li><%}%>\
</ul>\
<%}%>\
<%if(option.hasControl && data.switchBtn){%>\
<div class="switch uba bc-border <%if(data.switchBtn.mini){%>switch-mini<%}%>" data-checked="<%=data.switchBtn.value%>">\
<div class="switch-btn sc-bg-active "></div>\
</div>\
<%}%>\
<%if(option.hasAngle && !(option.hasControl && data.switchBtn)){%>\
<li class="fa fa-angle-right ulev2"></li>\
<%}%>\
<%if(option.hasCheckbox && option.align=="right"){%>\
<div class="checkbox  umar-l"><input type="checkbox" class="uabs ub-con"></div>\
<%}%>\
<%if(option.hasRadiobox && option.align=="right"){%>\
<div class="radiobox umar-l"><input type="radio" class="uabs ub-con" name="lv_radio_<%=option.id%>"></div>\
<%}%>\
</ul>\
</li>';
    var thinLineTmp = appcan.view.template(model_thinLine);
    var groupLineTmp = appcan.view.template(model_groupLine);
    var thickLineTmp = appcan.view.template(model_thickLine);
    var viewid = 1;
    function isWindows() {
        if(window.navigator.platform == "Win32") return true;
        if (!('ontouchstart' in window))
            return true;
    }

    function Listview(option) {
        appcan.extend(this, appcan.eventEmitter);
        var self = this;
        self.option = $.extend({
            selector : "body",
            type : "thinLine",
            hasIcon : true,
            hasAngle : true,
            hasSubTitle : false,
            hasTouchEffect : true,
            hasCheckbox : false,
            hasRadiobox : false,
            hasControl : false,
            hasGroup:false,
            align : 'left',
            multiLine : 1,
            touchClass : 'sc-bg-active',
            id : viewid++
        }, option, true);
        self.ele = $(self.option.selector);
        if (self.option.data) {
            self.set(data);
        }

    };

    Listview.prototype = {
        buildListview : function(data,group) {
            var container = $("<ul></ul>")
            var self = this;
            var html = (self.option.type == "thinLine") ? thinLineTmp : thickLineTmp;
            for (var i in data) {
                var ele = $(html({
                    data : data[i],
                    option : self.option,
                    index:i,
                    group:group
                }));
                ele[0]["lv_data"] = data[i];
                ele.on('tap', function(evt) {
                    self.itemClick(evt);
                });
                ele.on(isWindows() ? 'mousedown' : 'touchstart', function(evt) {
                    self.touchItem(evt)
                });
                ele.on('longTap', function(evt) {
                    self.longTapItem(evt)
                });
                ele.on('cancleTap', function(evt) {
                    self.longTapItem(evt)
                });
                if (self.option.hasCheckbox || self.option.hasRadiobox) {
                    (function(ele){
                        ele.find("input").on('change', function(evt) {
                            ele[0]["lv_data"].checked = evt.currentTarget.checked;
                            if (self.option.hasCheckbox){
                                self.emit("checkbox:change", self, ele, ele[0]["lv_data"]);
                            }
                            if (self.option.hasRadiobox){
                                self.emit("radio:change", self, ele, ele[0]["lv_data"]);
                            }
                        });
                    })(ele)
                }
                container.append(ele);
            }
            var switchBtns = $(".switch", container);
            appcan.switchBtn(switchBtns, function(obj, value) {
                var ele = obj.parent();
                ele[0]["lv_data"].switchBtn.value = value;
                self.emit("switch:change", self, ele, ele[0]["lv_data"]);
            })
            return container;
        },
        buildGroupview:function(data){
            var self = this;
            var con = $("<ul></ul>");
            for(var i in data){
                var ele = $(groupLineTmp({
                    data : data[i],
                    option : self.option,
                    index:i
                }));
                con.append(ele);
                con.append(self.buildListview(data[i].items,i));
            }
            return con;
        },
        add : function(data, dir) {
            var self = this;
            var container = self.buildListview(data);
            if (dir || dir == 1)
                self.ele.append(container)
            else {
                var first = self.ele.children().first();
                first.prepend(container);
            };
            return self;
        },
        set : function(data) {
            var self = this;
            var container;
            if(self.option.hasGroup){
                container = self.buildGroupview(data);
            }
            else
                container = self.buildListview(data);
            self.ele.html(container);
            return self;
        },
        itemClick : function(evt) {
            var self = this;
            var obj = $(evt.currentTarget);
            obj.removeClass(this.option.touchClass);
            if (self.option.hasCheckbox) {
                if($(evt.target).is('input[type=checkbox]')){
                    return;
                }
                var checkbox = obj.find("input")
                checkbox[0].checked = !checkbox[0].checked;
            }
            if (self.option.hasRadiobox) {
                if($(evt.target).is('input[type=radio]')){
                    return;
                }
                var radio = obj.find("input");
                radio[0].checked = true;
            }
            this.emit("click", self, obj, obj[0]["lv_data"], $(evt.target));
        },
        touchItem : function(evt) {
            if (this.option.hasTouchEffect) {
                var self = this;
                var obj = $(evt.currentTarget);
                if (obj[0]["lv_data"].switchBtn && self.option.hasControl)
                    return;
                obj.addClass(self.option.touchClass);
                setTimeout(function() {
                    obj.removeClass(self.option.touchClass);
                }, 300);
            }
        },
        longTapItem : function(evt) {
            if (this.option.hasTouchEffect) {
                var obj = $(evt.currentTarget);
                obj.removeClass(this.option.touchClass);
            }
        },
        updateItem : function(item, name, value) {
            switch(name) {
            case "title":
                $(".lv_title", item).html(value);
                break;
            case "subTitle":
                $(".lv_subTitle", item).html(value);
                break;
            }
        }
    }
    module.exports = function(option) {
        return new Listview(option);
    };
});
