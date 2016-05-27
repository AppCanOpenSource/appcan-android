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

package org.zywx.wbpalmstar.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.universalex.EUExWidget.SpaceClickListener;
import org.zywx.wbpalmstar.platform.myspace.MySpaceView;
import org.zywx.wbpalmstar.widgetone.WidgetOneApplication;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class EBrowser {

    public static final int F_BRW_FLAG_NONE = 0x0;
    public static final int F_BRW_FLAG_OPENING = 0x1;

    public static int webview_count = 0;

    private Context mContext;
    private EBrowserWidgetPool mBrwWindPol;
    private ENotification mNotifyMgr;
    private static int mflag;
    private boolean mfromPush;
    private Map<String, Bitmap> mBgBitmapCache;
    private WidgetOneApplication mApp;

    public EBrowser(Context context) {
        mContext = context;
        mApp = (WidgetOneApplication) mContext.getApplicationContext();
    }

    public void init(EBrowserWidgetPool eBrwWidPo) {
        mBrwWindPol = eBrwWidPo;
        webview_count = 0;
    }

    public static void setFlag(int flag) {
        mflag |= flag;
    }

    public static void clearFlag() {
        mflag &= F_BRW_FLAG_NONE;
    }

    public static boolean checkFlag(int flag) {

        return (mflag & flag) != 0;
    }

    public static int assignCountID() {
        int c = webview_count;
        webview_count++;
        return c;
    }

    public void dumpPageInfo(int type) {
        mBrwWindPol.dumpPageInfo(type);
    }

    public WWidgetData getRootWidget() {

        return mBrwWindPol.getRootWidget();
    }

    public EWidgetStack getWidgetStack() {
        return mBrwWindPol.getWidgetStack();
    }

    public boolean isFromPush() {
        return mfromPush;
    }

    public void setFromPush(boolean flag) {

        mfromPush = flag;
    }

    protected void start() {
        if (null == mBrwWindPol) {
            return;
        }
        mBrwWindPol.start();
    }

    protected void clean() {
        if (null == mBrwWindPol) {
            return;
        }
        clearFlag();
        mBrwWindPol.clean();
    }

    protected void hiddenShelter() {
        if (null == mBrwWindPol) {
            return;
        }
        ((EBrowserActivity) mContext).setPageFinish(true);
        mBrwWindPol.notifyHiddenShelter();
    }

    protected void pushNotify(String appType) {
        if (null == mBrwWindPol) {
            return;
        }
        mBrwWindPol.pushNotify(appType);
    }

    public void uexOnAuthorize(String id) {
        if (null == mBrwWindPol) {
            return;
        }
        mBrwWindPol.uexOnAuthorize(id);
    }

    protected void showHover(boolean isInSubWidget) {
        if (null == mBrwWindPol) {
            return;
        }
        mBrwWindPol.showHover(isInSubWidget);
    }

    public EBrowserWidget getWidget(String app_id) {

        return mBrwWindPol.getWidget(app_id);
    }

    protected Bitmap getImage(String bgPath) {
        if (null == bgPath) {
            return null;
        }
        if (null == mBgBitmapCache) {
            mBgBitmapCache = new Hashtable<String, Bitmap>();
        }
        Bitmap result = mBgBitmapCache.get(bgPath);
        if (null != result) {
            return result;
        }
        InputStream in = null;
        try {
            if (null != bgPath && 0 != bgPath.length()) {
                if (bgPath.startsWith("/sdcard")) {
                    File file = new File(bgPath);
                    in = new FileInputStream(file);
                } else if (bgPath.startsWith("widget/")) {
                    AssetManager asm = mContext.getAssets();
                    in = asm.open(bgPath);
                } else if (bgPath.startsWith("/data/data")) {
                    File file = new File(bgPath);
                    in = new FileInputStream(file);
                } else {
                    File file = new File(bgPath);
                    in = new FileInputStream(file);
                }
                if (null != in) {
                    result = BitmapFactory.decodeStream(in);
                    in.close();
                    mBgBitmapCache.put(bgPath, result);
                    return result;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (Exception e) {
                    ;
                }
            }
        }
        return null;
    }

    public void systemNotification(String title, String msg) {
        if (null == mNotifyMgr) {
            mNotifyMgr = new ENotification(mContext);
        }
        mNotifyMgr.notification(title, msg);
    }

    public void systemNotificationCancel(int id) {
        if (null == mNotifyMgr) {
            mNotifyMgr = new ENotification(mContext);
        }
        mNotifyMgr.cancelOne(id);
    }

    protected boolean isSpaceShown() {
        if (null == mBrwWindPol) {
            return false;
        }
        return mBrwWindPol.isSpaceShown();
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        if (null == mBrwWindPol) {
            return;
        }
        mBrwWindPol.onConfigurationChanged(newConfig);
    }

    protected void goBack() {
        if (null == mBrwWindPol) {
            return;
        }
        mBrwWindPol.goBack();
    }

    protected void stopLoad() {
        if (null == mBrwWindPol) {
            return;
        }
        mBrwWindPol.stopLoad();
    }

    protected void refresh() {
        if (null == mBrwWindPol) {
            return;
        }
        mBrwWindPol.refresh();
    }

    public void onAppPause() {
        if (null == mBrwWindPol) {
            return;
        }
        mBrwWindPol.onAppPause();
    }

    public void onAppStop() {
        if (null == mBrwWindPol) {
            return;
        }
        mBrwWindPol.onAppStop();
    }

    public void onAppResume() {
        if (null == mBrwWindPol) {
            return;
        }
        mBrwWindPol.onAppResume();
    }

    public void onAppKeyPress(int keyCode) {
        if (null == mBrwWindPol) {
            return;
        }
        mBrwWindPol.onAppKeyPress(keyCode);
    }

    public boolean isLockBackKey() {
        if (null == mBrwWindPol) {
            return false;
        }
        return mBrwWindPol.isLockBackKey();
    }

    public boolean isLockMenuKey() {
        if (null == mBrwWindPol) {
            return false;
        }
        return mBrwWindPol.isLockMenuKey();
    }

    public MySpaceView getAppCenter() {
        if (null == mBrwWindPol) {
            return null;
        }
        return mBrwWindPol.getAppCentView();
    }

    public void setMySpaceInfo(String inForResult, String inAnimiId,
                               String inInfo) {
        if (null == mBrwWindPol) {
            return;
        }
        mBrwWindPol.setMySpaceInfo(inForResult, inAnimiId, inInfo);
    }

    public void startWidget(WWidgetData inData, EWgtResultInfo inResult) {
        if (null == mBrwWindPol) {
            return;
        }
        mBrwWindPol.startWidget(inData, inResult);
    }

    public void exitMySpace() {
        if (null == mBrwWindPol) {
            return;
        }
        mBrwWindPol.exitMySpace();
    }

    public void finishWidget(String inResultInfo, String appId, boolean isWgtBG) {
        if (null == mBrwWindPol) {
            return;
        }
        mBrwWindPol.finishWidget(inResultInfo, appId, isWgtBG);
    }

    protected void showWidget() {
        if (null == mBrwWindPol) {
            return;
        }
        mBrwWindPol.showWidget();
    }

    protected void goMySpace() {
        if (null == mBrwWindPol) {
            return;
        }
        mBrwWindPol.goMySpace();
    }

    public void windowOpenAnalytics(EBrowserWindow preWind, EBrwViewEntry entry) {
        if (!EBrowserActivity.analytics) {
            return;
        }
        String beEndUrl = preWind.getRelativeUrl();
        String beShowUrl = entry.mRelativeUrl;
        Map<String, EBrowserView> endPopTable = preWind.getAllPopOver();
        int size = endPopTable.size();
        String[] beEndPopupUrls = new String[size];
        if (size > 0) {
            Set<Entry<String, EBrowserView>> set = endPopTable.entrySet();
            int counter = 0;
            for (Map.Entry<String, EBrowserView> enry : set) {
                EBrowserView temp = enry.getValue();
                String url = temp.getRelativeUrl();
                beEndPopupUrls[counter] = url;
                counter++;
            }
        }
        mApp.disPatchWindowOpen(beEndUrl, beShowUrl, beEndPopupUrls);
    }

    public void windowCloseAnalytics(EBrowserWindow preWind,
                                     EBrowserWindow nexWindow) {
        if (!EBrowserActivity.analytics) {
            return;
        }
        String beEndUrl = nexWindow.getRelativeUrl();
        String beShowUrl = preWind.getRelativeUrl();
        Map<String, EBrowserView> showPopTable = preWind.getAllPopOver();
        int showSize = showPopTable.size();
        String[] beShowPopupUrls = new String[showSize];
        if (showSize > 0) {
            Set<Entry<String, EBrowserView>> set = showPopTable.entrySet();
            int counter = 0;
            for (Map.Entry<String, EBrowserView> enry : set) {
                EBrowserView temp = enry.getValue();
                String url = temp.getRelativeUrl();
                beShowPopupUrls[counter] = url;
                counter++;
            }
        }
        Map<String, EBrowserView> endPopTable = nexWindow.getAllPopOver();
        int endSize = endPopTable.size();
        String[] beEndPopupUrls = new String[endSize];
        if (endSize > 0) {
            Set<Entry<String, EBrowserView>> set = endPopTable.entrySet();
            int counter = 0;
            for (Map.Entry<String, EBrowserView> enry : set) {
                EBrowserView temp = enry.getValue();
                String url = temp.getRelativeUrl();
                beEndPopupUrls[counter] = url;
                counter++;
            }
        }
        mApp.disPatchWindowClose(beEndUrl, beShowUrl, beEndPopupUrls,
                beShowPopupUrls);
    }

    public void windowBackAnalytics(EBrowserWindow preWind,
                                    EBrowserWindow curWind) {
        if (!EBrowserActivity.analytics) {
            return;
        }
        String beEndUrl = curWind.getRelativeUrl();
        String beShowUrl = preWind.getRelativeUrl();
        Map<String, EBrowserView> showPopTable = preWind.getAllPopOver();
        int showSize = showPopTable.size();
        String[] beShowPopupUrls = new String[showSize];
        if (showSize > 0) {
            Set<Entry<String, EBrowserView>> set = showPopTable.entrySet();
            int counter = 0;
            for (Map.Entry<String, EBrowserView> enry : set) {
                EBrowserView temp = enry.getValue();
                String url = temp.getRelativeUrl();
                beShowPopupUrls[counter] = url;
                counter++;
            }
        }
        Map<String, EBrowserView> endPopTable = curWind.getAllPopOver();
        int endSize = endPopTable.size();
        String[] beEndPopupUrls = new String[endSize];
        if (endSize > 0) {
            Set<Entry<String, EBrowserView>> set = endPopTable.entrySet();
            int counter = 0;
            for (Map.Entry<String, EBrowserView> enry : set) {
                EBrowserView temp = enry.getValue();
                String url = temp.getRelativeUrl();
                beEndPopupUrls[counter] = url;
                counter++;
            }
        }
        mApp.disPatchWindowBack(beEndUrl, beShowUrl, beEndPopupUrls,
                beShowPopupUrls);
    }

    public void windowForwardAnalytics(EBrowserWindow curWind,
                                       EBrowserWindow nextWind) {
        if (!EBrowserActivity.analytics) {
            return;
        }
        String beEndUrl = curWind.getRelativeUrl();
        String beShowUrl = nextWind.getRelativeUrl();
        Map<String, EBrowserView> showPopTable = nextWind.getAllPopOver();
        int showSize = showPopTable.size();
        String[] beShowPopupUrls = new String[showSize];
        if (showSize > 0) {
            Set<Entry<String, EBrowserView>> set = showPopTable.entrySet();
            int counter = 0;
            for (Map.Entry<String, EBrowserView> enry : set) {
                EBrowserView temp = enry.getValue();
                String url = temp.getRelativeUrl();
                beShowPopupUrls[counter] = url;
                counter++;
            }
        }
        Map<String, EBrowserView> endPopTable = curWind.getAllPopOver();
        int endSize = endPopTable.size();
        String[] beEndPopupUrls = new String[endSize];
        if (endSize > 0) {
            Set<Entry<String, EBrowserView>> set = endPopTable.entrySet();
            int counter = 0;
            for (Map.Entry<String, EBrowserView> enry : set) {
                EBrowserView temp = enry.getValue();
                String url = temp.getRelativeUrl();
                beEndPopupUrls[counter] = url;
                counter++;
            }
        }
        mApp.disPatchWindowForward(beEndUrl, beShowUrl, beEndPopupUrls,
                beShowPopupUrls);
    }

    public void popOpenAnalytics(String curWindowUrl, String beShowPopupUrl) {
        if (!EBrowserActivity.analytics) {
            return;
        }
        mApp.disPatchPopupOpen(curWindowUrl, beShowPopupUrl);
    }

    public void popCloseAnalytics(String beEndPopupUrl) {
        if (!EBrowserActivity.analytics) {
            return;
        }

        mApp.disPatchPopupClose(beEndPopupUrl);
    }

    public void onAppResumeAnalytics(String beShowUrl,
                                     Map<String, EBrowserView> beShowPops) {
        if (!EBrowserActivity.analytics) {
            return;
        }
        int showSize = beShowPops.size();
        String[] beShowPopupUrls = new String[showSize];
        Set<Entry<String, EBrowserView>> set = beShowPops.entrySet();
        int counter = 0;
        for (Map.Entry<String, EBrowserView> enry : set) {
            EBrowserView temp = enry.getValue();
            String url = temp.getRelativeUrl();
            beShowPopupUrls[counter] = url;
            counter++;
        }
        mApp.disPatchAppResume(null, beShowUrl, beShowPopupUrls);
    }

    public void onAppPauseAnalytics(String beEndUrl,
                                    Map<String, EBrowserView> beEndPops) {
        if (!EBrowserActivity.analytics) {
            return;
        }
        int endSize = beEndPops.size();
        String[] beEndPopupUrls = new String[endSize];
        Set<Entry<String, EBrowserView>> set = beEndPops.entrySet();
        int counter = 0;
        for (Map.Entry<String, EBrowserView> enry : set) {
            EBrowserView temp = enry.getValue();
            String url = temp.getRelativeUrl();
            beEndPopupUrls[counter] = url;
            counter++;
        }
        mApp.disPatchAppPause(beEndUrl, null, beEndPopupUrls);
    }

    public void startAnalytics(String startUrl) {
        if (!EBrowserActivity.analytics) {
            return;
        }

        mApp.disPatchAppStart(startUrl);
    }

    public void onLoadAppData(JSONObject json) {
        // TODO Auto-generated method stub

        if (null == mBrwWindPol) {
            return;
        }
        mBrwWindPol.onLoadAppData(json);

    }

    public void setSpaceEnable(SpaceClickListener listener) {
        mBrwWindPol.setSpaceEnable(listener);
    }

    public void onSlidingWindowStateChanged(int position) {
        if (null != mBrwWindPol) {
            mBrwWindPol.onSlidingWindowStateChanged(position);
        }
    }
}
