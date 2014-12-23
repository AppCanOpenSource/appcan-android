// 等待加载PhoneGap
	document.addEventListener("deviceready", onDeviceReady, false);
	
	// PhoneGap加载完毕
	function onDeviceReady() {
		var option = {frequency:3000, maximumAge: 3000, timeout: 5000, enableHighAccuracy: true };
		navigator.geolocation.getCurrentPosition(onSuccess, onError,option);
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
	
	// onError回调函数接收一个PositionError对象
	function onError(error) {
		alert('code: '    + error.code    + '\n' +
			'message: ' + error.message + '\n');
	}