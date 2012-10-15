package com.whooch.app.json;

import org.json.JSONException;
import org.json.JSONObject;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

public class PushSettingsEntry {
    
    // from JSON
    public String userId = null;
    
    public PushSettingsEntry() {
    }
    
    public PushSettingsEntry(JSONObject json, WindowManager windowMgr) {
        try {
            userId = json.getString("userId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    public OnClickListener getPushNotificationsClickListener(){
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            }
        };
    	
    }
    public OnClickListener getWhoochInvitationsClickListener(){
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
            
            }
        };
    }
    public OnClickListener getFriendRequestsClickListener(){
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            }
        };
    }
    public OnClickListener getReactionsClickListener(){
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            }
        };
    }
    
}