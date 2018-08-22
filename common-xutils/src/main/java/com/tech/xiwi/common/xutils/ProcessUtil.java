package com.tech.xiwi.common.xutils;


import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Process;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.List;

public abstract class ProcessUtil {

    private static class RunningProcessCompat {
        private RunningProcessCompat() {
        }

        public static boolean isRunningInForeground(Context context) {
            try {
                List runningTasks = ((ActivityManager) context.getSystemService(Service.ACTIVITY_SERVICE)).getRunningTasks(1);
                if (runningTasks == null || runningTasks.isEmpty()) {
                    return false;
                }
                return ((RunningTaskInfo) runningTasks.get(0)).topActivity.getPackageName().equals(context.getPackageName());
            } catch (Exception e) {
                return false;
            }
        }
    }

    private static final class LollipopRunningProcessCompat extends RunningProcessCompat {
        private LollipopRunningProcessCompat() {
            super();
        }

        public static boolean isRunningInForeground(Context context) {
            try {
                Field declaredField = RunningAppProcessInfo.class.getDeclaredField("processState");
                List<RunningAppProcessInfo> runningAppProcesses = ((ActivityManager) context.getSystemService(Service.ACTIVITY_SERVICE)).getRunningAppProcesses();
                if (runningAppProcesses == null || runningAppProcesses.isEmpty()) {
                    return false;
                }
                String packageName = context.getPackageName();
                for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
                    if (runningAppProcessInfo.importance == 100 && runningAppProcessInfo.importanceReasonCode == 0) {
                        try {
                            Integer valueOf = Integer.valueOf(declaredField.getInt(runningAppProcessInfo));
                            if (valueOf != null && valueOf.intValue() == 2 && runningAppProcessInfo.pkgList != null && runningAppProcessInfo.pkgList.length > 0) {
                                return runningAppProcessInfo.pkgList[0].equals(packageName);
                            }
                        } catch (Exception e) {
                        }
                    }
                }
                return false;
            } catch (Exception e2) {
            }
            return false;
        }

    }

    public static boolean isRunningInForeground(Context context) {
        if (VERSION.SDK_INT >= 21) {
            return LollipopRunningProcessCompat.isRunningInForeground(context);
        }
        return RunningProcessCompat.isRunningInForeground(context);
    }

    public static boolean isMainProcess(Context context) {
        try {
            List<RunningAppProcessInfo> runningAppProcesses = ((ActivityManager) context.getSystemService(Service.ACTIVITY_SERVICE)).getRunningAppProcesses();
            if (runningAppProcesses != null && runningAppProcesses.size() > 0) {
                int myPid = Process.myPid();
                String packageName = context.getPackageName();
                for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
                    if (packageName.equals(runningAppProcessInfo.processName)) {
                        return myPid == runningAppProcessInfo.pid;
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static String getPackageName() {
        String trim = null;
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/" + Process.myPid() + "/cmdline")));
            try {
                trim = bufferedReader.readLine().trim();
                return trim;
            } catch (IOException e) {
                e.printStackTrace();
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                        trim = String.valueOf(Process.myPid());
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                    trim = String.valueOf(Process.myPid());
                }
            }
        }

        return trim;
    }

    private ProcessUtil() {
    }
}

