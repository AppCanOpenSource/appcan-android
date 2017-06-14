appcan.button("#beginAnimition","btn-act", function(){
    uexWindow.beginAnimition();
});
appcan.button("#setAnimitionDelay","btn-act", function(){
    uexWindow.setAnimitionDelay(1000);
});
appcan.button("#setAnimitionDuration","btn-act", function(){
    uexWindow.setAnimitionDuration(3000);
});
appcan.button("#setAnimitionCurve","btn-act", function(){
    uexWindow.setAnimitionCurve(1);
});
appcan.button("#setAnimitionRepeatCount","btn-act", function(){
    uexWindow.setAnimitionRepeatCount(1);
});
appcan.button("#setAnimitionAutoReverse","btn-act", function(){
    uexWindow.setAnimitionAutoReverse(1);
});
appcan.button("#makeTranslation","btn-act", function(){
    uexWindow.makeTranslation(100,0,0);
});
appcan.button("#makeScale","btn-act", function(){
    uexWindow.makeScale(1.5,1,1);
});
appcan.button("#makeRotate","btn-act", function(){
    uexWindow.makeRotate(45,true,false,false);
});
appcan.button("#makeAlpha","btn-act", function(){
    uexWindow.makeAlpha(0.5);
});
appcan.button("#commitAnimition","btn-act", function(){
    uexWindow.commitAnimition();
});