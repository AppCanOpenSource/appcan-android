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

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Keep;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.acedes.ACEDes;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.base.view.SwipeView;
import org.zywx.wbpalmstar.base.vo.DownloadCallbackInfoVO;
import org.zywx.wbpalmstar.base.vo.WindowOptionsVO;
import org.zywx.wbpalmstar.engine.EBrowserHistory.EHistoryEntry;
import org.zywx.wbpalmstar.engine.external.Compat;
import org.zywx.wbpalmstar.engine.mpwindow.MPPopMenu;
import org.zywx.wbpalmstar.engine.multipop.MultiPopAdapter;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import org.zywx.wbpalmstar.engine.universalex.EUExScript;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.engine.universalex.EUExWidget.SpaceClickListener;
import org.zywx.wbpalmstar.engine.universalex.EUExWindow;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class EBrowserWindow extends SwipeView implements AnimationListener {

    private static final String TAG = "EBrowserWindow";
    public static final int F_WINDOW_FLAG_NONE = 0x0;
    public static final int F_WINDOW_FLAG_NEW = 0x1;
    public static final int F_WINDOW_FLAG_SAME = 0x2;
    public static final int F_WINDOW_FLAG_WILL_REMOWE = 0x4;
    public static final int F_WINDOW_FLAG_OPENING = 0x8;
    public static final int F_WINDOW_FLAG_OPPOP = 0x10;
    public static final int F_WINDOW_FLAG_OPPOP_END = 0x20;
    public static final int F_WINDOW_FLAG_SLIDING_WIN = 0x40;
    public static final String MP_WINDOW_CLICKED_TYPE_LEFT = "0";
    public static final String MP_WINDOW_CLICKED_TYPE_RIGHT = "1";
    public static final String MP_WINDOW_CLICKED_TYPE_MENU = "2";
    public static final String MP_WINDOW_CLICKED_TYPE_BOTTOM_LEFT = "3";
    public static final String MP_WINDOW_CLICKED_TYPE_CLOSE = "4";
    public static final String CALLBACK_METHOD_ON_MP_WINDOW_CLICKED = "uexWindow.onMPWindowClicked";
    public static final String CALLBACK_POST_GLOBAL_NOTI = "javascript:if(uexWindow.onGlobalNotification)"
            + "{uexWindow.onGlobalNotification('";
    public static final String CALLBACK_PUBLISH_GLOBAL_NOTI = "javascript:uexWindow.";
    public static final String TAG_CHANNEL_ID = "channelId";
    public static final String TAG_CHANNEL_FUNNAME = "functionName";
    public static final String TAG_CHANNEL_TYPE = "winType";
    public static final String TAG_CHANNEL_WINNAME = "winName";
    public static final String WIN_TYPE_MAIN = "main";
    public static final String WIN_TYPE_POP = "pop";
    private int mWindowStyle;
    private int mAnimId;
    private long mAnimDuration;
    private boolean mAnimFill;
    private int mflag;
    private int mWindPoType;
    private EBrowser mBrw;
    private EBounceView mBounceView;
    private EBrowserView mTopView;
    private EBrowserView mMainView;
    private EBrowserView mBottomView;

    private EBrowserWidget mBroWidget;
    private EBrowserHistory mObHistroy;
    private String mName;
    private Context mContext;
    private EBrowserView mAddView;
    private String mWinRegist;
    private EAdViewTimer mAddViewTimer;
    private boolean mPrevWillHidden;
    private boolean mOAuth;
    private boolean mHidden;
    private boolean mLockBackKey;
    private boolean mLockMenuKey;
    private Map<String, EBrowserView> mPopTable;
    private Map<String, ArrayList<EBrowserView>> mMultiPopTable;
    private Map<String, ViewPager> mMultiPopPager;
    private EBrowserProgress mGlobalProgress;
    private EPreloadQueue mPreQueue;
    private int mDateType;
    private ProgressDialog mGlobalProDialog;

    static final int VIEW_TOP = 0xfff01;
    static final int VIEW_MID = 0xfff02;
    static final int VIEW_BOTTOM = 0xfff03;

    private EBrowserToast mToast;
    private WindowHander mWindLoop;

    private EUExWindow mWindowCallback;

    public static boolean isShowDialog = false;
    private List<HashMap<String, String>> mChannelList = null;
    private List<HashMap<String, Object>> mResumeJs = null;

    public static String rootLeftSlidingWinName = "rootLeftSlidingWinName";
    public static String rootRightSlidingWinName = "rootRightSlidingWinName";
    private List<View> viewList = new ArrayList<View>();

    private RelativeLayout mMPWrapLayout;//公众号样式外层layout
    private LinearLayout bounceViewWrapper;
    private LinearLayout bounceViewMenu;//公众号菜单布局inputviews
    private LinearLayout inputviews;//公众号菜单布局inputviews
    private LinearLayout platform_mp_window_bottom_bar; //整个底部布局
    private RelativeLayout platform_mp_window_title_bar; //titleBar 布局
    private WindowOptionsVO mwindowOptionsVO;
    public EBrowserWindow(Context context, EBrowserWidget inParent) {
        super(context);
        mContext = context;
        mBroWidget = inParent;
        mPreQueue = new EPreloadQueue();
        mObHistroy = new EBrowserHistory();
        mWindLoop = new WindowHander(Looper.getMainLooper());
        mPopTable = new Hashtable<String, EBrowserView>();
        mMultiPopTable = new Hashtable<String, ArrayList<EBrowserView>>();
        mMultiPopPager = new HashMap<String, ViewPager>();

        setAnimationCacheEnabled(false);
        setAlwaysDrawnWithCacheEnabled(false);
    }

    public void init(EBrowser inBrw, EBrwViewEntry inEntry) {
        EUtil.viewBaseSetting(this);
        mBrw = inBrw;
        mWindowStyle = inEntry.mWindowStyle;
        if (null == mMainView) {
            if (mWindowStyle == EBrwViewEntry.WINDOW_SYTLE_MEDIA_PLATFORM){
                mMainView = new EBrowserView(mContext,
                        EBrwViewEntry.VIEW_TYPE_MAIN, this);
                mMainView.setVisibility(VISIBLE);
                mMainView.setName("main");
                mMainView.setExeJS(inEntry.mExeJS);
                if (inEntry.checkFlag(EBrwViewEntry.F_FLAG_WEBAPP)) {
                    mMainView.setWebApp(true);
                }
                LayoutInflater layoutInflater = LayoutInflater.from(mContext);
                //外面套了一层wrapLayout，便于头部和底部布局
                mMPWrapLayout = (RelativeLayout) layoutInflater.inflate(EUExUtil.getResLayoutID("platform_mp_window_wrapframe"), null);

                bounceViewWrapper = (LinearLayout) mMPWrapLayout.findViewById(EUExUtil.getResIdID("platform_mp_window_bounceview_wrapper"));
                //公众号的菜单根布局
                bounceViewMenu = (LinearLayout) mMPWrapLayout.findViewById(EUExUtil.getResIdID("platform_mp_window_layout_menu_bar"));
                inputviews = (LinearLayout) mMPWrapLayout.findViewById(EUExUtil.getResIdID("inputviews"));
                platform_mp_window_bottom_bar = (LinearLayout) mMPWrapLayout.findViewById(EUExUtil.getResIdID("platform_mp_window_bottom_bar"));
                platform_mp_window_title_bar=(RelativeLayout) mMPWrapLayout.findViewById(EUExUtil.getResIdID("platform_mp_window_title_bar"));
                //防止混淆导致布局文件出错，不在布局中直接引用EBounceView了
                mBounceView = new EBounceView(mContext);
                LayoutParams bParm = new LayoutParams(Compat.FILL, Compat.FILL);
                mBounceView.setLayoutParams(bParm);
                bounceViewWrapper.addView(mBounceView);
                EUtil.viewBaseSetting(mBounceView);
                mBounceView.setId(VIEW_MID);
                mBounceView.addView(mMainView);
                mwindowOptionsVO=inEntry.mWindowOptions;
                setWindowOptions(inEntry.mWindowOptions,false,true);
                addView(mMPWrapLayout);
            }else{
                mMainView = new EBrowserView(mContext,
                        EBrwViewEntry.VIEW_TYPE_MAIN, this);
                mMainView.setVisibility(VISIBLE);
                mMainView.setName("main");
                mBounceView = new EBounceView(mContext);
                EUtil.viewBaseSetting(mBounceView);
                mBounceView.setId(VIEW_MID);
                LayoutParams bParm = new LayoutParams(Compat.FILL, Compat.FILL);
                mBounceView.setLayoutParams(bParm);
                mBounceView.addView(mMainView);
                addView(mBounceView);
            }
        }
        mMainView.init();
        if (inEntry.isRootWindow()) {
            setName("root");
            mMainView.setRelativeUrl(mBroWidget.getWidget().m_indexUrl);

            if (!TextUtils.isEmpty(mBroWidget.getWidget().m_opaque)) {
                /**wanglei del 20151124*/
//                mMainView.setBrwViewBackground(mBroWidget.getWidget().getOpaque(),
//                        mBroWidget.getWidget().m_bgColor, mBroWidget.getWidget().m_indexUrl);
                /**wanglei add 20151124*/
                mBounceView.setBounceViewBackground(mBroWidget.getWidget().getOpaque(),
                        mBroWidget.getWidget().m_bgColor, mBroWidget.getWidget().m_indexUrl, mMainView);
            }

        } else {
            setName(inEntry.mWindName);
            setAnimId(inEntry.mAnimId);
            mMainView.setRelativeUrl(inEntry.mRelativeUrl);
            if (!inEntry.hasExtraInfo) {
                if (inEntry.checkFlag(EBrwViewEntry.F_FLAG_OPAQUE)) {
                    mMainView.setOpaque(true);
                } else {
                    mMainView.setOpaque(false);
                }
            }
            if (inEntry.hasExtraInfo) {
                /**wanglei del 20151124*/
//                mMainView.setBrwViewBackground(inEntry.mOpaque,
//                        inEntry.mBgColor, inEntry.mData);
                /**wanglei add 20151124*/
                mBounceView.setBounceViewBackground(inEntry.mOpaque,
                        inEntry.mBgColor, inEntry.mData, mMainView);
            }
            if (inEntry.mWindName.equals(EBrowserWindow.rootLeftSlidingWinName)
                    || inEntry.mWindName.equals(EBrowserWindow.rootRightSlidingWinName)) {
                mMainView.getSettings().setUseWideViewPort(false);
            }
        }
    }

    /**
     *
     * @param windowOptionsVO   传进来的数据
     * @param isHasBottomBarShow 传进来数据是否有控制菜单栏显示的字段
     * @param isInit 区分是否第一次设置
     */
    public void setWindowOptions(WindowOptionsVO windowOptionsVO,boolean isHasBottomBarShow,boolean isInit){
        if (mWindowStyle == EBrwViewEntry.WINDOW_SYTLE_MEDIA_PLATFORM){
            if(windowOptionsVO != null && mMPWrapLayout != null){
                if(!isInit) {
                    windowOptionsVO=optionVoCompensation(windowOptionsVO,isHasBottomBarShow);
                }
                //重新配置当前窗口的参数
                //初始化标题栏
                initMPWindowTopBar(mMPWrapLayout, windowOptionsVO);
                if (windowOptionsVO.isBottomBarShow){
                    //是否显示底部栏
                    platform_mp_window_bottom_bar.setVisibility(VISIBLE);
                    initMPWindowBottomBar(mMPWrapLayout, windowOptionsVO);
                }else{
                    TranslateAnimation animation = new TranslateAnimation(0.0f, 0.0f, 0.0f, 300.0f);
                    animation.setDuration(500);
                    platform_mp_window_bottom_bar.setAnimation(animation);
                    platform_mp_window_bottom_bar.setVisibility(GONE);
//                    EUExChatKeyboard euExChatKeyboard=new EUExChatKeyboard(mContext,mMainView);
//                    euExChatKeyboard.open(new String[]{params});
                    BDebug.w("setWindowOptions error: WindowOptions isBottomBar is False");
                }
            }else{
                BDebug.w("setWindowOptions error: windowOptionsVO is null or mMPWrapLayout is null");
            }
        }else{
            BDebug.w("setWindowOptions error: WindowStyle not supported :" + mWindowStyle);
        }
    }

    private WindowOptionsVO optionVoCompensation(WindowOptionsVO windowOptionsVO,boolean isHasBottomBarShow) {
        if(""!=windowOptionsVO.windowTitle&&null!=windowOptionsVO.windowTitle){
            mwindowOptionsVO.windowTitle=windowOptionsVO.windowTitle;
        }
        if(""!=windowOptionsVO.titleBarBgColor&&null!=windowOptionsVO.titleBarBgColor){
            mwindowOptionsVO.titleBarBgColor=windowOptionsVO.titleBarBgColor;
        }
        if(""!=windowOptionsVO.titleLeftIcon&&null!=windowOptionsVO.titleLeftIcon){
            mwindowOptionsVO.titleLeftIcon=windowOptionsVO.titleLeftIcon;
        }
        if(""!=windowOptionsVO.titleRightIcon&&null!=windowOptionsVO.titleRightIcon){
            mwindowOptionsVO.titleRightIcon=windowOptionsVO.titleRightIcon;
        }
        if(null!=windowOptionsVO.menuList){
            mwindowOptionsVO.menuList=windowOptionsVO.menuList;
        }
        if(isHasBottomBarShow){
            mwindowOptionsVO.isBottomBarShow=windowOptionsVO.isBottomBarShow;
        }
        return mwindowOptionsVO;
    }

    /**
     * 初始化公众号样式的顶部标题栏
     */
    private void initMPWindowTopBar(View rootView, WindowOptionsVO windowOptionsVO){
        //设置标题
        TextView windowTitle = (TextView)rootView.findViewById(EUExUtil.getResIdID("platform_mp_window_text_title"));
        windowTitle.setText(windowOptionsVO.windowTitle);
        //设置标题栏的颜色
        if(""!=windowOptionsVO.titleTextColor&&null!=windowOptionsVO.titleTextColor){
            windowTitle.setTextColor(Color.parseColor(windowOptionsVO.titleTextColor));
        }
        //设置标题栏的颜色
        if(""!=windowOptionsVO.titleBarBgColor&&null!=windowOptionsVO.titleBarBgColor){
            platform_mp_window_title_bar.setBackgroundColor(Color.parseColor(windowOptionsVO.titleBarBgColor));
        }
        platform_mp_window_title_bar.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        //右侧图标
        Button showDetailButton=(Button) rootView.findViewById(EUExUtil.getResIdID("platform_mp_window_button_detail_info"));
        showButtonIcon(showDetailButton,windowOptionsVO.titleRightIcon);
        //左侧图标
        Button backButton=(Button) rootView.findViewById(EUExUtil.getResIdID("platform_mp_window_button_back"));
        showButtonIcon(backButton,windowOptionsVO.titleLeftIcon);
        //关闭图标
        Button  closeButton= (Button) rootView.findViewById(EUExUtil.getResIdID("platform_mp_window_button_close"));
        showButtonIcon(closeButton,windowOptionsVO.titleCloseIcon);
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId()==EUExUtil.getResIdID("platform_mp_window_button_back")){
                    callbackOnMPWindowOnClicked(MP_WINDOW_CLICKED_TYPE_LEFT, null, null);
                }else if(v.getId()==EUExUtil.getResIdID("platform_mp_window_button_detail_info")){
                    callbackOnMPWindowOnClicked(MP_WINDOW_CLICKED_TYPE_RIGHT, null, null);// 显示个人信息页面
                }else if(v.getId()==EUExUtil.getResIdID("platform_mp_window_button_close")){
                    callbackOnMPWindowOnClicked(MP_WINDOW_CLICKED_TYPE_CLOSE,null,null);
                }
            }
        };
        backButton.setOnClickListener(listener);
        showDetailButton.setOnClickListener(listener);
        closeButton.setOnClickListener(listener);
    }

    public void setMpWindowStatus(boolean flag) {
        if(flag){
            platform_mp_window_bottom_bar.setVisibility(VISIBLE);
        }else {
            platform_mp_window_bottom_bar.setVisibility(GONE);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void showButtonIcon(ImageButton Image, String iconPath) {
        if(""!=iconPath && null!=iconPath){
            String IconImg=iconPath;
            IconImg=IconImg.substring(BUtility.F_Widget_RES_SCHEMA
                    .length());
            IconImg = BUtility.F_Widget_RES_path + IconImg;
            Bitmap leftIconImgBitmap =((EBrowserActivity)mContext).getImage(IconImg);
            if(null!=IconImg){
                BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(),
                        leftIconImgBitmap);
                if(null!=bitmapDrawable){
                    Image.setBackground(bitmapDrawable);
                }
            }
        }else{
            Image.setVisibility(GONE);
        }

    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void showButtonIcon(Button backButton,  String iconPath) {
        if(""!=iconPath && null!=iconPath){
            String IconImg=iconPath;
            IconImg=IconImg.substring(BUtility.F_Widget_RES_SCHEMA
                    .length());
            IconImg = BUtility.F_Widget_RES_path + IconImg;
            Bitmap leftIconImgBitmap =((EBrowserActivity)mContext).getImage(IconImg);
            if(null!=IconImg){
                BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(),
                        leftIconImgBitmap);
                if(null!=bitmapDrawable){
                    backButton.setBackground(bitmapDrawable);
                }
            }
        }else{
            backButton.setVisibility(GONE);
        }

    }
    /**
     * 初始化公众号样式的底部菜单和键盘输入栏
     *
     */
    private void initMPWindowBottomBar(View rootView, WindowOptionsVO windowOptionsVO){
        //底部外层
        LinearLayout layout_bottom_menu_toolbar = (LinearLayout)rootView.findViewById(EUExUtil.getResIdID("platform_mp_window_layout_custom_toolbar"));
        //菜单栏
        LinearLayout layout_menu = (LinearLayout)rootView.findViewById(EUExUtil.getResIdID("platform_mp_window_layout_custom_menu"));
        //键盘
        LinearLayout layout_exchange=(LinearLayout)rootView.findViewById(EUExUtil.getResIdID("platform_mp_window_exchange_layout"));

        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId()==EUExUtil.getResIdID("platform_mp_window_exchange_layout")){
                    //TODO 显示键盘输入
                    callbackOnMPWindowOnClicked(MP_WINDOW_CLICKED_TYPE_BOTTOM_LEFT,null,null);
                }
            }
        };
        layout_exchange.setOnClickListener(listener);

        //初始化菜单部分
        List<WindowOptionsVO.MPWindowMenuVO> menuList = windowOptionsVO.menuList;
        if (menuList != null && menuList.size() > 0) {
            layout_bottom_menu_toolbar.setVisibility(View.VISIBLE);
            layout_menu.removeAllViews();
            for (int i = 0; i < menuList.size(); i++) {
                final WindowOptionsVO.MPWindowMenuVO menuVO = menuList.get(i);
                //遍历增加菜单栏目
                LinearLayout layout = (LinearLayout) LayoutInflater.from(mContext).inflate(EUExUtil.getResLayoutID("platform_mp_window_menu_title_item"), null);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
                layout.setLayoutParams(lp);
                ImageView imageTab = (ImageView)layout.findViewById(EUExUtil.getResIdID("platform_mp_window_icon_tab"));
                TextView tvMenuName = (TextView) layout.findViewById(EUExUtil.getResIdID("platform_mp_window_tv_menu_name"));
                tvMenuName.setText(menuVO.menuTitle);

                if (menuVO.subItems!=null&&menuVO.subItems.size() > 0){
                    // 有子菜单项，显示三角
                    imageTab.setVisibility(VISIBLE);
                } else {
                    // 无子菜单项，隐藏三角
                    imageTab.setVisibility(GONE);
                }
                layout.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        try {
                            if (menuVO.subItems!=null && menuVO.subItems.size() > 0) {
                                MPPopMenu popupWindowMenu = new MPPopMenu(mContext, menuVO, 0, 0, new MPPopMenu.PopMenuClickListener() {
                                    @Override
                                    public void onClick(String itemId) {
                                        callbackOnMPWindowOnClicked(MP_WINDOW_CLICKED_TYPE_MENU, menuVO.menuId, itemId);
                                    }
                                });
                                popupWindowMenu.showAtLocation(v);
                            } else {
                                callbackOnMPWindowOnClicked(MP_WINDOW_CLICKED_TYPE_MENU, menuVO.menuId, null);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                layout_menu.addView(layout);
            }
            layout_bottom_menu_toolbar.setVisibility(View.VISIBLE);
        } else {
            layout_bottom_menu_toolbar.setVisibility(View.GONE);
        }
    }

    private void callbackOnMPWindowOnClicked(String type, String menuId, String itemId){
        JSONObject json = new JSONObject();
        try {
            json.put("type", type);
            if (menuId!=null) {
                json.put("menuId", menuId);
            }
            if (itemId!=null) {
                json.put("itemId", itemId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String data = json.toString();
        callbackToMainWebView(CALLBACK_METHOD_ON_MP_WINDOW_CLICKED, data);
    }

    /**
     * 用于特殊样式的窗口（如公众号窗口）进行功能性交互回调
     *
     */
    private void callbackToMainWebView(String callbackFunc, String jsonObject){
        String js = "javascript:if(" + callbackFunc + "){"
                + callbackFunc + "(" + jsonObject + ");}else{console.log('function " + callbackFunc +" not found.')}";
        if (null != mMainView) {
            mMainView.addUriTask(js);
        }
    }

    public boolean checkFlag(int flag) {

        return (mflag & flag) != 0;
    }

    public void setFlag(int flag) {
        mflag |= flag;
    }

    public void clearFlag() {
        mflag &= F_WINDOW_FLAG_NONE;
    }


    public void addViewToCurrentWindow(View child) {
        //Message msg = mWindLoop.obtainMessage();
        //msg.what = F_WHANDLER_ADD_VIEW;
        //msg.obj = child;
        //mWindLoop.sendMessage(msg);
        viewList.add(child);
        child.setTag(EViewEntry.F_PLUGIN_VIEW_TAG);
        Animation anim = child.getAnimation();
        addView(child);
        if (null != anim) {
            anim.start();
        }
        bringChildToFront(child);
    }

    public void removeViewFromCurrentWindow(View child) {
//        Message msg = mWindLoop.obtainMessage();
//        msg.what = F_WHANDLER_REMOVE_VIEW;
//        msg.obj = child;
//        mWindLoop.sendMessage(msg);
//         Animation removeAnim = child.getAnimation();
//         if (null != removeAnim) {
//         removeAnim.start();
//         }
//         removeView(child);

//        View children = (View) msg.obj;
        Animation removeAnim = child.getAnimation();
        if (null != removeAnim) {
            removeAnim.start();
        }
        removeViewList(child);
//        msg.obj = null;
    }


    public void createSlidingWindow(EBrwViewEntry entry) {
        mBroWidget.createSlidingWindow(entry);
    }

    public void createWindow(EBrowserView target, EBrwViewEntry entry) {
        /*boolean b1 = entry.checkFlag(EBrwViewEntry.F_FLAG_OBFUSCATION);
        int a = getWidget().m_obfuscation;
        boolean b2 = (0 == getWidget().m_obfuscation);
        if (b2) {
            Toast.makeText(mContext, "没有配置解密权限！", Toast.LENGTH_SHORT).show();
            return;
        }*/
        mBroWidget.createWindow(this, target, entry);
    }

    public void setWindowFrame(int x, int y, int duration) {

        mBroWidget.setWindowFrame(this, x, y, duration);
    }

    public void onCloseWindow(int inAnimID, long duration) {
        if (EBrowserAnimation.isFillAnim(inAnimID)) {
            setAnimFill(true);
        } else {
            setAnimId(inAnimID);
        }
        setAnimDuration(duration);
        mBroWidget.onCloseWindow(this);
        if (mChannelList != null) {
            mChannelList.clear();
            mChannelList = null;
        }

    }

    public void createSibling(EBrowserView view, EBrwViewEntry slbEntry) {
        Message msg = mWindLoop.obtainMessage();
        msg.obj = slbEntry;
        msg.what = F_WHANDLER_SLIBING_CREATE;
        mWindLoop.sendMessage(msg);
    }

    public void showSlibing(int type) {
        Message msg = mWindLoop.obtainMessage();
        msg.what = F_WHANDLER_SLIBING_SHOW;
        msg.arg1 = type;
        mWindLoop.sendMessage(msg);
    }

    public void closeSlibing(int type) {
        Message msg = mWindLoop.obtainMessage();
        msg.what = F_WHANDLER_SLIBING_CLOSE;
        msg.arg1 = type;
        mWindLoop.sendMessage(msg);
    }

    public void openPopover(EBrwViewEntry popEntry) {

//		Message msg = mWindLoop.obtainMessage();
//		msg.what = F_WHANDLER_POP_OPEN;
//		msg.obj = popEntry;
//		mWindLoop.sendMessage(msg);

        boolean isRootWidget = mBroWidget
                .checkWidgetType(EBrowserWidget.F_WIDGET_POOL_TYPE_ROOT);
        if (isRootWidget) {
            String curl = getRelativeUrl();
            mBrw.popOpenAnalytics(curl, popEntry.mRelativeUrl);
        }

        hPopOverOpen(popEntry);
    }

    public void closePopover(String inPopName) {
//		Message msg = mWindLoop.obtainMessage();
//		msg.what = F_WHANDLER_POP_CLOSE;
//		msg.obj = inPopName;
//		mWindLoop.sendMessage(msg);

        hPopOverClose(inPopName);
    }

    public void setPopoverFrame(String inPopName, int inX, int inY,
                                int inWidth, int inHeight) {
        EBrwViewEntry popEntry = new EBrwViewEntry(EBrwViewEntry.VIEW_TYPE_POP);
        popEntry.mViewName = inPopName;
        popEntry.mX = inX;
        popEntry.mY = inY;
        popEntry.mWidth = inWidth;
        popEntry.mHeight = inHeight;

        Message msg = mWindLoop.obtainMessage();
        msg.what = F_WHANDLER_POP_SET;
        msg.obj = popEntry;
        mWindLoop.sendMessage(msg);
    }

    public void evaluatePopoverScript(EBrowserView inWhich, String inWndName,
                                      String inPopName, String inScript) {
        if (null == inWndName || 0 == inWndName.length()) {
            EBrowserView old = mPopTable.get(inPopName);
            if (old != null) {
                addUriTask(old, inScript);
            }
        } else {
            mBroWidget.evaluatePopoverScript(inWhich, inWndName, inPopName,
                    inScript);
        }
    }

    public void setMultiPopoverFrame(String inPopName, int inX, int inY,
                                     int inWidth, int inHeight) {
        EBrwViewEntry popEntry = new EBrwViewEntry(EBrwViewEntry.VIEW_TYPE_POP);
        popEntry.mViewName = inPopName;
        popEntry.mX = inX;
        popEntry.mY = inY;
        popEntry.mWidth = inWidth;
        popEntry.mHeight = inHeight;

        Message msg = mWindLoop.obtainMessage();
        msg.what = F_WHANDLER_MULTIPOP_SET;
        msg.obj = popEntry;
        mWindLoop.sendMessage(msg);
    }

    private void hSetMultiPopOverFrame(EBrwViewEntry entity) {
        ArrayList<EBrowserView> list = mMultiPopTable.get(entity.mViewName);
        if (null == list || list.size() == 0) {
            return;
        }
        View vParent = (View) list.get(0).getParent();
        LayoutParams lParam = new LayoutParams(entity.mWidth, entity.mHeight);
        lParam.gravity = Gravity.NO_GRAVITY;
        lParam.leftMargin = entity.mX;
        lParam.topMargin = entity.mY;
        vParent.setLayoutParams(lParam);
    }

    public void evaluateMultiPopoverScript(EBrowserView inWhich, String inWndName,
                                           String inMultiPopName, String inPopName, String inScript) {
        if (null == inWndName || 0 == inWndName.length()) {
            ArrayList<EBrowserView> list = mMultiPopTable.get(inMultiPopName);
            if (null == list) {
                return;
            }
            for (int i = 1; i < list.size(); i++) {
                EBrowserView pop = list.get(i);
                if (inPopName.equals(pop.getName())) {
                    addUriTask(pop, inScript);
                    break;
                }
            }
        } else {
            mBroWidget.evaluateMultiPopoverScript(inWhich, inWndName,
                    inMultiPopName, inPopName, inScript);
        }
    }

    public void bringToFront(EBrowserView child) {
        View v = (View) child.getParent();
        Message msg = mWindLoop.obtainMessage();
        msg.what = F_WHANDLER_BRING_TO_FRONT;
        msg.obj = v;
        mWindLoop.sendMessage(msg);
    }

    public void sendToBack(EBrowserView child) {
        View v = (View) child.getParent();
        Message msg = mWindLoop.obtainMessage();
        msg.what = F_WHANDLER_SEND_TO_BACK;
        msg.obj = v;
        mWindLoop.sendMessage(msg);
    }

    public void insertAbove(EBrowserView child, String name) {
        View v = (View) child.getParent();
        EBrowserView bv = mPopTable.get(name);
        if (null != bv) {
            View v1 = (View) bv.getParent();
            EViewEntry ent = new EViewEntry();
            ent.obj = v;
            ent.obj1 = v1;
            Message msg = mWindLoop.obtainMessage();
            msg.what = F_WHANDLER_INSERT_ABOVE;
            msg.obj = ent;
            mWindLoop.sendMessage(msg);
        }
    }

    public void insertBelow(EBrowserView child, String name) {
        View v = (View) child.getParent();
        EBrowserView bv = mPopTable.get(name);
        if (null != bv) {
            View v1 = (View) bv.getParent();
            EViewEntry ent = new EViewEntry();
            ent.obj = v;
            ent.obj1 = v1;
            Message msg = mWindLoop.obtainMessage();
            msg.what = F_WHANDLER_INSERT_BELOW;
            msg.obj = ent;
            mWindLoop.sendMessage(msg);
        }
    }

    public void insertPopoverAbovePopover(String name, String name1) {
        EBrowserView bv = mPopTable.get(name);
        EBrowserView bv1 = mPopTable.get(name1);
        if (bv != null && bv1 != null) {
            View v = (View) bv.getParent();
            View v1 = (View) bv1.getParent();
            EViewEntry ent = new EViewEntry();
            ent.obj = v;
            ent.obj1 = v1;
            Message msg = mWindLoop.obtainMessage();
            msg.what = F_WHANDLER_INSERT_POPOVER_ABOVE_POPOVER;
            msg.obj = ent;
            mWindLoop.sendMessage(msg);
        }
    }

    public void insertPopoverBelowPopover(String name, String name1) {
        EBrowserView bv = mPopTable.get(name);
        EBrowserView bv1 = mPopTable.get(name1);
        if (bv != null && bv1 != null) {
            View v = (View) bv.getParent();
            View v1 = (View) bv1.getParent();
            EViewEntry ent = new EViewEntry();
            ent.obj = v;
            ent.obj1 = v1;
            Message msg = mWindLoop.obtainMessage();
            msg.what = F_WHANDLER_INSERT_POPOVER_BELOW_POPOVER;
            msg.obj = ent;
            mWindLoop.sendMessage(msg);
        }
    }

    public void bringPopoverToFront(String name) {
        EBrowserView bv = mPopTable.get(name);
        if (null != bv) {
            View v = (View) bv.getParent();
            Message msg = mWindLoop.obtainMessage();
            msg.what = F_WHANDLER_BING_POPOVER_TO_FRONT;
            msg.obj = v;
            mWindLoop.sendMessage(msg);
        }
    }

    public void sendPopoverToBack(String name) {
        EBrowserView bv = mPopTable.get(name);
        if (null != bv) {
            View v = (View) bv.getParent();
            Message msg = mWindLoop.obtainMessage();
            msg.what = F_WHANDLER_SEND_POPOVER_TO_BACK;
            msg.obj = v;
            mWindLoop.sendMessage(msg);
        }
    }

    public void setWindowHidden(EViewEntry entry) {

        Message msg = mWindLoop.obtainMessage();
        msg.what = F_WHANDLER_SET_VISIBLE;
        msg.obj = entry;
        mWindLoop.sendMessage(msg);
    }

    public final void showProgress() {
        if (null == mGlobalProgress) {
            mGlobalProgress = new EBrowserProgress(mContext);
            LayoutParams pl = new LayoutParams(-2, -2);
            pl.gravity = Gravity.CENTER;
            mGlobalProgress.setLayoutParams(pl);
            addView(mGlobalProgress);
        }
        if (!mGlobalProgress.isShown()) {
            mGlobalProgress.setVisibility(View.VISIBLE);
            mGlobalProgress.showProgress();
        }
    }

    public final void setGlobalProgress(int p) {
        if (null == mGlobalProgress || !mGlobalProgress.isShown()) {
            return;
        }
        mGlobalProgress.setProgress(p);
    }

    public final void hiddenProgress() {
        if (null != mGlobalProgress) {
            mGlobalProgress.hiddenProgress();
            mGlobalProgress.setVisibility(View.GONE);
        }
    }

    // type=0,表示重载单页面浮动窗口，其他表示多页面浮动窗口
    private void reloadPop(EBrwViewEntry entity, int type) {
        EBrowserView child;
        if (type == 0) {
            child = mPopTable.get(entity.mViewName);
        } else {
            child = mMultiPopTable.get(entity.mViewName).get(0);
        }

        child.setDateType(entity.mDataType);
        child.setQuery(entity.mQuery);
        View parent = (View) child.getParent();
        removeView(parent);
        LayoutParams popParm = new LayoutParams(entity.mWidth, entity.mHeight);
        // popParm.leftMargin = entity.mX;
        // popParm.topMargin = entity.mY;
        // parent.setLayoutParams(popParm);
        int parentRight = getRight();
        int parentBottom = getBottom();
        int newRight = entity.mX + entity.mWidth;
        if (newRight > parentRight) {
            popParm.rightMargin = parentRight - newRight;
        } else {
            popParm.rightMargin = 0;
        }
        int newBottom = entity.mY + entity.mHeight;
        if (newBottom > parentBottom) {
            popParm.bottomMargin = parentBottom - newBottom;
        } else if (0 == newBottom) {
            entity.mY = entity.mY + 1;
            popParm.bottomMargin = 0;
        } else {
            popParm.bottomMargin = 0;
        }
        popParm.topMargin = entity.mY;
        popParm.leftMargin = entity.mX;
        parent.setLayoutParams(popParm);
        addView(parent);
        switch (entity.mDataType) {
            case EBrwViewEntry.WINDOW_DATA_TYPE_URL:
//			if (entity.checkFlag(EBrwViewEntry.F_FLAG_OBFUSCATION)) {
                if ((getWidget().m_obfuscation == 1) && !entity.checkFlag(EBrwViewEntry.F_FLAG_WEBAPP)) {
                    child.needToEncrypt(child, entity.mUrl, 0);
                } else {
                    child.newLoadUrl(entity.mUrl);
                }
                mBrw.popCloseAnalytics(child.getRelativeUrl());
                mBrw.popOpenAnalytics(getRelativeUrl(), entity.mRelativeUrl);
                child.setRelativeUrl(entity.mRelativeUrl);
                break;
            case EBrwViewEntry.WINDOW_DATA_TYPE_DATA:
                child.newLoadData(entity.mData);
                break;
            case EBrwViewEntry.WINDOW_DATA_TYPE_DATA_URL:
                String data1 = ACEDes.decrypt(entity.mUrl, mContext, false,
                        entity.mData);
                child.loadDataWithBaseURL(entity.mUrl, data1,
                        EBrowserView.CONTENT_MIMETYPE_HTML,
                        EBrowserView.CONTENT_DEFAULT_CODE, entity.mUrl);
                break;
        }
    }

    public boolean checkMultiPop(ArrayList<EBrwViewEntry> entitys) {
        ArrayList<EBrowserView> childs = mMultiPopTable
                .get(entitys.get(0).mViewName);
        if (childs == null || childs.size() <= 1) {
            return false;
        }
        EBrowserView mainPop = childs.get(0);
        EBrwViewEntry entity = entitys.get(0);
        View parent = (View) mainPop.getParent();
        if (entity.hasExtraInfo) {
            /**wanglei del 20151124*/
//            mainPop.setBrwViewBackground(entity.mOpaque, entity.mBgColor, "");
            /**wanglei add 20151124*/
            ((EBounceView) parent).setBounceViewBackground(entity.mOpaque, entity.mBgColor, "", mMainView);
        }
        removeView(parent);
        LayoutParams popParm = new LayoutParams(entity.mWidth, entity.mHeight);
        int parentRight = getRight();
        int parentBottom = getBottom();
        int newRight = entity.mX + entity.mWidth;
        if (newRight > parentRight) {
            popParm.rightMargin = parentRight - newRight;
        } else {
            popParm.rightMargin = 0;
        }
        int newBottom = entity.mY + entity.mHeight;
        if (newBottom > parentBottom) {
            popParm.bottomMargin = parentBottom - newBottom;
        } else if (0 == newBottom) {
            entity.mY = entity.mY + 1;
            popParm.bottomMargin = 0;
        } else {
            popParm.bottomMargin = 0;
        }
        popParm.topMargin = entity.mY;
        popParm.leftMargin = entity.mX;
        parent.setLayoutParams(popParm);
        addView(parent);
        /**wanglei add 20151124*/
        if (entitys.size() > 0) {
            EBrwViewEntry entityTemp = entitys.get(0);
            EBrowserView childTemp = childs.get(0);
            if(entityTemp.hasExtraInfo){   
                ((EBounceView) childTemp.getParent()).setBounceViewBackground(
                        entityTemp.mOpaque, entityTemp.mBgColor, "", childTemp);
            }
        }
        for (int i = 0; i < entitys.size(); i++) {
            EBrwViewEntry entityTemp = entitys.get(0);
            EBrowserView childTemp = childs.get(0);
            childTemp.setDateType(entityTemp.mDataType);
            childTemp.setQuery(entityTemp.mQuery);
            /**wanglei add 20151124*/
            childTemp.setBackgroundColor(Color.TRANSPARENT);
            /**wanglei del 20151124*/
//            if (entityTemp.hasExtraInfo) {
//                childTemp.setBrwViewBackground(entityTemp.mOpaque, entityTemp.mBgColor, "");
//            }
            switch (entityTemp.mDataType) {
                case EBrwViewEntry.WINDOW_DATA_TYPE_URL:
//				if (entityTemp.checkFlag(EBrwViewEntry.F_FLAG_OBFUSCATION)) {
                    if ((getWidget().m_obfuscation == 1) && !entity.checkFlag(EBrwViewEntry.F_FLAG_WEBAPP)) {
                        childTemp.needToEncrypt(childTemp, entityTemp.mUrl, 0);
                    } else {
                        childTemp.newLoadUrl(entityTemp.mUrl);
                    }

                    childTemp.setRelativeUrl(entityTemp.mRelativeUrl);
                    break;
                case EBrwViewEntry.WINDOW_DATA_TYPE_DATA:
                    childTemp.newLoadData(entityTemp.mData);
                    break;
                case EBrwViewEntry.WINDOW_DATA_TYPE_DATA_URL:
                    String data1 = ACEDes.decrypt(entityTemp.mUrl, mContext,
                            false, entityTemp.mData);
                    childTemp.loadDataWithBaseURL(entityTemp.mUrl, data1,
                            EBrowserView.CONTENT_MIMETYPE_HTML,
                            EBrowserView.CONTENT_DEFAULT_CODE, entityTemp.mUrl);
                    break;
            }
        }

        return true;
    }

    public boolean checkPop(EBrwViewEntry entity) {
        EBrowserView child = mPopTable.get(entity.mViewName);
        if (child == null) {
            return false;
        }
        child.setDateType(entity.mDataType);
        child.setQuery(entity.mQuery);
        View parent = (View) child.getParent();
        boolean parentHasChanged=parent.getParent()!=this;//父View没有改变不需要remove和add操作
        if (parentHasChanged){
            removeView(parent);
        }
        LayoutParams popParm = new LayoutParams(entity.mWidth, entity.mHeight);
        popParm.gravity = Gravity.NO_GRAVITY;
        popParm.leftMargin = entity.mX;
        popParm.topMargin = entity.mY;
        popParm.bottomMargin = entity.mBottom;
        parent.setLayoutParams(popParm);
        if (entity.hasExtraInfo) {
            /**wanglei del 20151124*/
//            child.setBrwViewBackground(entity.mOpaque, entity.mBgColor, "");
            /**wanglei add 20151124*/
            ((EBounceView) child.getParent()).setBounceViewBackground(
                    entity.mOpaque, entity.mBgColor, "", child);
        }
        /**wanglei add 20151124*/
        child.setBackgroundColor(Color.TRANSPARENT);
        if (parentHasChanged) {
            addView(parent);
        }
        switch (entity.mDataType) {
            case EBrwViewEntry.WINDOW_DATA_TYPE_URL:
//			if (entity.checkFlag(EBrwViewEntry.F_FLAG_OBFUSCATION)) {
                if ((getWidget().m_obfuscation == 1) && !entity.checkFlag(EBrwViewEntry.F_FLAG_WEBAPP)) {
                    child.needToEncrypt(child, entity.mUrl, 0);
                } else {
                    child.newLoadUrl(entity.mUrl);
                }
                mBrw.popCloseAnalytics(child.getRelativeUrl());
                mBrw.popOpenAnalytics(getRelativeUrl(), entity.mRelativeUrl);
                child.setRelativeUrl(entity.mRelativeUrl);
                break;
            case EBrwViewEntry.WINDOW_DATA_TYPE_DATA:
                child.newLoadData(entity.mData);
                break;
            case EBrwViewEntry.WINDOW_DATA_TYPE_DATA_URL:
                String data1 = ACEDes.decrypt(entity.mUrl, mContext, false,
                        entity.mData);
                child.loadDataWithBaseURL(entity.mUrl, data1,
                        EBrowserView.CONTENT_MIMETYPE_HTML,
                        EBrowserView.CONTENT_DEFAULT_CODE, entity.mUrl);
                break;
        }
        return true;
    }

    public void openAd(int type, String inUrl, int inDTime, int inHeight,
                       int inWidth, int inInterval, int inFlag) {
        EViewEntry ade = new EViewEntry(type, inUrl, inDTime, inHeight,
                inWidth, inInterval, inFlag);
        Message msg = mWindLoop.obtainMessage();
        msg.what = F_WHANDLER_OPEN_ADD;
        msg.obj = ade;
        mWindLoop.sendMessage(msg);
    }

    public void closeAd() {
        Message msg = mWindLoop.obtainMessage();
        msg.what = F_WHANDLER_AD_CLOSE;
        mWindLoop.sendMessage(msg);
    }

    public void loadTop(EBrwViewEntry inEntry) {
        int viewHeight = Compat.WRAP;
        if (inEntry.mHeight > 0) {
            viewHeight = inEntry.mHeight;
        }
        if (null == mTopView) {
            mTopView = new EBrowserView(mContext, EBrwViewEntry.VIEW_TYPE_TOP,
                    this);
            mTopView.setId(VIEW_TOP);
            LayoutParams topParm = new LayoutParams(Compat.FILL, viewHeight);
            topParm.gravity = Gravity.TOP;
            mTopView.setLayoutParams(topParm);
            mTopView.setVisibility(INVISIBLE);
            addView(mTopView);
            mTopView.init();
        } else {
            mTopView.getLayoutParams().height = viewHeight;
            requestLayout();
        }
        mTopView.setQuery(inEntry.mQuery);
        switch (inEntry.mDataType) {
            case EBrwViewEntry.WINDOW_DATA_TYPE_URL:
                mTopView.newLoadUrl(inEntry.mUrl);
                break;
            case EBrwViewEntry.WINDOW_DATA_TYPE_DATA:
                mTopView.newLoadData(inEntry.mData);
                break;
            case EBrwViewEntry.WINDOW_DATA_TYPE_DATA_URL:
                String date1 = ACEDes.decrypt(inEntry.mUrl, mContext, false,
                        inEntry.mData);
                mTopView.loadDataWithBaseURL(inEntry.mUrl, date1,
                        EBrowserView.CONTENT_MIMETYPE_HTML,
                        EBrowserView.CONTENT_DEFAULT_CODE, inEntry.mUrl);
                break;
        }
    }

    public void loadBottom(EBrwViewEntry inEntry) {
        int viewHeight = Compat.WRAP;
        if (inEntry.mHeight > 0) {
            viewHeight = inEntry.mHeight;
        }
        if (null == mBottomView) {
            mBottomView = new EBrowserView(mContext,
                    EBrwViewEntry.VIEW_TYPE_BOTTOM, this);
            mBottomView.setId(VIEW_BOTTOM);
            LayoutParams bottomParm = new LayoutParams(Compat.FILL, viewHeight);
            bottomParm.gravity = Gravity.BOTTOM;
            mBottomView.setLayoutParams(bottomParm);
            mBottomView.setVisibility(INVISIBLE);
            addView(mBottomView);
            mBottomView.init();
        } else {
            mBottomView.getLayoutParams().height = viewHeight;
            requestLayout();
        }
        mBottomView.setQuery(inEntry.mQuery);
        switch (inEntry.mDataType) {
            case EBrwViewEntry.WINDOW_DATA_TYPE_URL:
                mBottomView.newLoadUrl(inEntry.mUrl);
                break;
            case EBrwViewEntry.WINDOW_DATA_TYPE_DATA:
                mBottomView.newLoadData(inEntry.mData);
                break;
            case EBrwViewEntry.WINDOW_DATA_TYPE_DATA_URL:
                String date1 = ACEDes.decrypt(inEntry.mUrl, mContext, false,
                        inEntry.mData);
                mBottomView.loadDataWithBaseURL(inEntry.mUrl, date1,
                        EBrowserView.CONTENT_MIMETYPE_HTML,
                        EBrowserView.CONTENT_DEFAULT_CODE, inEntry.mUrl);
                break;
        }
    }

    public void evaluateScript(EBrowserView inWhich, String inWindowName,
                               int inType, String inScript) {
        if (null == inWindowName || 0 == inWindowName.length()
                || inWindowName.equals(mName)) {
            switch (inType) {
                case EBrwViewEntry.VIEW_TYPE_MAIN:
                    addUriTask(mMainView, inScript);
                    break;
                case EBrwViewEntry.VIEW_TYPE_TOP:
                    if (null != mTopView) {
                        addUriTask(mTopView, inScript);
                    }
                    break;
                case EBrwViewEntry.VIEW_TYPE_BOTTOM:
                    if (null != mBottomView) {
                        addUriTask(mBottomView, inScript);
                    }
                    break;
            }
        } else {
            mBroWidget.evaluateScript(inWhich, inWindowName, inType, inScript);
        }
    }

    protected void onAppKeyPress(int keyCode) {
        if (null == mMainView) {
            return;
        }
        String js = "javascript:if(uexWindow.onKeyPressed){uexWindow.onKeyPressed("
                + keyCode + ");}";
        mMainView.loadUrl(js);
    }

    protected void onWindowUrlChange(String inWindName, String inUrl) {
        if (null == mMainView) {
            return;
        }
        String js = "javascript:if(uexWindow.onOAuthInfo){uexWindow.onOAuthInfo('"
                + inWindName + "','" + inUrl + "');}";
        mMainView.loadUrl(js);
    }

    protected void notifyVisibilityChanged(int visibility) {
        if (null == mMainView) {
            return;
        }
        String js = "javascript:if(uexWindow.onStateChange){uexWindow.onStateChange("
                + visibility + ");}";
        mMainView.loadUrl(js);
    }

    protected void onAppPause() {
        if (null == mMainView) {
            return;
        }
        mMainView.loadUrl(EUExScript.F_UEX_SCRIPT_APPPAUSE);
        pauseTimers();
        // EBrowserView.disablePlatformNotifications();

        boolean isRootWidget = mBroWidget
                .checkWidgetType(EBrowserWidget.F_WIDGET_POOL_TYPE_ROOT);
        if (isRootWidget) {
            mBrw.onAppPauseAnalytics(getRelativeUrl(), mPopTable);
        }
    }

    protected void onAppStop() {
        if (null == mMainView) {
            return;
        }
        mMainView.loadUrl(EUExScript.F_UEX_SCRIPT_APPSTOP);
    }

    protected void onAppResume() {
        if (null == mMainView) {
            return;
        }
        resumeTimers();
        // EBrowserView.enablePlatformNotifications();
        mMainView.loadUrl(EUExScript.F_UEX_SCRIPT_APPRESUME);

        boolean isRootWidget = mBroWidget
                .checkWidgetType(EBrowserWidget.F_WIDGET_POOL_TYPE_ROOT);
        if (isRootWidget) {
            mBrw.onAppResumeAnalytics(getRelativeUrl(), mPopTable);
        }
        executeOnResumeJS();
    }

    private void executeOnResumeJS() {
        if (mResumeJs != null && mResumeJs.size() != 0) {
            for (int i = 0; i < mResumeJs.size(); i++) {
                HashMap<String, Object> item = mResumeJs.get(i);
                String js = item.get("js").toString();
                Log.i("executeOnResumeJS", js);
                EBrowserView wv = (EBrowserView) item.get("wv");
                wv.loadUrl(js);
            }
            mResumeJs.clear();
            mResumeJs = null;
        }
    }

    protected void onWidgetResult(String callBack, String inResultInfo) {
        if (null != callBack) {
            String js = "javascript:if(typeof(" + callBack + ")!='undefined'){"
                    + callBack + "('" + inResultInfo + "')" + "}";
            mMainView.loadUrl(js);
        }
    }

    protected void notifyScreenOrientationChange(Configuration newConfig) {
        if (null == mMainView) {
            return;
        }
        int type = newConfig.orientation;
        if (type == Configuration.ORIENTATION_LANDSCAPE) {
            mMainView.loadUrl(EUExScript.F_UEX_SCRIPT_OC_LANDSCAPE);
        } else if (type == Configuration.ORIENTATION_PORTRAIT) {
            mMainView.loadUrl(EUExScript.F_UEX_SCRIPT_OC_PORTRAIT);
        }
    }

    protected void pushNotify(String function, String appType) {
        if (null == mMainView) {
            return;
        }
        String js = "javascript:" + function + "('" + appType + "');";
        mMainView.loadUrl(js);
    }

    public void uexOnAuthorize(String id) {
        if (null == mMainView) {
            return;
        }
        String js = "javascript:if(window.uexOnAuthorize){window.uexOnAuthorize('"
                + id + "')}";
        mMainView.loadUrl(js);
    }

    public void onLoadObfuscationData(String inUrl) {
        if (!isObfuscation()) {
            Toast.makeText(mContext, EUExUtil.getString("platform_no_configuration_decryption_permissions"), Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        mMainView.needToEncrypt(mMainView, BUtility.makeUrl(location(), inUrl),
                EBrowserHistory.UPDATE_STEP_ADD);
    }

    public void start(String url) {

        mMainView.start(url);
    }

    public void start1(String url) {

        mMainView.start1(url);
    }

    public void startWidget(WWidgetData inData, EWgtResultInfo inResult) {
        if (null == inData) {
            return;
        }
        mBrw.startWidget(inData, inResult);
    }

    public void needToEncrypt(String url) {

        mMainView.needToEncrypt(mMainView, url,
                EBrowserHistory.UPDATE_STEP_INIT);
    }

    public void newLoadData(String inDta) {

        mMainView.newLoadData(inDta);
    }

    public void loadDataWithBaseURL(String baseUrl, String data,
                                    String mimeType, String encoding, String historyUrl) {
        mMainView.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
    }

    public void onSetWindowFrameFinish() {

        mMainView.loadUrl(EUExScript.F_UEX_SCRIPT_SET_WINDOW_FRAME_END);
    }

    public void setQuery(int type, String query) {
        switch (type) {
            case EBrwViewEntry.VIEW_TYPE_MAIN:
                mMainView.setQuery(query);
                break;
            case EBrwViewEntry.VIEW_TYPE_TOP:
                mTopView.setQuery(query);
                break;
            case EBrwViewEntry.VIEW_TYPE_BOTTOM:
                mBottomView.setQuery(query);
                break;
        }
    }

    public void clearHistory() {
        mMainView.clearHistory();
        if (null != mTopView) {
            mTopView.clearHistory();
        }
        if (null != mBottomView) {
            mBottomView.clearHistory();
        }
    }

    public void clearCache(boolean flag) {
        mMainView.clearCache(true);
    }

    public void pauseTimers() {
        // if (null != mMainView) {
        // mMainView.pauseTimers();
        // }
        // if (null != mBottomView) {
        // mBottomView.pauseTimers();
        // }
        // if (null != mTopView) {
        // mTopView.pauseTimers();
        // }
    }

    public void resumeTimers() {
        // if (null != mMainView) {
        // mMainView.resumeTimers();
        // }
        // if (null != mBottomView) {
        // mBottomView.resumeTimers();
        // }
        // if (null != mTopView) {
        // mTopView.resumeTimers();
        // }
    }

    public void updateObfuscationHistroy(String inUrl, int step,
                                         boolean isObfuscation) {

        mObHistroy.update(inUrl, step, isObfuscation);
    }

    public EHistoryEntry getHistory(int step) {

        return mObHistroy.getHistory(step);
    }

    public EBrowser getBrowser() {

        return mBrw;
    }

    public void clearObfuscationHistroy() {
        mObHistroy.clear();
    }

    public String getOpener() {

        return mBroWidget.getResult().getOpener();
    }

    public boolean canGoBack() {
        if (isObfuscation()) {
            return mObHistroy.canGoBack();
        }
        return mMainView.canGoBack();
    }

    public boolean isObfuscation() {
        WWidgetData wgt = getWidget();
        if (null == wgt) {
            return false;
        }
        return 1 == getWidget().m_obfuscation ? true : false;
    }

    public boolean canGoForward() {
        if (isObfuscation()) {
            return mObHistroy.canGoForward();
        }
        return mMainView.canGoForward();
    }

    public void goBack() {
        Message msg = mWindLoop.obtainMessage();
        msg.what = F_WHANDLER_HISTROY_BACK;
        mWindLoop.sendMessage(msg);
    }

    public void goForward() {
        Message msg = mWindLoop.obtainMessage();
        msg.what = F_WHANDLER_HISTROY_FORWARD;
        mWindLoop.sendMessage(msg);
    }

    public void windowGoBack(int inAnimitionID, long duration) {

        mBroWidget.windowGoBack(this, inAnimitionID, duration);
    }

    public void windowGoForward(int inAnimitionID, long duration) {

        mBroWidget.windowGoForward(this, inAnimitionID, duration);
    }

    public void refresh() {

        mMainView.reload();
    }

    protected String location() {
        String url = mMainView.getUrl();
        if (null == url) {
            return null;
        }
        int index = url.indexOf("?");
        if (-1 != index) {
            url = url.substring(0, index);
        }
        return url;
    }

    // url width Query
    protected String getAbsoluteUrl() {

        return mMainView.getUrl();
    }

    protected String getRelativeUrl() {

        return mMainView.getRelativeUrl();
    }

    public int getWindPoType() {
        return mWindPoType;
    }

    public void setWindPoType(int flag) {
        mWindPoType = flag;
    }

    public boolean checkWindPoType(int flag) {

        return mWindPoType == flag;
    }

    public EBrowserView getTopView() {
        return mTopView;
    }

    public EBrowserView getMainView() {
        return mMainView;
    }

    public EBrowserView getBottomView() {
        return mBottomView;
    }

    public WWidgetData getWidget() {

        return mBroWidget.getWidget();
    }

    public WWidgetData getRootWidget() {

        return mBroWidget.getRootWidget();
    }

    public EBrowserWidget getWGT(String app_id) {

        return mBrw.getWidget(app_id);
    }

    public void clearPopQue() {
        mPreQueue.clear();
    }

    public String getName() {
        return mName;
    }

    public void setName(String inWindowName) {
        mName = inWindowName;
    }

    public int getDateType() {
        return mDateType;
    }

    public void setDateType(int dateType) {
        mDateType = dateType;
    }

    public boolean checkDateType(int dateType) {

        return mDateType == dateType;
    }

    public Map<String, EBrowserView> getAllPopOver() {

        return mPopTable;
    }

    public int getAnimId() {
        return mAnimId;
    }

    public void setAnimId(int inAnimId) {
        mAnimId = inAnimId;
    }

    public boolean isAnimFill() {
        return mAnimFill;
    }

    public void setAnimFill(boolean flag) {
        mAnimFill = flag;
    }

    public boolean isHidden() {
        return mHidden;
    }

    public void setHidden(boolean flag) {
        mHidden = flag;
    }

    public boolean isLockBackKey() {
        return mLockBackKey;
    }

    public void setLockBackKey(boolean flag) {
        mLockBackKey = flag;
    }

    public boolean isLockMenuKey() {
        return mLockMenuKey;
    }

    public void setLockMenuKey(boolean flag) {
        mLockMenuKey = flag;
    }

    public void toast(int type, int location, String inMsg, int inDuration) {
        EViewEntry en = new EViewEntry(type, location, inMsg, inDuration);
        Message msg = mWindLoop.obtainMessage();
        msg.what = F_WHANDLER_TOAST_SHOW;
        msg.obj = en;
        mWindLoop.sendMessage(msg);
    }

    public void closeToast() {
        Message msg = mWindLoop.obtainMessage();
        msg.what = F_WHANDLER_TOAST_CLOSE;
        mWindLoop.sendMessage(msg);
    }

    protected void onPageStarted(EBrowserView view, String url) {
        if (null != mWinRegist) {
            EBrowserWindow win = mBroWidget.getEBrowserWindow(mWinRegist);
            if (null != win) {
                win.onUrlChange(mName, url);
            }
        }
        mLockBackKey = false;
        mLockMenuKey = false;
        mBroWidget.notifyWindowStart(this, view, url);
    }

    protected void onUrlChange(String windName, String url) {

        onWindowUrlChange(windName, url);
    }

    private void addViewFinish(int type) {
        if (null != mAddView) {
            mAddView.loadUrl(EUExScript.F_UEX_SCRIPT_SELF_FINISH);
        }
    }

    private boolean popOverFinish(String name, int type) {
        EBrowserView popView = mPopTable.get(name);
        boolean isHasPop = false;
        if (null != popView) {
            popView.loadUrl(EUExScript.F_UEX_SCRIPT_SELF_FINISH);
            String popName = name;
            String popUrl = popView.getUrl();
            evaluateScript(mMainView, "root", 0, "javascript:if(uexWindow.onPopoverLoadFinishInRootWnd){uexWindow.onPopoverLoadFinishInRootWnd('" + popName + "','" + popUrl + "');}");
            isHasPop = true;
        }
        if (mMultiPopTable != null && mMultiPopTable.size() > 0) {
            for (Map.Entry<String, ArrayList<EBrowserView>> entry : mMultiPopTable
                    .entrySet()) {
                ArrayList<EBrowserView> temp = entry.getValue();
                if (null != temp && temp.size() > 0) {
                    for (int i = 0; i < temp.size(); i++) {
                        if ((temp.get(i).getName()).equals(name)) {
                            temp.get(i).loadUrl(EUExScript.F_UEX_SCRIPT_SELF_FINISH);
                            isHasPop = true;
                            String popName = name;
                            String popUrl = temp.get(i).getUrl();
                            evaluateScript(mMainView, "root", 0, "javascript:if(uexWindow.onPopoverLoadFinishInRootWnd){uexWindow.onPopoverLoadFinishInRootWnd('" + popName + "','" + popUrl + "');}");
                        }
                    }
                }
            }
        }
        if (isHasPop && checkFlag(EBrowserWindow.F_WINDOW_FLAG_OPPOP)) {
            mPreQueue.remove(name);
            if (checkFlag(EBrowserWindow.F_WINDOW_FLAG_OPPOP_END)) {
                if (mPreQueue.isEmpty()) {
                    return true;
                }
            } else {
                ;
            }
        }
        return false;
    }

    private void topFinish() {
        mMainView.loadUrl(EUExScript.F_UEX_SCRIPT_TOP_FINISH);
    }

    private void bottomFinish() {
        mMainView.loadUrl(EUExScript.F_UEX_SCRIPT_BOTTOM_FINISH);
    }

    protected void selfFinish(EBrowserView target) {
        if (null != target) {
            target.loadUrl(EUExScript.F_UEX_SCRIPT_SELF_FINISH);
        } else {
            mMainView.loadUrl(EUExScript.F_UEX_SCRIPT_SELF_FINISH);
        }
    }

    protected void onPageFinished(EBrowserView target, String url) {


        int type = target.getType();
        switch (type) {
            case EBrwViewEntry.VIEW_TYPE_TOP:
                topFinish();
                break;
            case EBrwViewEntry.VIEW_TYPE_BOTTOM:
                bottomFinish();
                break;
            case EBrwViewEntry.VIEW_TYPE_MAIN:
                if (checkFlag(EBrowserWindow.F_WINDOW_FLAG_OPPOP)) {
                    selfFinish(target);
                    return;
                }
                selfFinish(target);
                mBroWidget.notifyWindowFinish(this, target, url);
                // selfFinish(target);
                ((EBrowserActivity) mContext).loadByOtherApp();

                break;
            case EBrwViewEntry.VIEW_TYPE_ADD:
                addViewFinish(0);
                break;
            case EBrwViewEntry.VIEW_TYPE_POP:
                String name = target.getName();
//            target.setVisibility(VISIBLE);
                boolean isFinish = popOverFinish(name, 0);
                if (isFinish) {
                    mBroWidget.notifyWindowFinish(this, target, url);
                }
                break;
        }

    }

    private void notifyTopShown() {
        mMainView.loadUrl(EUExScript.F_UEX_SCRIPT_TOP_SHOW);
    }

    private void notifyBottomShown() {
        mMainView.loadUrl(EUExScript.F_UEX_SCRIPT_BOTTOM_SHOW);
    }

    public void setEBrowserWidget(EBrowserWidget ewidget) {

        mBroWidget = ewidget;
    }

    public EBrowserWidget getEBrowserWidget() {
        return mBroWidget;
    }

    public boolean isOAuth() {
        return mOAuth;
    }

    public void setOAuth(boolean flag) {
        mOAuth = flag;
    }

    public boolean isPrevWindowWillHidden() {

        return mPrevWillHidden;
    }

    public void setPrevWindowWillHidden(boolean flag) {

        mPrevWillHidden = flag;
    }

    public void setShouldOpenUrlInSystem(boolean flag) {
        mMainView.setShouldOpenInSystem(flag);
    }

    public int getWidgetType() {

        return mBroWidget.getWidgetType();
    }

    public void registUrlChangeNotify(String windowName) {

        mWinRegist = windowName;
    }

    public long getAnimDuration() {

        return mAnimDuration;
    }

    public void setAnimDuration(long animDuration) {

        mAnimDuration = animDuration;
    }

    public void setSupportZoom() {
        mMainView.setSupportZoom();
    }

    protected void stopLoad() {
        if (mMainView==null){
            return;
        }
        mMainView.stopLoading();
        for (Map.Entry<String, EBrowserView> entry : mPopTable.entrySet()) {
            EBrowserView temp = entry.getValue();
            temp.stopLoading();
        }
        for (Map.Entry<String, ArrayList<EBrowserView>> entry : mMultiPopTable
                .entrySet()) {
            ArrayList<EBrowserView> temp = entry.getValue();
            if (null != temp && temp.size() > 0) {
                for (int i = temp.size() - 1; i >= 0; i--) {
                    temp.get(i).stopLoading();
                }
            }
        }
        if (mBottomView != null) {
            mBottomView.stopLoading();
        }
        if (mTopView != null) {
            mTopView.stopLoading();
        }
        if (null != mAddView) {
            mAddView.stopLoading();
        }
    }

    public void reset() {
        if (mMainView==null){
            return;
        }
        mMainView.reset();
        if (null != mTopView) {
            mTopView.reset();
        }
        if (null != mBottomView) {
            mBottomView.reset();
        }
        mAnimId = EBrowserAnimation.ANIM_ID_NONE;
        mAnimDuration = EBrowserAnimation.defaultDuration;
        mBroWidget = null;
        mName = null;
        mOAuth = false;
        mAnimFill = false;
        mHidden = false;
        mLockBackKey = false;
        mLockMenuKey = false;
        mPrevWillHidden = false;
        mWinRegist = null;
        clearFlag();
        hCloseSlibing(1);
        hCloseSlibing(2);
        setWindPoType(F_WINDOW_FLAG_NONE);
        clearObfuscationHistroy();
        for (Map.Entry<String, EBrowserView> entry : mPopTable.entrySet()) {
            EBrowserView temp = entry.getValue();
            View parent = (View) temp.getParent();
            removeView(parent);
            temp.destroy();
        }
        mPopTable.clear();
        for (Map.Entry<String, ArrayList<EBrowserView>> entry : mMultiPopTable
                .entrySet()) {
            ArrayList<EBrowserView> temp = entry.getValue();
            if (null != temp && temp.size() > 0) {
                View parent = (View) temp.get(0).getParent();
                removeView(parent);
                for (int i = temp.size() - 1; i >= 0; i--) {
                    temp.get(i).destroy();
                }
            }
        }
        mMultiPopTable.clear();

        if (null != mAddView) {
            removeView(mAddView);
            mAddView.destroy();
            mAddView = null;
        }
        ViewGroup parent = (ViewGroup) getParent();
        if (null != parent) {
            parent.removeView(this);
        }
        if (mResumeJs != null) {
            mResumeJs.clear();
            mResumeJs = null;
        }
        clearViewList();
    }

    private void clearViewList() {
        for (View view : viewList) {
            if (view != null) {
                removeView(view);
            }
        }
        viewList.clear();
    }

    private void removeViewList(View view) {
        viewList.remove(view);
        removeView(view);
    }

    public void destory() {
        stopLoop();
        stopLoad();
        if (mMainView==null){
            return;
        }
        mMainView.clearCache(false);
        mMainView.destroy();
        if (null != mTopView) {
            mTopView.destroy();
        }
        if (null != mBottomView) {
            mBottomView.destroy();
        }
        for (Map.Entry<String, EBrowserView> entry : mPopTable.entrySet()) {
            EBrowserView temp = entry.getValue();
            if (null != temp) {
                temp.destroy();
            }
        }
        mPopTable.clear();
        for (Map.Entry<String, ArrayList<EBrowserView>> entry : mMultiPopTable
                .entrySet()) {

            ArrayList<EBrowserView> temp = entry.getValue();
            if (null != temp && temp.size() > 0) {
                for (int i = temp.size() - 1; i >= 0; i--) {

                    temp.get(i).destroy();
                }
            }
        }
        mMultiPopTable.clear();
        mMultiPopPager.clear();
        if (null != mAddView) {
            mAddView.destroy();
        }
        removeAllViews();
        ViewParent parent = getParent();

        if (null != parent) {
            ((EBrowserWidget) parent).removeView(this);
        }
        mTopView = null;
        mMainView = null;
        mBottomView = null;
        mPopTable = null;
        mMultiPopTable = null;
        mAddView = null;
        mBounceView = null;
        mBroWidget = null;
        mObHistroy = null;
        mName = null;
        mContext = null;
        mWinRegist = null;
        mAddViewTimer = null;
    }

    public void addUriTask(EBrowserView target, String uri) {
        EUriTask task = new EUriTask(target, uri);
        Message msg = mWindLoop.obtainMessage();
        msg.obj = task;
        msg.what = F_WHANDLER_ADD_URL_TARGET;
        mWindLoop.sendMessage(msg);
    }

    public void addUriTaskAsyn(EBrowserView target, String uri) {
        if (mResumeJs == null) {
            mResumeJs = new ArrayList<HashMap<String, Object>>();
        }
        HashMap<String, Object> item = new HashMap<String, Object>();
        item.put("js", uri);
        item.put("wv", target);
        mResumeJs.add(item);
    }

    public void addDialogTask(EDialogTask target) {
        Message msg = mWindLoop.obtainMessage();
        msg.what = F_WHANDLER_DIALOG;
        msg.obj = target;
        mWindLoop.sendMessage(msg);
    }

    public void addBounceTask(EViewEntry target, int what) {
        Message msg = mWindLoop.obtainMessage();
        msg.what = F_WHANDLER_BOUNCE_TASK;
        msg.obj = target;
        msg.arg1 = what;
        mWindLoop.sendMessage(msg);
    }

    public void stopLoop() {
        int len = TOTAL;
        for (int i = 0; i < len; ++i) {
            mWindLoop.removeMessages(i);
        }
    }

    private int getPopMinIndex(View cru) {
        int l = -1;
        for (Map.Entry<String, EBrowserView> entry : mPopTable.entrySet()) {
            EBrowserView bv = entry.getValue();
            View parent = (View) bv.getParent();
            if (cru == parent) {
                continue;
            }
            int index = indexOfChild(parent);
            if (l < 0) {
                l = index;
                continue;
            }
            if (index < l) {
                l = index;
            }
        }
        return l;
    }

    private void hOpenSlibing(EBrwViewEntry entry) {
        switch (entry.mType) {
            case EBrwViewEntry.VIEW_TYPE_TOP:
                loadTop(entry);
                break;
            case EBrwViewEntry.VIEW_TYPE_BOTTOM:
                loadBottom(entry);
                break;
        }
    }

    private void hShowSlibing(int type) {
        switch (type) {
            case EBrwViewEntry.VIEW_TYPE_TOP:
                if (null != mTopView) {
                    mTopView.setVisibility(VISIBLE);
                    notifyTopShown();
                }
                break;
            case EBrwViewEntry.VIEW_TYPE_BOTTOM:
                if (null != mBottomView) {
                    mBottomView.setVisibility(VISIBLE);
                    notifyBottomShown();
                }
                break;
        }
    }

    private void hCloseSlibing(int type) {
        switch (type) {
            case 1:
                if (null != mTopView) {
                    mTopView.setVisibility(INVISIBLE);
                }
                break;
            case 2:
                if (null != mBottomView) {
                    mBottomView.setVisibility(INVISIBLE);
                }
                break;
        }
    }

    private void hToastShow(EViewEntry entry) {
        if (null == mToast) {
            mToast = new EBrowserToast(mContext);
            addView(mToast);
        }
        LayoutParams parm = new LayoutParams(Compat.WRAP, Compat.WRAP);
        switch (entry.location) {
            case EBrowserToast.TOAST_LOCATION_LEFT_TOP:
                parm.gravity = Gravity.LEFT | Gravity.TOP;
                parm.topMargin = 10;
                parm.leftMargin = 5;
                break;
            case EBrowserToast.TOAST_LOCATION_TOP:
                parm.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
                parm.topMargin = 10;
                break;
            case EBrowserToast.TOAST_LOCATION_RIGHT_TOP:
                parm.gravity = Gravity.RIGHT | Gravity.TOP;
                parm.topMargin = 10;
                parm.rightMargin = 5;
                break;
            case EBrowserToast.TOAST_LOCATION_RIGHT:
                parm.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
                parm.rightMargin = 5;
                break;
            case EBrowserToast.TOAST_LOCATION_RIGHT_BOTTOM:
                parm.gravity = Gravity.RIGHT | Gravity.BOTTOM;
                parm.bottomMargin = 10;
                parm.rightMargin = 5;
                break;
            case EBrowserToast.TOAST_LOCATION_BOTTOM:
                parm.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                parm.bottomMargin = 10;
                break;
            case EBrowserToast.TOAST_LOCATION_BOTTOM_LEFT:
                parm.gravity = Gravity.LEFT | Gravity.BOTTOM;
                parm.bottomMargin = 10;
                parm.leftMargin = 5;
                break;
            case EBrowserToast.TOAST_LOCATION_LEFT:
                parm.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
                parm.leftMargin = 5;
                break;
            case EBrowserToast.TOAST_LOCATION_MIDDLE:
            default:
                parm.gravity = Gravity.CENTER;
                break;
        }
        mToast.setMsg(entry.msg);
        mToast.setLayoutParams(parm);
        mToast.setVisibility(VISIBLE);
        if (0 == entry.type) {
            mToast.hiddenProgress();
        } else {
            mToast.showProgress();
        }
        bringChildToFront(mToast);
        if (entry.duration > 0) {
            Message msg = mWindLoop.obtainMessage(F_WHANDLER_TOAST_CLOSE);
            mWindLoop.sendMessageDelayed(msg, entry.duration);
        }
    }

    private void hPopOverOpen(EBrwViewEntry entity) {
        if (checkPop(entity)) {
            return;
        }
        EBrowserView eView = new EBrowserView(mContext, entity.mType, this);
//		eView.setVisibility(INVISIBLE);
        eView.setName(entity.mViewName);
        eView.setRelativeUrl(entity.mRelativeUrl);
        eView.setDateType(entity.mDataType);
        LayoutParams newParm = new LayoutParams(entity.mWidth, entity.mHeight);
        newParm.gravity = Gravity.NO_GRAVITY;
        newParm.leftMargin = entity.mX;
        newParm.topMargin = entity.mY;
        newParm.bottomMargin = entity.mBottom;
        EBounceView bounceView = new EBounceView(mContext);
        EUtil.viewBaseSetting(bounceView);
        bounceView.setLayoutParams(newParm);
        bounceView.addView(eView);
        addView(bounceView);
        eView.setHWEnable(entity.mHardware);
        if (entity.checkFlag(EBrwViewEntry.F_FLAG_SHOULD_OP_SYS)) {
            eView.setShouldOpenInSystem(true);
        }
        if (!entity.hasExtraInfo) {
            if (entity.checkFlag(EBrwViewEntry.F_FLAG_OPAQUE)) {
                eView.setOpaque(true);
            } else {
                eView.setOpaque(false);
            }
        }
        if (entity.hasExtraInfo) {
            /** wanglei del 20151124*/
//            eView.setBrwViewBackground(entity.mOpaque, entity.mBgColor, "");
            /** wanglei add 20151124*/
            bounceView.setBounceViewBackground(entity.mOpaque, entity.mBgColor, "", eView);
        }
        /** wanglei add 20151124*/
        eView.setBackgroundColor(Color.TRANSPARENT);
        if (entity.checkFlag(EBrwViewEntry.F_FLAG_OAUTH)) {
            eView.setOAuth(true);
        }
        if (entity.checkFlag(EBrwViewEntry.F_FLAG_WEBAPP)) {
            eView.setWebApp(true);
        }
        eView.setQuery(entity.mQuery);
        eView.init();
        eView.setDownloadCallback(entity.mDownloadCallback);
        eView.setUserAgent(entity.mUserAgent);
        eView.setExeJS(entity.mExeJS);
        if (entity.checkFlag(EBrwViewEntry.F_FLAG_GESTURE)) {
            eView.setSupportZoom();
        }
        if (entity.mFontSize > 0) {
            eView.setDefaultFontSize(entity.mFontSize);
        }
        mPopTable.put(entity.mViewName, eView);
        if (checkFlag(EBrowserWindow.F_WINDOW_FLAG_OPPOP)) {
            mPreQueue.add(entity.mViewName);
        }
        switch (entity.mDataType) {
            case EBrwViewEntry.WINDOW_DATA_TYPE_URL:
//			if (entity.checkFlag(EBrwViewEntry.F_FLAG_OBFUSCATION)) {
                if ((getWidget().m_obfuscation == 1) && !entity.checkFlag(EBrwViewEntry.F_FLAG_WEBAPP)) {
                    eView.needToEncrypt(eView, entity.mUrl, 0);
                } else {
                    eView.newLoadUrl(entity.mUrl);
                }
                break;
            case EBrwViewEntry.WINDOW_DATA_TYPE_DATA:
                eView.newLoadData(entity.mData);
                break;
            case EBrwViewEntry.WINDOW_DATA_TYPE_DATA_URL:
                String date1 = ACEDes.decrypt(entity.mUrl, mContext, false,
                        entity.mData);
                eView.loadDataWithBaseURL(entity.mUrl, date1,
                        EBrowserView.CONTENT_MIMETYPE_HTML,
                        EBrowserView.CONTENT_DEFAULT_CODE, entity.mUrl);
                break;
        }
    }

    private void hSetPopOverFrame(EBrwViewEntry entity) {
        EBrowserView pop = mPopTable.get(entity.mViewName);
        if (pop == null) {
            return;
        }
        View vParent = (View) pop.getParent();
        LayoutParams lParam = new LayoutParams(entity.mWidth, entity.mHeight);
        lParam.gravity = Gravity.NO_GRAVITY;
        lParam.leftMargin = entity.mX;
        lParam.topMargin = entity.mY;
        vParent.setLayoutParams(lParam);
    }

    private void hPopOverClose(String popName) {
        EBrowserView target = mPopTable.get(popName);
        if (null != target) {
            mPopTable.remove(popName);
            if (target.supportZoom()) {
                target.destroyControl();
            }
            target.stopLoading();
            View parent = (View) target.getParent();
            removeView(parent);
            target.destroy();

            boolean isRootWidget = mBroWidget
                    .checkWidgetType(EBrowserWidget.F_WIDGET_POOL_TYPE_ROOT);
            if (isRootWidget) {
                mBrw.popCloseAnalytics(target.getRelativeUrl());
            }
            if (mChannelList != null) {
                for (int i = 0; i < mChannelList.size(); i++) {
                    HashMap<String, String> item = mChannelList.get(i);
                    if (item.containsKey(TAG_CHANNEL_WINNAME) && popName.equals(item.get(TAG_CHANNEL_WINNAME))) {
                        mChannelList.remove(i);
                    }
                }
            }
        }
    }

    private void hOpenAdd(EViewEntry entity) {
        if (null == mAddView) {
            mAddView = new EBrowserView(mContext, EBrwViewEntry.VIEW_TYPE_ADD,
                    this);
            LayoutParams addParm = new LayoutParams(entity.width, entity.height);
            switch (entity.type) {
                case EViewEntry.F_ADD_LOCATION_TOP: // top
                    addParm.gravity = Gravity.TOP | Gravity.LEFT;
                    break;
                case EViewEntry.F_ADD_LOCATION_MID: // full screen
                    // addParm.addRule(RelativeLayout.CENTER_IN_PARENT,
                    // RelativeLayout.TRUE);
                    break;
                case EViewEntry.F_ADD_LOCATION_BOTTOM: // bottom
                    addParm.gravity = Gravity.BOTTOM | Gravity.LEFT;
                    break;
                default:
                    addParm.gravity = Gravity.TOP | Gravity.LEFT;
                    break;
            }
            mAddView.setLayoutParams(addParm);
            mAddView.setVisibility(VISIBLE);
            if (entity.checkFlag(EBrwViewEntry.F_FLAG_SHOULD_OP_SYS)) {
                mAddView.setShouldOpenInSystem(true);
            } else {
                mAddView.setShouldOpenInSystem(false);
            }
            if (entity.checkFlag(EBrwViewEntry.F_FLAG_OPAQUE)) {
                mAddView.setOpaque(true);
            }
            addView(mAddView);
            mAddView.init();
        } else {
            LayoutParams addParm = new LayoutParams(entity.width, entity.height);
            switch (entity.type) {
                case EViewEntry.F_ADD_LOCATION_TOP: // top
                    addParm.gravity = Gravity.TOP | Gravity.LEFT;
                    break;
                case EViewEntry.F_ADD_LOCATION_MID: // full screen
                    // addParm.addRule(RelativeLayout.CENTER_IN_PARENT,
                    // RelativeLayout.TRUE);
                    break;
                case EViewEntry.F_ADD_LOCATION_BOTTOM: // bottom
                    addParm.gravity = Gravity.BOTTOM | Gravity.LEFT;
                    ;
                    break;
                default:
                    addParm.gravity = Gravity.TOP | Gravity.LEFT;
                    break;
            }
            mAddView.setLayoutParams(addParm);
        }
        mAddView.newLoadUrl(entity.url);
        if (entity.time > 0 && entity.interval > 0) {
            if (null == mAddViewTimer) {
                mAddViewTimer = new EAdViewTimer(entity.time, entity.interval) {
                    @Override
                    public void onShow() {
                        mAddView.setVisibility(VISIBLE);
                    }

                    @Override
                    public void onClose() {
                        mAddView.setVisibility(GONE);
                    }
                }.start();
            } else {
                mAddViewTimer.reStart(entity.time, entity.interval);
            }
        } else {
            if (null != mAddViewTimer) {
                mAddViewTimer.showAlway();
            }
        }
    }

    private void hCloseAdd() {
        if (null != mAddViewTimer) {
            mAddViewTimer.cancel();
            mAddView.stopLoading();
            removeView(mAddView);
            mAddView.destroy();
            mAddView = null;
        }
    }

    private void hBounceTask(EViewEntry bunceEnty, int what) {
        EBounceView BView = (EBounceView) bunceEnty.obj;
        switch (what) {
            case EViewEntry.F_BOUNCE_TASK_SHOW_BOUNCE_VIEW:
                BView.showBounceView(bunceEnty.type, bunceEnty.color,
                        bunceEnty.flag);
                break;
            case EViewEntry.F_BOUNCE_TASK_HIDDEN_BOUNCE_VIEW:
                BView.hiddenBounceView(bunceEnty.type);
                break;
            case EViewEntry.F_BOUNCE_TASK_RESET_BOUNCE_VIEW:
                BView.resetBounceView(bunceEnty.type);
                break;
            case EViewEntry.F_BOUNCE_TASK_SET_BOUNCE_VIEW:
                BView.setBounce(bunceEnty.flag != 0 ? true : false);
                break;
            case EViewEntry.F_BOUNCE_TASK_GET_BOUNCE_VIEW:
                BView.getBounce();
                break;
            case EViewEntry.F_BOUNCE_TASK_NOTIFY_BOUNCE_VIEW:
                BView.notifyBounceEvent(bunceEnty.type, bunceEnty.flag);
                break;
            case EViewEntry.F_BOUNCE_TASK_SET_BOUNCE_PARMS:
                BView.setBounceParms(bunceEnty.type, (JSONObject) bunceEnty.obj1,
                        bunceEnty.arg1);
                break;
            case EViewEntry.F_BOUNCE_TASK_TOP_BOUNCE_VIEW_REFRESH:
                BView.topBounceViewRefresh();
                break;
        }
    }

    public static final int F_WHANDLER_TOAST_SHOW = 0;
    public static final int F_WHANDLER_TOAST_CLOSE = 1;
    public static final int F_WHANDLER_SLIBING_CREATE = 2;
    public static final int F_WHANDLER_SLIBING_SHOW = 3;
    public static final int F_WHANDLER_SLIBING_CLOSE = 4;
    public static final int F_WHANDLER_HISTROY_BACK = 5;
    public static final int F_WHANDLER_HISTROY_FORWARD = 6;
    //	public static final int F_WHANDLER_POP_OPEN = 7;
//	public static final int F_WHANDLER_POP_CLOSE = 8;
    public static final int F_WHANDLER_POP_SET = 9;
    public static final int F_WHANDLER_ADD_URL_TARGET = 10;
    public static final int F_WHANDLER_OPEN_ADD = 11;
    public static final int F_WHANDLER_AD_CLOSE = 12;
    public static final int F_WHANDLER_DIALOG = 13;
    public static final int F_WHANDLER_ADD_VIEW = 14;
    public static final int F_WHANDLER_REMOVE_VIEW = 15;
    public static final int F_WHANDLER_BOUNCE_TASK = 16;

//	public static final int F_WHANDLER_MULTIPOP_OPEN = 17;

    public static final int F_WHANDLER_BRING_TO_FRONT = 18;
    public static final int F_WHANDLER_SEND_TO_BACK = 19;
    public static final int F_WHANDLER_INSERT_ABOVE = 20;
    public static final int F_WHANDLER_INSERT_BELOW = 21;
    public static final int F_WHANDLER_INSERT_POPOVER_ABOVE_POPOVER = 22;
    public static final int F_WHANDLER_INSERT_POPOVER_BELOW_POPOVER = 23;
    public static final int F_WHANDLER_BING_POPOVER_TO_FRONT = 24;
    public static final int F_WHANDLER_BRING_MULTIPOPOVER_TO_FRONT = 25;
    public static final int F_WHANDLER_SEND_POPOVER_TO_BACK = 26;
    public static final int F_WHANDLER_SEND_MULTIPOPOVER_TO_BACK = 27;
    public static final int F_WHANDLER_SET_VISIBLE = 28;
    public static final int F_WHANDLER_MULTIPOP_CLOSE = 29;
    public static final int F_WHANDLER_MULTIPOP_SELECTED_CHANGE = 30;
    public static final int F_SHOW_SOFTKEYBOARD = 31;
    public static final int F_WHANDLER_MULTIPOP_SET = 32;

    public static final int TOTAL = F_WHANDLER_SET_VISIBLE + 1;




    public class WindowHander extends Handler {

        public WindowHander(Looper loop) {
            super(loop);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case F_WHANDLER_TOAST_SHOW:// show
                    hToastShow((EViewEntry) msg.obj);
                    break;
                case F_WHANDLER_TOAST_CLOSE:// close
                    if (null != mToast) {
                        mToast.setVisibility(GONE);
                    }
                    break;
                case F_WHANDLER_SLIBING_CREATE:
                    hOpenSlibing((EBrwViewEntry) msg.obj);
                    break;
                case F_WHANDLER_SLIBING_SHOW:
                    hShowSlibing(msg.arg1);
                    break;
                case F_WHANDLER_SLIBING_CLOSE:
                    hCloseSlibing(msg.arg1);
                    break;
                case F_WHANDLER_HISTROY_BACK:
                    mMainView.goBack();
                    break;
                case F_WHANDLER_HISTROY_FORWARD:
                    mMainView.goForward();
                    break;
//			case F_WHANDLER_POP_OPEN:
//				hPopOverOpen((EBrwViewEntry) msg.obj);
//				break;
//			case F_WHANDLER_MULTIPOP_OPEN:
//				hMultiPopOverOpen((ArrayList<EBrwViewEntry>) msg.obj, msg.arg1);
//				break;
                case F_WHANDLER_POP_SET:
                    hSetPopOverFrame((EBrwViewEntry) msg.obj);
                    break;
//			case F_WHANDLER_POP_CLOSE:
//				hPopOverClose((String) msg.obj);
//				break;
                case F_WHANDLER_MULTIPOP_CLOSE:
                    hMultiPopOverClose((String) msg.obj);
                    break;
                case F_WHANDLER_MULTIPOP_SET:
                    hSetMultiPopOverFrame((EBrwViewEntry) msg.obj);
                    break;
                case F_WHANDLER_MULTIPOP_SELECTED_CHANGE:
                    hSetMuliPopOverSelected((String) msg.obj, msg.arg1);
                    break;
                case F_WHANDLER_ADD_URL_TARGET:
                    EUriTask task = (EUriTask) msg.obj;
                    task.exc();
                    break;
                case F_WHANDLER_OPEN_ADD:
                    hOpenAdd((EViewEntry) msg.obj);
                    break;
                case F_WHANDLER_AD_CLOSE:
                    hCloseAdd();
                    break;
                case F_WHANDLER_DIALOG:
                    EDialogTask dTask = (EDialogTask) msg.obj;
                    dTask.exc();
                    break;
                case F_WHANDLER_ADD_VIEW:
                    View childAdd = (View) msg.obj;
                    childAdd.setTag(EViewEntry.F_PLUGIN_VIEW_TAG);
                    Animation anim = childAdd.getAnimation();
                    addView(childAdd);
                    if (null != anim) {
                        anim.start();
                    }
                    bringChildToFront(childAdd);
                    break;
                case F_WHANDLER_REMOVE_VIEW:
                    View children = (View) msg.obj;
                    Animation removeAnim = children.getAnimation();
                    if (null != removeAnim) {
                        removeAnim.start();
                    }
                    removeViewList(children);
                    msg.obj = null;
                    break;
                case F_WHANDLER_BOUNCE_TASK:
                    EViewEntry bunceEnty = (EViewEntry) msg.obj;
                    hBounceTask(bunceEnty, msg.arg1);
                    break;
                case F_WHANDLER_BRING_TO_FRONT:
                    View child = (View) msg.obj;
                    bringChildToFront(child);
                    invalidate();
                    break;
                case F_WHANDLER_SEND_TO_BACK:
                    View child1 = (View) msg.obj;
                    removeView(child1);
                    int it = getPopMinIndex(child1);
                    addView(child1, it);
                    break;
                case F_WHANDLER_INSERT_ABOVE:
                    EViewEntry a = (EViewEntry) msg.obj;
                    View c1 = (View) a.obj;
                    View c2 = (View) a.obj1;
                    removeView(c1);
                    int i = indexOfChild(c2);
                    addView(c1, i + 1);
                    break;
                case F_WHANDLER_INSERT_BELOW:
                    EViewEntry b = (EViewEntry) msg.obj;
                    View c3 = (View) b.obj;
                    View c4 = (View) b.obj1;
                    removeView(c3);
                    int j = indexOfChild(c4);
                    addView(c3, j);
                    break;
                case F_WHANDLER_INSERT_POPOVER_ABOVE_POPOVER:
                    EViewEntry c = (EViewEntry) msg.obj;
                    View bn1 = (View) c.obj;
                    View bn2 = (View) c.obj1;
                    removeView(bn1);
                    int i2 = indexOfChild(bn2);
                    addView(bn1, i2 + 1);
                    break;
                case F_WHANDLER_INSERT_POPOVER_BELOW_POPOVER:
                    EViewEntry d = (EViewEntry) msg.obj;
                    View bo1 = (View) d.obj;
                    View bo2 = (View) d.obj1;
                    removeView(bo1);
                    int j2 = indexOfChild(bo2);
                    addView(bo1, j2);
                    break;
                case F_WHANDLER_BING_POPOVER_TO_FRONT:
                    View bp = (View) msg.obj;
                    bringChildToFront(bp);
                    invalidate();
                    break;
                case F_WHANDLER_SEND_POPOVER_TO_BACK:
                    View bq = (View) msg.obj;
                    removeView(bq);
                    int ir = getPopMinIndex(bq);
                    addView(bq, ir);
                    break;
                case F_WHANDLER_SET_VISIBLE:
                    EViewEntry entry = (EViewEntry) msg.obj;
                    int visible = 0 == entry.flag ? View.VISIBLE : View.GONE;
                    View target = null;
                    if (entry.bArg1) {
                        target = mPopTable.get(entry.arg1);
                    } else {
                        target = EBrowserWindow.this;
                    }
                    if (null != target) {
                        target.setVisibility(visible);
                    }
                    break;
                case F_SHOW_SOFTKEYBOARD:
                    try {
                        InputMethodManager imm = (InputMethodManager) mContext
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm.isActive()) {
                            imm.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    private void hMultiPopOverOpen(final ArrayList<EBrwViewEntry> entitys, final int index) {
        final ArrayList<EBounceView> viewList = new ArrayList<EBounceView>();
        EBrwViewEntry mainEntry = entitys.get(0);
        Log.d("multi", "entitys num:" + entitys.size());
        if (checkMultiPop(entitys)){
            return;
        }
        EBrowserView parentBrowerview = new EBrowserView(mContext,
                mainEntry.mType, this);
        parentBrowerview.setVisibility(VISIBLE);
        parentBrowerview.setName(mainEntry.mViewName);
        parentBrowerview.setRelativeUrl(mainEntry.mRelativeUrl);
        parentBrowerview.setDateType(mainEntry.mDataType);
        LayoutParams newParm = new LayoutParams(mainEntry.mWidth,
                mainEntry.mHeight);
        // newParm.leftMargin = entity.mX;
        // newParm.topMargin = entity.mY;
        int parentRight = getRight();
        int parentBottom = getBottom();
        int newRight = mainEntry.mX + mainEntry.mWidth;
        if (newRight > parentRight) {
            newParm.rightMargin = parentRight - newRight;
        } else {
            newParm.rightMargin = 0;
        }
        int newBottom = mainEntry.mY + mainEntry.mHeight;
        if (newBottom > parentBottom) {
            newParm.bottomMargin = parentBottom - newBottom;
        } else if (0 == newBottom) {
            mainEntry.mY = mainEntry.mY + 1;
            newParm.bottomMargin = 0;
        } else {
            newParm.bottomMargin = 0;
        }
        newParm.topMargin = mainEntry.mY;
        newParm.leftMargin = mainEntry.mX;
        EBounceView bounceView = new EBounceView(mContext);
        EUtil.viewBaseSetting(bounceView);
        bounceView.setLayoutParams(newParm);
        bounceView.addView(parentBrowerview);
        addView(bounceView);
        if (mainEntry.hasExtraInfo) {
            /** wanglei del 20151124*/
//            parentBrowerview.setBrwViewBackground(mainEntry.mOpaque, mainEntry.mBgColor, "");
            /** wanglei add 20151124*/
            bounceView.setBounceViewBackground(
                    mainEntry.mOpaque, mainEntry.mBgColor, "", parentBrowerview);
        } else {
            if (mainEntry.checkFlag(EBrwViewEntry.F_FLAG_OPAQUE)) {
                parentBrowerview.setOpaque(true);
            } else {
                parentBrowerview.setOpaque(false);
            }
        }
        parentBrowerview.setDownloadCallback(mainEntry.mDownloadCallback);
        parentBrowerview.init();

        for (int i = 1; i < entitys.size(); i++) {
            EBrwViewEntry entity = entitys.get(i);
            EBrowserView childView = new EBrowserView(mContext, entity.mType,
                    this);
            childView.setVisibility(VISIBLE);
            childView.setName(entity.mViewName);
            childView.setRelativeUrl(entity.mRelativeUrl);
            childView.setDateType(entity.mDataType);
            EBounceView bounceViewChild = new EBounceView(mContext);
            EUtil.viewBaseSetting(bounceViewChild);
            bounceViewChild.setLayoutParams(new LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
            if (entity.hasExtraInfo) {
                /** wanglei del 20151124*/
//                childView.setBrwViewBackground(entity.mOpaque, entity.mBgColor, "");
                /** wanglei add 20151124*/
                bounceViewChild.setBounceViewBackground(entity.mOpaque, entity.mBgColor, "", childView);
            }
            bounceViewChild.addView(childView);
            viewList.add(bounceViewChild);

        }
        BDebug.i("multi", "viewlist num:", viewList.size());
        LayoutParams pagerParm = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        ViewPager mPager = new ViewPager(mContext);
        mPager.setAdapter(new MultiPopAdapter(viewList));
        mPager.setCurrentItem(index);
        mPager.setOnPageChangeListener(new MyPageChangedListener(
                parentBrowerview.getName()));
        mPager.setLayoutParams(pagerParm);
        mPager.setBackgroundColor(Color.TRANSPARENT);
        parentBrowerview.addView(mPager);
        mMultiPopPager.put(parentBrowerview.getName(), mPager);
        final ArrayList<EBrowserView> list = new ArrayList<EBrowserView>();
        list.add(parentBrowerview);

        EBrowserView eBrowserView = loadOneOfMultiPop(viewList.get(index), entitys.get(index + 1), list);
        eBrowserView.setEBrowserViewChangeListener(new EBrowserView.OnEBrowserViewChangeListener() {
            @Override
            public void onPageFinish() {
                loadTheRestOfMultiPop(index, viewList, entitys, list);
            }
        });
        BDebug.i("multipop", index, "load...");
        mMultiPopTable.put(parentBrowerview.getName(), list);
    }

    private void loadTheRestOfMultiPop(int index, List<EBounceView> viewList, List<EBrwViewEntry> entries,
                                       ArrayList<EBrowserView> eBrowserViews) {
        for (int i = 0; i < viewList.size(); i++) {
            if (i == index) {
                continue;
            }
            loadOneOfMultiPop(viewList.get(i), entries.get(i + 1), eBrowserViews);
            BDebug.i("multipop", i, "load...");
        }
    }

    private EBrowserView loadOneOfMultiPop(EBounceView eBounceView, EBrwViewEntry entity, ArrayList<EBrowserView>
            eBrowserViews) {
        EBrowserView eView = eBounceView.mBrwView;
        if (entity.checkFlag(EBrwViewEntry.F_FLAG_SHOULD_OP_SYS)) {
            eView.setShouldOpenInSystem(true);
        }
        if (!entity.hasExtraInfo) {
            if (entity.checkFlag(EBrwViewEntry.F_FLAG_OPAQUE)) {
                eView.setOpaque(true);
            } else {
                eView.setOpaque(false);
            }
        }
        if (entity.checkFlag(EBrwViewEntry.F_FLAG_OAUTH)) {
            eView.setOAuth(true);
        }
        if (entity.checkFlag(EBrwViewEntry.F_FLAG_WEBAPP)) {
            eView.setWebApp(true);
        }
        eView.setQuery(entity.mQuery);
        eView.init();
        eView.setDownloadCallback(entity.mDownloadCallback);
        eView.setUserAgent(entity.mUserAgent);
        if (entity.checkFlag(EBrwViewEntry.F_FLAG_GESTURE)) {
            eView.setSupportZoom();
        }
        if (entity.mFontSize > 0) {
            eView.setDefaultFontSize(entity.mFontSize);
        }
        switch (entity.mDataType) {
            case EBrwViewEntry.WINDOW_DATA_TYPE_URL:
//				if (entity.checkFlag(EBrwViewEntry.F_FLAG_OBFUSCATION)) {
                if ((getWidget().m_obfuscation == 1) && !entity.checkFlag(EBrwViewEntry.F_FLAG_WEBAPP)) {
                    eView.needToEncrypt(eView, entity.mUrl, 0);
                } else {
                    eView.newLoadUrl(entity.mUrl);
                }
                break;
            case EBrwViewEntry.WINDOW_DATA_TYPE_DATA:
                eView.newLoadData(entity.mData);
                break;
            case EBrwViewEntry.WINDOW_DATA_TYPE_DATA_URL:
                String date1 = ACEDes.decrypt(entity.mUrl, mContext,
                        false, entity.mData);
                eView.loadDataWithBaseURL(entity.mUrl, date1,
                        EBrowserView.CONTENT_MIMETYPE_HTML,
                        EBrowserView.CONTENT_DEFAULT_CODE, entity.mUrl);
                break;
        }

        if (checkFlag(EBrowserWindow.F_WINDOW_FLAG_OPPOP)) {
            mPreQueue.add(entity.mViewName);
        }
        eBrowserViews.add(eView);
        return eView;
    }


    public void hSetMuliPopOverSelected(String obj, int arg1) {
        // TODO Auto-generated method stub
        ViewPager wvp = mMultiPopPager.get(obj);
        if (wvp != null) {
            wvp.setCurrentItem(arg1);
        }
    }

    private void hMultiPopOverClose(String obj) {
        // TODO Auto-generated method stub
        ArrayList<EBrowserView> list = mMultiPopTable.get(obj);
        if (null != list && list.size() > 1) {
            mMultiPopTable.remove(obj);
            EBrowserView mainWebview = list.get(0);
            View parent = (View) mainWebview.getParent();
            removeView(parent);
            for (EBrowserView ebv : list) {
                if (ebv.supportZoom()) {
                    ebv.destroyControl();
                }
                ebv.stopLoading();
                ebv.destroy();
            }

        }
        if (mMultiPopPager.get(obj) != null) {
            mMultiPopPager.remove(obj);
        }

    }

    public void openMultiPopover(EUExWindow callback,
                                 ArrayList<EBrwViewEntry> popEntrys, int inIndex) {
        // TODO Auto-generated method stub
        this.mWindowCallback = callback;
//		Message msg = mWindLoop.obtainMessage();
//		msg.what = F_WHANDLER_MULTIPOP_OPEN;
//		msg.obj = popEntrys;
//		msg.arg1 = inIndex;
//		mWindLoop.sendMessage(msg);

        hMultiPopOverOpen(popEntrys, inIndex);


    }

    public void setSelectedPopOverInMultiWindow(String name, int index) {
        // TODO Auto-generated method stub

        Message msg = mWindLoop.obtainMessage();
        msg.what = F_WHANDLER_MULTIPOP_SELECTED_CHANGE;
        msg.obj = name;
        msg.arg1 = index;
        mWindLoop.sendMessage(msg);
    }

    public void closeMultiPopover(String multiPopName) {
        // TODO Auto-generated method stub
        Message msg = mWindLoop.obtainMessage();
        msg.what = F_WHANDLER_MULTIPOP_CLOSE;
        msg.obj = multiPopName;
        mWindLoop.sendMessage(msg);

    }

    public boolean setPopoverVisibility(String popName, int visible) {
        int isVisible = ((0 == visible) ? View.GONE : View.VISIBLE);
        View vTarget = null;
        EBrowserView bv = mPopTable.get(popName);
        if (null != bv) {
            vTarget = (View) bv.getParent();
        } else {

            ArrayList<EBrowserView> list = mMultiPopTable.get(popName);
            if (null != list && list.size() > 1) {
                EBrowserView mainWebview = list.get(0);

                if (mainWebview != null) {

                    vTarget = (View) mainWebview.getParent();
                }
            }
        }

        if (vTarget != null) {
            vTarget.setVisibility(isVisible);
            return true;
        }else{
            BDebug.e("target popover is not found");
        }
        return false;
    }


    /**
     * 设置window是否开启硬件加速
     *
     * @param flag
     */
    public void setWindowHWEnable(int flag) {
        getMainView().setHWEnable(flag);
    }

    /**
     * 设置popover是否开启硬件加速
     *
     * @param popName
     * @param flag
     */
    public void setPopoverHardwareEnable(String popName, int flag) {
        EBrowserView pop = mPopTable.get(popName);
        if (pop != null) {
            pop.setHWEnable(flag);
        }
    }


    public class MyPageChangedListener implements
            ViewPager.OnPageChangeListener {
        public String name;

        public MyPageChangedListener(String name) {
            this.name = name;
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

            // TODO Auto-generated method stub
            /*
             * 此方法是在状态改变的时候调用，其中arg0这个参数 有三种状态（0，1，2）。arg0
			 * ==1的时辰默示正在滑动，arg0==2的时辰默示滑动完毕了，arg0==0的时辰默示什么都没做。
			 * 当页面开始滑动的时候，三种状态的变化顺序为（1，2，0）
			 */

            if (mWindowCallback != null) {

                final JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(EUExCallback.F_JK_MULTIPOP_NAME, name);
                    jsonObject.put(EUExCallback.F_JK_MULTIPOP_STATE, arg0);
                    mWindowCallback.jsCallback(
                            EUExWindow.function_cbOpenMultiPopover, 1,
                            EUExCallback.F_C_JSON, jsonObject.toString());
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        }

        // 此方法是页面跳转完后得到调用，arg0是你当前选中的页面的Position（位置编号）。
        @Override
        public void onPageSelected(int arg0) {
            if (mWindowCallback != null) {

                final JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(EUExCallback.F_JK_MULTIPOP_NAME, name);
                    jsonObject.put(EUExCallback.F_JK_MULTIPOP_INDEX, arg0);
                    mWindowCallback.jsCallback(
                            EUExWindow.function_cbOpenMultiPopover, 0,
                            EUExCallback.F_C_JSON, jsonObject.toString());
                    ArrayList<EBrowserView> views = mMultiPopTable.get(name);
                    for (int i = 0; i < views.size(); i++) {
                        if (i == arg0 + 1) {
                            views.get(i).setNeedScroll(true);
                        } else {
                            views.get(i).setNeedScroll(false);
                        }
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }

		/*
         * 当页面在滑动的时候会调用此方法，在滑动被停止之前，此方法回一直得到 调用。其中三个参数的含义分别为： arg0
		 * :当前页面，及你点击滑动的页面 arg1:当前页面偏移的百分比 arg2:当前页面偏移的像素位置
		 */

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // TODO Auto-generated method stub
            if (mWindowCallback != null) {
                final JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(EUExCallback.F_JK_MULTIPOP_NAME, name);
                    jsonObject.put(EUExCallback.F_JK_MULTIPOP_INDEX, arg0);
                    jsonObject.put(EUExCallback.F_JK_MULTIPOP_MOVE_PERCENT,
                            arg1 * 100);
                    jsonObject.put(EUExCallback.F_JK_MULTIPOP_MOVE_PX, arg2);
                    mWindowCallback.jsCallback(
                            EUExWindow.function_cbOpenMultiPopover, 2,
                            EUExCallback.F_C_JSON, jsonObject.toString());
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    protected void onSizeChanged(int l, int t, int oldl, int oldt) {
        super.onSizeChanged(l, t, oldl, oldt);

        if (t < oldt) {
            isShowDialog = true;
        } else {
            isShowDialog = false;
        }

    }

    public void createProgressDialog(String title, String content,
                                     boolean isCancel) {
        if (mGlobalProDialog == null) {
            mGlobalProDialog = new ProgressDialog(mContext,ProgressDialog.THEME_HOLO_DARK);
        }
        mGlobalProDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        if (!TextUtils.isEmpty(title)){
            mGlobalProDialog.setTitle(title);
        }
        mGlobalProDialog.setMessage(content);
        mGlobalProDialog.setCancelable(isCancel);
        mGlobalProDialog.show();
    }

    public void destroyProgressDialog() {
        if (mGlobalProDialog != null) {
            mGlobalProDialog.dismiss();
            mGlobalProDialog = null;
        }
    }

    public void postGlobalNotification(String des) {
        String js = CALLBACK_POST_GLOBAL_NOTI + des + "');}";
        EWindowStack windowStack = mBroWidget.getWindowStack();
        ELinkedList<EBrowserWindow> eBrwWins = windowStack.getAll();

        EBrowserWindow leftSlidingWin = windowStack
                .getSlidingWind(EBrowserWindow.rootLeftSlidingWinName);
        if (leftSlidingWin != null) {
            leftSlidingWin.addUriTask(leftSlidingWin.mMainView, js);
        }
        EBrowserWindow rightSlidingWin = windowStack
                .getSlidingWind(EBrowserWindow.rootRightSlidingWinName);
        if (rightSlidingWin != null) {
            rightSlidingWin.addUriTask(rightSlidingWin.mMainView, js);
        }

        for (int i = 0; i < eBrwWins.size(); i++) {
            EBrowserWindow eBrwWin = eBrwWins.get(i);
            eBrwWin.addUriTask(eBrwWin.mMainView, js);
            Collection<EBrowserView> eBrwViews = eBrwWin.mPopTable.values();
            for (EBrowserView entry : eBrwViews) {
                entry.addUriTask(js);
            }
        }

        /*Collection<EBrowserView> eBrwViews = mPopTable.values();
        for (EBrowserView entry : eBrwViews) {
            entry.addUriTask(js);
        }*/
    }

    public void subscribeChannelNotification(String channelId,
                                             String callbackFunction, String type, String name) {
        if (mChannelList == null) {
            mChannelList = new ArrayList<HashMap<String, String>>();
        }
        if (hasChannel(channelId, name, callbackFunction)) {
            return;
        }
        HashMap<String, String> item = new HashMap<String, String>();
        item.put(TAG_CHANNEL_ID, channelId);
        item.put(TAG_CHANNEL_FUNNAME, callbackFunction);
        item.put(TAG_CHANNEL_TYPE, type);
        if (!TextUtils.isEmpty(name)) {
            item.put(TAG_CHANNEL_WINNAME, name);
        }
        mChannelList.add(item);
    }

    private boolean hasChannel(String channelId, String name, String callbackFunction) {
        for (HashMap<String,String> item:mChannelList) {
            if (channelId.equals(item.get(TAG_CHANNEL_ID))
                    && name.equals(item.get(TAG_CHANNEL_WINNAME))
                    && callbackFunction.equals(item.get(TAG_CHANNEL_FUNNAME))) {
                return true;
            }
        }
        return false;
    }

    public void publishChannelNotification(String channelId, String des, boolean isJson) {
        EWidgetStack eWidgetStack = mBroWidget.getWidgetStack();
        for (int w = 0; w < eWidgetStack.length(); w++) {
            EWindowStack windowStack = eWidgetStack.get(w).getWindowStack();

            //Sliding window
            EBrowserWindow leftSlidingWin = windowStack
                    .getSlidingWind(EBrowserWindow.rootLeftSlidingWinName);
            if (leftSlidingWin != null) {
                List<HashMap<String, String>> list = leftSlidingWin.mChannelList;
                if (list != null && list.size() != 0) {
                    setCallback(leftSlidingWin.mMainView, list, channelId, des,
                            isJson, WIN_TYPE_MAIN);
                }
            }
            EBrowserWindow rightSlidingWin = windowStack
                    .getSlidingWind(EBrowserWindow.rootRightSlidingWinName);
            if (rightSlidingWin != null) {
                List<HashMap<String, String>> list = rightSlidingWin.mChannelList;
                if (list != null && list.size() != 0) {
                    setCallback(rightSlidingWin.mMainView, list, channelId, des,
                            isJson, WIN_TYPE_MAIN);
                }
            }

            //normal window
            ELinkedList<EBrowserWindow> eBrwWins = windowStack.getAll();
            for (int i = 0; i < eBrwWins.size(); i++) {
                EBrowserWindow eBrwWin = eBrwWins.get(i);
                List<HashMap<String, String>> list = eBrwWin.mChannelList;
                if (list == null || list.size() == 0) {
                    continue;
                }
                setCallback(eBrwWin.mMainView, list, channelId, des, isJson, WIN_TYPE_MAIN);

                //popover window
                Collection<EBrowserView> eBrwViews = eBrwWin.mPopTable.values();
                for (EBrowserView entry : eBrwViews) {
                    setCallback(entry, list, channelId, des, isJson, WIN_TYPE_POP);
                }

                //multiPopover window
                if (eBrwWin.mMultiPopTable != null && eBrwWin.mMultiPopTable.size() > 0) {
                    for (Map.Entry<String, ArrayList<EBrowserView>> entry : eBrwWin.mMultiPopTable
                            .entrySet()) {
                        ArrayList<EBrowserView> temp = entry.getValue();
                        if (null != temp && temp.size() > 0) {
                            for (int j = 0; j < temp.size(); j++) {
                                setCallback(temp.get(j), list, channelId, des, isJson, WIN_TYPE_POP);
                            }
                        }
                    }
                }
            }
        }
    }

    private void setCallback(final EBrowserView brwView, List<HashMap<String, String>> list, String channelId, String data, boolean isJson, String type) {
        String js;
        for (int i = 0; i < list.size(); i++) {
            final HashMap<String, String> entry = list.get(i);
            if (channelId.equals(entry.get(TAG_CHANNEL_ID)) && type.equals(entry.get(TAG_CHANNEL_TYPE))) {
                js = CALLBACK_PUBLISH_GLOBAL_NOTI + entry.get(TAG_CHANNEL_FUNNAME) + "('"
                        + data + "')";
                if (isJson) {
                    js = CALLBACK_PUBLISH_GLOBAL_NOTI + entry.get(TAG_CHANNEL_FUNNAME) + "(" + data + ")";
                }
                if (type.equals(WIN_TYPE_POP) && brwView.getName().equals(entry.get(TAG_CHANNEL_WINNAME))) {
                    brwView.addUriTask(js);
                } else if (type.equals(WIN_TYPE_MAIN)) {
                    brwView.addUriTask(js);
                }
            }
        }
    }

    public void onLoadAppData(JSONObject json) {
        // TODO Auto-generated method stub

        if (null == mMainView) {

            return;
        }
        if (json != null) {

            String js = "javascript:if(typeof(uexWidget)!='undefined'&&uexWidget.onLoadByOtherApp)"
                    + "{uexWidget.onLoadByOtherApp('" + json.toString() + "');}";

            mMainView.addUriTask(js);

        }

    }

    public void showSoftKeyboard() {
        Message msg = mWindLoop.obtainMessage();
        msg.what = F_SHOW_SOFTKEYBOARD;
        mWindLoop.sendMessage(msg);
    }

    public void setSpaceEnable(SpaceClickListener listener) {
        mBroWidget.setSpaceEnable(listener);
    }

    public void closeAboveWndByName(String windowName) {
        ELinkedList<EBrowserWindow> eBrwWins = mBroWidget.getWindowStack()
                .getAll();
        if (eBrwWins.size() == 1) {
            ((EBrowserActivity) mContext).exitApp(true);
            return;
        }
        int index = getWindowPosition(eBrwWins, windowName);
        if (index == -1) {
            ((EBrowserActivity) mContext).exitApp(true);
        } else if (index == eBrwWins.size() - 1) {
            for (int i = 0; i < eBrwWins.size() - 1; i++) {
                EBrowserWindow eBrwWin = eBrwWins.get(i);
                if (eBrwWin != null) {
                    mBroWidget.onCloseWindow(eBrwWin);
                }
            }
        } else {
            for (int i = 0; i <= index; i++) {
                EBrowserWindow eBrwWin = eBrwWins.get(i);
                if (eBrwWin != null) {
                    mBroWidget.onCloseWindow(eBrwWin);
                }
            }
        }
    }

    private int getWindowPosition(ELinkedList<EBrowserWindow> eBrwWins, String windowName) {
        if (TextUtils.isEmpty(windowName)) {
            return -1;
        }
        for (int i = 0; i < eBrwWins.size(); i++) {
            EBrowserWindow eBrwWin = eBrwWins.get(i);
            String name = eBrwWin.getName();
            if (windowName.equals(name)) {
                return i;
            }
        }
        return -1;
    }
    public void setExeJS(String exeJS){
        mMainView.setExeJS(exeJS);
    }
    public void closeWindowByAnimation(Animation anim) {

        if (anim != null) {
            startAnimation(anim);
            anim.setAnimationListener(this);
        } else {
            setVisibility(GONE);
            if (null != mBroWidget) {
                mBroWidget.destoryWindow(this);
            }
        }

    }

    @Override
    public void onAnimationStart(Animation animation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        // TODO Auto-generated method stub
        new Handler().post(new Runnable() {
            public void run() {
                setVisibility(GONE);
                if (null != mBroWidget) {
                    mBroWidget.destoryWindow(EBrowserWindow.this);
                }
            }
        });
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        // TODO Auto-generated method stub


    }

    public void addUriTaskSpeci(String winName, String js) {
        Log.i(TAG, "winName = " + winName + ", js = " + js);
        EBrowserWindow eBrwWin = mBroWidget.getEBrowserWindow(winName);
        if (eBrwWin != null) {
            EBrowserView eBrwView = eBrwWin.mMainView;
            if (eBrwView != null && !eBrwView.beDestroy()) {
                eBrwView.addUriTask(js);
            }
        }
    }

    public void onSlidingWindowStateChanged(int position) {
        if (null == mMainView) {
            return;
        }
        String js = "javascript:if(typeof(uexWindow)!='undefined'&&uexWindow.onSlidingWindowStateChanged){uexWindow.onSlidingWindowStateChanged("
                + position + ");}";
        mMainView.loadUrl(js);
    }

    public void reloadWindow() {
        mMainView.reload();
        // popover
        Collection<EBrowserView> eBrwViews = mPopTable.values();
        for (EBrowserView entry : eBrwViews) {
            entry.reload();
        }
        // multiPopover
        if (mMultiPopTable != null && mMultiPopTable.size() > 0) {
            for (Map.Entry<String, ArrayList<EBrowserView>> entry : mMultiPopTable.entrySet()) {
                ArrayList<EBrowserView> temp = entry.getValue();
                if (null != temp && temp.size() > 0) {
                    for (int j = 0; j < temp.size(); j++) {
                        temp.get(j).reload();
                    }
                }
            }
        }
    }

    @Keep
    public Map<String, ViewPager> getMultiPopPagerMap(){
        return mMultiPopPager;
    }

    public void setUserAgent(String userAgent) {
        mMainView.setUserAgent(userAgent);
    }

    /**
     * 设置window是否进行下载回调
     *
     * @param flag
     */
    public void setDownloadCallback(int flag) {
        mMainView.setDownloadCallback(flag);
    }

    public void executeCbDownloadCallbackJs(EBrowserView eBrwView, int callbackType, String url, String userAgent,
                                            String contentDisposition, String mimetype, long contentLength) {
        try {
            DownloadCallbackInfoVO info = new DownloadCallbackInfoVO();
            info.setUrl(url);
            info.setUserAgent(userAgent);
            info.setContentDisposition(contentDisposition);
            info.setMimetype(mimetype);
            info.setContentLength(contentLength);
            if (callbackType == 1) {  // 1 下载回调给主窗口，前端自己下载
                String name = eBrwView.checkType(EBrwViewEntry.VIEW_TYPE_MAIN) ? "" : eBrwView.getName();
                info.setWindowName(name);
                String js = EUExWindow.SCRIPT_HEADER + "if("
                        + EUExWindow.function_cbDownloadCallback + "){"
                        + EUExWindow.function_cbDownloadCallback + "("
                        + DataHelper.gson.toJson(info) + ");}";
                mMainView.loadUrl(js);
            } else if (callbackType == 2) {  // 2 下载回调给当前窗口，前端自己下载;
                String js = EUExWindow.SCRIPT_HEADER + "if("
                        + EUExWindow.function_cbDownloadCallback + "){"
                        + EUExWindow.function_cbDownloadCallback + "("
                        + DataHelper.gson.toJson(info) + ");}";
                eBrwView.loadUrl(js);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
