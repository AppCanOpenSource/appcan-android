/*function myTouchStart(touchEvent) {
	console.log("你好!");
	console.log("touchstart", touchEvent.touches[0].pageX, touchEvent.touches[0].pageY);
}
function myTouchMove(touchEvent) {
	console.log("touchmove", touchEvent.changedTouches[0].clientX, touchEvent.changedTouches[0].clientY);
}
function myTouchEnd(touchEvent) {
	console.log("touchend", touchEvent.targetTouches[0].screenX, touchEvent.targetTouches[0].screenY);
}

uexCanvas.addEventListener("touchstart", myTouchStart, false);
uexCanvas.addEventListener("touchend", myTouchEnd, false);
uexCanvas.addEventListener("touchmove", myTouchMove, false);*/
//uexCanvas.removeEventListener("touchstart", myTouchStart, false);

var ctx = uexCanvas.getContext('2d');
//ctx.fillStyle = "#FFA500";
//ctx.fillStyle = "orange"; 
//ctx.fillStyle = "rgb(255,165,0)"; 
//ctx.fillStyle = "rgba(255,165,0,1)";
//ctx.fillRect(0, 0, 300, 200);
//var img = new Image();
//img.src = "icon.png";
//ctx.drawImage(img, 0, 0);
//var xPosition = 100;
//var yPosition = 100;
//ctx.fillStyle = "#FFA500";
//console.log(ctx.fillStyle);
//ctx.fillText("your text", xPosition, yPosition, 100);
//ctx.fillRect(0, 0, 200, 200);
ctx.present();


 