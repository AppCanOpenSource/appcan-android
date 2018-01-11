package org.zywx.wbpalmstar.base.vo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * 窗口样式配置参数
 *
 * Created by zhangyipeng on 2018/1/9.
 */

public class WindowOptionsVO implements Parcelable {
    public String windowTitle;
    public boolean isBottomBarShow;
    public String titleLeftIcon;
    public String titleRightIcon;
    public List<MPWindowMenuVO> menuList;

    public static class MPWindowMenuVO implements Parcelable {
        public String menuId;
        public String menuTitle;
        public List<MPWindowMenuItemVO> subItems;

        public static class MPWindowMenuItemVO implements Parcelable {
            public String itemId;
            public String itemTitle;

            @Override
            public String toString() {
                return "MPWindowMenuItemVO{" +
                        "itemId='" + itemId + '\'' +
                        ", itemTitle='" + itemTitle + '\'' +
                        '}';
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(this.itemId);
                dest.writeString(this.itemTitle);
            }

            public MPWindowMenuItemVO() {
            }

            protected MPWindowMenuItemVO(Parcel in) {
                this.itemId = in.readString();
                this.itemTitle = in.readString();
            }

            public static final Parcelable.Creator<MPWindowMenuItemVO> CREATOR = new Parcelable.Creator<MPWindowMenuItemVO>() {
                @Override
                public MPWindowMenuItemVO createFromParcel(Parcel source) {
                    return new MPWindowMenuItemVO(source);
                }

                @Override
                public MPWindowMenuItemVO[] newArray(int size) {
                    return new MPWindowMenuItemVO[size];
                }
            };
        }

        @Override
        public String toString() {
            return "MPWindowMenuVO{" +
                    "menuId='" + menuId + '\'' +
                    ", menuTitle='" + menuTitle + '\'' +
                    ", subItems=" + subItems +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.menuId);
            dest.writeString(this.menuTitle);
            dest.writeTypedList(this.subItems);
        }

        public MPWindowMenuVO() {
        }

        protected MPWindowMenuVO(Parcel in) {
            this.menuId = in.readString();
            this.menuTitle = in.readString();
            this.subItems = in.createTypedArrayList(MPWindowMenuItemVO.CREATOR);
        }

        public static final Parcelable.Creator<MPWindowMenuVO> CREATOR = new Parcelable.Creator<MPWindowMenuVO>() {
            @Override
            public MPWindowMenuVO createFromParcel(Parcel source) {
                return new MPWindowMenuVO(source);
            }

            @Override
            public MPWindowMenuVO[] newArray(int size) {
                return new MPWindowMenuVO[size];
            }
        };
    }

    @Override
    public String toString() {
        return "WindowOptionsVO{" +
                "windowTitle='" + windowTitle + '\'' +
                ", isBottomBarShow=" + isBottomBarShow +
                ", titleLeftIcon='" + titleLeftIcon + '\'' +
                ", titleRightIcon='" + titleRightIcon + '\'' +
                ", menuList=" + menuList +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.windowTitle);
        dest.writeByte(this.isBottomBarShow ? (byte) 1 : (byte) 0);
        dest.writeString(this.titleLeftIcon);
        dest.writeString(this.titleRightIcon);
        dest.writeTypedList(this.menuList);
    }

    public WindowOptionsVO() {
    }

    protected WindowOptionsVO(Parcel in) {
        this.windowTitle = in.readString();
        this.isBottomBarShow = in.readByte() != 0;
        this.titleLeftIcon = in.readString();
        this.titleRightIcon = in.readString();
        this.menuList = in.createTypedArrayList(MPWindowMenuVO.CREATOR);
    }

    public static final Parcelable.Creator<WindowOptionsVO> CREATOR = new Parcelable.Creator<WindowOptionsVO>() {
        @Override
        public WindowOptionsVO createFromParcel(Parcel source) {
            return new WindowOptionsVO(source);
        }

        @Override
        public WindowOptionsVO[] newArray(int size) {
            return new WindowOptionsVO[size];
        }
    };
}
