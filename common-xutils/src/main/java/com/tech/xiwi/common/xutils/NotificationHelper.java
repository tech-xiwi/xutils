package com.tech.xiwi.common.xutils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;

import static android.provider.Settings.EXTRA_APP_PACKAGE;
import static android.provider.Settings.EXTRA_CHANNEL_ID;

public class NotificationHelper {
    public static boolean isOpenNotifySetting(Context context) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        // areNotificationsEnabled方法的有效性官方只最低支持到API 19，低于19的仍可调用此方法不过只会返回true，即默认为用户已经开启了通知。
        return manager.areNotificationsEnabled();
    }

    public static boolean gotoOpenSetting(Context context) {
        boolean jump;

        String packageName = null;
        int uid = 0;

        try {
            packageName = context.getPackageName();
            uid = context.getApplicationInfo().uid;
            jump = true;
        } catch (Exception e) {
            e.printStackTrace();
            jump = false;
        }

        if (!jump) {
            return jump;
        }

        try {
            // 根据isOpened结果，判断是否需要提醒用户跳转AppInfo页面，去打开App通知权限
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
            intent.putExtra(EXTRA_APP_PACKAGE, packageName);
            intent.putExtra(EXTRA_CHANNEL_ID, uid);

            //这种方案适用于 API21——25，即 5.0——7.1 之间的版本可以使用
            intent.putExtra("app_package", packageName);
            intent.putExtra("app_uid", uid);

            // 小米6 -MIUI9.6-8.0.0系统，是个特例，通知设置界面只能控制"允许使用通知圆点"——然而这个玩意并没有卵用，我想对雷布斯说：I'm not ok!!!
            //  if ("MI 6".equals(Build.MODEL)) {
            //      intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            //      Uri uri = Uri.fromParts("package", getPackageName(), null);
            //      intent.setData(uri);
            //      // intent.setAction("com.android.settings/.SubSettings");
            //  }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            jump = true;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                // 出现异常则跳转到应用设置界面：锤子坚果3——OC105 API25
                Intent intent = new Intent();
                //下面这种方案是直接跳转到当前应用的设置界面。
                //https://blog.csdn.net/ysy950803/article/details/71910806
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", packageName, null);
                intent.setData(uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                jump = true;
            } catch (Exception ee) {
                jump = false;
                ee.printStackTrace();
            }
        }
        return jump;
    }
}
