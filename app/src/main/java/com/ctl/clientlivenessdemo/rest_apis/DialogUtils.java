package com.ctl.clientlivenessdemo.rest_apis;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.ctl.clientlivenessdemo.R;

public class DialogUtils {
    private static ProgressDialog progressDialog;

    public static void showDialog(Context context) {
        try {
            if (progressDialog != null && !((Activity) context).isFinishing()) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
            progressDialog = new ProgressDialog(context);
            progressDialog.setCancelable(false);
            progressDialog.show();
            progressDialog.setContentView(R.layout.progress_dailog);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        } catch (Exception e) {
            showLog(e.getMessage());
        }
    }

    public static void showDialog(Context context, String title) {
        if (progressDialog != null && !((Activity) context).isFinishing()) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(title);
        progressDialog.show();
    }

    public static void dismissDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public static void showLog(String msg) {
        if (msg != null) {
            Log.e("loge>>>", msg);
        } else {
            Log.e("loge>>>", "Some problem occurred.");
        }
    }
}
