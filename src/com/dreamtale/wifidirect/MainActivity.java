package com.dreamtale.wifidirect;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dreamtale.wifidirect.broadcast.WiFiDirectReceiver;
import com.dreamtale.wifidirect.entity.ChatItem;
import com.dreamtale.wifidirect.interfaces.IWifiP2pListener;
import com.dreamtale.wifidirect.widget.ChatAdapter;
import com.dreamtale.wifidirect.widget.DeviceAdapter;

/**
 * The main activity.
 * 
 * @author dreamtale
 * 
 */
public class MainActivity extends Activity implements ChannelListener,
        PeerListListener, ConnectionInfoListener, GroupInfoListener,
        OnClickListener, OnItemClickListener, IWifiP2pListener
{
    private final static String TAG = "MainActivity";

    // The state of the view.
    private final static int STATE_VIEW_LOADING = 0x01;
    private final static int STATE_VIEW_FAIL = 0x02;
    private final static int STATE_VIEW_SUCCESS = 0x04;
    private final static int STATE_VIEW_SUCCESS_NO_DEVICE = 0x05;
    private final static int STATE_VIEW_CONNECT_FAIL = 0x06;

    private WifiP2pManager mP2pManager = null;
    private Channel mP2pChannel = null;
    private WiFiDirectReceiver mP2pReceiver = null;

    private StringBuffer mpassword = new StringBuffer();
    private ArrayList<WifiP2pDevice> mDeviceList = null;
    private DeviceAdapter mDeviceAdapter = null;

    // Layout view
    private Button mScanBtn = null;
    private ProgressBar mLoadingBar = null;
    private TextView mInfoView = null;
    private ListView mDeviceListView = null;
    private ListView mChatListView = null;
    private Button mGetPassBtn = null;
    private Button mStartChat = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mScanBtn = (Button) findViewById(R.id.scanbtn);
        mScanBtn.setOnClickListener(this);
        mLoadingBar = (ProgressBar) findViewById(R.id.loadingbar);
        mInfoView = (TextView) findViewById(R.id.infoview);
        mDeviceListView = (ListView) findViewById(R.id.devicelist);
        mDeviceListView.setOnItemClickListener(this);
        mChatListView = (ListView) findViewById(R.id.chatlist);
        mGetPassBtn = (Button) findViewById(R.id.passbtn);
        mGetPassBtn.setOnClickListener(this);
        mStartChat = (Button) findViewById(R.id.startchat);
        mStartChat.setOnClickListener(this);

        initWifiDirect();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerWifiReceiver();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unRegisterWifiReceiver();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onChannelDisconnected()
    {
        Log.d(TAG, "Channel disconnected!");
    }

    @Override
    public void onClick(View v)
    {
        int id = v.getId();
        switch (id)
        {
        case R.id.scanbtn:
            mP2pManager.discoverPeers(mP2pChannel, new ActionListener()
            {

                @Override
                public void onSuccess()
                {
                    Log.d(TAG, "discover peer success!");
                }

                @Override
                public void onFailure(int reason)
                {
                    Log.d(TAG, "discover peer fail and reasion code is " + reason);
                    setState(STATE_VIEW_FAIL, reason);
                }
            });
            setState(STATE_VIEW_LOADING, -1);
            break;
        case R.id.passbtn:
            if (0 != mpassword.length())
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Information").setMessage("The password is " + mpassword).create().show();
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Warning").setMessage("No successed connection.").create().show();
            }
            break;
        case R.id.startchat:
            startChat();
        default:
            break;
        }
    }



    private void setState(int state, int failedReason)
    {
        if (STATE_VIEW_LOADING == state)
        {
            mInfoView.setVisibility(View.INVISIBLE);
            mLoadingBar.setVisibility(View.VISIBLE);
        }
        else if (STATE_VIEW_FAIL == state)
        {
            String str = "";
            mInfoView.setVisibility(View.VISIBLE);
            mDeviceListView.setVisibility(View.INVISIBLE);
            mLoadingBar.setVisibility(View.INVISIBLE);
            
            if (WifiP2pManager.ERROR == failedReason)
            {
                str =getString(R.string.nodeviceavail);
            }
            else if (WifiP2pManager.BUSY == failedReason)
            {
                str =getString(R.string.servicebusy);
            }
            else if (WifiP2pManager.P2P_UNSUPPORTED == failedReason)
            {
                str =getString(R.string.notsupport);
            }
            Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
            mInfoView.setText(str);
            mLoadingBar.setVisibility(View.INVISIBLE);
        }
        else if (STATE_VIEW_SUCCESS_NO_DEVICE == state)
        {
            String str = getString(R.string.nodeviceavail);
            mLoadingBar.setVisibility(View.INVISIBLE);
            mDeviceListView.setVisibility(View.INVISIBLE);
            mInfoView.setVisibility(View.VISIBLE);
            mInfoView.setText(str);
            Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        }
        else if (STATE_VIEW_CONNECT_FAIL == state)
        {
            String str = getString(R.string.connectfail);
            Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        }
        else if (STATE_VIEW_SUCCESS == state)
        {
            mDeviceListView.setVisibility(View.VISIBLE);
            mInfoView.setVisibility(View.INVISIBLE);
            mLoadingBar.setVisibility(View.INVISIBLE);
        }
    }

    private void initWifiDirect()
    {
        mP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mP2pChannel = mP2pManager.initialize(this, getMainLooper(), this);
    }

    private void registerWifiReceiver()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        if (null == mP2pReceiver)
        {
            mP2pReceiver = new WiFiDirectReceiver(mP2pManager, mP2pChannel,
                    this);
            mP2pReceiver.setPeerListListener(this);
            mP2pReceiver.setConnectionInfoListener(this);
        }
        registerReceiver(mP2pReceiver, filter);
    }

    private void unRegisterWifiReceiver()
    {
        if (null != mP2pReceiver)
        {
            unregisterReceiver(mP2pReceiver);
            mP2pReceiver.setPeerListListener(null);
            mP2pReceiver.setConnectionInfoListener(null);
            mP2pReceiver = null;
        }
    }

    @Override
    public void onWiFiStateChange(int state)
    {
        Log.d(TAG, "WiFi State Changed to " + state);
        if (WifiP2pManager.WIFI_P2P_STATE_ENABLED == state)
        {

        }
        else
        {
            String str = getString(R.string.servicedisabled);
            mInfoView.setVisibility(View.VISIBLE);
            mInfoView.setText(str);
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers)
    {
        Log.d(TAG, "on peer available size is " + peers.getDeviceList().size());
        if (null == mDeviceList)
        {
            mDeviceList = new ArrayList<WifiP2pDevice>();
        }
        else
        {
            mDeviceList.clear();
        }
        mDeviceList.addAll(peers.getDeviceList());

        if (0 == mDeviceList.size())
        {
            setState(STATE_VIEW_SUCCESS_NO_DEVICE, -1);
        }
        else
        {
            setState(STATE_VIEW_SUCCESS, -1);
            if (null == mDeviceAdapter)
            {
                mDeviceAdapter = new DeviceAdapter(this, mDeviceList);
                mDeviceListView.setAdapter(mDeviceAdapter);
            }
            else
            {
                mDeviceAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id)
    {
        WifiP2pDevice device = mDeviceList.get(position);
        Log.d(TAG, "The device current status is " + device.status);

        if (WifiP2pDevice.CONNECTED == device.status)
        {
            mP2pManager.removeGroup(mP2pChannel, new ActionListener()
            {
                
                @Override
                public void onSuccess()
                {
                    Log.d(TAG, "removeGroup success");
                    if (null != mDeviceAdapter)
                    {
                        mDeviceAdapter.notifyDataSetChanged();
                    }
                }
                
                @Override
                public void onFailure(int reason)
                {
                    Log.d(TAG, "removeGroup fail and reason code is " + reason);
                }
            });
        }
        else if (WifiP2pDevice.INVITED == device.status)
        {
//            if (null != mP2pManager && null != mP2pChannel)
//            {
//                mP2pManager.cancelConnect(mP2pChannel, new ActionListener()
//                {
//                    
//                    @Override
//                    public void onSuccess()
//                    {
//                        Log.d(TAG, "cancel connect success");
//                    }
//                    
//                    @Override
//                    public void onFailure(int reason)
//                    {
//                        Log.d(TAG, "cancel connect fail");
//                    }
//                });
//            }
        }
        else if (WifiP2pDevice.FAILED == device.status || WifiP2pDevice.AVAILABLE == device.status)
        {
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            int forceWps = SystemPropertiesProxy.getInt(getApplicationContext(),"wifidirect.wps", -1);
            if (forceWps != -1)
            {
                config.wps.setup = forceWps;
            }
            else
            {
                if (device.wpsPbcSupported())
                {
                    config.wps.setup = WpsInfo.PBC;
                }
                else if (device.wpsKeypadSupported())
                {
                    config.wps.setup = WpsInfo.KEYPAD;
                }
                else
                {
                    config.wps.setup = WpsInfo.DISPLAY;
                }
            }
            
            mP2pManager.connect(mP2pChannel, config, new ActionListener()
            {

                @Override
                public void onSuccess()
                {
                    Log.d(TAG, "connnect peer success!");
                    if (null != mDeviceAdapter)
                    {
                        mDeviceAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(int reason)
                {
                    Log.d(TAG, "connnect peer fail and reason code is " + reason);
                    setState(STATE_VIEW_CONNECT_FAIL, reason);
                }
            });
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info)
    {
        Log.d(TAG, "ConnectionInfo Available!");
        if (null != mDeviceAdapter)
        {
            mDeviceAdapter.notifyDataSetChanged();
        }
        if (info.groupFormed)
        {
            if (info.isGroupOwner)
            {
                // Start socket server.
                
            }
            else
            {
                Log.d(TAG, "The group owner ip is " + info.groupOwnerAddress);
                // Start to connect to group owner.
            }
            
            if (null != mP2pManager && null != mP2pChannel)
            {
                mP2pManager.requestGroupInfo(mP2pChannel, this);
            }
        }
    }
    
    private void verifyWiFiDirect()
    {
        boolean support = getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT);
        String message = support ? "The device support WiFi Direct." : "The device doesn't support WiFi Direct.";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Information").setMessage(message).create().show();
    }
    
    private void startChat()
    {
        mScanBtn.setVisibility(View.GONE);
        mGetPassBtn.setVisibility(View.GONE);
        mStartChat.setVisibility(View.GONE);
        mChatListView.setAdapter(new ChatAdapter(this, dumyChatInfo()));
        flip();
    }

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group)
    {
        Log.d(TAG, "GroupInfo Available!");
        Log.d(TAG, "group name is " + group.getNetworkName() + " group password is " + group.getPassphrase());
        if (0 != mpassword.length())
        {
            mpassword.delete(0, mpassword.length() - 1);
        }
        mpassword.append(group.getPassphrase());
    }
    
    private Interpolator accelerator = new AccelerateInterpolator();
    private Interpolator decelerator = new DecelerateInterpolator();

    private void flip()
    {
        final ListView visibleList;
        final ListView invisibleList;
        if (mDeviceListView.getVisibility() == View.GONE)
        {
            visibleList = mChatListView;
            invisibleList = mDeviceListView;
        }
        else
        {
            invisibleList = mChatListView;
            visibleList = mDeviceListView;
        }
        ObjectAnimator visToInvis = ObjectAnimator.ofFloat(visibleList,
                "rotationY", 0f, 90f);
        visToInvis.setDuration(500);
        visToInvis.setInterpolator(accelerator);
        final ObjectAnimator invisToVis = ObjectAnimator.ofFloat(invisibleList,
                "rotationY", -90f, 0f);
        invisToVis.setDuration(500);
        invisToVis.setInterpolator(decelerator);
        visToInvis.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator anim)
            {
                visibleList.setVisibility(View.GONE);
                invisToVis.start();
                invisibleList.setVisibility(View.VISIBLE);
            }
        });
        visToInvis.start();
    }
    
    private ArrayList<ChatItem> dumyChatInfo()
    {
        ArrayList<ChatItem> list = new ArrayList<ChatItem>();
        for (int i = 0; i < 3; i++)
        {
            ChatItem item = new ChatItem();
            item.setContent("Hello");
            list.add(item);
        }
        return list;
    }
}
