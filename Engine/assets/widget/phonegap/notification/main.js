	// 等待加载PhoneGap
	document.addEventListener("deviceready", onDeviceReady, false); 
	
	// PhoneGap加载完毕
	function onDeviceReady() {
		// 空
	}
	
	// 警告对话框被忽视
	function alertDismissed() {
		// 进行处理
	}
	
	// 显示一个定制的警告框
	function showAlert() {
		navigator.notification.alert(
			'You are the winner!',  // 显示信息
			alertDismissed,         // 警告被忽视的回调函数
			'Game Over',            // 标题
			'Done'                  // 按钮名称
		);
	}
	
// 处理确认对话框返回的结果
	function onConfirm(button) {
		alert('You selected button ' + button);
	}
	
	// 显示一个定制的确认对话框
	function showConfirm() {
		navigator.notification.confirm(
			'You are the winner!',  // 显示信息
			onConfirm,              // 按下按钮后触发的回调函数，返回按下按钮的索引	
			'Game Over',            // 标题
			'Restart,Exit'          // 按钮标签
		);
	}
	// 蜂鸣三次
function playBeep() {
   	navigator.notification.beep(2);
}
// 震动两秒
function vibrate() {

   	navigator.notification.vibrate(2000);
}
