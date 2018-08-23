package com.tech.xiwi.common.xutils;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.UUID;

public class DeviceIdUtils {
    private static final String TAG = "DeviceId";

    /**
     * deviceID的组成为：渠道标志+识别符来源标志+hash后的终端识别符
     * <p>
     * 渠道标志为：
     * 1，andriod（a）
     * <p>
     * 识别符来源标志：
     * 1， wifi mac地址（wifi）；
     * 2， IMEI（imei）；
     * 3， 序列号（sn）；
     * 4， id：随机码。若前面的都取不到时，则随机生成一个随机码，需要缓存。
     *
     * @param context
     * @return
     */
    public static String getDeviceId(Context context) {
        SharedPreferences mShare = getSysShare(context, "sysCacheMap");
        if (mShare != null) {
            String uuid = mShare.getString("uuid", "");
            if (!TextUtils.isEmpty(uuid)) {
                return uuid;
            }
        }

        StringBuilder deviceId = new StringBuilder();
        // 渠道标志
        deviceId.append("a");

        try {
            //wifi mac地址
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
                String wifiMac = WlanMacHelper.getMac(context);
                if (!TextUtils.isEmpty(wifiMac)) {
                    deviceId.append("wifi");
                    deviceId.append(wifiMac);
                    Log.e("getDeviceId : ", strHandler(deviceId.toString()));
                    saveSysMap(context, "sysCacheMap", "uuid", strHandler(deviceId.toString()));
                    return strHandler(deviceId.toString());
                }
            }

            //IMEI（imei）
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                String imei = tm.getDeviceId();
                if (!TextUtils.isEmpty(imei)) {
                    deviceId.append("imei");
                    deviceId.append(imei);
                    Log.e(TAG, "getDeviceId : " + strHandler(deviceId.toString()));
                    saveSysMap(context, "sysCacheMap", "uuid", strHandler(deviceId.toString()));
                    return strHandler(deviceId.toString());
                }
            }

            //序列号（sn）
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                String sn = tm.getSimSerialNumber();
                if (!TextUtils.isEmpty(sn)) {
                    deviceId.append("sn");
                    deviceId.append(sn);
                    Log.e(TAG, "getDeviceId : " + strHandler(deviceId.toString()));
                    saveSysMap(context, "sysCacheMap", "uuid", strHandler(deviceId.toString()));
                    return strHandler(deviceId.toString());
                }
            }

            //如果上面都没有， 则生成一个id：随机码
            String uuid = getUUID(context);
            if (!TextUtils.isEmpty(uuid)) {
                deviceId.append("id");
                deviceId.append(uuid);
                Log.e(TAG, "getDeviceId : " + strHandler(deviceId.toString()));
                saveSysMap(context, "sysCacheMap", "uuid", strHandler(deviceId.toString()));
                return strHandler(deviceId.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            deviceId.append("id").append(getUUID(context));
        }

        Log.e(TAG, "getDeviceId : " + strHandler(deviceId.toString()));
        saveSysMap(context, "sysCacheMap", "uuid", strHandler(deviceId.toString()));
        return strHandler(deviceId.toString());

    }

    /**
     * 得到全局唯一UUID
     */
    private static String getUUID(Context context) {
        SharedPreferences mShare = getSysShare(context, "sysCacheMap");
        String uuid = null;
        if (mShare != null) {
            uuid = mShare.getString("uuid", "");
        }

        if (TextUtils.isEmpty(uuid)) {
            uuid = UUID.randomUUID().toString();
            uuid = strHandler(uuid);
            saveSysMap(context, "sysCacheMap", "uuid", uuid);
        }

        Log.e(TAG, "getUUID : " + uuid);
        return uuid;
    }

    private static String strHandler(String str) {
        return str.replaceAll(":", "").replaceAll("-", "");
    }

    private static void saveSysMap(Context context, String prefix, String key, String value) {
        try {
            getSysShare(context, prefix).edit().putString(key, value).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SharedPreferences getSysShare(Context context, String prefix) {
        return context.getSharedPreferences(prefix, Context.MODE_PRIVATE);
    }
}

