package org.zywx.wbpalmstar.engine.universalex.wrapper;

import org.zywx.wbpalmstar.base.vo.WindowActionSheetVO;
import org.zywx.wbpalmstar.base.vo.WindowAlertVO;
import org.zywx.wbpalmstar.base.vo.WindowAnimVO;
import org.zywx.wbpalmstar.base.vo.WindowConfirmVO;
import org.zywx.wbpalmstar.base.vo.WindowCreateProgressDialogVO;
import org.zywx.wbpalmstar.base.vo.WindowEvaluateMultiPopoverScriptVO;
import org.zywx.wbpalmstar.base.vo.WindowEvaluatePopoverScriptVO;
import org.zywx.wbpalmstar.base.vo.WindowEvaluateScriptVO;
import org.zywx.wbpalmstar.base.vo.WindowOpenMultiPopoverVO;
import org.zywx.wbpalmstar.base.vo.WindowOpenPopoverVO;
import org.zywx.wbpalmstar.base.vo.WindowOpenSlibingVO;
import org.zywx.wbpalmstar.base.vo.WindowOpenVO;
import org.zywx.wbpalmstar.base.vo.WindowPromptVO;
import org.zywx.wbpalmstar.base.vo.WindowSetFrameVO;
import org.zywx.wbpalmstar.base.vo.WindowSetMultiPopoverFrameVO;
import org.zywx.wbpalmstar.base.vo.WindowSetMultiPopoverSelectedVO;
import org.zywx.wbpalmstar.base.vo.WindowSetPopoverFrameVO;
import org.zywx.wbpalmstar.base.vo.WindowShowBounceViewVO;
import org.zywx.wbpalmstar.base.vo.WindowToastVO;
import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.engine.universalex.EUExWindow;

/**
 * Created by ylt on 16/8/3.
 *
 * 兼容Window 相关接口 Json传参
 */

public class WindowJsonWrapper {

    public static void open(EUExWindow window, WindowOpenVO openVO) {
        window.openMsg(new String[]{
                openVO.name,
                String.valueOf(openVO.dataType),
                openVO.data,
                String.valueOf(openVO.animID),
                String.valueOf(openVO.w),
                String.valueOf(openVO.h),
                String.valueOf(openVO.flag),
                String.valueOf(openVO.animDuration),
                openVO.extras == null ? null : DataHelper.gson.toJson(openVO.extras)
        });
    }

    public static void close(EUExWindow window, WindowAnimVO closeVO) {
        window.closeMsg(new String[]{
                String.valueOf(closeVO.animID),
                String.valueOf(closeVO.animDuration)
        });
    }

    public static void windowForward(EUExWindow window,WindowAnimVO animVO){
        window.windowForwardMsg(new String[]{
                String.valueOf(animVO.animID),
                String.valueOf(animVO.animDuration)
        });
    }

    public static void windowBack(EUExWindow window,WindowAnimVO animVO){
        window.windowBackMsg(new String[]{
                String.valueOf(animVO.animID),
                String.valueOf(animVO.animDuration)
        });
    }

    public static void setWindowFrame(EUExWindow window, WindowSetFrameVO frameVO){
        window.setWindowFrameMsg(new String[]{
                String.valueOf(frameVO.x),
                String.valueOf(frameVO.y),
                String.valueOf(frameVO.animDuration)
        });
    }

    public static void openSlibing(EUExWindow window, WindowOpenSlibingVO slibingVO){
        window.openSlibingMsg(new String[]{
                String.valueOf(slibingVO.type),
                String.valueOf(slibingVO.dataType),
                slibingVO.url,
                slibingVO.data,
                String.valueOf(slibingVO.w),
                String.valueOf(slibingVO.h)
        });
    }

    public static void evaluateScript(EUExWindow window, WindowEvaluateScriptVO scriptVO){
        window.evaluateScriptMsg(new String[]{
                scriptVO.name,
                String.valueOf(scriptVO.type),
                scriptVO.js
        });
    }

    public static void evaluatePopoverScript(EUExWindow window, WindowEvaluatePopoverScriptVO scriptVO){
        window.evaluatePopoverScript(new String[]{
                scriptVO.windowName,
                scriptVO.popName,
                scriptVO.js
        });
    }

    public static void evaluateMultiPopoverScript(EUExWindow window,
                                                  WindowEvaluateMultiPopoverScriptVO scriptVO){
        window.evaluateMultiPopoverScript(new String[]{
                scriptVO.windowName,
                scriptVO.popName,
                scriptVO.pageName,
                scriptVO.js
        });
    }

    public static void openPopover(EUExWindow window,
                                   WindowOpenPopoverVO popoverVO){
        window.openPopoverMsg(new String[]{
                popoverVO.name,
                String.valueOf(popoverVO.dataType),
                popoverVO.url,
                popoverVO.data,
                String.valueOf(popoverVO.x),
                String.valueOf(popoverVO.y),
                String.valueOf(popoverVO.w),
                String.valueOf(popoverVO.h),
                String.valueOf(popoverVO.fontSize),
                String.valueOf(popoverVO.flag),
                String.valueOf(popoverVO.bottomMargin),
                popoverVO.extras==null?null:DataHelper.gson.toJson(popoverVO.extras)
        });
    }

    public static void setPopoverFrame(EUExWindow window, WindowSetPopoverFrameVO frameVO){
        window.setPopoverFrameMsg(new String[]{
                frameVO.name,
                String.valueOf(frameVO.x),
                String.valueOf(frameVO.y),
                String.valueOf(frameVO.w),
                String.valueOf(frameVO.h)
        });
    }

    public static void openMultiPopover(EUExWindow window, WindowOpenMultiPopoverVO multiPopoverVO){
        window.openMultiPopoverMsg(new String[]{
                DataHelper.gson.toJson(multiPopoverVO.content),
                multiPopoverVO.name,
                String.valueOf(multiPopoverVO.dataType),
                String.valueOf(multiPopoverVO.x),
                String.valueOf(multiPopoverVO.y),
                String.valueOf(multiPopoverVO.w),
                String.valueOf(multiPopoverVO.h),
                String.valueOf(multiPopoverVO.fontSize),
                String.valueOf(multiPopoverVO.flag),
                String.valueOf(multiPopoverVO.indexSelected),
                multiPopoverVO.extras==null?null:DataHelper.gson.toJson(multiPopoverVO.extras)
        });
    }

    public static void setSelectedPopOverInMultiWindow(EUExWindow window,
                                                       WindowSetMultiPopoverSelectedVO selectedVO){
        window.setSelectedPopOverInMultiWindowMsg(new String[]{
                selectedVO.name,
                String.valueOf(selectedVO.index)
        });
    }

    public static void setMultiPopoverFrame(EUExWindow window, WindowSetMultiPopoverFrameVO frameVO){
        window.setMultiPopoverFrameMsg(new String[]{
                frameVO.name,
                String.valueOf(frameVO.x),
                String.valueOf(frameVO.y),
                String.valueOf(frameVO.w),
                String.valueOf(frameVO.h)
        });
    }

    public static void alert(EUExWindow window, WindowAlertVO alertVO){
        window.alert(new String[]{
                alertVO.title,
                alertVO.message,
                alertVO.buttonLabel
        });
    }

    public static void confirm(EUExWindow window, WindowConfirmVO confirmVO,String callbackId){
        window.confirm(new String[]{
                confirmVO.title,
                confirmVO.message,
                confirmVO.buttonLabels,
                callbackId
        });
    }

    public static void prompt(EUExWindow window, WindowPromptVO promptVO,String callbackId){
        window.prompt(new String[]{
                promptVO.title,
                promptVO.message,
                promptVO.defaultValue,
                promptVO.buttonLabels,
                promptVO.hint,
                callbackId
        });
    }

    public static void toast(EUExWindow window, WindowToastVO toastVO){
        window.toastMsg(new String[] {
                String.valueOf(toastVO.type),
                toastVO.location,
                toastVO.msg,
                String.valueOf(toastVO.duration)
        });
    }

    public static void createProgressDialog(EUExWindow window,
                                            WindowCreateProgressDialogVO dialogVO){
        window.createProgressDialogMsg(new String[]{
                dialogVO.title,
                dialogVO.msg,
                dialogVO.canCancel
        });
    }

    public static void actionSheet(EUExWindow window, WindowActionSheetVO actionSheetVO,String callbackId){
        window.actionSheetMsg(new String[]{
                actionSheetVO.title,
                actionSheetVO.cancel,
                actionSheetVO.buttons,
                callbackId
        });
    }

    public static void showBounceView(EUExWindow window, WindowShowBounceViewVO bounceViewVO){
        window.showBounceView(new String[]{
                String.valueOf(bounceViewVO.type),
                bounceViewVO.color,
                String.valueOf(bounceViewVO.flag)
        });
    }



}
