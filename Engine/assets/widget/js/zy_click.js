	var $support = {
		transform3d: ('WebKitCSSMatrix' in window),
		touch: ('ontouchstart' in window)
	};

	var $E = {
		start: $support.touch ? 'touchstart' : 'mousedown',
		move: $support.touch ? 'touchmove' : 'mousemove',
		end: $support.touch ? 'touchend' : 'mouseup',
		cancel: $support.touch ? 'touchcancel' : '',
		transEnd:'webkitTransitionEnd'
	};
	function getPage (event, page) {
		return $support.touch ? event.changedTouches[0][page] : event[page];
	}
	var zyClick = function(selector,clickFun,css){
		var self = this;
		self._clickFun = clickFun || null;
		self._click=false;
		self.css = css;
		if(typeof selector == 'object')
			self.element = selector;
		else if(typeof selector == 'string')
			self.element = document.getElementById(selector);
		self.attribute=self.element.getAttribute("onclick");
		self.element.removeAttribute("onclick");
		self.element.addEventListener($E.start, self, false);
		self.element.addEventListener($E.move, self, false);
		self.element.addEventListener($E.end, self, false);
		if($E.cancel!="")
		{
			self.element.addEventListener($E.cancel, self, false);
			document.addEventListener($E.cancel, self, false);
		}
		document.addEventListener($E.end, self, false);
		
		return self;
	}

	zyClick.prototype = {
		handleEvent: function(event) {
			var self = this;
			switch (event.type) {
				case $E.start:
					self._touchStart(event);
					break;
				case $E.move:
					self._touchMove(event);
					break;
				case $E.end:
				case $E.cancel:
					self._touchEnd(event);
					break;
			}
		},
        _touchStart: function(event) {
        	
            var self = this;
            self.start = true;
            self._click=true;
            self.startPageX = getPage(event, 'pageX');
            self.startPageY = getPage(event, 'pageY');
            self.startTime = event.timeStamp;
            if (self.css && !self.touchTime) {
                self.touchTime=setTimeout(function(){
                    if (self._click && self.element.className.indexOf(self.css)<0) 
                        self.element.className += (" " + self.css);
                }, 50);
			}
        },
        _touchMove: function(event) {
        	
            var self = this;
			if(!self.start)
				return;

            var pageX = getPage(event, 'pageX'),
                pageY = getPage(event, 'pageY'),
                deltaX = pageX - self.startPageX,
                deltaY = pageY - self.startPageY;

           if((Math.abs(deltaX)>5 || Math.abs(deltaY)>5))
           {
				if (self._click) {
					clearTimeout(self.touchTime);
					self.touchTime = null;
					self._click = false;
					if (self.css) 
						self.element.className = self.element.className.replace(" " + self.css, "");
				}
		   }		
		   else{

		   }
        },
        _touchEnd: function(event) {
        	
            var self = this;
			
            if(self.start == true)
            {
				if (self.touchTime) {
					clearTimeout(self.touchTime);
					self.touchTime=null;
				}
            	self.start = false;
            	if(self.css)
            		self.element.className=self.element.className.replace(" "+self.css,"");
            	if(self._click)
            	{
            		self._click=false;
            		if(event.timeStamp - self.startTime>1000)
            			return;
					if(event.type==$E.cancel)
						return;
            		if(self._clickFun)
	           			self._clickFun(self.element);
					if(self.attribute)
						eval(self.attribute);
            	}
            }
        },
        destroy: function() {
            var self = this;

            self.element.removeEventListener($E.start, self);
            self.element.removeEventListener($E.move, self);
            self.element.removeEventListener($E.end, self);
            self.element.removeEventListener($E.cancel, self);
            document.removeEventListener($E.end, self);
            document.removeEventListener($E.cancel, self);
        }
	}


