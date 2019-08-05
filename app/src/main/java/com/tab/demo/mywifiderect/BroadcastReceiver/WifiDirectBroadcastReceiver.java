package com.tab.demo.mywifiderect.BroadcastReceiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BroadcastReceiver";
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private Activity mActivity;
    private WifiP2pManager.PeerListListener mPeerListListener;
    private WifiP2pManager.ConnectionInfoListener mInfoListener;

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, Activity activity,
                                       WifiP2pManager.PeerListListener peerListListener,
                                       WifiP2pManager.ConnectionInfoListener infoListener
    ) {
        this.mManager = manager;
        this.mChannel = channel;
        this.mPeerListListener = peerListListener;
        this.mActivity = activity;
        this.mInfoListener = infoListener;
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        /*Broadcast intent action to indicate whether Wi-Fi p2p is enabled or disabled. An
         *extra EXTRA_WIFI_STATE provides the state information as int.*/
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state != WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(mActivity, "Wi-Fi p2p is not enabled", Toast.LENGTH_SHORT).show();
            }
        }

        /*Broadcast intent action indicating that the available peer list has changed.
         *This can be sent as a result of peers being found, lost or updated.*/
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            mManager.requestPeers(mChannel, mPeerListListener);
        }
        /*Broadcast intent action indicating that peer discovery has either started or stopped.
         *One extra EXTRA_DISCOVERY_STATE indicates whether discovery has started or stopped.*/
        else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
            int State = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);
            if (State == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED)
                Toast.makeText(mActivity, "discovery has started", Toast.LENGTH_SHORT).show();
            else if (State == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED)
                Toast.makeText(mActivity, "discovery has stopped", Toast.LENGTH_SHORT).show();

        }
        /**
         * Broadcast intent action indicating that the state of Wi-Fi p2p connectivity
         * has changed. One extra EXTRA_WIFI_P2P_INFO provides the p2p connection info in
         * the form of a WifiP2pInfo object. Another extra EXTRA_NETWORK_INFO provides
         * the network info in the form of a NetworkInfo. A third extra provides
         * the details of the group.*/
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (mManager == null) return;
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                Log.i(TAG, "p2p is connected");
                mManager.requestConnectionInfo(mChannel, mInfoListener);
            } else {
                Log.i(TAG, "p2p is disconnected");
            }
        }

        /*Respond to this device's wifi state changing*/
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

        }
    }
}
