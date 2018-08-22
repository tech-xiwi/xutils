package com.tech.xiwi.common.xutils;


import android.annotation.SuppressLint;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;

public class ToastCompatHelper {
    private static final String TAG = "ToastCompatHelper";

    private static class BinderProxyHookHandler implements InvocationHandler {
        IBinder base;
        Class<?> iInterface;
        Class<?> stub;

        @SuppressLint({"PrivateApi"})
        BinderProxyHookHandler(IBinder iBinder) {
            this.base = iBinder;
            try {
                this.stub = Class.forName("android.app.INotificationManager$Stub");
                this.iInterface = Class.forName("android.app.INotificationManager");
            } catch (Throwable e) {
                Log.e(ToastCompatHelper.TAG, "get INotificationManager err", e);
            }
        }

        public Object invoke(Object obj, Method method, Object[] objArr) throws Throwable {
            if (!"queryLocalInterface".equals(method.getName())) {
                return method.invoke(this.base, objArr);
            }
            return Proxy.newProxyInstance(obj.getClass().getClassLoader(), new Class[]{this.iInterface}, new NotificationHookHandler(this.base, this.stub));
        }
    }

    public static class NotificationHookHandler implements InvocationHandler {
        Object base;

        NotificationHookHandler(IBinder iBinder, Class<?> cls) {
            try {
                this.base = cls.getDeclaredMethod("asInterface", new Class[]{IBinder.class}).invoke(null, new Object[]{iBinder});
            } catch (Throwable e) {
                Log.e(ToastCompatHelper.TAG, "get asInterface err", e);
                ToastCompatHelper.releaseHook();
            }
        }

        public Object invoke(Object obj, Method method, Object[] objArr) throws Throwable {
            if ("enqueueToast".equals(method.getName())) {
                Log.d(ToastCompatHelper.TAG, "hook enqueueToast args=" + Arrays.toString(objArr));
                ToastCompatHelper.hookPoint(objArr[1]);
            }
            return method.invoke(this.base, objArr);
        }
    }

    private static class ToastHandler extends Handler {
        Handler mProxyHandler;

        ToastHandler(Handler handler) {
            super(handler.getLooper());
            this.mProxyHandler = handler;
        }

        public void dispatchMessage(Message message) {
            try {
                this.mProxyHandler.dispatchMessage(message);
            } catch (Throwable e) {
                Log.e(ToastCompatHelper.TAG, "dispatchMessage with BadTokenException: ", e);
            }
        }
    }

    public static void hook() {
        try {
            if (VERSION.SDK_INT >= 21 && VERSION.SDK_INT < 26) {
                Log.i(TAG, "hook start");
                Class cls = Class.forName("android.os.ServiceManager");
                IBinder iBinder = (IBinder) cls.getDeclaredMethod("getService", new Class[]{String.class}).invoke(null, new Object[]{"notification"});
                iBinder = (IBinder) Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{IBinder.class}, new BinderProxyHookHandler(iBinder));
                Field declaredField = cls.getDeclaredField("sCache");
                declaredField.setAccessible(true);
                ((Map) declaredField.get(null)).put("notification", iBinder);
                Field declaredField2 = Toast.class.getDeclaredField("sService");
                declaredField2.setAccessible(true);
                declaredField2.set(null, null);
            }
        } catch (Throwable e) {
            Log.e(TAG, "hook err", e);
        }
    }

    public static void releaseHook() {
        try {
            if (VERSION.SDK_INT >= 21 && VERSION.SDK_INT < 26) {
                Log.i(TAG, "hook start");
                Class cls = Class.forName("android.os.ServiceManager");
                IBinder iBinder = (IBinder) cls.getDeclaredMethod("getService", new Class[]{String.class}).invoke(null, new Object[]{"notification"});
                Field declaredField = cls.getDeclaredField("sCache");
                declaredField.setAccessible(true);
                ((Map) declaredField.get(null)).put("notification", iBinder);
                Field declaredField2 = Toast.class.getDeclaredField("sService");
                declaredField2.setAccessible(true);
                declaredField2.set(null, null);
            }
        } catch (Throwable e) {
            Log.e(TAG, "releaseHook err", e);
        }
    }

    private static void hookPoint(Object obj) {
        try {
            Field declaredField = obj.getClass().getDeclaredField("mHandler");
            declaredField.setAccessible(true);
            declaredField.set(obj, new ToastHandler((Handler) declaredField.get(obj)));
        } catch (Throwable e) {
            Log.e(TAG, "rp  ToastHandler err", e);
            releaseHook();
        }
    }
}
