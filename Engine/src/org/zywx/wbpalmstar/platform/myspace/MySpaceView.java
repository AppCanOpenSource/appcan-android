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

package org.zywx.wbpalmstar.platform.myspace;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.FileHelper;
import org.zywx.wbpalmstar.base.ResoureFinder;
import org.zywx.wbpalmstar.base.cache.MyAsyncTask;
import org.zywx.wbpalmstar.engine.EBrowser;
import org.zywx.wbpalmstar.engine.EBrowserAnimation;
import org.zywx.wbpalmstar.engine.EWgtResultInfo;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.platform.myspace.AppInfo.DelayInstallInfo;
import org.zywx.wbpalmstar.platform.myspace.AppInfo.DelayStartInfo;
import org.zywx.wbpalmstar.platform.myspace.AppInfo.DelayUninstallInfo;
import org.zywx.wbpalmstar.platform.myspace.AppInfo.DownloadData;
import org.zywx.wbpalmstar.platform.myspace.AppInfo.InstallInfo;
import org.zywx.wbpalmstar.platform.myspace.AppTaskList.AppDownTask;
import org.zywx.wbpalmstar.platform.myspace.RefreshableView.RefreshListener;
import org.zywx.wbpalmstar.platform.myspace.UserInfo.LoginInfo;
import org.zywx.wbpalmstar.platform.window.ActionSheetDialog;
import org.zywx.wbpalmstar.platform.window.ActionSheetDialog.ActionSheetDialogItemClickListener;
import org.zywx.wbpalmstar.widgetone.dataservice.WDataManager;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class MySpaceView extends RelativeLayout implements OnClickListener, OnItemClickListener,
        OnItemLongClickListener {

    public static final String TAG = "MySpaceView";
    private static final String USER_SP_NAME = "userConfig";
    private static final String USER_SP_KEY_USER_ID = "userId";
    private static final String USER_SP_KEY_SESSION_KEY = "sessionKey";
    private static final String USER_SP_KEY_FROM_WEBSITE = "website";
    private static final String USER_SP_LAST_UPDATE_RECMD_APPS_TIME = "last_update_recmd_apps_time";
    private LayoutInflater inflater;
    private Button btnBack;
    private Button btnSettings;
    private GridView recomdAppsGridView;
    private GridView myAppsGridView;
    private RecommendAppsAdapter recommendAppsAdapter;
    private MyAppsAdapter myAppsAdapter;
    private ActionSheetDialog actionSheetDialog;
    private ProgressDialog progressDialog;
    private AppDao appDao;
    private UserDao userDao;
    private DelayDao delayDao;
    private RecommendDao recommendDao;
    private boolean canSeeMoreWidget = false;
    // 当前用户的登录信息
    private LoginInfo currentLoginInfo;
    // 用户重新登录的登录信息
    private LoginInfo newLoginInfo;
    private String mainAppId;
    private String platformId = "1";
    private SharedPreferences sp;
    private EBrowser eBrowser;
    private WWidgetData moreWidget;
    private boolean needUpdateWhenBack = false;
    private AppDownTask lastDownTask;
    private int lastUpdateRecmdDay;
    private ScrollView scrollView;
    private RefreshableView refreshableView;
    private Calendar calendar = Calendar.getInstance();
    private ResoureFinder finder;
    private AppTaskList appsTaskList = new AppTaskList();

    public MySpaceView(EBrowser eBrowser, Context context) {
        super(context);
        finder = ResoureFinder.getInstance(context);
        this.eBrowser = eBrowser;
        inflater = LayoutInflater.from(getContext());
        appDao = new AppDao(context);
        userDao = new UserDao(context);
        delayDao = new DelayDao(getContext());
        recommendDao = new RecommendDao(getContext());
        initParams();
        setupView();
        showHistoryRecommendApps();
        reportDelayInfo(new Runnable() {
            @Override
            public void run() {
                reloadMyApps();
            }
        });
    }

    private void initParams() {
        sp = getContext().getSharedPreferences(USER_SP_NAME, Context.MODE_PRIVATE);
        lastUpdateRecmdDay = sp.getInt(USER_SP_LAST_UPDATE_RECMD_APPS_TIME, 0);
        currentLoginInfo = getLoginInfo();
        newLoginInfo = new LoginInfo();
        mainAppId = WDataManager.m_rootWgt.m_appId;
        if (WDataManager.m_rootWgt.getSpaceMoreAppStatus() == WWidgetData.F_MYSPACEMOREAPP_OPEN) {
            canSeeMoreWidget = true;
        }
        // mainAppId = "11007818";10030629 10007743
        moreWidget = WDataManager.getMoreWgt();
        BDebug.d(TAG, "initMySpace() mainAppId:" + mainAppId + " openMoreWidget: " + canSeeMoreWidget);
    }

    private void setupView() {
        inflater.inflate(finder.getLayoutId("platform_myspace_main"), this);
        btnBack = (Button) findViewById(finder.getId("platform_myspace_top_btn_back"));
        btnSettings = (Button) findViewById(finder.getId("platform_myspace_top_btn_settings"));
        btnBack.setOnClickListener(this);
        btnSettings.setOnClickListener(this);
        recomdAppsGridView = (GridView) findViewById(finder.getId("platform_myspace_gv_recommend_apps"));
        myAppsGridView = (GridView) findViewById(finder.getId("platform_myspace_gv_my_apps"));
        recomdAppsGridView.setOnItemClickListener(this);
        myAppsGridView.setOnItemClickListener(this);
        myAppsGridView.setOnItemLongClickListener(this);
        refreshableView = (RefreshableView) findViewById(finder.getId("platform_myspace_refreshable_view"));
        scrollView = (ScrollView) findViewById(finder.getId("platform_myspace_scroll_view"));
        refreshableView.sv = scrollView;
        refreshableView.setRefreshListener(new RefreshListener() {
            @Override
            public void onRefresh() {
                updateRecommendApps(new Runnable() {
                    @Override
                    public void run() {
                        refreshableView.finishRefresh();
                        // 保存最后一次刷新的时间
                        sp.edit().putInt(USER_SP_LAST_UPDATE_RECMD_APPS_TIME, calendar.get(Calendar.DAY_OF_YEAR))
                                .commit();
                    }
                });
            }
        });
        actionSheetDialog = new ActionSheetDialog(getContext());
        actionSheetDialog.setTitle(EUExUtil.getString("select_operation"));
    }

    private int lastOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        Activity activity = (Activity) getContext();
        switch (visibility) {
            case View.VISIBLE:
                // requestFocus();
                // 锁定屏幕
                lastOrientation = activity.getRequestedOrientation();
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case View.INVISIBLE:
            case View.GONE:
                // 还原
                activity.setRequestedOrientation(lastOrientation);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnBack) {
            eBrowser.exitMySpace();
        } else if (v == btnSettings) {
            new MyAsyncTask() {

                public void handleOnPreLoad(final MyAsyncTask task) {
                    String message = "";
                    if (currentLoginInfo.isInfoCompleted()) {
                        message = EUExUtil.getString("get_user_info");
                    } else {
                        message = EUExUtil.getString("get_login_list");
                    }
                    showProgressDialog(message, true, new OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            task.cancel(true);
                        }
                    });
                }

                ;

                public void handleOnCanceled(MyAsyncTask task) {
                    closeProgressDialog();
                    Toast.makeText(getContext(), EUExUtil.getString("operation_cancel"), Toast.LENGTH_SHORT).show();
                }

                ;

                protected Object doInBackground(Object... params) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return appDao.getSessionKey(mainAppId, 3);
                }

                ;

                public void handleOnCompleted(MyAsyncTask task, Object result) {
                    closeProgressDialog();
                    String sk = (String) result;
                    if (sk == null) {
                        Toast.makeText(getContext(), EUExUtil.getString("get_user_info_failed"), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (currentLoginInfo.isInfoCompleted()) {// 已经有登录信息
                        newLoginInfo = new LoginInfo();
                        newLoginInfo.sessionKey = sk;
                        startSettingsWidget(sk, currentLoginInfo.fromDomain);
                    } else {
                        currentLoginInfo.sessionKey = sk;
                        startLoginWidget(sk);
                    }
                }

                ;
            }.execute(new Object[]{});
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
        if (parent == recomdAppsGridView) {
            final DownloadData downloadInfo = recommendAppsAdapter.getItem(position);
            if (moreWidget.m_appId.equals(downloadInfo.appId)) {// 是更多widget
                EWgtResultInfo resultInfo = new EWgtResultInfo(null, null);
                resultInfo.setAnimiId(EBrowserAnimation.ANIM_ID_2);
                showProgressDialog("Loading...", false, null);
                ((Activity) getContext()).setRequestedOrientation(lastOrientation);
                eBrowser.startWidget(moreWidget, resultInfo);
            } else {
                switch (downloadInfo.mode) {
                    case AppInfo.APP_MODE_WAP:
                    case AppInfo.APP_MODE_NATIVE:
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.addCategory(Intent.CATEGORY_BROWSABLE);
                        intent.setData(Uri.parse(downloadInfo.downloadUrl));
                        try {
                            ((Activity) getContext()).startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(getContext(), EUExUtil.getString("can_not_find_suitable_app_perform_this_operation"),
                                    Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case AppInfo.APP_MODE_WIDGET:
                        if (appsTaskList.isExistTask(downloadInfo.appId)) {// 存在下载列表中
                            Toast.makeText(getContext(), EUExUtil.getString("app_is_download_please_wait"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (myAppsAdapter.checkDownloaded(downloadInfo.appId)) {// 已经下载，直接启动
                            startWidgetByAppId(downloadInfo.appId);
                        } else {// 尚未下载，验证登陆，开始下载
                            final InstallInfo info = new InstallInfo();
                            info.setDownloadInfo(downloadInfo);
                            if (!currentLoginInfo.isInfoCompleted()) {// 尚未登录
                                // 开始登陆
                                startLoginTask();
                                // 保存当前的下载任务，登陆完成后自动下载
                                lastDownTask = createAppDownloadTask(info);
                            } else {// 已经登录,添加到我的应用列表
                                myAppsAdapter.addItem(info);
                                myAppsGridView.invalidate();
                                createAppDownloadTask(info).execute(new Object[]{});
                            }
                        }
                        break;
                }
            }
        } else if (parent == myAppsGridView) {
            final InstallInfo installInfo = myAppsAdapter.getItem(position);
            if (installInfo.isDownload) {// 已经下载，直接执行
                startWidgetByAppId(installInfo.getAppId());
            } else {// 尚未下载，开始下载
                if (appsTaskList.isExistTask(installInfo.getAppId())) {// 正在下载，存在下载队列中
                    Toast.makeText(getContext(), EUExUtil.getString("app_is_download_please_wait"), Toast.LENGTH_SHORT).show();
                } else {// 尚未加入下载队列，开始下载
                    createAppDownloadTask(installInfo).execute(new Object[]{});
                }
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, final View item, final int position, long id) {
        final InstallInfo installInfo = myAppsAdapter.getItem(position);
        if (installInfo.isDownload) {
            String[] labels = {EUExUtil.getString("delete")};
            actionSheetDialog.setupData(labels);
            actionSheetDialog.setTitle(EUExUtil.getString("confirm_delete") + installInfo.getDownloadInfo().appName + "？");
            actionSheetDialog.setOnDialogItemClickedListener(new ActionSheetDialogItemClickListener() {

                @Override
                public void onItemClicked(ActionSheetDialog dialog, int index) {
                    if (index == ActionSheetDialog.INDEX_DELETE) {
                        new MyAsyncTask() {

                            public void handleOnPreLoad(MyAsyncTask task) {
                                showProgressDialog(EUExUtil.getString("is_uninstalling"), false, null);
                            }

                            ;

                            protected Object doInBackground(Object... params) {
                                File widgetFile = new File(installInfo.installPath);
                                boolean isDeleted = false;
                                if (FileHelper.deleteFile(widgetFile)) {// 卸载成功
                                    // 从数据库中删除安装记录
                                    userDao.uninstallApp(currentLoginInfo.userId, installInfo.getAppId());
                                    isDeleted = true;
                                    new Thread("Appcan-MySpaceReportUninstallInfo") {
                                        public void run() {
                                            String sessionKey = currentLoginInfo.sessionKey;
                                            String softwareId = installInfo.getDownloadInfo().softwareId;
                                            boolean isReport = appDao.reportUnistallWidget(sessionKey, mainAppId,
                                                    softwareId, platformId);
                                            if (!isReport) {
                                                delayDao.addDelayUninstallInfo(new DelayUninstallInfo(sessionKey,
                                                        mainAppId, softwareId, platformId, System.currentTimeMillis()
                                                        + ""));
                                            }
                                        }

                                        ;
                                    }.start();
                                }
                                return isDeleted;
                            }

                            ;

                            @Override
                            public void handleOnCompleted(MyAsyncTask task, Object result) {
                                closeProgressDialog();
                                myAppsAdapter.removeItemAtPostion(position);
                                myAppsGridView.invalidate();
                            }
                        }.execute(new Object[]{});
                    }
                }

                @Override
                public void onCanceled(ActionSheetDialog dialog) {

                }
            });
            actionSheetDialog.show();
        }
        return true;
    }

    public void notifyBackToAppCenter() {
        BDebug.d(TAG, "  needUpdate:" + needUpdateWhenBack);
        if (needUpdateWhenBack) {
            reloadMyApps();
        }
    }

    /**
     * 显示历史推荐列表
     */
    private void showHistoryRecommendApps() {
        new MyAsyncTask() {
            protected Object doInBackground(Object... params) {
                return recommendDao.getAllRecommendApps();
            }

            ;

            @SuppressWarnings("unchecked")
            @Override
            public void handleOnCompleted(MyAsyncTask task, Object result) {
                ArrayList<DownloadData> recommendAppsList = null;
                if (result == null) {
                    recommendAppsList = new ArrayList<DownloadData>();
                } else {
                    recommendAppsList = (ArrayList<DownloadData>) result;
                }
                if (recommendAppsList.size() > 0 && canSeeMoreWidget) {
                    recommendAppsList.add(getMoreAppInfo());
                }
                recommendAppsAdapter = new RecommendAppsAdapter(recommendAppsList, MySpaceView.this.getContext(),
                        recomdAppsGridView);
                recomdAppsGridView.setAdapter(recommendAppsAdapter);
                // 大于一天主动更新
                if (calendar.get(Calendar.DAY_OF_YEAR) - lastUpdateRecmdDay > 0) {
                    refreshableView.refresh();
                }
            }
        }.execute(new Object[]{});

    }

    private void updateRecommendApps(final Runnable runnable) {
        new MyAsyncTask() {

            @Override
            protected Object doInBackground(Object... params) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // 获得推荐应用列表
                ArrayList<DownloadData> recommendAppsList = appDao.requestRecommendAppsList(mainAppId, platformId, 3);
                if (recommendAppsList != null && recommendAppsList.size() > 0) {// 成功取得推荐列表
                    recommendDao.syncRecommendApps(recommendAppsList);// 与本地列表同步
                    // 是否显示更多
                    if (canSeeMoreWidget) {
                        recommendAppsList.add(getMoreAppInfo());
                    }
                }
                return recommendAppsList;
            }

            public void handleOnCompleted(MyAsyncTask task, Object result) {
                if (result != null) {
                    @SuppressWarnings("unchecked")
                    ArrayList<DownloadData> recommendList = (ArrayList<DownloadData>) result;
                    recommendAppsAdapter.reload(recommendList);
                    Toast.makeText(getContext(), EUExUtil.getString("refresh_success"), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), EUExUtil.getString("refresh_failed"), Toast.LENGTH_SHORT).show();
                }
                if (runnable != null) {
                    runnable.run();
                }
            }

            ;
        }.execute(new Object[]{});
    }

    /**
     * 切换账号后,刷新所有数据
     */
    private void reloadMyApps() {
        needUpdateWhenBack = false;
        new MyAsyncTask() {

            public void handleOnPreLoad(MyAsyncTask task) {
                showProgressDialog(EUExUtil.getString("get_user_info"), true, null);
            }

            ;

            protected Object doInBackground(Object... params) {
                ArrayList<DownloadData> myAppsList = null;
                if (currentLoginInfo.isInfoCompleted()) {// 用户登陆过
                    myAppsList = appDao.requestMyAppsList(platformId, mainAppId, currentLoginInfo.sessionKey, 3);
                }
                ArrayList<InstallInfo> localAppsList = userDao.getInstallInfosByUserId(currentLoginInfo.userId);
                ArrayList<InstallInfo> syncedMyAppsList = appDao.syncUserAppsList(myAppsList, localAppsList);
                return syncedMyAppsList;
            }

            ;

            public void handleOnCanceled(MyAsyncTask task) {
                closeProgressDialog();
            }

            ;

            public void handleOnCompleted(MyAsyncTask task, Object result) {
                closeProgressDialog();
                @SuppressWarnings("unchecked")
                ArrayList<InstallInfo> myAppsList = (ArrayList<InstallInfo>) result;
                if (myAppsList == null) {
                    myAppsList = new ArrayList<InstallInfo>();
                }
                if (myAppsAdapter == null) {
                    myAppsAdapter = new MyAppsAdapter(myAppsList, getContext(), myAppsGridView);
                } else {
                    myAppsAdapter.reload(myAppsList);
                }
                myAppsGridView.setAdapter(myAppsAdapter);
                if (lastDownTask != null) {// 启动上次未启动的Task
                    InstallInfo installInfo = myAppsAdapter.getInstallInfoByAppId(lastDownTask.installInfo.getAppId());
                    if (installInfo != null) {// 存在我的应用列表中
                        myAppsAdapter.removeItemByAppId(installInfo.getAppId());
                    }
                    myAppsAdapter.addItem(lastDownTask.installInfo);
                    lastDownTask.execute(new Object[]{});
                    lastDownTask = null;
                }
            }

            ;
        }.execute(new Object[]{});
    }

    private void showProgressDialog(String message, boolean cancelable, OnCancelListener cancelListener) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
        }
        progressDialog.setMessage(message);
        progressDialog.setCancelable(cancelable);
        progressDialog.setOnCancelListener(cancelListener);
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
            progressDialog.setOnCancelListener(null);
        }
    }

    private boolean saveUserLoginStatus(LoginInfo loginInfo) {
        return sp.edit().putString(USER_SP_KEY_USER_ID, loginInfo.userId)
                .putString(USER_SP_KEY_SESSION_KEY, loginInfo.sessionKey)
                .putString(USER_SP_KEY_FROM_WEBSITE, loginInfo.fromDomain).commit();
    }

    private LoginInfo getLoginInfo() {
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.userId = sp.getString(USER_SP_KEY_USER_ID, null);
        loginInfo.sessionKey = sp.getString(USER_SP_KEY_SESSION_KEY, null);
        loginInfo.fromDomain = sp.getString(USER_SP_KEY_FROM_WEBSITE, null);
        return loginInfo;
    }

    private DownloadData getMoreAppInfo() {
        WWidgetData widgetData = WDataManager.getMoreWgt();
        DownloadData downloadInfo = new DownloadData();
        downloadInfo.appId = widgetData.m_appId;
        downloadInfo.appName = widgetData.m_widgetName;
        downloadInfo.downloadUrl = widgetData.m_indexUrl;
        downloadInfo.iconLoc = "res://drawable/plugin_myspace_item_add_bg";
        downloadInfo.mode = AppInfo.APP_MODE_WAP;
        return downloadInfo;
    }

    public String getSessionKey() {
        return currentLoginInfo.sessionKey;
    }

    public void onUserLoginCallback(String json) {
        LoginInfo loginInfo = appDao.getLoginInfo(json);

        if (loginInfo != null && loginInfo.isServerInfoCompleted()) {// 登录成功
            BDebug.d(TAG, "Login success! sessionKey:" + loginInfo.sessionKey + " fromDomain:" + loginInfo.fromDomain);
            if (currentLoginInfo.isInfoCompleted()) {// 属于重新登录，覆盖之前的SessionKey
                currentLoginInfo.sessionKey = newLoginInfo.sessionKey;
                currentLoginInfo.fromDomain = loginInfo.fromDomain;
                currentLoginInfo.userId = loginInfo.userId;
            } else {// 初次登录
                currentLoginInfo.fromDomain = loginInfo.fromDomain;
                currentLoginInfo.userId = loginInfo.userId;
            }
            saveUserLoginStatus(currentLoginInfo);
            reloadMyApps();
        } else {
            currentLoginInfo.sessionKey = null;
            newLoginInfo.sessionKey = null;
            Toast.makeText(getContext(), EUExUtil.getString("login_failed"), Toast.LENGTH_SHORT).show();
        }
    }

    ;

    public void notifyLoginOut(OnLoginOutCallback callback) {
        boolean result = sp.edit().clear().commit();
        if (result) {
            currentLoginInfo.clearInfo();
            needUpdateWhenBack = true;
        }
        callback.onLoginOut(result);
    }

    public void notifyDownloadApp(String json) {
        final DownloadData downloadInfo = appDao.getWebAppDownloadInfo(json);
        if (downloadInfo == null) {
            Toast.makeText(getContext(), EUExUtil.getString("download_info_error"), Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentLoginInfo != null && currentLoginInfo.isInfoCompleted()) {
            if (myAppsAdapter.checkDownloaded(downloadInfo.appId)) {// 已经存在
                Toast.makeText(getContext(), EUExUtil.getString("app_has_download_please_run"), Toast.LENGTH_SHORT).show();
                return;
            }
            if (appsTaskList.isExistTask(downloadInfo.appId)) {// 存在下载列表中
                Toast.makeText(getContext(), EUExUtil.getString("app_downloading"), Toast.LENGTH_SHORT).show();
                return;
            }
            new MyAsyncTask() {
                public void handleOnCompleted(MyAsyncTask task, Object result) {
                    InstallInfo installInfo = new InstallInfo();
                    installInfo.setDownloadInfo(downloadInfo);
                    myAppsAdapter.addItem(installInfo);
                    createAppDownloadTask(installInfo).execute(new Object[]{});
                }

                ;
            }.execute(new Object[]{});
        } else {
            startLoginTask();
            InstallInfo installInfo = new InstallInfo();
            installInfo.setDownloadInfo(downloadInfo);
            lastDownTask = createAppDownloadTask(installInfo);
        }
    }

    /**
     * 根据AppId启动一个Widget
     *
     * @param appId
     */
    private void startWidgetByAppId(String appId) {
        final InstallInfo info = myAppsAdapter.getInstallInfoByAppId(appId);
        if (info != null && info.isDownload) {
            WWidgetData widgetData = appDao.getWidgetDataByInstallPath(info.installPath);
            if (widgetData == null) {
                Toast.makeText(getContext(), EUExUtil.getString("cannot_find_this_widget"), Toast.LENGTH_SHORT).show();
            } else {
                BDebug.d(TAG, "startWidget:" + widgetData.m_indexUrl);
                EWgtResultInfo inResult = new EWgtResultInfo(null, null);
                inResult.setAnimiId(EBrowserAnimation.ANIM_ID_2);
                showProgressDialog("Loading...", false, null);
                ((Activity) getContext()).setRequestedOrientation(lastOrientation);
                eBrowser.startWidget(widgetData, inResult);
                new Thread("Appcan-MyspaceReportWidgetInfo") {
                    public void run() {
                        final String sessionKey = currentLoginInfo.sessionKey;
                        final String softwareId = info.getDownloadInfo().softwareId;
                        final boolean isReport = appDao.reportStartWidget(sessionKey, softwareId);
                        BDebug.i(TAG, info.getDownloadInfo().appName + " startReport:" + isReport);
                        if (!isReport) {// 未上报成功，记录此信息
                            delayDao.addDelayStartInfo(new DelayStartInfo(sessionKey, softwareId, System
                                    .currentTimeMillis() + ""));
                        }
                    }

                    ;
                }.start();
            }
        }
    }

    public void notifyWidgetLoadFinish() {
        closeProgressDialog();
    }

    private void startLoginWidget(String sessionKey) {
        EWgtResultInfo resultInfo = new EWgtResultInfo(null, null);
        resultInfo.setAnimiId(EBrowserAnimation.ANIM_ID_2);
        WWidgetData widgetData = WDataManager.getLoginListWgt(mainAppId, sessionKey);
        BDebug.d(TAG, "startLoginWidget: " + widgetData.m_indexUrl);
        showProgressDialog("Loading...", false, null);
        eBrowser.startWidget(widgetData, resultInfo);
    }

    private void startSettingsWidget(String sessionKey, String fromDomain) {
        EWgtResultInfo resultInfo = new EWgtResultInfo(null, null);
        resultInfo.setAnimiId(EBrowserAnimation.ANIM_ID_2);
        WWidgetData widgetData = WDataManager.getLoginListWgt(mainAppId, sessionKey);
        widgetData.m_indexUrl = widgetData.m_indexUrl + "&fromDomain=" + fromDomain;
        BDebug.d(TAG, "startSettingsWidget: " + widgetData.m_indexUrl);
        showProgressDialog("Loading...", false, null);
        eBrowser.startWidget(widgetData, resultInfo);
    }

    private AppDownTask createAppDownloadTask(final InstallInfo info) {
        return new AppTaskList.AppDownTask(info, myAppsGridView) {
            @Override
            public void handleOnPreLoad(MyAsyncTask task) {
                super.handleOnPreLoad(task);
                appsTaskList.addTask(this);
            }

            @Override
            protected Object doInBackground(Object... params) {
                Object result = super.doInBackground(params);
                if (result != null) {// 安装保存用户应用下载信息
                    info.installPath = (String) result;
                    info.isDownload = true;
                    userDao.installApp(currentLoginInfo.userId, info);// 保存安装信息
                    // 上报安装信息
                    new Thread("Appcan-MySpaceReportInstallInfo") {
                        public void run() {
                            String sessionKey = currentLoginInfo.sessionKey;
                            String softwareId = info.getDownloadInfo().softwareId;
                            boolean isReport = appDao
                                    .reportInstallWidget(sessionKey, mainAppId, softwareId, platformId);
                            BDebug.i(TAG, info.getDownloadInfo().appName + " installReport:" + isReport);
                            if (!isReport) {
                                delayDao.addDelayInstallInfo(new DelayInstallInfo(sessionKey, mainAppId, softwareId,
                                        platformId, System.currentTimeMillis() + ""));
                            }
                        }

                        ;
                    }.start();

                }
                return result;
            }

            public void handleOnCanceled(MyAsyncTask task) {
                super.handleOnCanceled(task);
                appsTaskList.removeTask(this);
                myAppsAdapter.removeItemByAppId(info.getAppId());
            }

            ;

            public void handleOnCompleted(MyAsyncTask task, Object result) {
                super.handleOnCompleted(task, result);
                appsTaskList.removeTask(this);
                if (result == null) {
                    myAppsAdapter.removeItemByAppId(info.getAppId());
                    myAppsGridView.requestLayout();
                    Toast.makeText(getContext(), EUExUtil.getString("install_failed"),
                            Toast.LENGTH_SHORT).show();
                }
            }

            ;
        };
    }

    private void startLoginTask() {
        new MyAsyncTask() {
            public void handleOnPreLoad(final MyAsyncTask task) {
                showProgressDialog(EUExUtil.getString("get_open_login_list"), true, new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        task.cancel(true);
                    }
                });
            }

            ;

            public void handleOnCanceled(MyAsyncTask task) {
                closeProgressDialog();
                Toast.makeText(getContext(), EUExUtil.getString("operation_cancel"), Toast.LENGTH_SHORT).show();
            }

            ;

            protected Object doInBackground(Object... params) {
                return appDao.getSessionKey(mainAppId, 3);
            }

            ;

            public void handleOnCompleted(MyAsyncTask task, Object result) {
                closeProgressDialog();
                if (result == null) {
                    Toast.makeText(getContext(), EUExUtil.getString("get_info_failed"), Toast.LENGTH_SHORT).show();
                    return;
                }
                currentLoginInfo.sessionKey = (String) result;
                startLoginWidget(currentLoginInfo.sessionKey);
            }

            ;
        }.execute(new Object[]{});
    }

    public static interface OnLoginOutCallback {
        void onLoginOut(boolean result);
    }

    private void reportDelayInfo(final Runnable runnable) {
        new MyAsyncTask() {
            protected Object doInBackground(Object... params) {
                JsonUtil jsonUtil = new JsonUtil();
                // 上报启动延迟信息
                ArrayList<DelayStartInfo> startInfos = delayDao.getAllDelayStartInfo();
                if (startInfos != null && startInfos.size() > 0) {
                    String json = jsonUtil.combineDelayStartReportInfo(startInfos);
                    if (CommonUtility.postData(CommonUtility.URL_DELAY_START_REPORT, json.getBytes())) {
                        delayDao.removeAllDelayStartInfo();
                    }
                }

                // 上报安装延迟信息
                ArrayList<DelayInstallInfo> installInfos = delayDao.getAllDelayInstallInfo();
                if (installInfos != null && installInfos.size() > 0) {
                    String json = jsonUtil.combineDelayInstallReportInfo(installInfos);
                    if (CommonUtility.postData(CommonUtility.URL_DELAY_INSTALL_REPORT, json.getBytes())) {
                        delayDao.removeAllDelayInstallInfo();
                    }
                }

                // 上报卸载延迟信息
                ArrayList<DelayUninstallInfo> uninstallInfos = delayDao.getAllDelayUninstallInfo();
                if (uninstallInfos != null && uninstallInfos.size() > 0) {
                    String json = jsonUtil.combineDelayUninstallReportInfo(uninstallInfos);
                    if (CommonUtility.postData(CommonUtility.URL_DELAY_UNISTALL_REPORT, json.getBytes())) {
                        delayDao.removeAllDelayUninstallInfo();
                    }
                }
                return null;
            }

            ;

            public void handleOnCompleted(MyAsyncTask task, Object result) {
                if (runnable != null) {
                    runnable.run();
                }
            }

            ;
        }.execute(new Object[]{});
    }

}
