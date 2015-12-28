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

package org.zywx.wbpalmstar.platform.myspace;

import android.os.Parcel;
import android.os.Parcelable;

public class AppInfo {
    public static final int APP_MODE_WAP = 8;
    public static final int APP_MODE_WIDGET = 1;
    public static final int APP_MODE_NATIVE = 7;

    public static class DownloadData implements Parcelable {
        public String appId;
        public String softwareId;
        public int mode;
        public String appSize;
        public String appName;
        public String iconLoc;
        public String downloadUrl;

        public DownloadData() {

        }

        public DownloadData(Parcel in) {
            this.appId = in.readString();
            this.softwareId = in.readString();
            this.mode = in.readInt();
            this.appSize = in.readString();
            this.appName = in.readString();
            this.iconLoc = in.readString();
            this.downloadUrl = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeString(this.appId);
            parcel.writeString(this.softwareId);
            parcel.writeInt(this.mode);
            parcel.writeString(this.appSize);
            parcel.writeString(this.appName);
            parcel.writeString(this.iconLoc);
            parcel.writeString(this.downloadUrl);
        }

        public boolean isCorrect() {
            if (appId != null && softwareId != null && appName != null && downloadUrl != null && iconLoc != null
                    && appSize != null) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DownloadData)) {
                return false;
            }
            DownloadData other = (DownloadData) o;
            return this.softwareId.equals(other.softwareId);
        }

        @Override
        public String toString() {
            return "appId:" + appId + " softId:" + softwareId + " appName:" + appName + " mode:" + mode + " downURL:"
                    + downloadUrl + " iconUrl:" + iconLoc;
        }

        public static final Creator<DownloadData> CREATOR = new Creator<DownloadData>() {
            public DownloadData createFromParcel(Parcel in) {
                return new DownloadData(in);
            }

            public DownloadData[] newArray(int size) {
                return new DownloadData[size];
            }

        };
    }

    public static class InstallInfo implements Parcelable {
        public static final int TRUE = 1;
        public static final int FALSE = 0;
        public String installPath;
        // 是否已下载
        public boolean isDownload = false;
        private DownloadData downloadInfo;

        @Override
        public int describeContents() {
            return 0;
        }

        public InstallInfo() {
            downloadInfo = new DownloadData();
        }

        public void setDownloadInfo(DownloadData downloadInfo) {
            this.downloadInfo = downloadInfo;
        }

        public DownloadData getDownloadInfo() {
            return downloadInfo;
        }

        public void setAppId(String appId) {
            this.downloadInfo.appId = appId;
        }

        public String getAppId() {
            return this.downloadInfo.appId;
        }

        public InstallInfo(Parcel in) {
            this.installPath = in.readString();
            this.isDownload = (in.readInt() == 1 ? true : false);
            this.downloadInfo = DownloadData.CREATOR.createFromParcel(in);
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeString(this.installPath);
            parcel.writeInt(this.isDownload ? 1 : 0);
            downloadInfo.writeToParcel(parcel, flags);
        }

        public static final Creator<InstallInfo> CREATOR = new Creator<InstallInfo>() {
            public InstallInfo createFromParcel(Parcel in) {
                return new InstallInfo(in);
            }

            public InstallInfo[] newArray(int size) {
                return new InstallInfo[size];
            }

        };

        public String toString() {
            return "installPath: " + installPath + "  download: " + isDownload;
        }

        ;

    }

    public static class ReportInfo {
        public String userId;
        public String appId;
        public int reportType;
        public int reportCount;
        public static final int TYPE_INSTALL = 0;
        public static final int TYPE_UNINSTALL = 1;
    }

    // public static class RecommendInfo implements Parcelable {
    // private DownloadData downloadInfo;
    // public byte[] iconData;
    //
    // public RecommendInfo() {
    // downloadInfo = new DownloadData();
    // }
    //
    // public void setDownloadInfo(DownloadData downloadInfo) {
    // this.downloadInfo = downloadInfo;
    // }
    //
    // public DownloadData getDownloadInfo() {
    // return downloadInfo;
    // }
    //
    // public RecommendInfo(Parcel in) {
    // in.readByteArray(this.iconData);
    // this.downloadInfo = DownloadData.CREATOR.createFromParcel(in);
    // }
    //
    // @Override
    // public void writeToParcel(Parcel parcel, int flags) {
    // parcel.writeByteArray(iconData);
    // downloadInfo.writeToParcel(parcel, flags);
    // }
    //
    // public static final Creator<RecommendInfo> CREATOR = new
    // Creator<RecommendInfo>() {
    // public RecommendInfo createFromParcel(Parcel in) {
    // return new RecommendInfo(in);
    // }
    //
    // public RecommendInfo[] newArray(int size) {
    // return new RecommendInfo[size];
    // }
    // };
    //
    // @Override
    // public int describeContents() {
    // return 0;
    // }
    // }

    public static class DelayStartInfo {
        public String sessionKey;
        public String softwareId;
        public String reportTime;

        public DelayStartInfo() {

        }

        public DelayStartInfo(String sessionKey, String softwareId, String reportTime) {
            this.sessionKey = sessionKey;
            this.softwareId = softwareId;
            this.reportTime = reportTime;
        }

        @Override
        public String toString() {
            return "sessionKey:" + sessionKey + "  softwareId:" + softwareId + "  reportTime:" + reportTime;
        }
    }

    public static class DelayInstallInfo {
        public String sessionKey;
        public String mainAppId;
        public String softwareId;
        public String platformId;
        public String reportTime;

        public DelayInstallInfo() {

        }

        public DelayInstallInfo(String sessionKey, String mainAppId, String softwareId, String platformId,
                                String reportTime) {
            this.sessionKey = sessionKey;
            this.mainAppId = mainAppId;
            this.softwareId = softwareId;
            this.platformId = platformId;
            this.reportTime = reportTime;
        }

        @Override
        public String toString() {
            return "sessionKey:" + sessionKey + "  appId:" + mainAppId + "  softwareId:" + softwareId + "  platformId:"
                    + platformId + "  reportTime:" + reportTime;
        }
    }

    public static class DelayUninstallInfo {
        public String sessionKey;
        public String mainAppId;
        public String softwareId;
        public String platformId;
        public String reportTime;

        public DelayUninstallInfo() {

        }

        public DelayUninstallInfo(String sessionKey, String mainAppId, String softwareId, String platformId,
                                  String reportTime) {
            this.sessionKey = sessionKey;
            this.mainAppId = mainAppId;
            this.softwareId = softwareId;
            this.platformId = platformId;
            this.reportTime = reportTime;
        }

        @Override
        public String toString() {
            return "sessionKey:" + sessionKey + "  appId:" + mainAppId + "  softwareId:" + softwareId + "  platformId:"
                    + platformId + "  reportTime:" + reportTime;
        }
    }

}
