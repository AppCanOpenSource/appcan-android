package org.zywx.wbpalmstar.engine.mpwindow;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import org.zywx.wbpalmstar.base.vo.WindowOptionsVO.MPWindowMenuVO;
import org.zywx.wbpalmstar.base.vo.WindowOptionsVO.MPWindowMenuVO.MPWindowMenuItemVO;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

/**
 * 公众号样式浮动菜单控制类
 */
public class MPPopMenu {

    /**
     * 浮动菜单点击的回调监听接口
     */
    public interface PopMenuClickListener {
		void onClick(String itemId);
	}

	private Context mContext;
	private PopupWindow mPopupWindow;
	private LinearLayout mMenuListLayout;
	private int mWidth, mHeight;
	private View mContainerView;
	private PopMenuClickListener mPopMenuClickListener;
	private MPWindowMenuVO mMenuVO;

	public MPPopMenu(Context context, MPWindowMenuVO menuVO, int width, int height, PopMenuClickListener popMenuClickListener) {
		this.mContext = context;
		this.mWidth = width;
		this.mHeight = height;
		mMenuVO = menuVO;
		mPopMenuClickListener = popMenuClickListener;
		mContainerView = LayoutInflater.from(context).inflate(EUExUtil.getResLayoutID("platform_mp_window_popmenu_layout"), null);
		//浮动菜单外层layout
		mMenuListLayout = (LinearLayout) mContainerView.findViewById(EUExUtil.getResIdID("platform_mp_window_menu_items_container"));
		//初始化浮动菜单内部的子项目
        initMenuSubItem();
		mMenuListLayout.setFocusableInTouchMode(true);
		mMenuListLayout.setFocusable(true);
		mPopupWindow = new PopupWindow(mContainerView, width == 0 ? LayoutParams.WRAP_CONTENT : width, height == 0 ? LayoutParams.WRAP_CONTENT : height);
	}

	// 下拉式 弹出 pop菜单 parent 右下角
	public void showAsDropDown(View parent) {
		mPopupWindow.setBackgroundDrawable(new ColorDrawable());
		mPopupWindow.showAsDropDown(parent);
		// 设置允许在外点击消失
		mPopupWindow.setOutsideTouchable(true);
		// 使其聚集
		mPopupWindow.setFocusable(true);
		// 刷新状态
		mPopupWindow.update();
		mPopupWindow.setOnDismissListener(new OnDismissListener() {
			// 在dismiss中恢复透明度
			@Override
			public void onDismiss() {
			}
		});
	}

	public void showAtLocation(View parent) {
		mPopupWindow.setBackgroundDrawable(new ColorDrawable());
		mContainerView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		int[] location = new int[2];
		int popupWidth = mContainerView.getMeasuredWidth();
		int popupHeight =  mContainerView.getMeasuredHeight();
		parent.getLocationOnScreen(location);
		int x = (location[0] + parent.getWidth() / 2) - popupWidth / 2;
		int y = location[1] - popupHeight;
		mPopupWindow.showAtLocation(parent, Gravity.NO_GRAVITY, x , y);
		// 设置允许在外点击消失
		mPopupWindow.setOutsideTouchable(true);
		// 使其聚集
		mPopupWindow.setFocusable(true);
		// 刷新状态
		mPopupWindow.update();

		mPopupWindow.setOnDismissListener(new OnDismissListener() {

			// 在dismiss中恢复透明度
			@Override
			public void onDismiss() {
			}
		});
	}

	// 隐藏菜单
	public void dismiss() {
		mPopupWindow.dismiss();
	}

	public void initMenuSubItem() {
		mMenuListLayout.removeAllViews();
		for (int i = 0; i < mMenuVO.subItems.size(); i++) {
		    final MPWindowMenuItemVO menuItemVO = mMenuVO.subItems.get(i);
			LinearLayout layoutMenuItem = (LinearLayout) LayoutInflater.from(mContext).inflate(EUExUtil.getResLayoutID("platform_mp_window_popmenu_item"), null);
			layoutMenuItem.setFocusable(true);
			TextView tvMenuItemTitle = (TextView) layoutMenuItem.findViewById(EUExUtil.getResIdID("platform_mp_window_pop_item_title_textview"));
			View menuItemDecline = layoutMenuItem.findViewById(EUExUtil.getResIdID("platform_mp_window_pop_item_line"));
			if (i == mMenuVO.subItems.size()-1) {
				//最后一个不加分割线
				menuItemDecline.setVisibility(View.GONE);
			}
			tvMenuItemTitle.setText(menuItemVO.itemTitle);
			layoutMenuItem.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
                    mPopMenuClickListener.onClick(menuItemVO.itemId);
					dismiss();
				}
			});
			mMenuListLayout.addView(layoutMenuItem);
		}
		mMenuListLayout.setVisibility(View.VISIBLE);
	}
}
