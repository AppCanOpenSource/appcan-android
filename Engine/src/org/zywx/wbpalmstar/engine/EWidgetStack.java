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

import java.util.Hashtable;

public class EWidgetStack {

    private ELinkedList<EBrowserWidget> mWidgetList;
    private Hashtable<String, EBrowserWidget> mWidgetTable;
    private int m_curWidgetIndex;

    public EWidgetStack() {
        m_curWidgetIndex = -1;
        mWidgetTable = new Hashtable<String, EBrowserWidget>();
        mWidgetList = new ELinkedList<EBrowserWidget>();
    }

    public EBrowserWidget get(String inWidgetNmae) {

        return mWidgetTable.get(inWidgetNmae);
    }

    public EBrowserWidget get(int index) {
        if (index < 0 && index >= length()) {

            return null;
        }
        return mWidgetList.get(index);
    }

    public int indexOf(EBrowserWidget wgt) {

        return mWidgetList.indexOf(wgt);
    }

    public void remove(EBrowserWidget widget) {
        String key = widget.getWidget().m_appId;
        mWidgetTable.remove(key);
        mWidgetList.remove(widget);
        m_curWidgetIndex--;
    }

    public EBrowserWidget peek() {

        return get(m_curWidgetIndex);
    }

    public EBrowserWidget first() {

        return get(0);
    }

    public EBrowserWidget last() {

        return get(length() - 1);
    }

    public EBrowserWidget next() {

        return get(m_curWidgetIndex + 1);
    }

    public EBrowserWidget prev() {

        return get(m_curWidgetIndex - 1);
    }

    public int length() {

        return mWidgetList.size();
    }

    public void add(EBrowserWidget inBrwWidget) {
        String key = inBrwWidget.getWidget().m_appId;
        mWidgetTable.put(key, inBrwWidget);
        mWidgetList.add(inBrwWidget);
        m_curWidgetIndex++;
    }
}
