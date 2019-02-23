package org.zywx.wbpalmstar.base.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import org.zywx.wbpalmstar.engine.callback.RequestPermissionsCallBcak;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhang on 2018/12/19.
 */

public class PermissionUtils {

    /**
     * 普通的拒绝，没有勾选不在提示
     */
    public static final int REQUESTFLAGDENIED = 2;
    /**
     * 拒绝勾选了不在提示
     */
    public static final int REQUESTFLAGDENIEDNOASK = 3;

    /**
     * requestPermissions single
     * shouldShowRequestPermissionRationale
     * 1.上次选择禁止并勾选：下次不在询问	false
     * 2.第一次打开App时	 false
     * 3.上次弹出权限点击了禁止（但没有勾选“下次不在询问”）	 true
     *
     * @param mActivity
     * @param permission
     * @param requestCode
     * @param requestPermissionsCallBcak
     */
    public static void requestPermissions(Activity mActivity, String permission, int requestCode, RequestPermissionsCallBcak requestPermissionsCallBcak) {
        //判断权限是否开启
        if (ContextCompat.checkSelfPermission(mActivity, permission) != PackageManager.PERMISSION_GRANTED) {
            mActivity.requestPermissions(new String[]{permission}, requestCode);
        } else {
            //已经拥有权限，可以做自己想做的操作
            requestPermissionsCallBcak.requestPermissionsSucesss(requestCode);
        }
    }

    /**
     * 单个权限提示
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     * @param requestPermissionsCallBcak
     */
    public static void onRequestPermissionsResult(Activity mActivity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, RequestPermissionsCallBcak requestPermissionsCallBcak) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //权限开启成功
            requestPermissionsCallBcak.requestPermissionsSucesss(requestCode);
        } else {
            //权限开始失败
            if (!ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permissions[0])) {
                requestPermissionsCallBcak.requestPermissionfailure(REQUESTFLAGDENIED, requestCode);
            } else {
                requestPermissionsCallBcak.requestPermissionfailure(REQUESTFLAGDENIEDNOASK, requestCode);
            }
        }
    }

    /**
     * 申请多个权限的问题
     *
     * @param mActivity
     * @param permissions
     * @param requestCode
     * @param requestPermissionsCallBcak
     */
    public static void requestPermissions(Activity mActivity, String[] permissions, int requestCode, RequestPermissionsCallBcak requestPermissionsCallBcak) {
        List<String> permissionLists = new ArrayList<String>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(mActivity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionLists.add(permission);
            }
        }
        if (!permissionLists.isEmpty()) {
            ActivityCompat.requestPermissions(mActivity, permissionLists.toArray(new String[permissionLists.size()]), requestCode);
        } else {
            //表示全都授权了
            requestPermissionsCallBcak.requestPermissionsSucesss(requestCode);
        }
    }

    /**
     * 多个权限
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     * @param requestPermissionsCallBcak
     */
    public static void onRequestPermissionsResults(Activity mActivity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, RequestPermissionsCallBcak requestPermissionsCallBcak) {
        if (grantResults.length > 0) {
            //存放没授权的权限
            List<String> deniedPermissions = new ArrayList<String>();
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i]);
                }
            }
            if (deniedPermissions.isEmpty()) {
                requestPermissionsCallBcak.requestPermissionsSucesss(requestCode);
            } else {
                for (String deniedPermission : deniedPermissions) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, deniedPermission)) {
                        requestPermissionsCallBcak.requestPermissionfailure(REQUESTFLAGDENIED, deniedPermissions);
                        return;
                    }
                }
                requestPermissionsCallBcak.requestPermissionfailure(REQUESTFLAGDENIEDNOASK, deniedPermissions);
            }
        }
    }

}
