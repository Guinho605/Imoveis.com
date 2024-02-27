package com.inmoveiscom.imoveiscom.activity;

import android.content.Context;

import com.inmoveiscom.imoveiscom.activity.BaseActivity;

public class NetworkChangeListenerImpl implements NetworkChangeReceiver.NetworkChangeListener {

    private Context mContext;

    public NetworkChangeListenerImpl(Context context) {
        mContext = context;
    }

    @Override
    public void onNetworkChange(boolean isConnected) {
        if (mContext instanceof BaseActivity) {
            ((BaseActivity) mContext).onNetworkChange(isConnected);
        }
    }
}
