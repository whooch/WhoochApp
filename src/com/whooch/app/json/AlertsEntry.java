package com.whooch.app.json;

import org.json.JSONException;
import org.json.JSONObject;

import android.view.View.OnClickListener;

abstract public class AlertsEntry {
    
    public String alertType = null;

    abstract public String getUserImageUrl();
    abstract public String getWhoochImageUrl();
    abstract public String getMessage();
    abstract public OnClickListener getAcceptClickListener();
    abstract public OnClickListener getDeclineClickListener();
    
    public AlertsEntry() {
    }
    
    public AlertsEntry(JSONObject json) {
        try {
            alertType = json.getString("alertType");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
}