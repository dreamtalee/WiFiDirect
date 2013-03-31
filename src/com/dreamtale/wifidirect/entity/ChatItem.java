package com.dreamtale.wifidirect.entity;

public class ChatItem
{
    private boolean mIsSelf     = false;
    private String mChatContent = null;
    
    public void setSelf(boolean isSelf)
    {
        mIsSelf = isSelf;
    }
    
    public boolean isSelf()
    {
        return mIsSelf;
    }
    
    public void setContent(String content)
    {
        mChatContent = content;
    }
    
    public String getContent()
    {
        return mChatContent;
    }
}
