package com.dreamtale.wifidirect.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

import com.dreamtale.wifidirect.interfaces.IWifiP2pListener;

/**
 * The receiver that listen the state of the wifi direct. 
 * 
 * @author dreamtale
 *
 */
public class WiFiDirectReceiver extends BroadcastReceiver
{
    private final static String TAG = "WiFiDirectReceiver";
    
    private WifiP2pManager mP2pManager = null;
    private Channel mP2pChannel = null;
    private IWifiP2pListener mP2pListener = null;
    private PeerListListener mPeerListListener = null;
    private ConnectionInfoListener mConnectionInfoListener = null;
    
    public WiFiDirectReceiver(WifiP2pManager manager, Channel channel, IWifiP2pListener listener)
    {
        mP2pManager = manager;
        mP2pChannel = channel;
        mP2pListener = listener;
    }
    
    public void setPeerListListener(PeerListListener listener)
    {
        mPeerListListener = listener;
    }
    
    public void setConnectionInfoListener(ConnectionInfoListener listener)
    {
        mConnectionInfoListener = listener;
    }
    
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action))
        {
            // notify the wifi direct state changed.
            Log.d(TAG, "wifi p2p_state_changed");
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (null != mP2pListener)
            {
                mP2pListener.onWiFiStateChange(state);
            }
        }
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action))
        {
            // notify the peer list changed.
            Log.d(TAG, "wifi_p2p_peers_changed");
            if (null != mP2pManager && null != mP2pChannel && null != mPeerListListener)
            {
                mP2pManager.requestPeers(mP2pChannel, mPeerListListener);
            }
        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
        {
            // notify the connection state changed.
            Log.d(TAG, "wifi_p2p_connection_changed");
            if (null != mP2pManager && null != mP2pChannel)
            {
                NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (null != info && info.isConnected() && null != mConnectionInfoListener)
                {
                    mP2pManager.requestConnectionInfo(mP2pChannel, mConnectionInfoListener);
                }
            }
        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action))
        {
            // notify the current device state changed.
            Log.d(TAG, "wifi_p2p_this_device_changed");
        }
    }
}
