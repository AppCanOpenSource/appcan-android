// 等待加载PhoneGap
document.addEventListener("deviceready", onDeviceReady, false); 

// PhoneGap加载完毕
function onDeviceReady() {
	//playAudio("bg.mp3");
}

// 音频播放器
var my_media = null;
var mediaTimer = null;
var allLenght = null;
// 播放音频
function playAudio(src) {
	// 从目标文件创建Media对象
	my_media = new Media(src, onSuccess, onError);

	// 播放音频
	my_media.play();
     allLenght = my_media.getDuration();
	// 每秒更新一次媒体播放到的位置
	if (mediaTimer == null) {
		mediaTimer = setInterval(function() {
			// 获取媒体播放到的位置
			my_media.getCurrentPosition(		
		
				//获取成功后调用的回调函数
				function(position) {
					if (position > -1) {
						setAudioPosition((position) + " sec "+allLenght);
					}
				},
				// 发生错误后调用的回调函数
				function(e) {
					console.log("Error getting pos=" + e);
					setAudioPosition("Error: " + e);
				}
			);
		}, 1000);
	}
	//5秒之后设置到10秒的位置
		//setTimeout(function() {
		//	my_media.seekTo(10000);
		//}, 5000);
}

// 暂停音频播放
function pauseAudio() {
	if (my_media) {
		my_media.pause();
	}
}

// 停止音频播放
function stopAudio() {
	if (my_media) {
		my_media.stop();
	}
	clearInterval(mediaTimer);
	mediaTimer = null;
}

// 创建Media对象成功后调用的回调函数
function onSuccess() {
	console.log("playAudio():Audio Success");
}

// 创建Media对象出错后调用的回调函数
function onError(error) {
	alert('code: '    + error.code    + '\n' + 
		'message: ' + error.message + '\n');
}

// 设置音频播放位置
function setAudioPosition(position) {
	document.getElementById('audio_position').innerHTML = position;
}

//从sd卡上选择文件
function selectMusic(){
	navigator.camera.getPicture(onMusicSuccess, onFail, { quality: 50,destinationType: destinationType.FILE_URI,sourceType: source });
};
function onMusicSuccess(imageURI){
	my_media = new Media(imageURI, onSuccess, onError);

	// 播放音频
	my_media.play();
     allLenght = my_media.getDuration();
	// 每秒更新一次媒体播放到的位置
	if (mediaTimer == null) {
		mediaTimer = setInterval(function() {
			// 获取媒体播放到的位置
			my_media.getCurrentPosition(		
		
				//获取成功后调用的回调函数
				function(position) {
					if (position > -1) {
						setAudioPosition((position) + " sec "+allLenght);
					}
				},
				// 发生错误后调用的回调函数
				function(e) {
					console.log("Error getting pos=" + e);
					setAudioPosition("Error: " + e);
				}
			);
		}, 1000);
	}
	//5秒之后设置到10秒的位置
		//setTimeout(function() {
		//	my_media.seekTo(10000);
		//}, 5000);
};
function onFail(mesage) {
	alert('Failed because: ' + message);
}