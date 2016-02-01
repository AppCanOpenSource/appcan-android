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

import android.util.Log;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class EWindowStack {

    private ELinkedList<EBrowserWindow> mWindList;
    private Map<String, EBrowserWindow> mWindMap;
    private Map<String, EBrowserWindow> mSlidingWindMap;

    public EWindowStack() {
        mWindList = new ELinkedList<EBrowserWindow>();
        mWindMap = new Hashtable<String, EBrowserWindow>();
        mSlidingWindMap = new Hashtable<String, EBrowserWindow>();
    }

    public void addSlidingWindMap(EBrowserWindow window) {
        String name = window.getName();
        if (name != null && name.trim().length() > 0) {
            mSlidingWindMap.put(name, window);
        }
    }

    public EBrowserWindow getSlidingWind(String name) {
        if (null != name && name.trim().length() != 0) {
            return mSlidingWindMap.get(name);
        }
        return null;
    }

    public void add(EBrowserWindow view) {
        String name = view.getName();
        if (name != null && name.trim().length() > 0) {
            mWindMap.put(name, view);
        }
        mWindList.addFirst(view);
    }

    public void addOnlyMap(EBrowserWindow wind) {
        String name = wind.getName();
        if (name != null && name.trim().length() > 0) {
            mWindMap.put(name, wind);
        }
    }

    public EBrowserWindow first() {

        return mWindList.getFirst();
    }

    public EBrowserWindow last() {

        return mWindList.getLast();
    }

    public EBrowserWindow get(String name) {
        if (null != name && name.trim().length() != 0) {
            return mWindMap.get(name);
        }
        return null;
    }

    public EBrowserWindow next(EBrowserWindow curWind) {

        int location = mWindList.indexOf(curWind);
        if (location - 1 >= 0) {
            return mWindList.get(location - 1);
        }
        return null;
    }

    public EBrowserWindow prev(EBrowserWindow curWind) {

        int location = mWindList.indexOf(curWind);
        if (location + 1 < mWindList.size()) {
            return mWindList.get(location + 1);
        }
        return null;
    }

    public int length() {

        return mWindList.size();
    }

    public void remove(EBrowserWindow view) {
        String name = view.getName();
        if (name != null && name.trim().length() > 0) {
            mWindMap.remove(name);
        }
        mWindList.remove(view);
    }

    public void removeFromList(EBrowserWindow view) {

        mWindList.remove(view);
    }

    public EBrowserWindow contains(String name) {
        EBrowserWindow obj = mWindMap.get(name);
        if (null != obj) {
            mWindList.remove(obj);
            mWindMap.remove(name);
            return obj;
        }
        return null;
    }

    public ELinkedList<EBrowserWindow> getAll() {

        return mWindList;
    }

    public void clearFractureLink() {
        int len = mWindList.size() - 1;
        for (int i = len; i >= 0; --i) {
            EBrowserWindow fracture = mWindList.get(i);
            if (fracture.checkFlag(EBrowserWindow.F_WINDOW_FLAG_WILL_REMOWE)) {
                fracture.clearFlag();
                mWindList.remove(i);
            }
        }
    }

    private void checkRemnant() {
        Iterator<Map.Entry<String, EBrowserWindow>> iterator = mWindMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, EBrowserWindow> entry = iterator.next();
            EBrowserWindow temp = entry.getValue();
            if (!mWindList.contains(temp)) {
                temp.destory();
                iterator.remove();
            }
        }
    }

    public void destroy() {
        checkRemnant();
        for (int i = 0; i < mWindList.size; ++i) {
            mWindList.get(i).destory();
        }
        mWindList.clear();
        mWindMap.clear();
//		mWindList = null;
//		mWindMap = null;
    }

    public void printWindStack() {
        for (int i = 0; i < mWindList.size(); ++i) {
            Log.i("ldx", "windName:" + mWindList.get(i).getName() + " , >> location:" + i);
        }
        Log.i("ldx", "first:" + mWindList.getFirst().getName() + " , last:" + mWindList.getLast().getName() + "####");
    }
}
