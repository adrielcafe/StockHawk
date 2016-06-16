package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;

public class Util {

    private static ConnectivityManager connectivityManager;

    public static boolean isConnected(final Activity activity){
        if(connectivityManager == null) {
            connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = (info != null && info.isConnected());
        if(!isConnected){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, R.string.network_toast, Toast.LENGTH_SHORT).show();
                }
            });
        }
        return isConnected;
    }

}