// 等待加载PhoneGap
	document.addEventListener("deviceready", onDeviceReady, false);  

	// PhoneGap加载完毕，可以安全调用PhoneGap方法
	function onDeviceReady() {
		var options = { frequency: 3000 }
		navigator.compass.getCurrentHeading(onSuccess, onError,options);
	}  
	function onSuccess(heading){
		alert('Heading: ' + heading.magneticHeading +'\n'+
							'trueHeading:' + heading.trueHeading + '\n' +
							'headingAccuracy:' + heading.headingAccuracy +'\n'+
							'timestamp:' + heading.timestamp);
	};
	function onError(err){
		alert(err.code);
	};