
	var pictureSource;		//图片来源
	var destinationType;		//设置返回值的格式
	
	// 等待PhoneGap连接设备
	document.addEventListener("deviceready",onDeviceReady,false);
	
	// PhoneGap准备就绪，可以使用！
	function onDeviceReady() {
		pictureSource=navigator.camera.PictureSourceType;
		destinationType=navigator.camera.DestinationType;
	}
	
	// 当成功获得一张照片的Base64编码数据后被调用
	function onPhotoDataSuccess(imageData) {
	
		// 取消注释以查看Base64编码的图像数据
		// console.log(imageData);
		// 获取图像句柄
		var smallImage = document.getElementById('smallImage');
			 
		// 取消隐藏的图像元素
		smallImage.style.display = 'block';
		
		// 显示拍摄的照片
		// 使用内嵌CSS规则来缩放图片
		//smallImage.src = "data:image/jpeg;base64," + imageData;
		smallImage.src = imageData;
	}
	   
   // 当成功得到一张照片的URI后被调用
   function onPhotoURISuccess(imageURI) {
	
		// 取消注释以查看图片文件的URI
		// console.log(imageURI);
		// 获取图片句柄
		var largeImage = document.getElementById('largeImage');
		 
		// 取消隐藏的图像元素
		largeImage.style.display = 'block';
	
		// 显示拍摄的照片
		// 使用内嵌CSS规则来缩放图片
		largeImage.src = imageURI;
	}
	   
   // “Capture Photo”按钮点击事件触发函数
   function capturePhoto() {

		// 使用设备上的摄像头拍照，并获得Base64编码字符串格式的图像
		//navigator.camera.getPicture(onPhotoDataSuccess, onFail, { quality: 20,destinationType: destinationType.DATA_URL});
		navigator.camera.getPicture(onPhotoDataSuccess, onFail, { quality: 20,destinationType: destinationType.FILE_URI});
   }
   
   // “Capture Editable Photo”按钮点击事件触发函数
   function capturePhotoEdit() {

		// 使用设备上的摄像头拍照，并获得Base64编码字符串格式的可编辑图像
		navigator.camera.getPicture(onPhotoDataSuccess, onFail, { quality: 20,destinationType: destinationType.FILE_URI,allowEdit: true });
   }
	   
   //“From Photo Library”/“From Photo Album”按钮点击事件触发函数
   function getPhoto(source) {
   
   		// 从设定的来源处获取图像文件URI
		navigator.camera.getPicture(onPhotoURISuccess, onFail, { quality: 50,
		destinationType: destinationType.FILE_URI,sourceType: source });
		//更多参数 allowEdit,EncodingType,targetWidth,targetHeight,MediaType
   }

   // 当有错误发生时触发此函数
   function onFail(mesage) {
		alert('Failed because: ' + message);
   }
	