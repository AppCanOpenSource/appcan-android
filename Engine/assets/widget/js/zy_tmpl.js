	var zy_tmpl_count=function(dd)
	{
		if(Object.prototype.toString.apply(dd)==="[object Array]")
		{
			return dd.length;
		}
		else
		{	
			var c=0;
			for(var i in dd)
				c++;
			return c;
		}
	}	
	var _f = function(d,c,k1,k2,l){
		var q = c.match(/(first:|last:)(\"|\'*)([^\"\']*)(\"|\'*)/);
		if(!q) return;
		if(q[1]==k1){
			if(q[2]=='\"'||q[2]=='\''){
				return q[3];
			}
			else
				return d[q[3]];
		}
		else if(q[1]==k2 && l>1)
			return "";
	}
	var t_f = function(t,d,i,l,cb){
			return t.replace( /\$\{([^\}]*)\}/g,function(m,c){
			if(c.match(/index:/)){
				return i;
			}
			if(c.match(/cb:/) && cb){
				return cb(d,c.match(/cb:(.*)/));
			}
			if(c.match(/lastcb:/) && cb){
				if(i==(l-1))
					return cb(d,c.match(/cb:(.*)/));
			}
			if(i==0){
				var s=_f(d,c,"first:","last:",l);
				if(s) return s;
			}
			if(i==(l-1)){
				var s= _f(d,c,"last:","first:",l);
				if(s) return s;
			}
			var ar=c.split('.');
			var res=d;
			for(var key in ar)
				res=res[ar[key]];
			return res||"";
		});
	}
	
var zy_tmpl = function(t,dd,l,cb,scb){
	var r = "";
	{
		var index=0;
		for(var i in dd)
		{
			if(scb)
				scb(0,i,dd[i]);
			var rr=t_f(t,dd[i],index,l,cb);
			if(scb)
				scb(1,rr,dd[i]);
			r+=rr;
			index++;
		}
	}
	return r;	
}

var zy_tmpl_s = function(t,dd,cb)
{
	return t_f(t,dd,-1,-1,cb);
}
