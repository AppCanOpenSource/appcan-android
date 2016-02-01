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

import org.zywx.wbpalmstar.platform.myspace.AppInfo.InstallInfo;

import java.util.ArrayList;

public class UserInfo {
    public String userId;
    private ArrayList<InstallInfo> installedApps;

    public UserInfo() {
        installedApps = new ArrayList<InstallInfo>();
    }

    public void addInstallApp(InstallInfo installInfo) {
        installedApps.add(installInfo);
    }

    public void removeInstallApp(InstallInfo installInfo) {
        if (installInfo == null || installInfo.getAppId() == null) {
            return;
        }
        for (int i = 0, size = installedApps.size(); i < size; i++) {
            final InstallInfo current = installedApps.get(i);
            if (installInfo.getAppId().equals(current.getAppId())) {
                installedApps.remove(i);
                break;
            }
        }
    }

    public ArrayList<InstallInfo> getAllInstallInfo() {
        return installedApps;
    }

    public void setInstalledApps(ArrayList<InstallInfo> infos) {
        this.installedApps = infos;
    }

    @Override
    public String toString() {
        return "userId:" + this.userId + "  " + super.toString();
    }

    public static class LoginInfo {
        public String userId;
        public String sessionKey;
        public String fromDomain;

        public boolean isInfoCompleted() {
            if (userId != null && sessionKey != null && fromDomain != null) {
                return true;
            } else {
                return false;
            }
        }

        public boolean isServerInfoCompleted() {
            if (userId != null && fromDomain != null) {
                return true;
            } else {
                return false;
            }
        }

        public void clearInfo() {
            this.userId = null;
            this.sessionKey = null;
            this.fromDomain = null;
        }

        @Override
        public String toString() {
            return "userId:" + userId + "  sessionKey:" + sessionKey + "  fromDomain:" + fromDomain + "  "
                    + super.toString();
        }
    }

}
