package com.dreamtale.wifidirect.widget;

import java.util.ArrayList;

import com.dreamtale.wifidirect.R;

import android.content.Context;
import android.content.res.Resources;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * The adapter for the device list.
 * 
 * @author dreamtale
 *
 */
public class DeviceAdapter extends BaseAdapter
{
    private Context mContext = null;
    private ArrayList<WifiP2pDevice> mDeviceList = null;
    
    public DeviceAdapter(Context context, ArrayList<WifiP2pDevice> list)
    {
        mContext = context;
        mDeviceList = list;
    }
    
    @Override
    public int getCount()
    {
        return mDeviceList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mDeviceList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder = null;
        if (null == convertView)
        {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.deviceitem, null);
            viewHolder = new ViewHolder();
            viewHolder.mNameView = (TextView) convertView.findViewById(R.id.devicename);
            viewHolder.mStatusView = (TextView) convertView.findViewById(R.id.devicestatus);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        
        WifiP2pDevice device = mDeviceList.get(position);
        if (null != device)
        {
            viewHolder.mNameView.setText(device.deviceName  + "  mac: " + device.deviceAddress);
            viewHolder.mStatusView.setText(getStatusStr(device.status));
        }
        
        return convertView;
    }
    
    private String getStatusStr(int status)
    {
        Resources res = mContext.getResources();
        if (WifiP2pDevice.AVAILABLE == status)
        {
            return res.getString(R.string.status_avail);
        }
        else if (WifiP2pDevice.CONNECTED == status)
        {
            return res.getString(R.string.status_connected);
        }
        else if (WifiP2pDevice.FAILED == status)
        {
            return res.getString(R.string.status_failed);
        }
        else if (WifiP2pDevice.INVITED == status)
        {
            return res.getString(R.string.status_invalid);
        }
        else if (WifiP2pDevice.UNAVAILABLE == status)
        {
            return res.getString(R.string.status_unavail);
        }
        return "";
    }

    static class ViewHolder
    {
        TextView mNameView;
        TextView mStatusView;
    }
}
