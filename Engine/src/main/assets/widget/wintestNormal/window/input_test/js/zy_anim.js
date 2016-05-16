var curAnim=null;

function zy_anim_over()
{
	curAnim.ao.style.display = "none";
	curAnim.ao.style.zIndex="0";
	curAnim.ao.style['opacity']="";
	curAnim.ao.style['opacity']="";
	curAnim.ao.style['webkitTransform']="";
	curAnim.ao.style['webkitTransform']="";
	clearInterval(curAnim.int);
	curAnim.bo.style.position = "relative";
	curAnim.ao.style.position = "relative";
	if(curAnim.cb)
		curAnim.cb(curAnim);
	curAnim=null;
	if(window.chrome)
    {
	  document.body.style['overflowX']="hidden";
	  document.body.style['overflowY']="auto";
    }
	

}

function zy_anim_slide_h() {
    var left = 0;
    left = parseInt(curAnim.ao.style.left, 10);
    left = left - (window.innerWidth / curAnim.s) * curAnim.d;
    curAnim.bo.style.display = "block";

    
    var pos = left + curAnim.ao.clientWidth * curAnim.d;
    if (pos * curAnim.d < 0) pos = 0;
    curAnim.ao.style.left = left + "px";
    curAnim.bo.style.left = pos + "px";
    if (pos * curAnim.d > 0) {
        //setTimeout("zy_anim_slide_h()", 1);
    } else {
		zy_anim_over();
    }
}

function zy_anim_slide_v() {
    var top = 0;
    top = parseInt(curAnim.bo.style.top, 10);
    top = top - (window.innerWidth / curAnim.s) * curAnim.d;
    curAnim.bo.style.display = "block";

	if(top * curAnim.d<0) top=0;
    curAnim.bo.style.top = top + "px";
    if (top * curAnim.d > 0) {
        //setTimeout("zy_anim_slide_v()", 1);
    } else {
		zy_anim_over();
    }
}

function zy_anim_fade_() {
	var opa=parseFloat(curAnim.bo.style.opacity);
    curAnim.bo.style.opacity = opa+0.2*(5.0/curAnim.s);
    if (curAnim.bo.style.opacity < 1.0) {
        //setTimeout("zy_anim_fade_()", 1);
    } else {
        zy_anim_over();
    }
}

function zy_anim_pop_() {
	curAnim.sc=curAnim.sc+0.2*(5.0/curAnim.s)*curAnim.d;
	if(curAnim.d>0)
    	curAnim.bo.style['webkitTransform']= "scale("+curAnim.sc+")";
    else
    	curAnim.ao.style['webkitTransform']= "scale("+curAnim.sc+")";
	
    if (curAnim.sc < 1.0 && curAnim.sc>0) {
        //setTimeout("zy_anim_pop_()", 1);
    } else {
        zy_anim_over();
    }
}

function zy_anim(ao, bo, d,type,cb) {
    this.ao = ao;
    this.bo = bo;
    this.d = d;
    this.cb=cb;
    if(window.chrome)
    {
	  document.body.style['overflowX']="hidden";
	  document.body.style['overflowY']="hidden";
    }
    if (!window.chrome) this.s = 5;
    else
    this.s = 20;
    switch(type)
    {
    	case 0:
	    ao.style.left = ao.clientLeft + "px";
	    bo.style.left = ao.clientLeft + ao.clientWidth * d + "px";
	    break;
	    case 1:
		bo.style.left="0px";
	    bo.style.top = window.innerHeight*d + "px";	    
		ao.style.zIndex="-1";
		bo.style.zIndex="0";
	    break;
		case 2:
		bo.style.left="0px";
	    bo.style.top = "0px";	    
		ao.style.left="0px";
	    ao.style.top = "0px";
		ao.style.zIndex="-1";
		bo.style.zIndex="0";
		ao.style.opacity=1.0;	    
		bo.style.opacity=0.0;	    
		break;
		case 3:
		bo.style.left="0px";
	    bo.style.top = "0px";	    
		ao.style.left="0px";
	    ao.style.top = "0px";
		ao.style.zIndex=-1*d;
		bo.style.zIndex="0";
		ao.style['webkitTransform']="scale(1)";
		if(d>0)
		{
			bo.style['webkitTransform']="scale(0)";
			this.sc=0;
		}
		else
		{
			bo.style['webkitTransform']="scale(1)";	
			this.sc=1;
		}				
		break;
    }
    bo.style.display = "block";
    bo.style.position = "absolute";
    ao.style.position = "absolute";
}

function zy_anim_slide(a, b, d,cb) {
	if(curAnim) 
		return;
    if (!d) d = 1;
    curAnim = new zy_anim(document.getElementById(a), document.getElementById(b), d,0,cb);
    curAnim.int=setInterval("zy_anim_slide_h()", 1);
}
function zy_anim_slide_ver(a, b, d,cb) {
	if(curAnim) 
		return;
    if (!d) d = 1;
    curAnim = new zy_anim(document.getElementById(a), document.getElementById(b), d,1,cb);
    curAnim.int=setInterval("zy_anim_slide_v()", 1);
}
function zy_anim_fade(a, b, d,cb) {
	if(curAnim)
	 return;
    if (!d) d = 1;
    curAnim = new zy_anim(document.getElementById(a), document.getElementById(b), d,2,cb);
    curAnim.int=setInterval("zy_anim_fade_()", 1);
}
function zy_anim_pop(a, b, d,cb) {
	if(curAnim) 
	  return;
    if (!d) d = 1;
    curAnim = new zy_anim(document.getElementById(a), document.getElementById(b), d,3,cb);
    curAnim.int=setInterval("zy_anim_pop_()", 1);
}

