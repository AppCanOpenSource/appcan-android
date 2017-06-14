appcan.button("#alert","btn-act", function(){
    uexWindow.alert({
        title:"提示",
        message:"alert框测试",
        buttonLabel:"OK"
    });
});
appcan.button("#confirm","btn-act", function(){
    uexWindow.confirm({
        title:"警告",
        message:"确定退出么?",
        buttonLabels:"OK,Cancel"
    },function(index){
        $("#confirm_result_display").html("你选择了第" + (index + 1) + "个按钮！");
    });
});
appcan.button("#prompt","btn-act", function(){
    uexWindow.prompt({
        title:"提示",
        message:"请输入内容:",
        defaultValue:"",
        buttonLabels:"OK,Cancel"
    },function(index,data){
        $("#prompt_result_display").html("你选择了第" + (index + 1) + "个按钮！输入框内容为:" + data);
    });
});
appcan.button("#actionSheet","btn-act", function(){
    uexWindow.actionSheet({
        title:"菜单",
        cancel:"Cancel",
        buttons:"Opt1,Opt2,Opt3,Opt4,Opt5,Opt6"
    },function(index){
        $("#actionSheet_result_display").html("你选择了第"+(index+1)+"个按钮");
    });
});
appcan.button("#toast","btn-act", function(){
    uexWindow.toast({
        type:1,
        location:5,
        msg:"toast测试...",
        duration:0
    });
});
appcan.button("#closeToast","btn-act", function(){
    uexWindow.closeToast();
});
appcan.button("#createProgressDialog","btn-act", function(){
    uexWindow.createProgressDialog({
        title:'提示',
        msg:'ProgressDialog测试，可取消的，点击除对话框外的地方...',
        canCancel:0
    });
});
appcan.button("#destroyProgressDialog","btn-act", function(){
    uexWindow.createProgressDialog({
        title:'提示',
        msg:'ProgressDialog测试，不可取消的，2秒钟后自动关闭...',
        canCancel:1
    });
    setTimeout(function(){
        uexWindow.destroyProgressDialog();
    }, 2000);
});