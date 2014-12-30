function zy_slectmenu(id)
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
    }	   				
  }
}


    function zy_slider(e)
    {
    	e.preventDefault();
    	var ao=e.currentTarget;
    	var a = ao.getElementsByTagName("a")[0];
    	var d=e.pageX-ao.offsetLeft;
    	var w=ao.clientWidth;
    	var p=(d)*100/w;
    	if(p>100) p=100;
    	if(p<0) p=0;
    	a.style.left=p+"%";

    }    
    
    function zy_for(e,cb)
    {
    	var ch = e.currentTarget.previousElementSibling;
    	if(ch.nodeName == "INPUT")
    	{
    		if(ch.type=="checkbox")
    			ch.checked=!ch.checked;
    		if(ch.type=="radio" && !ch.checked)
    			ch.checked="checked";
    			
    	}
    	if(cb)
    		cb(e,ch.checked);
    }
    
    function zy_fold(e,col)
    {
     	var a=e.currentTarget.nextElementSibling;
     	if(a.nodeName=="DIV")
     	{
     		if(col)
     			a.className='ui-collapsible-content';
     		else
     			a.className='ui-collapsible-content ui-collapsible-content-collapsed';
     	}
    }
    
    function zy_fix(header,footer,run,cb)
    {
        window.uexOnload = function(type){	
         	switch(type)
      		{
      			case 0:
      			{
    	  			if(header)
    	  			{
    	  				
    	  				var ho = document.getElementById(header);
    	  				ho.style.fontSize=window.getComputedStyle(ho,null).fontSize;
    	  				if(ho){
    	  					uexWindow.openSlibing("1", "2", "head.html", ho.outerHTML, "", ho.offsetHeight);
    	  				}
    	  					
    	  			}
    	  			if(footer) 			
    	  			{
    	  				var fo = document.getElementById(footer);
    	  				fo.style.fontSize=window.getComputedStyle(fo,null).fontSize;
    	  				if(fo){
    	  					uexWindow.openSlibing("2", "2", "head.html", fo.outerHTML, "", fo.offsetHeight);
    	  				}
    	  					
    	  			}
    	  			if(cb)
    	  				cb();
      			}
      			break;
      			case 1:
      				var ao=document.getElementById(header);
      				if(ao)
      				{
      					uexWindow.showSlibing("1");
      				}
      				break;
      			case 2:
      				var bo=document.getElementById(footer);
      				if(bo)
      				{
       					uexWindow.showSlibing("2");
      				}
      				break;
          	}
      	};
      	window.uexOnshow=function(type)
      	{
      		switch(type)
      		{
				case 1:
					var ao=document.getElementById(header);
					if(ao)
					{
						ao.style.display="none";
					}
					break;
				case 2:
					var bo=document.getElementById(footer);
					if(bo)
					{
						bo.style.display="none";
					}
					break;
      		}
      	};
      	if(run)
		{
      		window.uexOnload(0);
		}
    }


