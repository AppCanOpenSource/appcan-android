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

import java.util.LinkedList;


public final class EBrowserHistory {

    public static final int UPDATE_STEP_INIT = 0;
    public static final int UPDATE_STEP_BACK = -1;
    public static final int UPDATE_STEP_FORWARD = 1;
    public static final int UPDATE_STEP_ADD = 2;

    private LinkedList<EHistoryEntry> mHistroy;
    private int mCurrentIndex;


    public EBrowserHistory() {
        mHistroy = new LinkedList<EHistoryEntry>();
    }

    public void update(String inUrl, int step, boolean isObfuscation) {
        switch (step) {
            case UPDATE_STEP_INIT: //init
                EHistoryEntry entry = new EHistoryEntry(inUrl, isObfuscation);
                mHistroy.add(mCurrentIndex, entry);
                break;
            case UPDATE_STEP_ADD:    //new url
                mCurrentIndex += 1;
                EHistoryEntry entry1 = new EHistoryEntry(inUrl, isObfuscation);
                mHistroy.add(mCurrentIndex, entry1);
                removeSurplus();
                break;
            case UPDATE_STEP_BACK:      //back
                mCurrentIndex -= 1;
                break;
            case UPDATE_STEP_FORWARD: //forward
                mCurrentIndex += 1;
                break;
        }
    }

    public void clear() {
        mHistroy.clear();
        mCurrentIndex = 0;

    }

    public boolean canGoBack() {

        return mCurrentIndex > 0;
    }

    public boolean canGoForward() {

        return mCurrentIndex < mHistroy.size();
    }

    public EHistoryEntry getHistory(int step) {
        int outIndex = mCurrentIndex + step;
        int maxIndex = mHistroy.size() - 1;
        if (outIndex < 0 || outIndex > maxIndex) {
            return null;
        }
        return mHistroy.get(outIndex);
    }

    private void removeSurplus() {
        int removeIndex = mCurrentIndex + 1;
        int len = mHistroy.size();
        while (len > removeIndex) {
            if (mHistroy.get(removeIndex) != null) {
                mHistroy.remove(len - 1);
                len--;
            }
        }
    }

    public class EHistoryEntry {
        public String mUrl;
        public boolean mIsObfuscation;

        public EHistoryEntry(String inUrl, boolean isObfuscation) {
            mUrl = inUrl;
            mIsObfuscation = isObfuscation;
        }
    }
}
