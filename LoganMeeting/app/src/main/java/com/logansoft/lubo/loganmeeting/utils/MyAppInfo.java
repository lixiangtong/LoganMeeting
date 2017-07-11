package com.logansoft.lubo.loganmeeting.utils;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by logansoft on 2017/7/11.
 */

public class MyAppInfo {

    public static String getVersionName(Context context){
        try {
            String packageName = context.getPackageName();
            String versionName = context.getPackageManager().getPackageInfo(packageName, 0).versionName;
            return versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
