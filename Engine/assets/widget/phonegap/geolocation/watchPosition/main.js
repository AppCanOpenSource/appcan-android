
	// 等待加载PhoneGap
	document.addEventListener("deviceready", onDeviceReady, false); 
	
	var watchID = null;
	
	// PhoneGap加载完毕
	function onDeviceReady() {
		// 每隔3秒钟更新一次
		var options = { frequency: 3000, maximumAge: 3000, timeout: 5000, enableHighAccuracy: true };
		watchID = navigator.geolocation.watchPosition(onSuccess, onError, options);
	}
	
	// 获取位置信息成功时调用的回调函数
	function onSuccess(position) {
		var element = document.getElementById('geolocation');
		element.innerHTML = 'Latitude: '           + position.coords.latitude              + '<br />' +
							'Longitude: '          + position.coords.longitude             + '<br />' +
							'Altitude: '           + position.coords.altitude              + '<br />' +
							'Accuracy: '           + position.coords.accuracy              + '<br />' +
							'Altitude Accuracy: '  + position.coords.altitudeAccuracy      + '<br />' +
							'Heading: '            + position.coords.heading               + '<br />' +
							'Speed: '              + position.coords.speed                 + '<br />' +
							'Timestamp: '          + new Date(position.timestamp)          + '<br />';
	}
	// 清除前述已经开始的监视
	function clearWatch() {
		if (watchID != null) {
			navigator.geolocation.clearWatch(watchID);
			watchID = null;
		}
	}
	
	// onError回调函数接收一个PositionError对象
	function onError(error) {
		alert('code: '    + error.code    + '\n' +
			'message: ' + error.message + '\n');
	}
