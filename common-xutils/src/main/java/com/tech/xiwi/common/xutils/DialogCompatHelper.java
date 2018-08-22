package com.tech.xiwi.common.xutils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class DialogCompatHelper {
    private static final String TAG = "DialogCompatHelper";

    public static void showDialog(Dialog dialog) {
        if (dialog != null) {
            Context context = dialog.getContext();
            if ((context instanceof Activity) && ((Activity) context).isFinishing()) {
                Log.w(TAG, "showDialog: activity isFinishing ");
                return;
            }
            if (context instanceof ContextWrapper) {
                ContextWrapper contextWrapper = (ContextWrapper) context;
                if ((contextWrapper.getBaseContext() instanceof Activity) && ((Activity) contextWrapper.getBaseContext()).isFinishing()) {
                    Log.w(TAG, "showDialog: activity isFinishing ");
                    return;
                }
            }
            dialog.show();
        }
    }

    public static void showDialogFragment(DialogFragment dialogFragment, FragmentManager fragmentManager, String tag) {
        if (dialogFragment != null && fragmentManager != null && tag != null) {
            Activity activity = dialogFragment.getActivity();
            if (activity == null) {
                Log.w(TAG, "showDialogFragment: activity is null ");
                return;
            } else if (activity.isFinishing()) {
                Log.w(TAG, "showDialogFragment: activity isFinishing ");
                return;
            } else {
                dialogFragment.show(fragmentManager, tag);
                return;
            }
        }
    }

    public static int showDialogFragment(DialogFragment dialogFragment, FragmentTransaction fragmentTransaction, String tag) {
        if (dialogFragment == null || fragmentTransaction == null || tag == null) {
            return 0;
        }
        Activity activity = dialogFragment.getActivity();
        if (activity == null) {
            Log.w(TAG, "showDialogFragment: activity is null ");
            return 0;
        } else if (!activity.isFinishing()) {
            return dialogFragment.show(fragmentTransaction, tag);
        } else {
            Log.w(TAG, "showDialogFragment: activity isFinishing ");
            return 0;
        }
    }

}
