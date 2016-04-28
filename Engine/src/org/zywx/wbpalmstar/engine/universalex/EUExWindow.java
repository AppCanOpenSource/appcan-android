/*
 *  Copyright (C) 2014 The AppCan Open Source Project.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.zywx.wbpalmstar.engine.universalex;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.slidingmenu.lib.SlidingMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.ResoureFinder;
import org.zywx.wbpalmstar.base.util.SpManager;
import org.zywx.wbpalmstar.base.vo.CreateContainerVO;
import org.zywx.wbpalmstar.base.vo.SetSwipeCloseEnableVO;
import org.zywx.wbpalmstar.base.vo.ShareInputVO;
import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.engine.EBounceView;
import org.zywx.wbpalmstar.engine.EBrowser;
import org.zywx.wbpalmstar.engine.EBrowserActivity;
import org.zywx.wbpalmstar.engine.EBrowserAnimation;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.EBrowserWidget;
import org.zywx.wbpalmstar.engine.EBrowserWindow;
import org.zywx.wbpalmstar.engine.EBrwViewEntry;
import org.zywx.wbpalmstar.engine.EDialogTask;
import org.zywx.wbpalmstar.engine.ESystemInfo;
import org.zywx.wbpalmstar.engine.EUtil;
import org.zywx.wbpalmstar.engine.EViewEntry;
import org.zywx.wbpalmstar.platform.window.ActionSheetDialog;
import org.zywx.wbpalmstar.platform.window.ActionSheetDialog.ActionSheetDialogItemClickListener;
import org.zywx.wbpalmstar.platform.window.PromptDialog;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class EUExWindow extends EUExBase {
    public static final String tag = "uexWindow";

    public static final String function_confirm = "uexWindow.cbConfirm";
    public static final String function_prompt = "uexWindow.cbPrompt";
    public static final String function_actionSheet = "uexWindow.cbActionSheet";
    public static final String function_selectList = "uexWindow.cbSelectList";
    public static final String function_getState = "uexWindow.cbGetState";
    public static final String function_getQuery = "uexWindow.cbGetUrlQuery";
    public static final String function_pageBack = "uexWindow.cbPageBack";
    public static final String function_pageForward = "uexWindow.cbPageForward";
    public static final String function_cbOpenMultiPopover = "uexWindow.cbOpenMultiPopover";
    public static final String function_cbBounceState = "uexWindow.cbBounceState";
    public static final String function_cbslipedUpward = "uexWindow.slipedUpward"; //不建议使用
    public static final String function_cbslipedDownward = "uexWindow.slipedDownward";//不建议使用
    public static final String function_cbslipedUpEdge = "uexWindow.slipedUpEdge";//不建议使用
    public static final String function_cbslipedDownEdge = "uexWindow.slipedDownEdge";//不建议使用
    public static final String function_cbCreatePluginViewContainer = "uexWindow.cbCreatePluginViewContainer";
    public static final String function_cbClosePluginViewContainer = "uexWindow.cbClosePluginViewContainer";
    public static final String function_cbShowPluginViewContainer = "uexWindow.cbShowPluginViewContainer";
    public static final String function_cbHidePluginViewContainer = "uexWindow.cbHidePluginViewContainer";
    public static final String function_onPluginContainerPageChange = "uexWindow.onPluginContainerPageChange";

    public static final String function_onSlipedUpward = "uexWindow.onSlipedUpward";
    public static final String function_onSlipedDownward = "uexWindow.onSlipedDownward";
    public static final String function_onSlipedUpEdge = "uexWindow.onSlipedUpEdge";
    public static final String function_onSlipedDownEdge = "uexWindow.onSlipedDownEdge";

    public static final String m_AdUrl = "http://wgb.tx100.com/mobile/adver.wg";

    private static final String TAG_BUNDLE_PARAM = "param";
    private static final String TAG_BUNDLE_PARAM_NAME = "name";

    private static final int MSG_FUNCTION_CLOSE = 0;
    private static final int MSG_FUNCTION_CLOSE_ABOVE_WND_BY_NAME = 1;
    private static final int MSG_FUNCTION_OPEN = 2;
    private static final int MSG_FUNCTION_OPEN_POP = 3;
    private static final int MSG_FUNCTION_FORWARD = 4;
    private static final int MSG_FUNCTION_BACK = 5;
    private static final int MSG_FUNCTION_PAGEFORWARD = 6;
    private static final int MSG_FUNCTION_PAGEBACK = 7;
    private static final int MSG_FUNCTION_WINDOWFORWARD = 8;
    private static final int MSG_FUNCTION_WINDOWBACK = 9;
    private static final int MSG_FUNCTION_SETWINDOWFRAME = 10;
    private static final int MSG_FUNCTION_OPENSLIBING = 11;
    private static final int MSG_FUNCTION_CLOSESLIBING = 12;
    private static final int MSG_FUNCTION_SHOWSLIBING = 13;
    private static final int MSG_FUNCTION_LOADOBFUSCATIONDATA = 14;
    private static final int MSG_FUNCTION_TOAST = 15;
    private static final int MSG_FUNCTION_CLOSETOAST = 16;
    private static final int MSG_FUNCTION_CLOSEPOPOVER = 17;
    private static final int MSG_FUNCTION_SETPOPOVERFRAME = 18;
    private static final int MSG_FUNCTION_OPENMULTIPOPOVER = 19;
    private static final int MSG_FUNCTION_CLOSEMULTIPOPOVER = 20;
    private static final int MSG_FUNCTION_SETSELECTEDPOPOVERINMULTIWINDOW = 21;
    private static final int MSG_FUNCTION_BRINGTOFRONT = 22;
    private static final int MSG_FUNCTION_SENDTOBACK = 23;
    private static final int MSG_FUNCTION_INSERTABOVE = 24;
    private static final int MSG_FUNCTION_INSERTBELOW = 25;
    private static final int MSG_FUNCTION_BRINGPOPOVERTOFRONT = 26;
    private static final int MSG_FUNCTION_SENDPOPOVERTOBACK = 27;
    private static final int MSG_FUNCTION_INSERTPOPOVERABOVEPOPOVER = 28;
    private static final int MSG_FUNCTION_INSERTPOPOVERBELOWPOPOVER = 29;
    private static final int MSG_FUNCTION_INSERTWINDOWABOVEWINDOW = 30;
    private static final int MSG_FUNCTION_INSERTWINDOWBELOWWINDOW = 31;
    private static final int MSG_FUNCTION_SETORIENTATION = 32;
    private static final int MSG_FUNCTION_SETSLIDINGWIN = 33;
    private static final int MSG_FUNCTION_SETSLIDINGWIN_ENABLE = 34;
    private static final int MSG_FUNCTION_TOGGLE_SLIDINGWIN = 35;
    private static final int MSG_FUNCTION_REFRESH = 36;
    private static final int MSG_FUNCTION_SETMULTIPOPOVERFRAME = 37;
    private static final int MSG_PUBLISH_CHANNEL_NOTIFICATION = 38;
    private static final int MSG_SET_WINDOW_HIDDEN = 39;
    private static final int MSG_OPEN_AD = 40;
    private static final int MSG_SHOW_SOFT_KEYBOARD = 41;
    private static final int MSG_ACTION_SHEET = 42;
    private static final int MSG_STATUS_BAR_NOTIFICATION = 43;
    private static final int MSG_CREATE_PROGRESS_DIALOG = 44;
    private static final int MSG_DESTROY_PROGRESS_DIALOG = 45;
    private static final int MSG_POST_GLOBAL_NOTIFICATION = 46;
    private static final int MSG_SUBSCRIBE_CHANNEL_NOTIFICATION = 47;
    private static final int MSG_FUNCTION_RELOAD = 48;
    private static final int MSG_FUNCTION_GET_SLIDING_WINDOW_STATE = 50;
    private static final int MSG_SET_IS_SUPPORT_SLIDE_CALLBACK = 51;
    private static final int MSG_PLUGINVIEW_CONTAINER_CREATE = 52;
    private static final int MSG_PLUGINVIEW_CONTAINER_CLOSE = 53;
    private static final int MSG_PLUGINVIEW_CONTAINER_SET = 54;
    private static final int MSG_PLUGINVIEW_CONTAINER_SHOW = 55;
    private static final int MSG_PLUGINVIEW_CONTAINER_HIDE = 56;
    private static final int MSG_PUBLISH_CHANNEL_NOTIFICATION_FOR_JSON = 57;
    private static final int MSG_SET_IS_SUPPORT_SWIPE_CALLBACK = 58;
    private static final int MSG_DISTURB_LONG_PRESS_GESTURE = 59;
    private static final int MSG_FUNCTION_SETAUTOROTATEENABLE= 60;
    private AlertDialog mAlert;
    private AlertDialog.Builder mConfirm;
    private PromptDialog mPrompt;
    private ResoureFinder finder;

    public static final String KEY_HARDWARE = "hardware";//硬件加速

    public EUExWindow(Context context, EBrowserView inParent) {
        super(context, inParent);
        inParent.setScrollCallBackContex(this);
        finder = ResoureFinder.getInstance(context);

    }

    public void open(String[] parm) {
        if (parm.length < 7) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_OPEN;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void openMsg(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        String inWindowName = parm[0];
        if (!checkWindPermission(inWindowName)) {
            showPermissionDialog(inWindowName);
            return;
        }
        String inDataType = parm[1];
        String inData = parm[2];
        String inAnimitionID = parm[3];
        String inWidth = parm[4];
        String inHeight = parm[5];
        String inFlag = parm[6];
        String animDuration = null;
        boolean opaque = false;
        /**赋初值，避免不传bgColor崩溃*/
        String bgColor = "#00000000";
        boolean hasExtraInfo = false;
        int hardware = -1;
        if (parm.length > 7) {
            animDuration = parm[7];
        }
        if (parm.length > 8) {
            String jsonData = parm[8];
            try {
                JSONObject json = new JSONObject(jsonData);
                String extraInfo = json.getString(EBrwViewEntry.TAG_EXTRAINFO);
                JSONObject data = new JSONObject(extraInfo);
                if (data.has(WWidgetData.TAG_WIN_BG_OPAQUE)) {
                    opaque = Boolean.valueOf(data.getString(WWidgetData.TAG_WIN_BG_OPAQUE));
                    hasExtraInfo = true;
                }
                if (data.has(WWidgetData.TAG_WIN_BG_COLOR)) {
                    bgColor = data.getString(WWidgetData.TAG_WIN_BG_COLOR);
                    hasExtraInfo = true;
                }
                hardware = data.optInt(KEY_HARDWARE, -1);
                if (hardware != -1) {
                    hasExtraInfo = true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String cUrl = mBrwView.getCurrentUrl();
        boolean op = EBrowser.checkFlag(EBrowser.F_BRW_FLAG_OPENING);
        boolean hi = curWind.isHidden();
        boolean eq = curWind.getName().equals(inWindowName);
        if (op || hi || eq) {
            return;
        }
        int width = 0;
        int height = 0;
        int flag = 0;
        int dataType = 0;
        int animitionId = EBrowserAnimation.ANIM_ID_NONE;
        long duration = EBrowserAnimation.defaultDuration;
        try {
            if (null != inAnimitionID && inAnimitionID.length() != 0) {
                animitionId = Integer.parseInt(inAnimitionID);
            }
            if (null != animDuration && animDuration.length() != 0
                    && !animDuration.equals("undefined")) {
                duration = Long.parseLong(animDuration);
            }
            dataType = Integer.valueOf(inDataType);
            width = parseWidth(inWidth);
            height = parseHeight(inHeight);
            flag = Integer.parseInt(inFlag);
        } catch (Exception e) {
            errorCallback(0, EUExCallback.F_E_UEXWINDOW_OPEN, "Illegal parameter");
            return;
        }
        WWidgetData wgt = mBrwView.getCurrentWidget();
        EBrwViewEntry windEntry = new EBrwViewEntry(EBrwViewEntry.VIEW_TYPE_MAIN);
        String data = null;
        if (EBrwViewEntry.isData(dataType)) {
            data = inData;
        } else {
            String wgtroot = "wgtroot://";
            if (inData.startsWith(wgtroot)) {
                String initUrl = wgt.m_indexUrl;
                inData = inData.substring(wgtroot.length());
                inData = BUtility.makeUrl(initUrl, inData);
                data = inData;
            } else {
                data = BUtility.makeUrl(cUrl, inData);
            }
            windEntry.mRelativeUrl = inData;
        }
        String query = null;
        if (Build.VERSION.SDK_INT >= 11) {
            if (EBrwViewEntry.isUrl(dataType) && data != null) {
                int index = data.indexOf("?");
                if (index > 0) {
                    query = data.substring(index + 1);
                    if (!data.startsWith("http")) {
                        data = data.substring(0, index);
                    }
                }
            }
        }
        windEntry.mPreWindName = curWind.getName();
        windEntry.mQuery = query;
        windEntry.mWindName = inWindowName;
        windEntry.mDataType = dataType;
        windEntry.mData = data;
        windEntry.mAnimId = animitionId;
        windEntry.mWidth = width;
        windEntry.mHeight = height;
        windEntry.mFlag = flag;
        windEntry.mAnimDuration = duration;
        windEntry.mOpaque = opaque;
        windEntry.mBgColor = bgColor;
        windEntry.mHardware = hardware;
        windEntry.hasExtraInfo = hasExtraInfo;
        curWind.createWindow(mBrwView, windEntry);
    }

    public void openPresentWindow(String[] params){
        if (params.length < 7) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_OPEN;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public int getHeight(String[] params){
        return mBrwView.getBrowserWindow().getHeight();
    }

    public int getWidth(String[] params){
        return mBrwView.getBrowserWindow().getWidth();
    }

    private boolean checkWindPermission(String windName) {
        WWidgetData rootWgt = mBrwView.getRootWidget();
        ArrayList<String> winds = rootWgt.disableRootWindowsList;
        if (null == windName || windName.trim().length() == 0 || null == winds) {
            return true;
        }
        for (String name : winds) {
            if (windName.equals(name)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkWindPopPermission(String windPopName) {
        WWidgetData rootWgt = mBrwView.getRootWidget();
        ArrayList<String>  winds = rootWgt.disableSonWindowsList;
        if (null == windPopName || windPopName.trim().length() == 0
                || null == winds) {
            return true;
        }
        for (String name : winds) {
            if (windPopName.equals(name)) {
                return false;
            }
        }
        return true;
    }

    private void showPermissionDialog(final String windName) {
        EBrowserActivity activity = (EBrowserActivity) mContext;
        /*if (!activity.isVisable()) {
			return;
		}*/
        Runnable ui = new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder dia = new AlertDialog.Builder(mContext);
                dia.setTitle(EUExUtil.getString("warning"));
                dia.setMessage(String.format(EUExUtil.getString("no_permission_to_open_window"), windName));
                dia.setCancelable(false);
                dia.setPositiveButton(EUExUtil.getString("confirm"), null);
                dia.show();
            }
        };
        activity.runOnUiThread(ui);
    }

    public void setOrientation(String[] parm) {
        if (parm.length < 1) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_SETORIENTATION;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void setOrientationMsg(String[] parm) {
        if (null != mBrwView) {
            EBrowserActivity activity = (EBrowserActivity) mContext;
            int or = activity.getRequestedOrientation();
            try {
                or = Integer.parseInt(parm[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            activity.changeConfiguration(or);
        }
    }

    public void setAutorotateEnable(String[] parm) {
        if (parm.length < 1) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_SETAUTOROTATEENABLE;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void setAutorotateEnableMsg(String[] parm) {
        int enabled = 0;
        try {
            enabled = Integer.parseInt(parm[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != mBrwView) {
            EBrowserActivity activity = (EBrowserActivity) mContext;
            activity.setAutorotateEnable(enabled);
        }
    }

    public void setWindowFrame(String[] parm) {
        if (parm.length < 3) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_SETWINDOWFRAME;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void setWindowFrameMsg(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        String inX = parm[0];
        String inY = parm[1];
        String animDuration = parm[2];
        int x = 0, y = 0, duration = 250;
        try {
            if (null != inX && inX.length() != 0) {
                x = Integer.parseInt(inX);
            }
            if (null != inY && inY.length() != 0) {
                y = Integer.parseInt(inY);
            }
            if (null != animDuration && animDuration.length() != 0) {
                duration = Integer.valueOf(animDuration);
            }
        } catch (Exception e) {
            errorCallback(0, EUExCallback.F_E_UEXWINDOW_EVAL, "Illegal parameter");
            return;
        }
        curWind.setWindowFrame(x, y, duration);
    }

    public void close(String[] parm) {
        Message msg = mHandler.obtainMessage();
        msg.what = MSG_FUNCTION_CLOSE;
        msg.obj = this;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void exit(String[] parm) {
        int len = parm.length;
        boolean showDialog = !(len > 0 && "0".equals(parm[0]));
        ((EBrowserActivity) mContext).exitApp(showDialog);
    }

    public void closeMsg(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        if (mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_POP)) {
            curWind.closePopover(mBrwView.getName());
            return;
        }

        if (mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_ADD)) {
            curWind.closeAd();
            return;
        }

        if ("root".equals(mBrwView.getWindowName())) {
            ((EBrowserActivity) mContext).exitApp(true);
            return;
        }
        String inAnimitionID = null;
        String animDuration = null;
        switch (parm.length) {
            case 0:
                break;
            case 1:
                inAnimitionID = parm[0];
                break;
            case 2:
                inAnimitionID = parm[0];
                animDuration = parm[1];
                break;
        }
        int animId = EBrowserAnimation.ANIM_ID_FILL;
        long duration = EBrowserAnimation.defaultDuration;
        try {
            if (null != inAnimitionID && inAnimitionID.length() != 0) {
                animId = Integer.parseInt(inAnimitionID);
            }
            if (null != animDuration && animDuration.length() != 0
                    && !animDuration.equals("undefined")) {
                duration = Long.parseLong(animDuration);
            }
        } catch (Exception e) {
            errorCallback(0, EUExCallback.F_E_UEXWINDOW_CLOSE, "Illegal parameter");
        }
        curWind.onCloseWindow(animId, duration);
    }

    public void closeAboveWndByName(String[] parm) {
        String windowName = "";
        if (parm.length > 0 && !TextUtils.isEmpty(parm[0])) {
            windowName = parm[0];
        }
        Message msg = mHandler.obtainMessage();
        msg.what = MSG_FUNCTION_CLOSE_ABOVE_WND_BY_NAME;
        msg.obj = this;
        Bundle b = new Bundle();
        b.putString(TAG_BUNDLE_PARAM_NAME, windowName);
        msg.setData(b);
        mHandler.sendMessage(msg);
    }

    public void refresh(String[] params) {
        Message msg = mHandler.obtainMessage();
        msg.what = MSG_FUNCTION_REFRESH;
        msg.obj = this;
        mHandler.sendMessage(msg);
    }

    public void reload(String[] params) {
        Message msg = mHandler.obtainMessage();
        msg.what = MSG_FUNCTION_RELOAD;
        msg.obj = this;
        mHandler.sendMessage(msg);
    }

    public void openSlibing(String[] parm) {
        if (parm.length < 6) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_OPENSLIBING;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void openSlibingMsg(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        float nowScale = 1.0f;
        int versionA = Build.VERSION.SDK_INT;

        if (versionA <= 18) {
            nowScale = mBrwView.getScale();
        }
        float sc = nowScale;
        String inType = parm[0];
        String inDataType = parm[1];
        String inUrl = parm[2];
        String inData = parm[3];
        String inWidth = parm[4];
        String inHeight = parm[5];
        int type = 0;
        int dataType = 0;
        int width = 0;
        int height = 0;
        try {
            type = Integer.parseInt(inType);
            dataType = Integer.parseInt(inDataType);
            width = parseWidth(inWidth);
            height = (int) (parseHeight(inHeight) * sc);
        } catch (Exception e) {
            errorCallback(0, EUExCallback.F_E_UEXWINDOW_OPENS, "Illegal parameter");
            return;
        }
        String url = BUtility.makeUrl(mBrwView.getCurrentUrl(), inUrl);
        String query = null;
        if (Build.VERSION.SDK_INT >= 11) {
            if (url != null) {
                int index = url.indexOf("?");
                if (index > 0) {
                    query = url.substring(index + 1);
                    if (!url.startsWith("http")) {
                        url = url.substring(0, index);
                    }
                }
            }
        }
        EBrwViewEntry slbEntry = new EBrwViewEntry(type);
        slbEntry.mQuery = query;
        slbEntry.mDataType = dataType;
        slbEntry.mUrl = url;
        slbEntry.mData = inData;
        slbEntry.mWidth = width;
        slbEntry.mHeight = height;
        curWind.createSibling(mBrwView, slbEntry);
    }

    public void showSlibing(String[] parm) {
        if (parm.length < 1) {
            return;
        }
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_SHOWSLIBING;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void showSlibingMsg(String[] parm) {
        String inType = parm[0];
        int eType = 0;
        try {
            eType = Integer.parseInt(inType);
        } catch (Exception e) {
            errorCallback(0, EUExCallback.F_E_UEXWINDOW_SHOWS,
                    "Illegal parameter");
            return;
        }
        mBrwView.getBrowserWindow().showSlibing(eType);
    }

    public void closeSlibing(String[] parm) {
        if (parm.length < 1) {
            return;
        }
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_CLOSESLIBING;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void closeSlibingMsg(String[] parm) {
        String inType = parm[0];
        int eType = 0;
        try {
            eType = Integer.parseInt(inType);
        } catch (Exception e) {
            errorCallback(0, EUExCallback.F_E_UEXWINDOW_CLOSES, "Illegal parameter");
            return;
        }
        mBrwView.getBrowserWindow().closeSlibing(eType);
    }

    public void evaluateScript(String[] parm) {
        if (parm.length < 3) {
            return;
        }
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        String inWindowName = parm[0];
        String inType = parm[1];
        String inScript = parm[2];
        int eType = 0;
        try {
            eType = Integer.parseInt(inType);
        } catch (Exception e) {
            errorCallback(0, EUExCallback.F_E_UEXWINDOW_EVAL, "Illegal parameter");
            return;
        }
        curWind.evaluateScript(mBrwView, inWindowName, eType, SCRIPT_HEADER + inScript);
    }

    public void preOpenStart(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        curWind.clearPopQue();
    }

    public void preOpenFinish(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        curWind.setFlag(EBrowserWindow.F_WINDOW_FLAG_OPPOP_END);
    }


    public void toggleSlidingWindow(String[] param) {
        if (param.length <= 0) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_TOGGLE_SLIDINGWIN;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, param);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void hanldeToggleSlidingWindow(String[] param) {
        try {
            JSONObject jsonObject = new JSONObject(param[0]);
            int isLeft = jsonObject.optInt("mark", 0);
            int isReload = jsonObject.optInt("reload", 0);
            EBrowserActivity activity = (EBrowserActivity) mContext;

            SlidingMenu slidingMenu = activity.globalSlidingMenu;

            if (slidingMenu.isMenuShowing()) {

                slidingMenu.toggle();
            } else {
                if (isLeft == 0) { //left

                    slidingMenu.showMenu();
                    if (isReload == 1) {
                        EBrowserWindow leftWindow = (EBrowserWindow) slidingMenu.getMenu();
                        if (leftWindow != null) {
                            leftWindow.refresh();
                        }
                    }
                } else if (isLeft == 1) { // right
                    slidingMenu.showSecondaryMenu();
                    if (isReload == 1) {
                        EBrowserWindow rightWindow = (EBrowserWindow) slidingMenu.getSecondaryMenu();
                        if (rightWindow != null) {
                            rightWindow.refresh();
                        }
                    }
                }

            }
        } catch (Exception e) {
        }
    }

    public void getSlidingWindowState(String[] param) {
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_GET_SLIDING_WINDOW_STATE;
        mHandler.sendMessage(msg);
    }

    private void hanldeGetSlidingWindowState() {
        EBrowserActivity activity = (EBrowserActivity) mContext;
        SlidingMenu slidingMenu = activity.globalSlidingMenu;
        if (slidingMenu != null) {
            int state = slidingMenu.getCurrentItem();
            String js = "javascript:if(uexWindow.cbSlidingWindowState){uexWindow.cbSlidingWindowState("
                    + state + ");}";
            mBrwView.addUriTask(js);
        }
    }

    public void setSlidingWindowEnabled(String[] param) {
        if (param.length <= 0) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_SETSLIDINGWIN_ENABLE;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, param);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void hanldeSetSlidingWindowEnabled(String[] param) {
        try {
            int value = Integer.parseInt(param[0]);
            boolean enable = (value == 1) ? true : false;
            EBrowserActivity activity = (EBrowserActivity) mContext;

            activity.globalSlidingMenu.setSlidingEnabled(enable);

        } catch (Exception e) {
        }
    }


    public void setSlidingWindow(String[] param) {
        if (param.length <= 0) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_SETSLIDINGWIN;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, param);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void handleSetSlidingWin(String[] param) {
        String jsonStr = param[0];
        EBrowserActivity activity = (EBrowserActivity) mContext;
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            int with = 0;
            String url;
            int slidingMode = SlidingMenu.LEFT;
            boolean isAttach = false;
            JSONObject leftJsonObj = null;
            JSONObject rightJsonObj = null;
            View menuView;
            if (activity.globalSlidingMenu.getParent() != null) {
                return;
            }

            String animationId = jsonObject.optString("animationId");
            if (jsonObject.has("leftSliding")) {
                leftJsonObj = new JSONObject(jsonObject.getString("leftSliding"));
                if (leftJsonObj != null) {
                    slidingMode = SlidingMenu.LEFT;
                    with = leftJsonObj.getInt("width");
                    url = leftJsonObj.getString("url");
                    if (with > 0) {
                        activity.globalSlidingMenu.setBehindWidth(with);
                    }
                    menuView = LayoutInflater.from(mContext).inflate(finder.getLayoutId("menu_frame"), null);
                    activity.globalSlidingMenu.setMenu(menuView);
                    addBrowserWindowToSldingWin(url, EBrowserWindow.rootLeftSlidingWinName);
                    isAttach = true;
                }
            }

            if (jsonObject.has("rightSliding")) {
                rightJsonObj = new JSONObject(jsonObject.getString("rightSliding"));
                if (rightJsonObj != null) {
                    slidingMode = SlidingMenu.RIGHT;
                    with = rightJsonObj.getInt("width");
                    url = rightJsonObj.getString("url");
                    if (with > 0) {
                        activity.globalSlidingMenu.setBehindWidth(with);
                    }
                    menuView = LayoutInflater.from(mContext).inflate(finder.getLayoutId("menu_frame_two"), null);
                    activity.globalSlidingMenu.setSecondaryMenu(menuView);
                    activity.globalSlidingMenu.setSecondaryShadowDrawable(finder.getDrawable("shadowright"));
                    addBrowserWindowToSldingWin(url, EBrowserWindow.rootRightSlidingWinName);
                    isAttach = true;
                }
            }

            if ("1".equals(animationId)) {
                //仿QQ侧边栏动画
                activity.globalSlidingMenu.setBehindCanvasTransformer(new SlidingMenu.CanvasTransformer() {
                    @Override
                    public void transformCanvas(Canvas canvas, float percentOpen) {
                        float scale = (float) (percentOpen * 0.25 + 0.75);
                        canvas.scale(scale, scale, 0, canvas.getHeight() / 2);
                    }
                });
                activity.globalSlidingMenu.setAboveCanvasTransformer(new SlidingMenu.CanvasTransformer() {
                    @Override
                    public void transformCanvas(Canvas canvas, float percentOpen) {
                        float scale = (float) (1 - percentOpen * 0.20);
                        canvas.scale(scale, scale, canvas.getWidth() / 2, canvas.getHeight() / 2);
                    }
                });
                activity.globalSlidingMenu.setFadeEnabled(false);
            } else {
                activity.globalSlidingMenu.setShadowWidthRes(EUExUtil.getResDimenID("shadow_width"));
                if (!jsonObject.has("leftSliding") && jsonObject.has("rightSliding")) {
                    activity.globalSlidingMenu.setShadowDrawable(EUExUtil.getResDrawableID("shadowright"));
                } else {
                    activity.globalSlidingMenu.setShadowDrawable(EUExUtil.getResDrawableID("shadow"));
                }
                activity.globalSlidingMenu.setFadeDegree(0.35f);
            }

            String bg = jsonObject.optString("bg");
            if (!TextUtils.isEmpty(bg)) {
                setViewBackground(activity.globalSlidingMenu, bg, mBrwView.getCurrentWidget().m_indexUrl);
            }

            if (leftJsonObj != null && rightJsonObj != null) {
                slidingMode = SlidingMenu.LEFT_RIGHT;
            }

            if (isAttach == true) {
                activity.globalSlidingMenu.setMode(slidingMode);
                activity.globalSlidingMenu.attachToActivity(activity, SlidingMenu.SLIDING_CONTENT);
                mBrwView.setBackgroundColor(Color.TRANSPARENT);
            }
        } catch (JSONException e) {
        }
    }

    public void setViewBackground(View view, String bgColor, String baseUrl) {

        if (bgColor.startsWith("#") || bgColor.startsWith("rgb")) {
            int color = BUtility.parseColor(bgColor);
            view.setBackgroundColor(color);
        } else {
            String path = BUtility.makeRealPath(BUtility.makeUrl(mBrwView.getCurrentUrl(baseUrl), bgColor),
                    mBrwView.getCurrentWidget().m_widgetPath, mBrwView.getCurrentWidget().m_wgtType);
            Bitmap bitmap = BUtility.getLocalImg(mContext, path);
            Drawable d = null;
            if (bitmap != null) {
                d = new BitmapDrawable(mContext.getResources(), bitmap);
            }
            int version = Build.VERSION.SDK_INT;
            if (version < 16) {
                view.setBackgroundDrawable(d);
            } else {
                view.setBackground(d);
            }
        }

    }

    public void addBrowserWindowToSldingWin(String url, String winName) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        String inWindowName = winName;
        String inData = url;

        String cUrl = mBrwView.getCurrentUrl();
        boolean eq = curWind.getName().equals(inWindowName);
        if (eq) {
            return;
        }

        int dataType = 0;


        WWidgetData wgt = mBrwView.getCurrentWidget();
        EBrwViewEntry windEntry = new EBrwViewEntry(EBrwViewEntry.VIEW_TYPE_MAIN);
        String data = null;
        if (EBrwViewEntry.isData(dataType)) {
            data = inData;
        } else {
            String wgtroot = "wgtroot://";
            if (inData.startsWith(wgtroot)) {
                String initUrl = wgt.m_indexUrl;
                inData = inData.substring(wgtroot.length());
                inData = BUtility.makeUrl(initUrl, inData);
                data = inData;
            } else {
                data = BUtility.makeUrl(cUrl, inData);
            }
            windEntry.mRelativeUrl = inData;
        }
        String query = null;
        if (Build.VERSION.SDK_INT >= 11) {
            if (EBrwViewEntry.isUrl(dataType) && data != null) {
                int index = data.indexOf("?");
                if (index > 0) {
                    query = data.substring(index + 1);
                    if (!url.startsWith("http")) {
                        data = data.substring(0, index);
                    }
                }
            }
        }
        windEntry.mQuery = query;
        windEntry.mWindName = inWindowName;
        windEntry.mDataType = dataType;
        windEntry.mData = data;
        curWind.createSlidingWindow(windEntry);
    }


    public void openPopover(String[] parm) {
        if (parm.length < 10) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_OPEN_POP;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        if (parm.length > 11) {
            //取第12个参数的延迟加载字段
            long delay = 0l;
            try {
                JSONObject json = new JSONObject(parm[11]);
                JSONObject data = new JSONObject(json.getString(EBrwViewEntry.TAG_EXTRAINFO));
                if (data.has(EBrwViewEntry.TAG_DELAYTIME)) {
                    delay = Long.valueOf(data.getString(EBrwViewEntry.TAG_DELAYTIME));
                }
            } catch (Exception e) {
            }
            mHandler.sendMessageDelayed(msg, delay);
        } else {
            mHandler.sendMessage(msg);
        }
    }

    public void openPopoverMsg(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        float nowScale = 1.0f;

        int versionA = Build.VERSION.SDK_INT;

        if (versionA <= 18) {
            nowScale = mBrwView.getScale();
        }
        float sc = nowScale;
        String inPopName = parm[0];
        String windPopName = curWind.getName() + inPopName;
        if (!checkWindPopPermission(windPopName)) {
            showPermissionDialog(windPopName);
            return;
        }
        String inDataType = parm[1];
        String inUrl = parm[2];
        String inData = parm[3];
        String inX = parm[4];
        String inY = parm[5];
        String inWidth = parm[6];
        String inHeight = parm[7];
        String inFontSize = parm[8];
        String inFlag = parm[9];
        String marginBottom = null;
        if (parm.length > 10) {
            marginBottom = parm[10];
        }
        boolean opaque = false;
        /**赋初值，避免不传bgColor崩溃*/
        String bgColor = "#00000000";
        boolean hasExtraInfo = false;
        int hardware = -1;
        if (parm.length > 11) {
            String jsonData = parm[11];
            try {
                JSONObject json = new JSONObject(jsonData);
                String extraInfo = json.getString(EBrwViewEntry.TAG_EXTRAINFO);
                JSONObject data = new JSONObject(extraInfo);
                if (data.has(WWidgetData.TAG_WIN_BG_OPAQUE)) {
                    opaque = Boolean.valueOf(data.getString(WWidgetData.TAG_WIN_BG_OPAQUE));
                    hasExtraInfo = true;
                }
                if (data.has(WWidgetData.TAG_WIN_BG_COLOR)) {
                    bgColor = data.getString(WWidgetData.TAG_WIN_BG_COLOR);
                    hasExtraInfo = true;
                }
                hardware = data.optInt(KEY_HARDWARE, -1);
                if (hardware != -1) {
                    hasExtraInfo = true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        int dType = 0;
        int x = 0;
        int y = 0;
        int w = -1;
        int h = -1;
        int fonts = 0;
        int flag = 0;
        int bottom = 0;
        if (null == inPopName || 0 == inPopName.length()) {
            errorCallback(0, EUExCallback.F_E_UEXWINDOW_EVAL, "Illegal parameter");
            return;
        }
        try {
            if (null != inDataType && inDataType.length() != 0) {
                dType = Integer.valueOf(inDataType);
            }
            if (null != inX && inX.length() != 0) {
                x = (int) (Integer.valueOf(inX) * sc);
            }
            if (null != inY && inY.length() != 0) {
                y = (int) (Integer.valueOf(inY) * sc);
            }
            if (null != inWidth && inWidth.length() != 0
                    && !"0".equals(inWidth)) {
                w = (int) (Integer.valueOf(inWidth) * sc);
            }
            if (null != inHeight && inHeight.length() != 0
                    && !"0".equals(inHeight)) {
                h = (int) (Integer.valueOf(inHeight) * sc);
            }
            if (null != inFontSize && inFontSize.length() != 0) {
                fonts = Integer.valueOf(inFontSize);
            }
            if (null != inFlag && inFlag.length() != 0) {
                flag = Integer.valueOf(inFlag);
            }
            if (null != marginBottom && marginBottom.length() != 0
                    && !"0".equals(marginBottom)) {
                bottom = (int) (Integer.valueOf(marginBottom) * sc);
            }
        } catch (Exception e) {
            errorCallback(0, EUExCallback.F_E_UEXWINDOW_EVAL, "Illegal parameter");
            return;
        }
        WWidgetData wgt = mBrwView.getCurrentWidget();
        String url = null;
        if (null != inUrl) {
            String wgtroot = "wgtroot://";
            if (inUrl.startsWith(wgtroot)) {
                String initUrl = wgt.m_indexUrl;
                inUrl = inUrl.substring(wgtroot.length());
                inUrl = BUtility.makeUrl(initUrl, inUrl);
                url = inUrl;
            } else {
                url = BUtility.makeUrl(mBrwView.getCurrentUrl(), inUrl);
            }
        }
        EBrwViewEntry popEntry = new EBrwViewEntry(EBrwViewEntry.VIEW_TYPE_POP);
        popEntry.mRelativeUrl = inUrl;
        popEntry.mViewName = inPopName;
        popEntry.mDataType = dType;
        popEntry.mUrl = url;
        popEntry.mData = inData;
        popEntry.mX = x;
        popEntry.mY = y;
        popEntry.mWidth = w;
        if (bottom > 0) {
            h = -1;
        }
        popEntry.mHeight = h;
        popEntry.mFontSize = fonts;
        popEntry.mFlag = flag;
        popEntry.mBottom = bottom;
        popEntry.mOpaque = opaque;
        popEntry.mBgColor = bgColor;
        popEntry.mHardware = hardware;
        popEntry.hasExtraInfo = hasExtraInfo;
        String query = null;
        if (Build.VERSION.SDK_INT >= 11) {
            if (url != null && url.trim().length() != 0) {
                int index = url.indexOf("?");
                if (index > 0) {
                    query = url.substring(index + 1);
                    if (!url.startsWith("http")) {
                        url = url.substring(0, index);
                        popEntry.mUrl = url;
                    }
                }
            }
        }
        popEntry.mQuery = query;
        curWind.openPopover(popEntry);
    }

    public void closePopover(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_CLOSEPOPOVER;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void closePopoverMsg(String[] parm) {
        String inPopName = null;
        if (parm.length < 1) {
            inPopName = mBrwView.getName();
        } else {
            inPopName = parm[0];
            if (null == inPopName || 0 == inPopName.length()) {
                inPopName = mBrwView.getName();
            }
        }
        mBrwView.getBrowserWindow().closePopover(inPopName);
    }

    public void setPopoverFrame(String[] parm) {
        if (parm.length < 5) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_SETPOPOVERFRAME;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void setHardwareEnable(final String[] params) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (params != null && params.length > 0) {
                    EBrowserWindow curWindow = mBrwView.getBrowserWindow();
                    int flag = Integer.parseInt(params[0]);
                    curWindow.setWindowHWEnable(flag);
                }
            }
        });
    }


    public void setPopHardwareEnable(final String[] params) {
        BDebug.i(params.toString());
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (params != null && params.length > 1) {
                    String popoverName = params[0];
                    int flag = Integer.parseInt(params[1]);
                    EBrowserWindow curWindow = mBrwView.getBrowserWindow();
                    if (curWindow == null) {
                        return;
                    }
                    curWindow.setPopoverHardwareEnable(popoverName, flag);
                }
            }
        });
    }


    public void setPopoverFrameMsg(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }

        float nowScale = 1.0f;

        int versionA = Build.VERSION.SDK_INT;

        if (versionA <= 18) {
            nowScale = mBrwView.getScale();
        }
        float sc = nowScale;
        String inPopName = parm[0];
        String inX = parm[1];
        String inY = parm[2];
        String inWidth = parm[3];
        String inHeight = parm[4];
        int x = 0, y = 0, w = -1, h = -1;
        try {
            if (null != inX && inX.length() != 0) {
                x = (int) (Integer.valueOf(inX) * sc);
            }
            if (null != inY && inY.length() != 0) {
                y = (int) (Integer.valueOf(inY) * sc);
            }
            if (null != inWidth && inWidth.length() != 0) {
                w = (int) (Integer.valueOf(inWidth) * sc);
            }
            if (null != inHeight && inHeight.length() != 0) {
                h = (int) (Integer.valueOf(inHeight) * sc);
            }
        } catch (Exception e) {
            errorCallback(0, EUExCallback.F_E_UEXWINDOW_EVAL, "Illegal parameter");
            return;
        }
        curWind.setPopoverFrame(inPopName, x, y, w, h);
    }

    public void openMultiPopover(String[] parm) {
        Log.d("multi", "open multi pop");
        if (parm.length < 10) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_OPENMULTIPOPOVER;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        if (parm.length > 10) {
            //取第11个参数的延迟加载字段
            long delay = 0l;
            try {
                JSONObject json = new JSONObject(parm[10]);
                JSONObject data = new JSONObject(json.getString(EBrwViewEntry.TAG_EXTRAINFO));
                if (data.has(EBrwViewEntry.TAG_DELAYTIME)) {
                    delay = Long.valueOf(data.getString(EBrwViewEntry.TAG_DELAYTIME));
                }
            } catch (Exception e) {
            }
            mHandler.sendMessageDelayed(msg, delay);
        } else {
            mHandler.sendMessage(msg);
        }
    }

    public void openMultiPopoverMsg(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        WWidgetData wgt = mBrwView.getCurrentWidget();
        ArrayList<EBrwViewEntry> popEntrys = new ArrayList<EBrwViewEntry>();

        EBrwViewEntry mainPopEntry = new EBrwViewEntry(EBrwViewEntry.VIEW_TYPE_POP);

        float nowScale = 1.0f;

        int versionA = Build.VERSION.SDK_INT;

        if (versionA <= 18) {
            nowScale = mBrwView.getScale();
        }
        float sc = nowScale;
        String inContent = parm[0];
        String inMultiPopName = parm[1];

        // 父节点属性，
        String inDataType = parm[2];
        String inX = parm[3];
        String inY = parm[4];
        String inWidth = parm[5];
        String inHeight = parm[6];
        String inFontSize = parm[7];
        String inFlag = parm[8];
        String inIndexSelect = parm[9];

        boolean opaque = false;
        /**赋初值，避免不传bgColor崩溃*/
        String bgColor = "#00000000";
        boolean hasExtraInfo = false;
        if (parm.length > 10) {
            String jsonData = parm[10];
            try {
                JSONObject json = new JSONObject(jsonData);
                String extraInfo = json.getString(EBrwViewEntry.TAG_EXTRAINFO);
                JSONObject data = new JSONObject(extraInfo);
                if (data.has(WWidgetData.TAG_WIN_BG_OPAQUE)) {
                    opaque = Boolean.valueOf(data.getString(WWidgetData.TAG_WIN_BG_OPAQUE));
                    hasExtraInfo = true;
                }
                if (data.has(WWidgetData.TAG_WIN_BG_COLOR)) {
                    bgColor = data.getString(WWidgetData.TAG_WIN_BG_COLOR);
                    hasExtraInfo = true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String windPopName = curWind.getName() + inMultiPopName;
        if (!checkWindPopPermission(windPopName)) {
            showPermissionDialog(windPopName);
            return;
        }

        int dType = 0;
        int x = 0;
        int y = 0;
        int w = -1;
        int h = -1;
        int fonts = 0;
        int flag = 0;
        int indexSelect = 0;

        String[] childUrl = null;

        try {
            if (null == inMultiPopName || 0 == inMultiPopName.length()) {
                errorCallback(0, EUExCallback.F_E_UEXWINDOW_EVAL, "Illegal parameter");
                return;
            }
            if (null != inDataType && inDataType.length() != 0) {
                dType = Integer.valueOf(inDataType);
            }
            if (null != inX && inX.length() != 0) {
                x = (int) (Integer.valueOf(inX) * sc);
            }
            if (null != inY && inY.length() != 0) {
                y = (int) (Integer.valueOf(inY) * sc);
            }
            if (null != inWidth && inWidth.length() != 0) {
                w = (int) (Integer.valueOf(inWidth) * sc);
            }
            if (null != inHeight && inHeight.length() != 0) {
                h = (int) (Integer.valueOf(inHeight) * sc);
            }
            if (null != inFontSize && inFontSize.length() != 0) {
                fonts = Integer.valueOf(inFontSize);
            }
            if (null != inFlag && inFlag.length() != 0) {
                flag = Integer.valueOf(inFlag);

            }
            if (null != inIndexSelect && inIndexSelect.length() != 0) {
                indexSelect = Integer.valueOf(inIndexSelect);

            }

            mainPopEntry.mViewName = inMultiPopName;
            mainPopEntry.mDataType = dType;
            mainPopEntry.mX = x;
            mainPopEntry.mY = y;
            mainPopEntry.mWidth = w;
            mainPopEntry.mHeight = h;
            mainPopEntry.mFontSize = fonts;
            mainPopEntry.mFlag = flag;
            mainPopEntry.mOpaque = opaque;
            mainPopEntry.mBgColor = bgColor;
            mainPopEntry.hasExtraInfo = hasExtraInfo;
            popEntrys.add(mainPopEntry);

            JSONObject content = new JSONObject(inContent);
            JSONArray jsonContent = content.getJSONArray("content");
            int j = jsonContent.length();
            Log.d("multi", "jsonContent num:" + j);

            childUrl = new String[j];

            for (int i = 0; i < jsonContent.length(); i++) {
                EBrwViewEntry popEntry = new EBrwViewEntry(
                        EBrwViewEntry.VIEW_TYPE_POP);
                boolean opaque1 = false;
                /**赋初值，避免不传bgColor崩溃*/
                String bgColor1 = "#00000000";
                boolean hasExtraInfo1 = false;
                if (jsonContent.getJSONObject(i).has(EBrwViewEntry.TAG_EXTRAINFO)) {
                    try {
                        String extraInfo = jsonContent.getJSONObject(i).getString(EBrwViewEntry.TAG_EXTRAINFO);
                        JSONObject data = new JSONObject(extraInfo);
                        if (data.has(WWidgetData.TAG_WIN_BG_OPAQUE)) {
                            opaque1 = Boolean.valueOf(data.getString(WWidgetData.TAG_WIN_BG_OPAQUE));
                            hasExtraInfo1 = true;
                        }
                        if (data.has(WWidgetData.TAG_WIN_BG_COLOR)) {
                            bgColor1 = data.getString(WWidgetData.TAG_WIN_BG_COLOR);
                            hasExtraInfo1 = true;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                popEntry.mOpaque = opaque1;
                popEntry.mBgColor = bgColor1;
                popEntry.hasExtraInfo = hasExtraInfo1;
                popEntry.mViewName = jsonContent.getJSONObject(i).getString(
                        "inPageName");

                childUrl[i] = jsonContent.getJSONObject(i).getString("inUrl");
                popEntry.mData = jsonContent.getJSONObject(i).getString(
                        "inData");
                popEntry.mFlag = jsonContent.getJSONObject(i).optInt(
                        "flag");

                if (null == popEntry.mViewName
                        || popEntry.mViewName.length() == 0) {
                    popEntry.mViewName = i + "";// 如果没有命名，则自动以序号作为命名
                }
                if (null != childUrl[i]) {
                    String wgtroot = "wgtroot://";
                    if (childUrl[i].startsWith(wgtroot)) {
                        String initUrl = wgt.m_indexUrl;
                        childUrl[i] = childUrl[i].substring(wgtroot.length());
                        childUrl[i] = BUtility.makeUrl(initUrl, childUrl[i]);

                    } else {
                        childUrl[i] = BUtility.makeUrl(mBrwView.getCurrentUrl(), childUrl[i]);
                    }
                    popEntry.mRelativeUrl = childUrl[i];
                    popEntry.mUrl = childUrl[i];
                    String query = null;
                    if (Build.VERSION.SDK_INT >= 11) {
                        if (childUrl[i] != null
                                && childUrl[i].trim().length() != 0) {
                            int index = childUrl[i].indexOf("?");
                            if (index > 0) {
                                query = childUrl[i].substring(index + 1);
                                if (!childUrl[i].startsWith("http")) {
                                    childUrl[i] = childUrl[i].substring(0, index);
                                    popEntry.mUrl = childUrl[i];
                                }
                            }
                        }
                    }
                    popEntry.mQuery = query;
                }

                popEntry.mDataType = dType;
                popEntry.mX = x;
                popEntry.mY = y;
                popEntry.mWidth = w;
                popEntry.mHeight = h;
                popEntry.mFontSize = fonts;

                popEntrys.add(popEntry);
            }
            Log.d("multi", "popEntrys num:" + popEntrys.size());
        } catch (Exception e) {
            errorCallback(0, EUExCallback.F_E_UEXWINDOW_EVAL, "Illegal parameter");
            return;
        }
        curWind.openMultiPopover(this, popEntrys, indexSelect);
    }

    public void setSelectedPopOverInMultiWindow(String[] parm) {
        if (parm == null || parm.length < 2) {
            errorCallback(0, EUExCallback.F_E_UEXWINDOW_EVAL, "Illegal parameter");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_SETSELECTEDPOPOVERINMULTIWINDOW;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void setSelectedPopOverInMultiWindowMsg(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        String name = parm[0];
        int index = Integer.valueOf(parm[1]);
        curWind.setSelectedPopOverInMultiWindow(name, index);
    }

    public void closeMultiPopover(String[] parm) {
        if (parm == null || parm.length < 1) {
            errorCallback(0, EUExCallback.F_E_UEXWINDOW_EVAL, "Illegal parameter");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_CLOSEMULTIPOPOVER;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void closeMultiPopoverMsg(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        String multiPopName = parm[0];
        curWind.closeMultiPopover(multiPopName);
    }

    public void setMultiPopoverFrame(String[] parm) {
        if (parm.length < 5) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_SETMULTIPOPOVERFRAME;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void setMultiPopoverFrameMsg(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        float nowScale = 1.0f;
        int versionA = Build.VERSION.SDK_INT;
        if (versionA <= 18) {
            nowScale = mBrwView.getScale();
        }
        float sc = nowScale;
        String inPopName = parm[0];
        String inX = parm[1];
        String inY = parm[2];
        String inWidth = parm[3];
        String inHeight = parm[4];
        int x = 0, y = 0, w = -1, h = -1;
        try {
            if (null != inX && inX.length() != 0) {
                x = (int) (Integer.valueOf(inX) * sc);
            }
            if (null != inY && inY.length() != 0) {
                y = (int) (Integer.valueOf(inY) * sc);
            }
            if (null != inWidth && inWidth.length() != 0) {
                w = (int) (Integer.valueOf(inWidth) * sc);
            }
            if (null != inHeight && inHeight.length() != 0) {
                h = (int) (Integer.valueOf(inHeight) * sc);
            }
        } catch (Exception e) {
            errorCallback(0, EUExCallback.F_E_UEXWINDOW_EVAL, "Illegal parameter");
            return;
        }
        curWind.setMultiPopoverFrame(inPopName, x, y, w, h);
    }

    public void evaluateMultiPopoverScript(String[] parm) {
        if (parm.length < 4) {
            return;
        }
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        String inWndName = parm[0];
        String inMultiPopName = parm[1];
        String inPopName = parm[2];
        String inScript = parm[3];
        curWind.evaluateMultiPopoverScript(mBrwView, inWndName, inMultiPopName, inPopName, SCRIPT_HEADER + inScript);
    }

    public void evaluatePopoverScript(String[] parm) {
        if (parm.length < 3) {
            return;
        }
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        String inWndName = parm[0];
        String inPopName = parm[1];
        String inScript = parm[2];
        curWind.evaluatePopoverScript(mBrwView, inWndName, inPopName, SCRIPT_HEADER + inScript);
    }

    public void bringToFront(String[] parm) {
        if (!mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_POP)) {
            return;
        }
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_BRINGTOFRONT;
        mHandler.sendMessage(msg);
    }

    public void sendToBack(String[] parm) {
        if (!mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_POP)) {
            return;
        }
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_SENDTOBACK;
        mHandler.sendMessage(msg);
    }

    public void insertAbove(String[] parm) {
        if (!mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_POP) || parm.length < 1) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_INSERTABOVE;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void insertAboveMsg(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        String popName = parm[0];
        curWind.insertAbove(mBrwView, popName);
    }

    public void insertBelow(String[] parm) {
        if (!mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_POP) || parm.length < 1) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_INSERTBELOW;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void insertBelowMsg(String[] parm) {
        String popName = parm[0];
        mBrwView.getBrowserWindow().insertBelow(mBrwView, popName);
    }

    public void insertPopoverAbovePopover(String[] parm) {
        if (!mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_MAIN)
                || parm.length < 2) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_INSERTPOPOVERABOVEPOPOVER;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void insertPopoverAbovePopoverMsg(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        String popName1 = parm[0];
        String popName2 = parm[1];
        curWind.insertPopoverAbovePopover(popName1, popName2);
    }

    public void insertPopoverBelowPopover(String[] parm) {
        if (!mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_MAIN)
                || parm.length < 2) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_INSERTPOPOVERBELOWPOPOVER;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void insertPopoverBelowPopoverMsg(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        String popName1 = parm[0];
        String popName2 = parm[1];
        curWind.insertPopoverBelowPopover(popName1, popName2);
    }

    public void bringPopoverToFront(String[] parm) {
        if (!mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_MAIN)
                || parm.length < 1) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_BRINGPOPOVERTOFRONT;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void bringPopoverToFrontMsg(String[] parm) {
        String popName = parm[0];
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        curWind.bringPopoverToFront(popName);
    }

    public void sendPopoverToBack(String[] parm) {
        if (!mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_MAIN)
                || parm.length < 1) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_SENDPOPOVERTOBACK;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void sendPopoverToBackMsg(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        String popName = parm[0];
        curWind.sendPopoverToBack(popName);
    }

    public void insertWindowAboveWindow(String[] parm) {
        if (!mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_MAIN)
                || parm.length < 2) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_INSERTWINDOWABOVEWINDOW;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void insertWindowAboveWindowMsg(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        EBrowserWidget curWgt = curWind.getEBrowserWidget();
        String wName1 = parm[0];
        String wName2 = parm[1];
        curWgt.insertWindowAboveWindow(wName1, wName2);
    }

    public void insertWindowBelowWindow(String[] parm) {
        if (!mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_MAIN)
                || parm.length < 2) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_INSERTWINDOWBELOWWINDOW;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void insertWindowBelowWindowMsg(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        EBrowserWidget curWgt = curWind.getEBrowserWidget();
        String wName1 = parm[0];
        String wName2 = parm[1];
        curWgt.insertWindowBelowWindow(wName1, wName2);
    }

    public void setWindowHidden(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_SET_WINDOW_HIDDEN;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void setWindowHiddenMsg(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        int flag = 0;
        boolean curPopOver = false;
        String popName = null;
        if (mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_POP)) {
            curPopOver = true;
            popName = mBrwView.getName();
        }
        try {
            flag = Integer.parseInt(parm[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        EViewEntry entry = new EViewEntry();
        entry.bArg1 = curPopOver;
        entry.arg1 = popName;
        entry.flag = flag;
        curWind.setWindowHidden(entry);
    }

    public void beginAnimition(String[] parm) {
        if (!mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_POP)) {
            return;
        }
        mBrwView.beginAnimition();
    }

    public void setAnimitionDelay(String[] parm) {
        if (!mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_POP)) {
            return;
        }
        long delay = 0;
        try {
            if (parm[0] != null && parm[0].length() > 0) {
                delay = Long.parseLong(parm[0]);
            }
        } catch (Exception e) {
            ;
        }
        mBrwView.setAnimitionDelay(delay);
    }

    public void setAnimitionDuration(String[] parm) {
        if (!mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_POP)) {
            return;
        }
        long duration = 250;
        try {
            if (parm[0] != null && parm[0].length() > 0) {
                duration = Long.parseLong(parm[0]);
            }
        } catch (Exception e) {
            ;
        }
        mBrwView.setAnimitionDuration(duration);
    }

    public void setAnimitionCurve(String[] parm) {
        if (!mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_POP)) {
            return;
        }
        int curve = 0;
        try {
            if (parm[0] != null && parm[0].length() > 0) {
                curve = Integer.parseInt(parm[0]);
            }
        } catch (Exception e) {
            ;
        }
        mBrwView.setAnimitionCurve(curve);
    }

    public void setAnimitionRepeatCount(String[] parm) {
        if (!mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_POP)) {
            return;
        }
        int count = 0;
        try {
            if (parm[0] != null && parm[0].length() > 0) {
                count = Integer.parseInt(parm[0]);
            }
        } catch (Exception e) {
            ;
        }
        mBrwView.setAnimitionRepeatCount(count);
    }

    public void setAnimitionAutoReverse(String[] parm) {
        if (!mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_POP)) {
            return;
        }
        boolean auto = false;
        try {
            if (parm[0] != null && parm[0].length() > 0) {
                int value = Integer.parseInt(parm[0]);
                auto = value == 0 ? false : true;
            }
        } catch (Exception e) {
            ;
        }
        mBrwView.setAnimitionAutoReverse(auto);
    }

    public void makeTranslation(String[] parm) {
        if (!mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_POP)) {
            return;
        }
        float tx = 0, ty = 0, tz = 0;
        try {
            if (parm[0] != null && parm[0].length() > 0) {
                tx = Float.parseFloat(parm[0]);
            }
            if (parm[1] != null && parm[1].length() > 0) {
                ty = Float.parseFloat(parm[1]);
            }
            if (parm[2] != null && parm[2].length() > 0) {
                tz = Float.parseFloat(parm[2]);
            }
        } catch (Exception e) {
            ;
        }
        float nowScale = 1.0f;

        int versionA = Build.VERSION.SDK_INT;

        if (versionA <= 18) {
            nowScale = mBrwView.getScale();
        }

        float sc = nowScale;
        tx = tx * sc;
        ty = ty * sc;
        mBrwView.makeTranslation(tx, ty, tz);
    }

    public void makeScale(String[] parm) {
        if (!mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_POP)) {
            return;
        }
        float tx = 1.0f, ty = 1.0f, tz = 1.0f;
        try {
            if (parm[0] != null && parm[0].length() > 0) {
                float x = Float.parseFloat(parm[0]);
                if (x != 0) {
                    tx = x;
                }
            }
            if (parm[1] != null && parm[1].length() > 0) {
                float y = Float.parseFloat(parm[1]);
                if (y != 0) {
                    ty = y;
                }
            }
            if (parm[2] != null && parm[2].length() > 0) {
                float z = Float.parseFloat(parm[2]);
                if (z != 0) {
                    tz = z;
                }
            }
        } catch (Exception e) {
            ;
        }
        mBrwView.makeScale(tx, ty, tz);
    }

    public void makeRotate(String[] parm) {
        if (!mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_POP)) {
            return;
        }
        float fd = 0, px = 0, py = 0, pz = 0;
        try {
            if (parm[0] != null && parm[0].length() > 0) {
                fd = Float.parseFloat(parm[0]);
            }
            if (parm[1] != null && parm[1].length() > 0) {
                px = Float.parseFloat(parm[1]);
            }
            if (parm[2] != null && parm[2].length() > 0) {
                py = Float.parseFloat(parm[2]);
            }
            if (parm[3] != null && parm[3].length() > 0) {
                pz = Float.parseFloat(parm[3]);
            }
        } catch (Exception e) {
            ;
        }
        mBrwView.makeRotate(fd, px, py, pz);
    }

    public void makeAlpha(String[] parm) {
        if (!mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_POP)) {
            return;
        }
        float fa = 0;
        try {
            if (parm[0] != null && parm[0].length() > 0) {
                fa = Float.parseFloat(parm[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mBrwView.makeAlpha(fa);
    }

    public void commitAnimition(String[] parm) {
        if (!mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_POP)) {
            return;
        }
        mBrwView.commitAnimition();
    }

    public void openAd(String[] params) {
        if (params == null || params.length < 4) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_OPEN_AD;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void openAdMsg(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        WWidgetData wd = mBrwView.getCurrentWidget();
        boolean b1 = mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_MAIN);
        boolean b2 = 0 == wd.m_widgetAdStatus;
        boolean b3 = parm.length < 4;
        if (!b1 || b2 || b3) {
            // 0 means do not show ad
            return;
        }
        String inType = parm[0];
        String inDTime = parm[1];
        String inInterval = parm[2];
        String inFlag = parm[3];
        int type = 0, flag = 0, dtime = 0, interval = 0, w = RelativeLayout.LayoutParams.FILL_PARENT, h = 50;
        MessageDigest md = null;
        int density = ESystemInfo.getIntence().mDensityDpi;
        switch (density) {
            case DisplayMetrics.DENSITY_LOW:
                h = 40;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                h = 50;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                h = 60;
                break;
            case 320: // DisplayMetrics.DENSITY_XHIGH from 2.3.3
                h = 70;
                break;
        }
        try {
            if (null != inType && inType.length() != 0) {
                type = Integer.parseInt(inType);
            }

            if (null != inDTime && inDTime.length() != 0) {
                dtime = Integer.parseInt(inDTime);
            }

            if (null != inInterval && inInterval.length() != 0) {
                interval = Integer.parseInt(inInterval);
            }

            if (null != inFlag && inFlag.length() != 0) {
                flag = Integer.parseInt(inFlag);
            }
            md = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            errorCallback(0, EUExCallback.F_E_UEXWINDOW_EVAL, "Illegal parameter");
            return;
        }
        StringBuffer sb = new StringBuffer(m_AdUrl);
        sb.append("?appid=");
        sb.append(wd.m_appId);
        sb.append("&pt=1");
        sb.append("&dw=");
        sb.append(ESystemInfo.getIntence().mWidthPixels);
        sb.append("&dh=");
        sb.append(ESystemInfo.getIntence().mHeightPixels);
        sb.append("&md5=");
        if (null == md) {
            return;
        }
        String jid = wd.m_appId + "BD7463CD-D608-BEB4-C633-EF3574213060";
        md.reset();
        md.update(jid.getBytes());
        byte[] md5Bytes = md.digest();
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        sb.append(hexValue);
        sb.append("&type=");
        if (type == 1) {
            sb.append(1);
            h = w;
        } else {
            sb.append(0);
        }
        String url = sb.toString();
        curWind.openAd(type, url, dtime * 1000, h, w, interval * 1000, flag);
    }

    public void loadObfuscationData(String[] parm) {
        if (parm.length < 1) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_LOADOBFUSCATIONDATA;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void loadObfuscationDataMsg(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        String inUrl = parm[0];
        curWind.onLoadObfuscationData(inUrl);
    }

    public void back(String[] parm) {
        if (mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_POP)) {
            return;
        }
        EBrowserWindow wind = mBrwView.getBrowserWindow();
        if (null == wind) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_BACK;
        mHandler.sendMessage(msg);
    }

    public void forward(String[] parm) {
        if (mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_POP)) {
            return;
        }
        EBrowserWindow wind = mBrwView.getBrowserWindow();
        if (null == wind) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_FORWARD;
        mHandler.sendMessage(msg);
    }

    public void pageBack(String[] parm) {
        int state = 1;
        boolean can = mBrwView.canGoBack();
        state = can ? 1 : 0;
        if (can) {
            Message msg = new Message();
            msg.obj = this;
            msg.what = MSG_FUNCTION_PAGEBACK;
            mHandler.sendMessage(msg);
        }
        jsCallback(function_pageBack, 0, EUExCallback.F_C_INT, state);
        // if (mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_POP)) {
        // return;
        // }
        // EBrowserWindow wind = mBrwView.getBrowserWindow();
        // if(null == wind){
        // return;
        // }
        // wind.goBack();
    }

    public void pageForward(String[] parm) {
        int state = 1;
        boolean can = mBrwView.canGoForward();
        state = can ? 1 : 0;
        if (can) {
            Message msg = new Message();
            msg.obj = this;
            msg.what = MSG_FUNCTION_PAGEFORWARD;
            mHandler.sendMessage(msg);
        }
        jsCallback(function_pageForward, 0, EUExCallback.F_C_INT, state);
        // if (mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_POP)) {
        // return;
        // }
        // EBrowserWindow wind = mBrwView.getBrowserWindow();
        // if(null == wind){
        // return;
        // }
        // wind.goForward();
    }

    public void setReportKey(String[] parm) {
        if (parm.length < 2) {
            return;
        }
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        String inKeyCode = parm[0];
        String inEnable = parm[1];
        try {
            int code = Integer.parseInt(inKeyCode);
            int is = Integer.parseInt(inEnable);
            boolean flag = false;
            if (is == EUExCallback.F_C_TRUE) {
                flag = true;
            }
            if (EUExCallback.F_C_Key_Back == code) {
                curWind.setLockBackKey(flag);
            } else if (EUExCallback.F_C_Key_Menu == code) {
                curWind.setLockMenuKey(flag);
            }
        } catch (Exception e) {
            errorCallback(0, EUExCallback.F_E_UEXWINDOW_SETKEY, "Illegal parameter");
            return;
        }
    }

    public void setSwipeRate(String[] param) {
        if (param.length < 1) {
            return;
        }
        String slop = param[0];
        int swipe = -1;
        try {
            swipe = Integer.parseInt(slop);
        } catch (Exception e) {
            ;
        }
        if (swipe > 0) {
            ESystemInfo.getIntence().mSwipeRate = swipe;
        }
    }

    public void setSwipeCloseEnable(String[] param) {
        SetSwipeCloseEnableVO input = DataHelper.gson.fromJson(param[0], SetSwipeCloseEnableVO.class);
        if (input != null) {
            EBrowserWindow curWindow = mBrwView.getBrowserWindow();
            curWindow.setSwipeEnabled(input.getEnable() == 1);
        }
    }

    public void windowBack(String[] parm) {
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_WINDOWBACK;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void windowBackMsg(String[] parm) {
        String inAnimitionID = null;
        String animDuration = null;
        switch (parm.length) {
            case 0:
                break;
            case 1:
                inAnimitionID = parm[0];
                break;
            case 2:
                inAnimitionID = parm[0];
                animDuration = parm[1];
                break;

        }
        int animId = EBrowserAnimation.ANIM_ID_FILL;
        long duration = EBrowserAnimation.defaultDuration;
        try {
            if (null != inAnimitionID && inAnimitionID.length() != 0) {
                animId = Integer.parseInt(inAnimitionID);
            }

            if (null != animDuration && animDuration.length() != 0) {
                duration = Long.parseLong(animDuration);
            }
        } catch (Exception e) {
            errorCallback(0, EUExCallback.F_E_UEXWINDOW_WBACK, "Illegal parameter");
            return;
        }
        mBrwView.getBrowserWindow().windowGoBack(animId, duration);
    }

    public void putLocalData(String[] params) {
        SpManager.getInstance().putString(params[0],params[1]);
    }

    public String getLocalData(String[] params) {
        return SpManager.getInstance().getString(params[0], "");
    }

    public void windowForward(String[] parm) {
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_WINDOWFORWARD;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void windowForwardMsg(String[] parm) {
        String inAnimitionID = null;
        String animDuration = null;
        switch (parm.length) {
            case 0:
                break;
            case 1:
                inAnimitionID = parm[0];
                break;
            case 2:
                inAnimitionID = parm[0];
                animDuration = parm[1];
                break;

        }
        int animId = EBrowserAnimation.ANIM_ID_NONE;
        long duration = EBrowserAnimation.defaultDuration;
        try {
            if (null != inAnimitionID && inAnimitionID.length() != 0) {
                animId = Integer.parseInt(inAnimitionID);
            }

            if (null != animDuration && animDuration.length() != 0) {
                duration = Long.parseLong(animDuration);
            }
        } catch (Exception e) {
            errorCallback(0, EUExCallback.F_E_UEXWINDOW_WFORWARD, "Illegal parameter");
            return;
        }
        mBrwView.getBrowserWindow().windowGoForward(animId, duration);
    }

    public void getBounce(String[] parm) {
        mBrwView.getBounce();
    }

    public void setBounce(String[] parm) {
        if (parm.length < 1) {
            return;
        }
        int isBounce = 0;
        try {
            isBounce = Integer.parseInt(parm[0]);
        } catch (Exception e) {
            ;
        }
        mBrwView.setBounce(isBounce);
    }

    public void notifyBounceEvent(String[] parm) {
        if (parm.length < 2) {
            return;
        }
        String inType = parm[0];
        String inStatus = parm[1];
        int type = 0;
        int status = 0;
        try {
            type = Integer.parseInt(inType);
            status = Integer.parseInt(inStatus);
        } catch (Exception e) {
            return;
        }
        mBrwView.notifyBounceEvent(type, status);
    }

    public String getWindowName(String[] params){
        return mBrwView.getWindowName();
    }

    public void showBounceView(String[] parm) {
        if (parm.length < 3) {
            return;
        }
        String inType = parm[0];
        String inColor = parm[1];
        String inFlag = parm[2];
        int type = 0;
        int flag = 0;
        try {
            type = Integer.parseInt(inType);
            flag = Integer.parseInt(inFlag);
        } catch (Exception e) {
            return;
        }
        mBrwView.showBounceView(type, inColor, flag);
    }

    public void resetBounceView(String[] parm) {
        if (parm.length < 1) {
            return;
        }
        String inType = parm[0];
        int type = 0;
        try {
            type = Integer.parseInt(inType);
        } catch (Exception e) {
            return;
        }
        mBrwView.resetBounceView(type);
    }

    public void setBounceParams(String[] parm) {
        if (parm.length < 2) {
            return;
        }
        String inType = parm[0];
        String inJson = parm[1];
        String inGuestId = null;
        if (3 == parm.length) {
            inGuestId = parm[2];
        }
        int type = -1;
        JSONObject json = null;
        try {
            type = Integer.parseInt(inType);
            json = new JSONObject(inJson);
        } catch (Exception e) {
            return;
        }
        mBrwView.setBounceParams(type, json, inGuestId);
    }

    public void hiddenBounceView(String[] parm) {
        if (parm.length < 1) {
            return;
        }
        String inType = parm[0];
        int type = 0;
        try {
            type = Integer.parseInt(inType);
        } catch (Exception e) {
            return;
        }
        mBrwView.hiddenBounceView(type);
    }

    public void topBounceViewRefresh(String[] parm) {
        if (!mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_MAIN)) {
            mBrwView.topBounceViewRefresh();
        }
    }

    public void alert(String[] parm) {
        if (parm.length < 3) {
            return;
        }
        EBrowserWindow wind = mBrwView.getBrowserWindow();
        if (null == wind) {
            return;
        }
        String inTitle = parm[0];
        String inMessage = parm[1];
        String inButtonLable = parm[2];
        EDialogTask task = new EDialogTask();
        task.type = EDialogTask.F_TYPE_ALERT;
        task.title = inTitle;
        task.msg = inMessage;
        task.defaultValue = inButtonLable;
        task.mUexWind = this;
        wind.addDialogTask(task);
    }

    public void confirm(String[] parm) {
        if (parm.length < 3) {
            return;
        }
        String inTitle = parm[0];
        String inMessage = parm[1];
        String[] inButtonLable = parm[2].split(",");
        EDialogTask task = new EDialogTask();
        task.type = EDialogTask.F_TYPE_CONFIRM;
        task.title = inTitle;
        task.msg = inMessage;
        task.buttonLables = inButtonLable;
        task.mUexWind = this;
        mBrwView.getBrowserWindow().addDialogTask(task);
    }

    public void prompt(String[] parm) {
        if (parm.length < 4) {
            return;
        }
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        String inTitle = parm[0];
        String inMessage = parm[1];
        String inDefaultValue = parm[2];
        String[] inButtonLables = parm[3].split(",");
        EDialogTask task = new EDialogTask();
        task.type = EDialogTask.F_TYPE_PROMPT;
        task.title = inTitle;
        task.msg = inMessage;
        task.defaultValue = inDefaultValue;
        task.buttonLables = inButtonLables;
        if (parm.length>4) {
            task.hint =parm[4];
        }
        task.mUexWind = this;
        curWind.addDialogTask(task);
    }

    public void toast(String[] parm) {
        if (parm.length < 4) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_TOAST;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void toastMsg(String[] parm) {
        String inType = parm[0];
        String inLocation = parm[1];
        String inMsg = parm[2];
        String inDuration = parm[3];
        int type = 0;
        int location = 0;
        int duration = 0;
        try {
            type = Integer.parseInt(inType);
            location = Integer.parseInt(inLocation);
            if (null != inDuration && 0 != inDuration.length()) {
                duration = Integer.parseInt(inDuration);
            }
        } catch (Exception e) {
            errorCallback(0, EUExCallback.F_E_UEXWINDOW_TOAST, "Illegal parameter");
        }
        mBrwView.getBrowserWindow().toast(type, location, inMsg, duration);
    }

    public void closeToast(String[] parm) {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_FUNCTION_CLOSETOAST;
        mHandler.sendMessage(msg);
    }

    public int getState(String[] parm) {
        int state = mBrwView.getBrowserWindow().isShown() ? 0 : 1;
        jsCallback(function_getState, 0, EUExCallback.F_C_INT, state);
        return state;
    }

    public String getUrlQuery(String[] parm) {
        String query = mBrwView.getQuery();
        jsCallback(function_getQuery, 0, EUExCallback.F_C_TEXT, query);
        return query;
    }

    public void private_alert(String inTitle, String inMessage, String inButtonLable) {
		/*if (!((EBrowserActivity) mContext).isVisable()) {
			return;
		}*/
        if (null != mAlert) {
            return;
        }
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(inTitle);
            builder.setMessage(inMessage);
            builder.setCancelable(false);
            builder.setPositiveButton(inButtonLable, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    mAlert = null;
                }
            });
            mAlert = builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void private_confirm(String inTitle, String inMessage, String[] inButtonLable) {
		/*if (!((EBrowserActivity) mContext).isVisable()) {
			return;
		}*/
        if (inButtonLable == null) {
            return;
        }
        //修复：多个appcan.window.alert()同时执行时，只会弹出一次的问题
//		if (null != mConfirm) {
//			return;
//		}
        try {
            int length = inButtonLable.length;
            if (length > 0 && length <= 3) {
                mConfirm = new AlertDialog.Builder(mContext);
                mConfirm.setTitle(inTitle);
                mConfirm.setMessage(inMessage);
                mConfirm.setCancelable(false);
                switch (length) {
                    case 1:
                        mConfirm.setPositiveButton(inButtonLable[0], new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                jsCallback(function_confirm, 0,
                                        EUExCallback.F_C_INT, 0);
                                dialog.dismiss();
                                mConfirm = null;
                            }
                        }).show();
                        break;
                    case 2:
                        mConfirm.setPositiveButton(inButtonLable[0],
                                new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        jsCallback(function_confirm, 0,
                                                EUExCallback.F_C_INT, 0);
                                        dialog.dismiss();
                                        mConfirm = null;
                                    }
                                })
                                .setNegativeButton(inButtonLable[1],
                                        new OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog,
                                                                int which) {
                                                jsCallback(function_confirm, 0,
                                                        EUExCallback.F_C_INT, 1);
                                                dialog.dismiss();
                                                mConfirm = null;
                                            }
                                        }).show();
                        break;
                    case 3:
                        mConfirm.setPositiveButton(inButtonLable[0],
                                new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        jsCallback(function_confirm, 0,
                                                EUExCallback.F_C_INT, 0);
                                        dialog.dismiss();
                                        mConfirm = null;
                                    }
                                });
                        mConfirm.setNeutralButton(inButtonLable[1],
                                new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        jsCallback(function_confirm, 0,
                                                EUExCallback.F_C_INT, 1);
                                        dialog.dismiss();
                                        mConfirm = null;
                                    }
                                });
                        mConfirm.setNegativeButton(inButtonLable[2],
                                new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        jsCallback(function_confirm, 0,
                                                EUExCallback.F_C_INT, 2);
                                        dialog.dismiss();
                                        mConfirm = null;
                                    }
                                }).show();
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void private_prompt(String inTitle, String inMessage, String inDefaultValue, String[] inButtonLables,
                               String hint) {
		/*if (!((EBrowserActivity) mContext).isVisable()) {
			return;
		}*/
        if (null != mPrompt) {
            return;
        }
        if (inButtonLables != null && inButtonLables.length == 2) {
            final JSONObject jsonObject = new JSONObject();
            mPrompt = PromptDialog.show(mContext, inTitle, inMessage, inDefaultValue,hint, inButtonLables[0], new
                    OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        final PromptDialog wPromptDialog = (PromptDialog) dialog;
                        hideSoftKeyboard(wPromptDialog.getWindowToken());
                        dialog.dismiss();
                        mPrompt = null;
                        jsonObject.put(EUExCallback.F_JK_NUM, 0);
                        jsonObject.put(EUExCallback.F_JK_VALUE, wPromptDialog.getInput());
                        jsCallback(function_prompt, 0, EUExCallback.F_C_JSON, jsonObject.toString());
                    } catch (Exception e) {
                        errorCallback(0, 0, e.toString());
                        e.printStackTrace();
                    }
                }
            }, inButtonLables[1], new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final PromptDialog wPromptDialog = (PromptDialog) dialog;
                    hideSoftKeyboard(wPromptDialog.getWindowToken());
                    dialog.dismiss();
                    mPrompt = null;
                    try {
                        jsonObject.put(EUExCallback.F_JK_NUM, 1);
                        jsonObject.put(EUExCallback.F_JK_VALUE, wPromptDialog.getInput());
                        jsCallback(function_prompt, 0, EUExCallback.F_C_JSON, jsonObject.toString());
                    } catch (Exception e) {
                        errorCallback(0, 0, e.toString());
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void hideSoftKeyboard(IBinder wToken) {
        try {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(wToken, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSoftKeyboard(String[] params) {
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_SHOW_SOFT_KEYBOARD;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void showSoftKeyboardMsg() {
        //boolean flag = mBrwView.hasFocus();
        //if(flag){
        mBrwView.getBrowserWindow().showSoftKeyboard();
        //}
    }

    public void hideSoftKeyboard(String[] params) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideSoftKeyboard(mBrwView.getWindowToken());
            }
        });
    }

    public void setWindowScrollbarVisible(String[] params) {
        if (null == params || params.length < 1)
            return;
        boolean visible = Boolean.parseBoolean(params[0]);
        mBrwView.setHorizontalScrollBarEnabled(visible);
        mBrwView.setVerticalScrollBarEnabled(visible);
    }

    public void actionSheet(String[] params) {
        if (params == null || params.length < 3) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_ACTION_SHEET;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void actionSheetMsg(String[] params) {
        final int length = params.length;
        boolean op = EBrowser.checkFlag(EBrowser.F_BRW_FLAG_OPENING);
        if (length < 3 || op) {
            return;
        }
        EBrowser.setFlag(EBrowser.F_BRW_FLAG_OPENING);
        String inTitle = params[0];
        String inCancel = params[1];
        final String[] btnLabels = params[2].split(",");
        ActionSheetDialog.show(mContext, btnLabels, inTitle, inCancel, new ActionSheetDialogItemClickListener() {

            @Override
            public void onItemClicked(ActionSheetDialog dialog, int postion) {
                jsCallback(function_actionSheet, 0, EUExCallback.F_C_INT, postion);
                EBrowser.clearFlag();
            }

            @Override
            public void onCanceled(ActionSheetDialog dialog) {
                jsCallback(function_actionSheet, 0, EUExCallback.F_C_INT, btnLabels.length);
                EBrowser.clearFlag();
            }
        });
    }

    public void statusBarNotification(String[] params) {
        if (params == null || params.length < 2) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_STATUS_BAR_NOTIFICATION;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void statusBarNotificationMsg(String[] parm) {
        if (parm.length < 2) {
            return;
        }
        String title = parm[0];
        String msg = parm[1];
        mBrwView.getBrowserWindow().getBrowser().systemNotification(title, msg);
    }

    private int parseHeight(String str) {
        if (null == str || 0 == str.length()) {
            return 0;
        }
        if (str.endsWith("%")) {
            int ph = ESystemInfo.getIntence().mHeightPixels;
            return (Integer.parseInt(str.replace("%", "")) * ph) / 100;
        }
        return Integer.parseInt(str);
    }

    private int parseWidth(String str) {
        if (null == str || 0 == str.length()) {
            return 0;
        }
        if (str.endsWith("%")) {
            int pw = ESystemInfo.getIntence().mWidthPixels;
            return (Integer.parseInt(str.replace("%", "")) * pw) / 100;
        }
        return Integer.parseInt(str);
    }

    private void closeAlert() {
        if (mAlert != null) {
            mAlert.dismiss();
            mAlert = null;
        }
    }

    @Override
    public boolean clean() {
        closeToast(null);
        closeAlert();
        // mBrwView.resetBounceView(0);
        // mBrwView.resetBounceView(1);
        destroyProgressDialog(null);
        return true;
    }

    public void createProgressDialog(String[] params) {
        if (params == null || params.length < 2) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_CREATE_PROGRESS_DIALOG;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void createProgressDialogMsg(String[] params) {
        if (params.length < 2) {
            return;
        }
        String title = params[0];
        String message = params[1];
        boolean isCancel = true;
        if (params.length > 2) {
            final String flag = params[2];
            if (!TextUtils.isEmpty(flag) && params[2].trim().length() != 0) {
                isCancel = flag.equals("0");
            }
        }
        mBrwView.getBrowserWindow().createProgressDialog(title, message, isCancel);
    }

    public void destroyProgressDialog(String[] params) {
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_DESTROY_PROGRESS_DIALOG;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void destroyProgressDialogMsg() {
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        curWind.destroyProgressDialog();
    }

    public void postGlobalNotification(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_POST_GLOBAL_NOTIFICATION;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void postGlobalNotificationMsg(String[] params) {
        if (params.length == 0) {
            return;
        }
        String des = params[0];
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        curWind.postGlobalNotification(des);
    }

    public void subscribeChannelNotification(String[] params) {
        if (params == null || params.length < 2) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_SUBSCRIBE_CHANNEL_NOTIFICATION;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void subscribeChannelNotificationMsg(String[] params) {
        if (params.length < 2) {
            return;
        }
        String channelId = params[0];
        if (TextUtils.isEmpty(channelId)) {
            BDebug.e("channelId is empty!!!");
            return;
        }
        String callbackFunction = params[1];
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        String type = null;
        String name = null;
        if (mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_POP) ||
                mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_TOP) ||
                mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_BOTTOM)) {
            type = EBrowserWindow.WIN_TYPE_POP;
            name = mBrwView.getName();
        }
        if (mBrwView.checkType(EBrwViewEntry.VIEW_TYPE_MAIN)) {
            type = EBrowserWindow.WIN_TYPE_MAIN;
            name = curWind.getName();
        }
        if (TextUtils.isEmpty(type)) return;
        curWind.subscribeChannelNotification(channelId, callbackFunction, type, name);
    }

    public void publishChannelNotification(String[] params) {
        if (params == null || params.length < 2) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_PUBLISH_CHANNEL_NOTIFICATION;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void publishChannelNotificationMsg(String[] params) {
        if (params.length < 2) {
            return;
        }
        String channelId = params[0];
        if (TextUtils.isEmpty(channelId)) {
            BDebug.e("channelId is empty!!!");
            return;
        }
        String des = params[1];
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        curWind.publishChannelNotification(channelId, des, false);
    }

    public void publishChannelNotificationForJson(String[] params) {
        if (params == null || params.length < 2) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_PUBLISH_CHANNEL_NOTIFICATION_FOR_JSON;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void publishChannelNotificationForJsonMsg(String[] params) {
        if (params.length < 2) {
            return;
        }
        String channelId = params[0];
        if (TextUtils.isEmpty(channelId)) {
            BDebug.e("channelId is empty!!!");
            return;
        }
        String des = params[1];
        EBrowserWindow curWind = mBrwView.getBrowserWindow();
        if (null == curWind) {
            return;
        }
        curWind.publishChannelNotification(channelId, des, true);
    }

    public void setMultilPopoverFlippingEnbaled(String[] params) {
        if (params.length < 1) {
            return;
        }
        int enabled = 0;
        try {
            enabled = Integer.parseInt(params[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mBrwView.setIsMultilPopoverFlippingEnbaled(enabled == 1 ? true : false);
    }

    public void setIsSupportSlideCallback(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_SET_IS_SUPPORT_SLIDE_CALLBACK;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private void setIsSupportSlideCallbackMsg(String[] params) {
        String json = params[0];
        try {
            JSONObject jsonObject = new JSONObject(json);
            boolean isSupport = Boolean.valueOf(jsonObject.getString("isSupport"));
            mBrwView.setIsSupportSlideCallback(isSupport);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setIsSupportSwipeCallback(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_SET_IS_SUPPORT_SWIPE_CALLBACK;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private void setIsSupportSwipeCallbackMsg(String[] params) {
        String json = params[0];
        try {
            JSONObject jsonObject = new JSONObject(json);
            boolean isSupport = Boolean.valueOf(jsonObject.getString("isSupport"));
            View bv = (View) mBrwView.getParent();
            if (bv != null && bv instanceof EBounceView) {
                ((EBounceView) bv).setIsSupportSwipeCallback(isSupport);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void disturbLongPressGesture(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_DISTURB_LONG_PRESS_GESTURE;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private void disturbLongPressGestureMsg(String[] params) {
        int disturb = Integer.parseInt(params[0]);
        mBrwView.setDisturbLongPressGesture(disturb == 0 ? false : true);
    }

    public void createPluginViewContainer(String[] parm) {
        Message msg = mHandler.obtainMessage();
        msg.what = MSG_PLUGINVIEW_CONTAINER_CREATE;
        msg.obj = this;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    /**
     * 添加一个容器
     *
     * @param params
     */
    private void createPluginViewContainerMsg(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        final CreateContainerVO inputVO = DataHelper.gson.fromJson(params[0], CreateContainerVO.class);

        EBrowserWindow mWindow = mBrwView.getBrowserWindow();
        int count = mWindow.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = mWindow.getChildAt(i);
            if (view instanceof ContainerViewPager) {
                ContainerViewPager pager = (ContainerViewPager) view;
                if (inputVO.getId().equals((String) pager.getContainerVO().getId())) {
                    return;
                }
            }//end instance
        }//end for

        ContainerViewPager containerViewPager = new ContainerViewPager(mContext, inputVO);
        ContainerAdapter containerAdapter = new ContainerAdapter(new
                Vector<FrameLayout>());
        containerViewPager.setAdapter(containerAdapter);
        containerViewPager.setOnPageChangeListener(new ContainerViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int index) {
				String js = SCRIPT_HEADER + "if("
						+ function_onPluginContainerPageChange + "){"
						+ function_onPluginContainerPageChange + "(" + inputVO.getId()
						+ "," + EUExCallback.F_C_INT + "," + index
						+ SCRIPT_TAIL;
				onCallback(js);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams((int) inputVO.getW(), (int) inputVO.getH());
        lp.leftMargin = (int) inputVO.getX();
        lp.topMargin = (int) inputVO.getY();
        if(mBrwView != null){
        	mBrwView.addViewToCurrentWindow(containerViewPager, lp);
            String js = SCRIPT_HEADER + "if(" + function_cbCreatePluginViewContainer + "){"
                    + function_cbCreatePluginViewContainer + "(" + inputVO.getId() + "," + EUExCallback.F_C_TEXT + ",'"
                    + "success" + "'" + SCRIPT_TAIL;
            onCallback(js);
        }
    }

    public void showPluginViewContainer(String[] parm) {
        Message msg = mHandler.obtainMessage();
        msg.what = MSG_PLUGINVIEW_CONTAINER_SHOW;
        msg.obj = this;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    /**
     * 显示隐藏的容器
     *
     * @param params
     */
    private void showPluginViewContainerMsg(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        try {
            JSONObject json = new JSONObject(params[0]);
            String opid = json.getString("id");

            EBrowserWindow mWindow = mBrwView.getBrowserWindow();
            int count = mWindow.getChildCount();
            for (int i = 0; i < count; i++) {
                View view = mWindow.getChildAt(i);
                if (view instanceof ContainerViewPager) {
                    ContainerViewPager pager = (ContainerViewPager) view;
                    if (opid.equals(pager.getContainerVO().getId())) {
                        pager.setVisibility(View.VISIBLE);
                        String js = SCRIPT_HEADER + "if(" + function_cbShowPluginViewContainer + "){"
                                + function_cbShowPluginViewContainer + "(" + opid + "," + EUExCallback.F_C_TEXT + ",'"
                                + "success" + "'" + SCRIPT_TAIL;
                        onCallback(js);
                        return;
                    }
                }//end instance
            }//end for
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hidePluginViewContainer(String[] parm) {
        Message msg = mHandler.obtainMessage();
        msg.what = MSG_PLUGINVIEW_CONTAINER_HIDE;
        msg.obj = this;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    /**
     * 隐藏显示的容器
     *
     * @param params
     */
    private void hidePluginViewContainerMsg(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        try {
            JSONObject json = new JSONObject(params[0]);
            String opid = json.getString("id");

            EBrowserWindow mWindow = mBrwView.getBrowserWindow();
            int count = mWindow.getChildCount();
            for (int i = 0; i < count; i++) {
                View view = mWindow.getChildAt(i);
                if (view instanceof ContainerViewPager) {
                    ContainerViewPager pager = (ContainerViewPager) view;
                    if (opid.equals(pager.getContainerVO().getId())) {
                        pager.setVisibility(View.GONE);
                        String js = SCRIPT_HEADER + "if(" + function_cbHidePluginViewContainer + "){"
                                + function_cbHidePluginViewContainer + "(" + opid + "," + EUExCallback.F_C_TEXT + ",'"
                                + "success" + "'" + SCRIPT_TAIL;
                        onCallback(js);
                        return;
                    }
                }//end instance
            }//end for
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closePluginViewContainer(String[] parm) {
        Message msg = mHandler.obtainMessage();
        msg.what = MSG_PLUGINVIEW_CONTAINER_CLOSE;
        msg.obj = this;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    /**
     * 移除一个容器
     *
     * @param params
     */
    private void closePluginViewContainerMsg(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        try {
            JSONObject json = new JSONObject(params[0]);
            String opid = json.getString("id");

            EBrowserWindow mWindow = mBrwView.getBrowserWindow();
            int count = mWindow.getChildCount();
            for (int i = 0; i < count; i++) {
                View view = mWindow.getChildAt(i);
                if (view instanceof ContainerViewPager) {
                    ContainerViewPager pager = (ContainerViewPager) view;
                    if (opid.equals(pager.getContainerVO().getId())) {
                        removeViewFromCurrentWindow(pager);
                        ContainerAdapter adapter = (ContainerAdapter) pager.getAdapter();
                        Vector<FrameLayout> views = adapter.getViewList();
                        int size = views.size();
                        for (int j = 0; j < size; j++) {
                            views.get(j).removeAllViews();
                        }
                        views.clear();
                        pager = null;
                        String js = SCRIPT_HEADER + "if(" + function_cbClosePluginViewContainer + "){"
                                + function_cbClosePluginViewContainer + "(" + opid + "," + EUExCallback.F_C_TEXT + ",'"
                                + "success" + "'" + SCRIPT_TAIL;
                        onCallback(js);
                        return;
                    }
                }//end instance
            }//end for
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPageInContainer(String[] parm) {
        Message msg = mHandler.obtainMessage();
        msg.what = MSG_PLUGINVIEW_CONTAINER_SET;
        msg.obj = this;
        Bundle bd = new Bundle();
        bd.putStringArray(TAG_BUNDLE_PARAM, parm);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private void setPageInContainerMsg(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        try {
            JSONObject json = new JSONObject(params[0].toString());
            String opid = json.getString("id");

            EBrowserWindow mWindow = mBrwView.getBrowserWindow();
            int count = mWindow.getChildCount();
            for (int i = 0; i < count; i++) {
                View view = mWindow.getChildAt(i);
                if (view instanceof ContainerViewPager) {
                    ContainerViewPager pager = (ContainerViewPager) view;
                    if (opid.equals((String) pager.getContainerVO().getId())) {
                        int index = json.optInt("index");
                        pager.setCurrentItem(index);
                        return;
                    }
                }//end instance
            }//end for
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ContainerViewPager extends ViewPager {
        private CreateContainerVO mContainerVO;

        public ContainerViewPager(Context context, CreateContainerVO containerVO) {
            super(context);
            this.mContainerVO = containerVO;
        }

        public CreateContainerVO getContainerVO() {
            return mContainerVO;
        }
    }

    class ContainerAdapter extends PagerAdapter {
        Vector<FrameLayout> viewList;
        int mChildCount = 0;

        public ContainerAdapter(Vector<FrameLayout> viewList) {
            this.viewList = viewList;
        }

        public Vector<FrameLayout> getViewList() {
            return viewList;
        }

        public void setViewList(Vector<FrameLayout> viewList) {
            this.viewList = viewList;
        }

        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object arg1) {
            return view == arg1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(viewList.get(position));
            return viewList.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            if (mChildCount > 0) {
                mChildCount--;
                return POSITION_NONE;
            }
            return super.getItemPosition(object);
        }

        @Override
        public void notifyDataSetChanged() {
            mChildCount = getCount();
            super.notifyDataSetChanged();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View item = viewList.get(position);
            container.removeView(item);
        }
    }


    public void share(String[] params){
        String jsonStr=params[0];
        ShareInputVO inputVO=DataHelper.gson.fromJson(jsonStr,ShareInputVO.class);
        if (!TextUtils.isEmpty(inputVO.getImgPath())){
            inputVO.setImgPath(BUtility.getRealPathWithCopyRes(mBrwView,inputVO.getImgPath()));
        }
        if (inputVO.getImgPaths()!=null){
            List<String> realImagePaths=new ArrayList<String>();
            for (String path:inputVO.getImgPaths()){
                realImagePaths.add(BUtility.getRealPathWithCopyRes(mBrwView,path));
            }
            inputVO.setImgPaths(realImagePaths);
        }
        EUtil.share(mContext,inputVO);
    }

    @Override
    public void onHandleMessage(Message msg) {
        if (mBrwView == null || mBrwView.getBrowserWindow() == null || msg == null) {
            return;
        }
        EBrowserWindow eBrwWin = mBrwView.getBrowserWindow();
        String[] param = msg.getData().getStringArray(TAG_BUNDLE_PARAM);
        switch (msg.what) {
            case MSG_FUNCTION_CLOSE:
                if (param != null) closeMsg(param);
                break;
            case MSG_FUNCTION_CLOSE_ABOVE_WND_BY_NAME:
                String windowName = msg.getData().getString(TAG_BUNDLE_PARAM_NAME);
                eBrwWin.closeAboveWndByName(windowName);
                break;
            case MSG_FUNCTION_OPEN:
                if (param != null) openMsg(param);
                break;
            case MSG_FUNCTION_OPEN_POP:
                if (param != null) openPopoverMsg(param);
                break;
            case MSG_FUNCTION_FORWARD:
                eBrwWin.goForward();
                break;
            case MSG_FUNCTION_BACK:
                eBrwWin.goBack();
                break;
            case MSG_FUNCTION_PAGEFORWARD:
                mBrwView.goForward();
                break;
            case MSG_FUNCTION_PAGEBACK:
                mBrwView.goBack();
                break;
            case MSG_FUNCTION_WINDOWFORWARD:
                if (param != null) windowForwardMsg(param);
                break;
            case MSG_FUNCTION_WINDOWBACK:
                if (param != null) windowBackMsg(param);
                break;
            case MSG_FUNCTION_SETWINDOWFRAME:
                if (param != null) setWindowFrameMsg(param);
                break;
            case MSG_FUNCTION_OPENSLIBING:
                if (param != null) openSlibingMsg(param);
                break;
            case MSG_FUNCTION_CLOSESLIBING:
                if (param != null) closeSlibingMsg(param);
                break;
            case MSG_FUNCTION_SHOWSLIBING:
                if (param != null) showSlibingMsg(param);
                break;
            case MSG_FUNCTION_LOADOBFUSCATIONDATA:
                if (param != null) loadObfuscationDataMsg(param);
                break;
            case MSG_FUNCTION_TOAST:
                if (param != null) toastMsg(param);
                break;
            case MSG_FUNCTION_CLOSETOAST:
                eBrwWin.closeToast();
                break;
            case MSG_FUNCTION_CLOSEPOPOVER:
                if (param != null) closePopoverMsg(param);
                break;
            case MSG_FUNCTION_SETPOPOVERFRAME:
                if (param != null) setPopoverFrameMsg(param);
                break;
            case MSG_FUNCTION_OPENMULTIPOPOVER:
                if (param != null) openMultiPopoverMsg(param);
                break;
            case MSG_FUNCTION_CLOSEMULTIPOPOVER:
                if (param != null) closeMultiPopoverMsg(param);
                break;
            case MSG_FUNCTION_SETMULTIPOPOVERFRAME:
                if (param != null) setMultiPopoverFrameMsg(param);
                break;
            case MSG_FUNCTION_SETSELECTEDPOPOVERINMULTIWINDOW:
                if (param != null) setSelectedPopOverInMultiWindowMsg(param);
                break;
            case MSG_FUNCTION_BRINGTOFRONT:
                eBrwWin.bringToFront(mBrwView);
                break;
            case MSG_FUNCTION_SENDTOBACK:
                eBrwWin.sendToBack(mBrwView);
                break;
            case MSG_FUNCTION_INSERTABOVE:
                if (param != null) insertAboveMsg(param);
                break;
            case MSG_FUNCTION_INSERTBELOW:
                if (param != null) insertBelowMsg(param);
                break;
            case MSG_FUNCTION_BRINGPOPOVERTOFRONT:
                if (param != null) bringPopoverToFrontMsg(param);
                break;
            case MSG_FUNCTION_SENDPOPOVERTOBACK:
                if (param != null) sendPopoverToBackMsg(param);
                break;
            case MSG_FUNCTION_INSERTPOPOVERABOVEPOPOVER:
                if (param != null) insertPopoverAbovePopoverMsg(param);
                break;
            case MSG_FUNCTION_INSERTPOPOVERBELOWPOPOVER:
                if (param != null) insertPopoverBelowPopoverMsg(param);
                break;
            case MSG_FUNCTION_INSERTWINDOWABOVEWINDOW:
                if (param != null) insertWindowAboveWindowMsg(param);
                break;
            case MSG_FUNCTION_INSERTWINDOWBELOWWINDOW:
                if (param != null) insertWindowBelowWindowMsg(param);
                break;
            case MSG_FUNCTION_SETORIENTATION:
                if (param != null) setOrientationMsg(param);
                break;
            case MSG_FUNCTION_SETAUTOROTATEENABLE:
                if(param != null) setAutorotateEnableMsg(param);
                break;
            case MSG_FUNCTION_SETSLIDINGWIN:
                handleSetSlidingWin(param);
                break;
            case MSG_FUNCTION_SETSLIDINGWIN_ENABLE:
                hanldeSetSlidingWindowEnabled(param);
                break;
            case MSG_FUNCTION_TOGGLE_SLIDINGWIN:
                hanldeToggleSlidingWindow(param);
                break;
            case MSG_FUNCTION_GET_SLIDING_WINDOW_STATE:
                hanldeGetSlidingWindowState();
                break;
            case MSG_FUNCTION_REFRESH:
                String url = mBrwView.getRelativeUrl();
                mBrwView.loadUrl(url);
                mBrwView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBrwView.clearHistory();
                    }
                }, 1000);
                break;
            case MSG_FUNCTION_RELOAD:
                mBrwView.reload();
                break;
            case MSG_SET_WINDOW_HIDDEN:
                setWindowHiddenMsg(param);
                break;
            case MSG_OPEN_AD:
                openAdMsg(param);
                break;
            case MSG_SHOW_SOFT_KEYBOARD:
                showSoftKeyboardMsg();
                break;
            case MSG_ACTION_SHEET:
                actionSheetMsg(param);
                break;
            case MSG_STATUS_BAR_NOTIFICATION:
                statusBarNotificationMsg(param);
                break;
            case MSG_CREATE_PROGRESS_DIALOG:
                createProgressDialogMsg(param);
                break;
            case MSG_DESTROY_PROGRESS_DIALOG:
                destroyProgressDialogMsg();
                break;
            case MSG_POST_GLOBAL_NOTIFICATION:
                postGlobalNotificationMsg(param);
                break;
            case MSG_SUBSCRIBE_CHANNEL_NOTIFICATION:
                subscribeChannelNotificationMsg(param);
                break;
            case MSG_PUBLISH_CHANNEL_NOTIFICATION:
                publishChannelNotificationMsg(param);
                break;
            case MSG_PUBLISH_CHANNEL_NOTIFICATION_FOR_JSON:
                publishChannelNotificationForJsonMsg(param);
                break;
            case MSG_SET_IS_SUPPORT_SLIDE_CALLBACK:
                setIsSupportSlideCallbackMsg(param);
                break;
            case MSG_SET_IS_SUPPORT_SWIPE_CALLBACK:
                setIsSupportSwipeCallbackMsg(param);
                break;
            case MSG_DISTURB_LONG_PRESS_GESTURE:
                disturbLongPressGestureMsg(param);
                break;
            case MSG_PLUGINVIEW_CONTAINER_CREATE:
                createPluginViewContainerMsg(param);
                break;
            case MSG_PLUGINVIEW_CONTAINER_CLOSE:
                closePluginViewContainerMsg(param);
                break;
            case MSG_PLUGINVIEW_CONTAINER_SET:
                setPageInContainerMsg(param);
                break;
            case MSG_PLUGINVIEW_CONTAINER_SHOW:
                showPluginViewContainerMsg(param);
                break;
            case MSG_PLUGINVIEW_CONTAINER_HIDE:
                hidePluginViewContainerMsg(param);
                break;
            default:
                break;
        }
    }

}
