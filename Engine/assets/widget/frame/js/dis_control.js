function dis_slectmenu(id,cb)
{
  var sl = document.getElementById(id);
  if(sl)
  {
  	var sp = sl.parentElement; //<span>
  	if(sp)
  	{
      var ch = sp.getElementsByTagName("span")[0];
      var ch = ch.getElementsByTagName("span")[0];
      var t = sl.options[sl.selectedIndex].text;
      if(ch)	
      {
        ch.innerHTML=t;
      }	
      
      if(cb)
	  {
    	  cb(sl.selectedIndex);
	  }
  		
    }	   				
  }
}
var params = {};
function parseParam(){
	var loc = String(document.location);
	var pieces = loc.substr(loc.indexOf('?') + 1).split('&');
	params.keys=[];
	for (var i = 0; i < pieces.length; i+=1){
	    var keyVal = pieces[i].split('=');
	    params[keyVal[0]] = decodeURIComponent(keyVal[1]);
	    params.keys.push(keyVal[0]);
	}
}

function $$(id){
	if(id!=null){
		return document.getElementById(id);
	}
	return null;
}
/*
*todo	  set htmlele for element by id
*example  setHtml("id", "<a>value</a>");
*/
function setHtml(id, html){
	if ("string" == typeof(id)) {
		var ele=$$(id);
		if(ele!=null){
			ele.innerHTML=html==null?"":html;
		}
	}else if( id != null){
		id.innerHTML=html==null?"":html;
	}
}
/**
 * 判断参数是否定义
 *
 * @author LLX	
 * @param  para
 * return  boolean
 */

function isDefine(para)
{
	if(typeof para == 'undefined' || para =="" || para == null || para == 'undefined')
	{
		return false;
	}else
	{
		return true;
	}
}

//判断变量是否定义
function isUndefined(variable) {
	return typeof variable == 'undefined' ? true : false;
}

function getValue(id){
	return getAttrById(id, "value");
}
function setValue(id, value){
	setAttrById(id, "value", value)
}

function getAttrById(id, name){
	if("string" == typeof(id)){
		var ele=$$(id);
		if(ele != null){
			if(name != null){
				var tmp=ele[name];
				if(tmp != null){
					return tmp;
				}
			}
		}
	}else{
		if(id != null){
			return id[name];
		}
	}
	return null;
}
function setAttrById(id, name, value){
	if("string" == typeof(id)){
		var ele=$$(id);
		if(ele != null){
			if(name != null){
				ele[name]=value;
			}
		}
	}else{
		if(id != null && name != null){
			id[name]=value;
		}
	}
}