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


public class EWindGarbHeap {

    private ELinkedList<EBrowserWindow> mInvalidHeap;
    private static final int mCount = 5;


    public EWindGarbHeap() {

        mInvalidHeap = new ELinkedList<EBrowserWindow>();
    }

    public void put(EBrowserWindow wind) {
        if (mInvalidHeap.contains(wind)) {
            return;
        }
        wind.stopLoop();
        wind.stopLoad();
        mInvalidHeap.addFirst(wind);
        if (mInvalidHeap.size >= mCount) {
            for (int i = 4; i > 1; i--) {
                mInvalidHeap.get(i).destory();
                mInvalidHeap.remove(i);
            }
            Runtime.getRuntime().gc();
        }
    }

    public EBrowserWindow get() {
        EBrowserWindow last = mInvalidHeap.pollLast();
        if (null != last) {
            last.reset();
            return last;
        }
        return null;
    }

    public void destroy() {
        int len = mInvalidHeap.size;
        for (int i = 0; i < len; ++i) {
            mInvalidHeap.get(i).destory();
        }
        mInvalidHeap.clear();
//		mInvalidHeap = null;
    }
}
