(function($) {
    function isWindows() {
        if(window.navigator.platform == "Win32") return true;
        if (!('ontouchstart' in window))
            return true;
    }

    var touchEnd = isWindows() ? 'MSPointerUp pointerup mouseup' : 'touchend MSPointerUp pointerup';

    var scrollBoxConfig = Backbone.Model.extend({
        defaults : {
            "bounce" : 1
        }
    });
    var scrollView = Backbone.View.extend({//options...
        initialize : function(option) {
            var $el = this.$el = option.$el;
            this.bounceBox = $(".scrollbox", this.$el);
            this.bounceHeight = $(".box_bounce", this.bounceBox).height();
            this.bounceStatus = $(".bounce_status", this.bounceBox);
            $el[0].scrollView = this;
            this.render();
            this.listenTo(this.model, "change:bounce", function(data) {
                data.changed.bounce && !this.bounceStatus.hasClass("active") && this.bounceStatus.addClass("active");
                !data.changed.bounce && this.bounceStatus.hasClass("active") && this.bounceStatus.removeClass("active");
            })
            this.listenTo(this.model, "change:percent", function(data) {
                var switchBounce = this.model.get("bounce");
                var bounceStatus = this.model.get("bounceStatus");
                var percent = data.changed.percent;
                switchBounce == 1 && percent > 30 && bounceStatus != 1 && this.model.set("bounceStatus", 1);
                switchBounce == 1 && percent <= 30 && bounceStatus != 0 && this.model.set("bounceStatus", 0);
            })
            this.listenTo(this.model, "change:bounceStatus", function(data) {
                if (this.model.get("bounce") == 1) {
                    if (data.changed.bounceStatus == 1)
                        (this.bounceStatus.removeClass("active"), $(this.bounceStatus[1]).addClass("active"), this.trigger("dragToReload"));
                    else if (data.changed.bounceStatus == 0)
                        (this.bounceStatus.removeClass("active"), $(this.bounceStatus[0]).addClass("active"));
                    else if (data.changed.bounceStatus == 2) {(this.bounceStatus.removeClass("active"), $(this.bounceStatus[2]).addClass("active"));
                        this.bounceBox.css("-webkit-transform", "translate3d(0px," + this.bounceHeight * 0.33 + "px,0px)");
                        this.trigger("releaseToReload");
                    }
                }
            })
        },
        reset : function() {
            this.model.set("bottomStatus", "0");
            if (this.bounceBox) {
                var self = this;
                var switchBounce = this.model.get("bounce");
                this.model.set("bounceStatus", "0");
                self.bounceBox.css("-webkit-transform", "translate3d(0px," + 0 + "px,0px)");
            }
        },
        hide : function() {
            this.model.set("bounce", 0);
            this.bounceStatus.removeClass("active")
        },
        show : function() {
            this.model.set("bounce", 1);
            $(this.bounceStatus[0]).addClass("active");
        },
        reload : function() {
            if (this.bounceBox) {
                !this.bounceBox.hasClass("utra") && this.bounceBox.addClass("utra");
                var switchBounce = this.model.get("bounce");
                this.model.set("bounceStatus", "2");
            }
        },
        render : function() {
            var self = this;
            var $el = this.$el;
            if (this.model.get("bounce") != 1) {
                this.bounceStatus.removeClass("active");
            }
            self.$el.on("swipeMoveDown", function(e, _args) {
                self.bounceHeight = $(".box_bounce", self.bounceBox).height();
                if (!e._args)
                    e._args = _args;
                if ($el.scrollTop() == 0) {
                    self.bounceBox.hasClass("utra") && self.bounceBox.removeClass("utra");
                    var bounceStatus = self.model.get("bounceStatus");
                    if (bounceStatus == 2) {
                        self.trigger("onReloading", bounceStatus);
                        return;
                    }
                    var percent = parseInt(e._args.dy / 3 * 100 / self.bounceHeight);
                    self.bounceBox.css("-webkit-transform", "translate3d(0px," + e._args.dy / 3 + "px,0px)");
                    e._args.e.preventDefault();
                    self.trigger("draging", {
                        percent : percent
                    });
                    self.model.set("percent", percent);
                }
            })
            self.$el.on(touchEnd, function(e) {
                if ($el.scrollTop() == 0) {
                    if (self.bounceBox) {
                        !self.bounceBox.hasClass("utra") && self.bounceBox.addClass("utra");
                        var status = self.model.get("bounceStatus");
                        if (!status || status == 0) {
                            self.trigger("release", {});
                            self.reset();
                        } else if (status == 1) {
                            self.reload();
                        }
                    }
                }
            })
            var scrollContainer = $el;
            if ($el[0].tagName == "BODY")
                scrollContainer = $(document);
            scrollContainer.on("scroll", function() {
                if (self.bounceBox) {
                    var h = self.bounceBox.height();
                    var ch = $el.height();
                    if ($el.scrollTop() + ch >= h - 50) {
                        var bottomStatus = self.model.get("bottomStatus");
                        if (bottomStatus != 1) 
                        {
                            self.model.set("bottomStatus", "1");
                            self.trigger("scrollbottom");
                        }
                    }
                }
            })
        }
    });
    $.scrollbox = function($el, option) {
        if ($el[0].scrollView) {
            $el[0].scrollView.model.set(option);
            return $el[0].scrollView;
        }
        var opt = _.extend({}, option);
        var model = new scrollBoxConfig(opt);
        return new scrollView({
            $el : $el,
            model : model
        });
    }
})($)
