appcan.define("treeview", function($, exports, module) {

    var listTmp ='<li class="treeview">\
        <div class="treeview-header ubb bc-border bc-text ub uinn ub-ac">\
        <%if (option.hasIcon) {%>\
        <div class="lis-icon-s ub-img" style="background-image:url(<%=icon%>)"></div>\
        <%}%>\
        <div class="ub-f1 ut-s"><%=header%></div>\
        <%if(option.hasAngle){%>\
        <div class="fa fa-angle-down ulev2 utra sc-text"></div>\
        <%}%>\
        </div>\
        <div class="treeview-content umh6 bc-text uhide">\
        <%=content%>\
        </div>\
        </li>';
        
    var listRender = appcan.view.template(listTmp);

    var isEmulator = (window.navigator.platform == "Win32");

    var touchEvent = {
        'start' : isEmulator ? 'mousedown' : 'touchstart',
        'move' : isEmulator ? 'mousemove' : 'touchmove',
        'end' : isEmulator ? 'mouseup' : 'touchend',
        'cancel' : 'touchcancel'
    };


    var Treeview = function(option) {
        appcan.extend(this, appcan.eventEmitter);
        this.option = $.extend({
            selector : "body",
            type : '',
            hasIcon : false,
            hasAngle : true,
            hasTouchEffect : true,
            touchClass : 'sc-bg-active',
            isCloseOther : true,
            defaultOpen : -1,
            autoScrollTop : false
        }, option, true);
        this.ele = $(this.option.selector);
        if (this.option.data) {
            this.set(this.option.data);
        }
        this.open(this.option.defaultOpen);
    };

    Treeview.prototype = {
        constructor : Treeview,
        createPlugin : function(selector) {
            if (!this.plugin) {
                return;
            }
            var option = $.extend({
                selector : selector
            }, this.pluginOption, true);
            return this.plugin(option)
        },
        getContent : function(data, settings) {
            var contentData = data.content;
            if (!contentData) {
                return;
            }
            var self = this;
            var index = 0;
            if (!settings.type) {
                if ((appcan.isArray(contentData) || appcan.isPlainObject(contentData)) && !$.zepto.isZ(contentData)) {
                    var warper = $('<div class="listview"></div>');
                    settings.option.selector = warper;
                    var lv = appcan.listview(settings.option);
                    lv.set(contentData);
                    lv.on('click', function(ele, obj, subobj) {
                        self.emit("listviewClick", self,ele, obj, subobj, data);
                    })
                    warper[0].plugin = lv;
                    return warper;
                }
                return contentData;
            } else {
                var plugin = appcan[settings.type];
                if (plugin) {
                    settings.option.data = contentData;
                    return plugin(settings.option);
                }
            }
        },
        buildTreeview : function(data, settings) {
            var container = $("<ul></ul>");
            var self = this;
            for (var i in data) {
                var html = listRender;
                var option = {};
                option = $.extend({}, settings && settings.option, true);
                option = $.extend(option, data[i].option, true);
                var processType = data[i].type || (settings && settings.type);
                settings = {
                    option : option,
                    type : processType
                };
                var content = self.getContent(data[i], settings);
                var ele = $(html({
                    header : data[i].header,
                    icon:data[i].icon,
                    content : '',
                    option : self.option
                }));
                var tvHeader = ele.find('.treeview-header');
                tvHeader[0]["tv_data"] = data[i];
                if (!settings.type) {
                    if (content) {
                        ele.find('.treeview-content').html(content);
                    } else {
                        ele.find('.treeview-header .fa-angle-down').removeClass('fa-angle-down').addClass('fa-angle-right');
                        ele.find('.treeview-content').remove();
                    }
                } else {
                    ele[0]['plugin'] = content;
                }
                tvHeader.on('tap', function(evt) {
                    self.itemClick(evt);
                });
                tvHeader.on(touchEvent.start, function(evt) {
                    self.touchItem(evt);
                });
                tvHeader.on('longTap', function(evt) {
                    self.longTapItem(evt);
                });
                tvHeader.on('swipe', function(evt) {
                    self.longTapItem(evt);
                });
                ele.on('tap', function(evt) {
                    self.treeItemClick(evt)
                });
                container.append(ele);
            }
            return container;
        },
        set : function(data, settings) {
            var self = this;
            var container = self.buildTreeview(data, settings);
            self.ele.html(container);
            return self;
        },
        add : function(data, dir, settings) {
            var self = this;
            var container = self.buildTreeview(data, settings);
            if (dir || dir == 1) {
                self.ele.append(container)
            } else {
                var first = self.ele.children().first();
                first.prepend(container);
            };
            return self;
        },
        treeItemClick : function(evt) {
            var obj = $(evt.currentTarget);
            var tvHeader = obj.find('.treeview-header');
            this.emit("click", self, obj, tvHeader[0]["tv_data"], $(evt.target));
        },
        itemClick : function(evt) {
            var treeviewHeader = $(evt.currentTarget);
            var data = treeviewHeader[0]["tv_data"];
            if(data.content)
                this.showItem(treeviewHeader);
        },
        showItem : function(header) {
            var treeviewHeader = header;
            var obj = treeviewHeader.parent();

            var contentEle = obj.find('.treeview-content');
            if(treeviewHeader.length){
                var data = treeviewHeader[0]["tv_data"];
                if(data.content)
                    obj.siblings().find('.treeview-header').removeClass(this.option.touchClass);
            }
            if (this.option.isCloseOther) {
                obj.siblings().find('.treeview-content').addClass('uhide');
                obj.siblings().find('.fa-angle-down').removeClass('fa-rotate-180');
            }
            if (contentEle.hasClass('uhide')) {
                contentEle.removeClass('uhide');
                treeviewHeader.addClass(this.option.touchClass);
                obj.find('.fa-angle-down').addClass('fa-rotate-180');
            } else {
                contentEle.addClass('uhide');
                treeviewHeader.removeClass(this.option.touchClass);
                obj.find('.fa-angle-down').removeClass('fa-rotate-180');
            }
            if (this.option.autoScrollTop) {
                var offset = treeviewHeader.offset();
                var top = offset && offset.top || 0;
                $(window).scrollTop(top);
            }
        },
        touchItem : function(evt) {
            var self = this;
            var obj = $(evt.currentTarget);
            if (this.option.hasTouchEffect ) {
                var data = obj[0]["tv_data"];
                obj.addClass(self.option.touchClass);
                if(!data.content)
                {
                    setTimeout(function(){
                        obj.removeClass(self.option.touchClass);
                    },300);
                }
            }

        },
        longTapItem : function(evt) {
            if(this.option.hasTouchEffect){
                var header = $(evt.currentTarget);;
                var obj = header.parent();
                var contentEle = obj.find('.treeview-content');
                if(contentEle.hasClass("uhide")){
                    header.removeClass(this.option.touchClass);
                }
            }
        },
        //打开索引位置的选项
        open : function(index) {
            index = parseInt(index, 10);
            if (isNaN(index) || index < 0) {
                return;
            }
            this.showItem(this.ele.find('.treeview').eq(index).find('.treeview-header'));
        },
        hideItem:function(header){
            var treeviewHeader = header;
            var obj = treeviewHeader.parent();
            
            var contentEle = obj.find('.treeview-content');
            if(treeviewHeader.length){
                var data = treeviewHeader[0]["tv_data"];
                if(data.content){
                    obj.siblings().find('.treeview-header').removeClass(this.option.touchClass);
                }
            }
            /*
            if (this.option.isCloseOther) {
                obj.siblings().find('.treeview-content').addClass('uhide');
                obj.siblings().find('.fa-angle-down').removeClass('fa-rotate-180');
            }
            */
            if (contentEle.hasClass('uhide')) {
                //contentEle.removeClass('uhide');
                //treeviewHeader.addClass(this.option.touchClass);
                //obj.find('.fa-angle-down').addClass('fa-rotate-180');
            } 
            else {
                contentEle.addClass('uhide');
                treeviewHeader.removeClass(this.option.touchClass);
                obj.find('.fa-angle-down').removeClass('fa-rotate-180');
            }
            if (this.option.autoScrollTop) {
                var offset = treeviewHeader.offset();
                var top = offset && offset.top || 0;
                $(window).scrollTop(top);
            }
            
        },
        //收起指定所以位置的选项
        hide:function(index){
            index = parseInt(index, 10);
            if (isNaN(index) || index < 0) {
                return;
            }
            this.hideItem(this.ele.find('.treeview').eq(index).find('.treeview-header'));
        },
        //收起所有的选项
        hideAll:function(){
            for(var i=0,len=this.ele.find('.treeview').length;i<len;i++){
                this.hide(i);
            }
        }
    };

    module.exports = function(option) {
        return new Treeview(option);
    };
});
