	// watch id 是当前“watchAcceleration”的引用
	var watchID = null;
	
	// 等待加载PhoneGap
	document.addEventListener("deviceready", onDeviceReady, false);
	
	// PhoneGap加载完毕
	function onDeviceReady() {
	     //getCurrentAcceleration 这个方法ios不支持 一直返回0
		//navigator.accelerometer.getCurrentAcceleration(onSuccess, onError);
		startWatch();
	}
	
	// 开始监视加速度
	function startWatch() {
	
	// 每隔3秒钟更新一次加速度数据
	var options = { frequency: 3000 };
	
	watchID = navigator.accelerometer.watchAcceleration(onSuccess, onError, options);
	
	}
	
	// 停止监视加速度
	function stopWatch() {
		if (watchID) {
			navigator.accelerometer.clearWatch(watchID);
			watchID = null;
		}
	}
	
	// onSuccess: 获取当前加速度数据的快照
	function onSuccess(acceleration) {
		var element = document.getElementById('accelerometer');
		element.innerHTML = 'Acceleration X: ' + acceleration.x + '<br />' +
							'Acceleration Y: ' + acceleration.y + '<br />' +
							'Acceleration Z: ' + acceleration.z + '<br />' +
							'Timestamp: '      + acceleration.timestamp + '<br />';
	}
	
	// onError: 获取加速度失败
	function onError() {
		alert('onError!');
	}

