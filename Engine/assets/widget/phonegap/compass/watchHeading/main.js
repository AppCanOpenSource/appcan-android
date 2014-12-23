// watchID 是当前“watchHeading”的引用
	var watchID = null;
	
	// 等待加载PhoneGap
	document.addEventListener("deviceready", onDeviceReady, false);
	
	// PhoneGap加载完毕
	function onDeviceReady() {
		//startWatch();
	}
	
	// 开始监视罗盘
	function startWatch() {
	
		// 每隔3秒钟更新一次罗盘的朝向信息
		var options = { frequency: 3000 };
		watchID = navigator.compass.watchHeading(onSuccess, onError, options);
	}
	
	// 停止监视罗盘
	function stopWatch() {
		if (watchID) {
			navigator.compass.clearWatch(watchID);
			watchID = null;
		}
	}
	
	// onSuccess: 返回罗盘的当前朝向
	function onSuccess(heading) {
		var element = document.getElementById('heading');
			element.innerHTML = 'Heading: ' + heading.magneticHeading +'<br>'+
							'trueHeading:' + heading.trueHeading + '<br>' +
							'headingAccuracy:' + heading.headingAccuracy +'<br>'+
							'timestamp:' + heading.timestamp;
	}
	
	// onError: 获取罗盘朝向失败
	function onError(compassError) {
		alert('Compass error: ' + compassError.code);
	}