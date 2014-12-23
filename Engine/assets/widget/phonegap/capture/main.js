	// 采集操作成功完成后的回调函数
	function captureSuccess(mediaFiles) { 
	
	alert(mediaFiles[0].name+","+mediaFiles[0].fullPath+","+mediaFiles[0].type+","+mediaFiles[0].size+","+mediaFiles[0].lastModifiedDate);
		var media = mediaFiles[0];
		media.getFormatData(mediaSuc,mediaErr);
		//var i, len; 
		//for (i = 0, len = mediaFiles.length; i < len; i += 1) {
		//	uploadFile(mediaFiles[i]); 
		//} 
	} 
	function mediaSuc(mediaData){
	//codecs 音频及视频内容的实际格式
	//bitrate 文件内容的平均比特率。对于图像文件，属性值为0。
	//duration 以秒为单位的视频或音频剪辑时长，图像文件的该属性值为0。（数字类型）
	//height 用像素表示的图像或视频高度，音频剪辑的该属性值为0。（数字类型）
	//width 用像素表示的图像或视频的宽度，音频剪辑的该属性值为0。
		//alert(mediaData.height+","+mediaData.width);
	};
	function mediaErr(err){
		alert("err");
	};
	
	// 采集操作出错后的回调函数
	function captureError(error) { 
		var msg = 'An error occurred during capture: ' + error.code;
		navigator.notification.alert(msg, null, 'Uh oh!'); 
	} 
	
	// “Capture Audio”按钮点击事件触发函数
	function captureAudio() { 
	
		
		var options = {duration: 10 };
		navigator.device.capture.captureAudio(captureSuccess, captureError, options);
	} 
	 function captureImage(){
		navigator.device.capture.captureImage(captureSuccess, captureError);
	 };
	 function captureVideo (){
		navigator.device.capture.captureVideo(captureSuccess, captureError);
	 };
	// 上传文件到服务器 
	function uploadFile(mediaFile) {
		var ft = new FileTransfer(), 
		path = mediaFile.fullPath, 
		name = mediaFile.name; 
		ft.upload(path,
				"http://my.domain.com/upload.php",
				function(result) { 
					console.log('Upload success: ' + result.responseCode); 
					console.log(result.bytesSent + ' bytes sent'); 
				}, 
				function(error) { 
					console.log('Error uploading file ' + path + ': ' + error.code); 
				}, 
				{ fileName: name });  
	} 