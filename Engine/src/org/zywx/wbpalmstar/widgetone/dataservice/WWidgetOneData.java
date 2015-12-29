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

package org.zywx.wbpalmstar.widgetone.dataservice;

import android.os.Parcel;
import android.os.Parcelable;

public class WWidgetOneData implements Parcelable {
    public String m_widgetOneId;
    public String m_widgetOneVer;
    public String m_screenSize;

    public static final Parcelable.Creator<WWidgetOneData> CREATOR = new Creator<WWidgetOneData>() {
        public WWidgetOneData createFromParcel(Parcel source) {
            WWidgetOneData widgetOne = new WWidgetOneData();
            widgetOne.m_widgetOneId = source.readString();
            widgetOne.m_widgetOneVer = source.readString();
            widgetOne.m_screenSize = source.readString();
            return widgetOne;
        }

        public WWidgetOneData[] newArray(int size) {
            return new WWidgetOneData[size];
        }
    };

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub
        dest.writeString(m_widgetOneId);
        dest.writeString(m_widgetOneVer);
        dest.writeString(m_screenSize);
    }
}
