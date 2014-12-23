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
	var zyEvent = function(selector,moveFun,endFun,lock,transEnd){
		var self = this;
		var max = 50;
		self.Max = {X:max,Y:max};
		self.endFun = endFun || null;
		self.moveFun = moveFun || null;
		self.transEnd = transEnd||null;
		self.type = "";
		self._vendor=(window.navigator.userAgent.indexOf("Android 4.")>=0);
		self._locked=lock;
		self.start = false;
		if(typeof selector == 'object')
			self.element = selector;
		else if(typeof selector == 'string')
			self.element = document.getElementById(selector);
		self.element.addEventListener($E.start, self, false);
		self.element.addEventListener($E.move, self, false);
		self.element.addEventListener($E.transEnd, self, false);
		if($E.cancel!="")
		{
			self.element.addEventListener($E.cancel, self, false);
			document.addEventListener($E.cancel, self, false);
		}
		document.addEventListener($E.end, self, false);

		return self;
	}

	zyEvent.prototype = {
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
				case 'click':
					self._click(event);
					break;
				case $E.transEnd:
					if(self.transEnd)
						self.transEnd(event);
					break;
				
			}
		},
        _touchStart: function(event) {
        	
            var self = this;
            self.start = true;
            if (!$support.touch) {
                event.preventDefault();
            }
            if(self._locked)
            {
	            event.stopPropagation();
            }
			self.type = "";
            self.startPageX = getPage(event, 'pageX');
            self.startPageY = getPage(event, 'pageY');
            self.startTime = event.timeStamp;
        },
        _touchMove: function(event) {
        	
            var self = this;
			if(!self.start)
				return;
				
            var pageX = getPage(event, 'pageX'),
                pageY = getPage(event, 'pageY'),
                deltaX = pageX - self.startPageX,
                deltaY = pageY - self.startPageY;
		   if(self._vendor)//Android 4.0 need prevent default
		   {
		   		event.preventDefault();
		   }
           if(self._locked)
           {
           		event.preventDefault();
	            event.stopPropagation();
           }

				if(Math.abs(deltaX)> Math.abs(deltaY) && deltaX<-self.Max.X)
					self.type = "left";
				if(Math.abs(deltaX)> Math.abs(deltaY) && deltaX>self.Max.X)
					self.type = "right";
				if(Math.abs(deltaX)< Math.abs(deltaY) && deltaY<-self.Max.Y)
					self.type = "up";
				if(Math.abs(deltaX)< Math.abs(deltaY) && deltaY>self.Max.Y)
					self.type = "down";
                if(self.moveFun)
                	self.moveFun(self.element,deltaX,deltaY);

        },
        _touchEnd: function(event) {
        	
            var self = this;
            if(self._locked)
            {
           		event.preventDefault();
	            event.stopPropagation();
            }
            if(self.start == true)
            {
            	var pageX = getPage(event, 'pageX'),
            
                pageY = getPage(event, 'pageY'),
                deltaX = pageX - self.startPageX,
                deltaY = pageY - self.startPageY;
                
            	if(self.endFun)
            		self.endFun(self.element,deltaX,deltaY,self.type);
            	self.start = false;
            }
        },
        _click: function(event) {
            var self = this;

            event.stopPropagation();
            event.preventDefault();
        },
        destroy: function() {
            var self = this;

            self.element.removeEventListener($E.start, self);
            self.element.removeEventListener($E.move, self);
            self.element.removeEventListener($E.transEnd, self);
            self.element.removeEventListener($E.cancel, self);
            document.removeEventListener($E.end, self);
            document.removeEventListener($E.cancel, self);
        }
	}


