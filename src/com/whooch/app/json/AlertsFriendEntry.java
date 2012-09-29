package com.whooch.app.json;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import com.whooch.app.AlertsActivity;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;

public class AlertsFriendEntry extends AlertsEntry {
    
    // used to callback into Activity to refresh alerts
    AlertsActivity mParentActivity;
    
    // from JSON
    public String userId = null;
    public String userImage = null;
    public String userName = null;
    
    // derived attributes
    public String userImageUriSmall = null;
    public String userImageUriMedium = null;
    public String userImageUriLarge = null;    
    public String userImageUriDefault = null;
    
    public AlertsFriendEntry() {
        super();
    }
    
    public AlertsFriendEntry(JSONObject json, WindowManager windowMgr, AlertsActivity parentActivity) {        
        super(json);
        
        mParentActivity = parentActivity;
        
        try {
            userId = json.getString("userId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            userImage = json.getString("userImage");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            userName = json.getString("userName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        // determine the URLs for the user image
        if (userImage != null && userId != null) {
            userImageUriSmall = Settings.cdnUrl + "u" + userId + "_s" + userImage;
            userImageUriMedium = Settings.cdnUrl + "u" + userId + "_m" + userImage;
            userImageUriLarge = Settings.cdnUrl + "u" + userId + "_l" + userImage;
        }
        
        // determine proper image to use based on the current screen resolution
        DisplayMetrics metrics = new DisplayMetrics();
        windowMgr.getDefaultDisplay().getMetrics(metrics);
        switch(metrics.densityDpi){
        case DisplayMetrics.DENSITY_LOW:
            userImageUriDefault = userImageUriSmall;
        case DisplayMetrics.DENSITY_MEDIUM:
            userImageUriDefault = userImageUriMedium;
        case DisplayMetrics.DENSITY_HIGH:
            userImageUriDefault = userImageUriLarge;
        }
    }

    @Override
    public String getImageUrl() {
        return userImageUriDefault;
    }

    @Override
    public String getMessage() {
        return userName + " has sent you a friend request";
    }

    public OnClickListener getAcceptClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                WhoochApiCallTask task = new WhoochApiCallTask(mParentActivity.getActivityContext(),
                                                               new ConfirmFriendRequest(), false);
                task.execute();
            }
        };
    }
    
    public OnClickListener getDeclineClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                WhoochApiCallTask task = new WhoochApiCallTask(mParentActivity.getActivityContext(),
                                                               new DeclineFriendRequest(), false);
                task.execute();
            }
        };
    }
    
    private class ConfirmFriendRequest implements WhoochApiCallInterface {
                
        public HttpRequestBase getHttpRequest() {
            HttpPost request = new HttpPost(Settings.apiUrl + "/alerts/acceptfriend");
            
            // Add data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("userId", userId));
            try {
                request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                // TODO error handling
            }
            
            return request;
        }
        
        public void handleResponse(String responseString) {}
        
        public void postExecute(int statusCode) {
            mParentActivity.refreshAlertsCallback();
        }
    }
    
    private class DeclineFriendRequest implements WhoochApiCallInterface {
        
        public HttpRequestBase getHttpRequest() {
            HttpPost request = new HttpPost(Settings.apiUrl + "/alerts/declinefriend");
            
            // Add data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("userId", userId));
            try {
                request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                // TODO error handling
            }
            
            return request;
        }
        
        public void handleResponse(String responseString) {}
        
        public void postExecute(int statusCode) {
            mParentActivity.refreshAlertsCallback();
        }
    }
    
}