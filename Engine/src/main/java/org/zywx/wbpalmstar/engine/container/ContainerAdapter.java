package org.zywx.wbpalmstar.engine.container;

import android.support.annotation.Keep;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.List;
import java.util.Vector;

/**
 * Created by ylt on 2016/10/8.
 */
@Keep
public class ContainerAdapter extends PagerAdapter {
    Vector<FrameLayout> mViewList;
    private List<String> mTitles;
    int mChildCount = 0;

    @Keep
    public ContainerAdapter(Vector<FrameLayout> viewList) {
        this(viewList,null);
    }

    public ContainerAdapter(Vector<FrameLayout> viewList, List<String> titles) {
        this.mViewList = viewList;
        this.mTitles=titles;
    }

    public Vector<FrameLayout> getViewList() {
        return mViewList;
    }

    public void setViewList(Vector<FrameLayout> viewList) {
        this.mViewList = viewList;
    }

    @Override
    public int getCount() {
        return mViewList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object arg1) {
        return view == arg1;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mViewList.get(position));
        return mViewList.get(position);
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
    public CharSequence getPageTitle(int position) {
        if (mTitles!=null&&mTitles.size()>position){
            return mTitles.get(position);
        }
        return super.getPageTitle(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Keep
    public void setContainerTitles(List<String> titles){
        this.mTitles=titles;
    }
}
