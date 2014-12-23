var ctx = null;
var bird_img = new Array();
var bird_img_w = null;
var birds_arr = new Array();
var bg_img = new Image(); 
var img_w = 91;
var canv_w = window.innerWidth;
var canv_h = window.innerHeight;
var bglen=0;
var flagl=1;
var frames = 0;
var fps = 0;
var startTime = 0;
var canvasReady = false;
function initCanvas(){
	if(window.innerHeight < window.innerWidth){
		//uexBrowserView.evaluateScript("B2AHideCanvas();");
		canvasReady = false;
	}else{
		//uexBrowserView.evaluateScript("B2AShowCanvas();");
		canvasReady = true;
	}
};
Math.roll = function (min, max){ //返回min-max的随机数并取小于等于该数的最大整数
    return Math.floor((max - min + 1) * Math.random() + min);
}

Game ={
	init:function(_ctx,n,interval){
		//initCanvas();
		ctx = _ctx;
		for(var j=1;j<24;j++){
			var _img = new Image();
			_img.src = "gif/"+j+".png";
			bird_img[j-1] = _img;
		}
		bg_img.src = "gif/background.png";
		//bg_img.onload = function(){
			bird_img_w = bg_img.width;
			//if(bg_img.complete) {
				for(var i = 0; i<n;i++){
					p = new Vector(Math.roll(0+img_w/2, canv_w-img_w/2), Math.roll(0+img_w/2, canv_h-img_w/2));
					v = new Vector(canv_w, 0).rotate(Math.roll(0, Math.PI));
					birds_arr.push(new Bird(p,v,img_w,ctx));
				}
				Game.start(interval);
			//}
		//}
				// window.onresize = function() {
				//        initCanvas();
				//   };
				//   window.onorientationchange = function() {
				 //       initCanvas();
				 //   };
				    uexCanvas.addEventListener('touchstart',function(event){
				    	var closeX = event.clientX;
				    	var closeY = event.clientY;
				    	if(closeY<40 && closeX>280){
				    		//uexBrowserView.evaluateScript("B2ACloseCanvas();");
				    	}
				    },false);
				    
				    
				    uexCanvas.addEventListener('touchmove',function(event){
				    	Game.doOnce();
				    },false);
				    
				    
	},
	start:function(interval){
		var nowTime = new Date().getTime();
		for (t in birds_arr) { 
			birds_arr[t].lastMoveTime = nowTime;	
		}
		setInterval(function(){Game.doOnce();}, interval,1);
	},
	doOnce : function(){
		if(true){
		var nowTime = new Date().getTime();
		ctx.clearRect(0, 0,window.innerWidth,window.innerHeight);
		//ctx.drawImage(bg_img,bglen,0,canv_w,canv_h,0,0,window.innerWidth,window.innerHeight);
		ctx.drawImage(bg_img,0,0,window.innerWidth,window.innerHeight);
		if(flagl>0)
			++bglen;
		else
			--bglen;
		if(bglen+canv_w==bird_img_w)
			flagl=-1;
		if(bglen==0)
			flagl=1;
		for (t in birds_arr) {
			birds_arr[t].move(nowTime);	//计算位置
		}
		for (t in birds_arr) {
			birds_arr[t].draw();	//绘制物体
		}
		ctx.font = "bold 24px sans-serif";
		ctx.fillStyle="#f00";
		ctx.fillText("Fps:"+fps,10,20);
		ctx.fillRect(0,0,10,10);
		//console.log("Fps:"+fps);
		
		frames += 1;
		if(nowTime - startTime >= 1000){
			fps = parseInt(frames / ((nowTime - startTime) / 1000));
			startTime = nowTime;
			frames = 0;
		}
		}
		ctx.present();
	}
	
	
}

Bird = function(p,v,w,ctx){
	 this.p = p == null ? new Vector() : p;
     this.v = v == null ? new Vector() : v;
	 this.w = w;
};
Bird.prototype={
	lastMoveTime: null,
	wave : 0 ,
	draw: function(){},
	move: function(nowTime){
		if (this.lastMoveTime == null || nowTime == null ) return false; 
		
		var newP = this.p.clone();
		
		dt = (nowTime - this.lastMoveTime) / 1000;
		newP.add(this.v.clone().scale(dt));
		
		//碰撞检测
		if (newP.x+(img_w/2) > canv_w || newP.x-(img_w/2) < 0){newP.set(this.p);this.v.x = -this.v.x;}
		if (newP.y+(img_w/2) > canv_h || newP.y-(img_w/2) < 0){newP.set(this.p);this.v.y = -this.v.y;}		
		this.p.set(newP);
		this.lastMoveTime = nowTime;
		++this.wave; 
	},
	draw: function(){
		var i = this.wave%bird_img.length;
		ctx.save();
		ctx.drawImage(bird_img[i],this.p.x-this.w/2,this.p.y-this.w/2);
		ctx.restore();
	},
};

Vector = function(x, y){
	this.x = x == null ? 0 : x;
	this.y = y == null ? 0 : y;
	return this;
};
Vector.prototype = {
	clone: function(){ return new Vector(this.x, this.y); },		//复制
	set: function(v){ this.x = v.x; this.y = v.y; return this; },	//赋值
	add: function(v){ this.x += v.x; this.y += v.y; return this; },	//加
	rotate: function(a){
            var ca = Math.cos(a); var sa = Math.sin(a);
            with (this) { var rx = x * ca - y * sa; var ry = x * sa + y * ca; x = rx; y = ry; }
            return this;
        },//旋转
	scale: function(s){ this.x *= s; this.y *= s; return this; },	//数乘向量
};
Game.init(uexCanvas.getContext('2d'),50,30);