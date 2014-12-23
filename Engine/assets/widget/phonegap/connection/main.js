// 等待加载PhoneGap
	document.addEventListener("deviceready", onDeviceReady, false);  

	// PhoneGap加载完毕，可以安全调用PhoneGap方法
	function onDeviceReady() { 
		checkConnection(); 
	}  

	function checkConnection() { 
		var networkState = navigator.network.connection.type; 		 

		var states = {}; 
		states[Connection.UNKNOWN]  = 'Unknown connection'; 
		states[Connection.ETHERNET] = 'Ethernet connection'; 
		states[Connection.WIFI]     = 'WiFi connection'; 
		states[Connection.CELL_2G]  = 'Cell 2G connection'; 
		states[Connection.CELL_3G]  = 'Cell 3G connection'; 
		states[Connection.CELL_4G]  = 'Cell 4G connection'; 
		states[Connection.NONE]     = 'No network connection'; 

		alert('Connection type: ' + states[networkState]); 
}