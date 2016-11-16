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

package org.zywx.wbpalmstar.engine.multipop;

import android.support.annotation.Keep;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import org.zywx.wbpalmstar.engine.EBounceView;

import java.util.ArrayList;
import java.util.List;

@Keep
public class MultiPopAdapter extends PagerAdapter {

    ArrayList<EBounceView> mViewList;
    private List<String> mPageTitles;

    @Keep
    public MultiPopAdapter(ArrayList<EBounceView> viewList) {
        super();
        this.mViewList = viewList;
    }

    @Override
    public int getCount() {
        return mViewList.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mViewList.get(position));

    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mViewList.get(position));

        return mViewList.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (mPageTitles!=null&&mPageTitles.size()>position){
            return mPageTitles.get(position);
        }
        return super.getPageTitle(position);
    }

    @Keep
    public void setMultiPopTitles(List<String> pageTitles){
        this.mPageTitles=pageTitles;
    }

}
