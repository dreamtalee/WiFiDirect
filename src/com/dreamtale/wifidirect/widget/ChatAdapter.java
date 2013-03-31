package com.dreamtale.wifidirect.widget;

import java.util.ArrayList;

import com.dreamtale.wifidirect.R;
import com.dreamtale.wifidirect.entity.ChatItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChatAdapter extends BaseAdapter
{
    private Context mContext = null;
    private ArrayList<ChatItem> mDataList = null;
    
    public ChatAdapter(Context context, ArrayList<ChatItem> dataList)
    {
        mContext = context;
        mDataList = dataList;
    }
    
    @Override
    public int getCount()
    {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mDataList.get(position);
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
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_item, null);
            viewHolder.chatView = (TextView)convertView.findViewById(R.id.chattext);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        ChatItem item = mDataList.get(position);
        viewHolder.chatView.setText(item.getContent());
        return convertView;
    }

    static class ViewHolder
    {
        TextView chatView;
    }
}
