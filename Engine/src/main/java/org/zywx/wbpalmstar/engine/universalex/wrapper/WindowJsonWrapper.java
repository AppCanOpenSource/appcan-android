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
 */

public class WindowJsonWrapper {

    public static void open(EUExWindow window, WindowOpenVO openVO) {
        window.openMsg(new String[]{
                openVO.name,
                openVO.dataType,
                openVO.data,
                openVO.animID,
                openVO.w,
                openVO.h,
                openVO.flag,
                openVO.animDuration,
                openVO.extras == null ? null : DataHelper.gson.toJson(openVO.extras)
        });
    }

    public static void close(EUExWindow window, WindowAnimVO closeVO) {
        window.closeMsg(new String[]{
                closeVO.animID,
                closeVO.animDuration
        });
    }

    public static void windowForward(EUExWindow window,WindowAnimVO animVO){
        window.windowForwardMsg(new String[]{
                animVO.animID,
                animVO.animDuration
        });
    }

    public static void windowBack(EUExWindow window,WindowAnimVO animVO){
        window.windowBackMsg(new String[]{
                animVO.animID,
                animVO.animDuration
        });
    }

    public static void setWindowFrame(EUExWindow window, WindowSetFrameVO frameVO){
        window.setWindowFrameMsg(new String[]{
                frameVO.x,
                frameVO.y,
                frameVO.animDuration
        });
    }

    public static void openSlibing(EUExWindow window, WindowOpenSlibingVO slibingVO){
        window.openSlibingMsg(new String[]{
                slibingVO.type,
                slibingVO.dataType,
                slibingVO.url,
                slibingVO.data,
                slibingVO.w,
                slibingVO.h
        });
    }

    public static void evaluateScript(EUExWindow window, WindowEvaluateScriptVO scriptVO){
        window.evaluateScriptMsg(new String[]{
                scriptVO.name,
                scriptVO.type,
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
                popoverVO.dataType,
                popoverVO.url,
                popoverVO.data,
                popoverVO.x,
                popoverVO.y,
                popoverVO.w,
                popoverVO.h,
                popoverVO.fontSize,
                popoverVO.flag,
                popoverVO.bottomMargin,
                popoverVO.extraInfo==null?null:DataHelper.gson.toJson(popoverVO.extraInfo)
        });
    }

    public static void setPopoverFrame(EUExWindow window, WindowSetPopoverFrameVO frameVO){
        window.setPopoverFrameMsg(new String[]{
                frameVO.name,
                frameVO.x,
                frameVO.y,
                frameVO.w,
                frameVO.h
        });
    }

    public static void openMultiPopover(EUExWindow window, WindowOpenMultiPopoverVO multiPopoverVO){
        window.openMultiPopoverMsg(new String[]{
                DataHelper.gson.toJson(multiPopoverVO.content),
                multiPopoverVO.name,
                multiPopoverVO.dataType,
                multiPopoverVO.x,
                multiPopoverVO.y,
                multiPopoverVO.w,
                multiPopoverVO.h,
                multiPopoverVO.fontSize,
                multiPopoverVO.flag,
                multiPopoverVO.indexSelected,
                multiPopoverVO.extraInfo==null?null:DataHelper.gson.toJson(multiPopoverVO.extraInfo)
        });
    }

    public static void setSelectedPopOverInMultiWindow(EUExWindow window,
                                                       WindowSetMultiPopoverSelectedVO selectedVO){
        window.setSelectedPopOverInMultiWindowMsg(new String[]{
                selectedVO.name,
                selectedVO.index
        });
    }

    public static void setMultiPopoverFrame(EUExWindow window, WindowSetMultiPopoverFrameVO frameVO){
        window.setMultiPopoverFrameMsg(new String[]{
                frameVO.name,
                frameVO.x,
                frameVO.y,
                frameVO.w,
                frameVO.h
        });
    }

    public static void alert(EUExWindow window, WindowAlertVO alertVO){
        window.setSlidingWindow(new String[]{
                alertVO.title,
                alertVO.message,
                alertVO.buttonLabel
        });
    }

    public static void confirm(EUExWindow window, WindowConfirmVO confirmVO){
        window.confirm(new String[]{
                confirmVO.title,
                confirmVO.message,
                confirmVO.buttonLabel
        });
    }

    public static void prompt(EUExWindow window, WindowPromptVO promptVO){
        window.prompt(new String[]{
                promptVO.title,
                promptVO.message,
                promptVO.defaultValue,
                promptVO.buttonLabel,
                promptVO.hint
        });
    }

    public static void toast(EUExWindow window, WindowToastVO toastVO){
        window.toastMsg(new String[] {
                toastVO.type,
                toastVO.location,
                toastVO.msg,
                toastVO.duration
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

    public static void actionSheet(EUExWindow window, WindowActionSheetVO actionSheetVO){
        window.actionSheetMsg(new String[]{
                actionSheetVO.title,
                actionSheetVO.cancel,
                actionSheetVO.buttons
        });
    }

    public static void showBounceView(EUExWindow window, WindowShowBounceViewVO bounceViewVO){
        window.showBounceView(new String[]{
                bounceViewVO.type,
                bounceViewVO.color,
                bounceViewVO.flag
        });
    }



}
