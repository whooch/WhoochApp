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

public class AlertsTrailingEntry extends AlertsEntry {
    
    // used to callback into Activity to refresh alerts
    AlertsActivity mParentActivity;
    
    // from JSON
    public String leaderUserId = null;
    public String leaderUserImage = null;
    public String leaderUserName = null;
    public String whoochId = null;
    public String whoochImage = null;
    public String whoochName = null;
    
    // derived attributes
    public String whoochImageUriSmall = null;
    public String whoochImageUriMedium = null;
    public String whoochImageUriLarge = null;
    public String whoochImageUriDefault = null;
    
    public String leaderUserImageUriSmall = null;
    public String leaderUserImageUriMedium = null;
    public String leaderUserImageUriLarge = null;    
    public String leaderUserImageUriDefault = null;
    
    public AlertsTrailingEntry() {
        super();
    }
    
    public AlertsTrailingEntry(JSONObject json, WindowManager windowMgr, AlertsActivity parentActivity) {        
        super(json);
        
        mParentActivity = parentActivity;
        
        JSONObject leaderJson = new JSONObject();
        try {
            leaderJson = json.getJSONObject("leader");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            leaderUserId = leaderJson.getString("userId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            leaderUserImage = leaderJson.getString("userImage");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            leaderUserName = leaderJson.getString("userName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            whoochId = json.getString("whoochId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            whoochImage = json.getString("whoochImage");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            whoochName = json.getString("whoochName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        // determine the URLs for the whooch image
        if (whoochImage != null && whoochId != null) {
            if (whoochImage.equals("defaultWhooch.png")) {
                whoochImageUriSmall = Settings.cdnUrl + "s_" + whoochImage;
                whoochImageUriMedium = Settings.cdnUrl + "m_" + whoochImage;
                whoochImageUriLarge = Settings.cdnUrl + "l_" + whoochImage;
            } else {
                whoochImageUriSmall = Settings.cdnUrl + "w" + whoochId + "_s" + whoochImage;
                whoochImageUriMedium = Settings.cdnUrl + "w" + whoochId + "_m" + whoochImage;
                whoochImageUriLarge = Settings.cdnUrl + "w" + whoochId + "_l" + whoochImage;
            }
        }
        
        // determine the URLs for the user image
        if (leaderUserImage != null && leaderUserId != null) {
            if(leaderUserImage.equals("defaultUser.png")) {
            	leaderUserImageUriSmall = Settings.cdnUrl + "s_" + leaderUserImage;
            	leaderUserImageUriMedium = Settings.cdnUrl + "m_" + leaderUserImage;
            	leaderUserImageUriLarge = Settings.cdnUrl + "l_" + leaderUserImage;
            } else {
                leaderUserImageUriSmall = Settings.cdnUrl + "u" + leaderUserId + "_s" + leaderUserImage;
                leaderUserImageUriMedium = Settings.cdnUrl + "u" + leaderUserId + "_m" + leaderUserImage;
                leaderUserImageUriLarge = Settings.cdnUrl + "u" + leaderUserId + "_l" + leaderUserImage;
            }
        }
        
        // determine proper image to use based on the current screen resolution
        DisplayMetrics metrics = new DisplayMetrics();
        windowMgr.getDefaultDisplay().getMetrics(metrics);
        switch(metrics.densityDpi){
        case DisplayMetrics.DENSITY_LOW:
            whoochImageUriDefault = whoochImageUriSmall;
            leaderUserImageUriDefault = leaderUserImageUriSmall;
        case DisplayMetrics.DENSITY_MEDIUM:
            whoochImageUriDefault = whoochImageUriMedium;
            leaderUserImageUriDefault = leaderUserImageUriMedium;
        case DisplayMetrics.DENSITY_HIGH:
            whoochImageUriDefault = whoochImageUriLarge;
            leaderUserImageUriDefault = leaderUserImageUriLarge;
        }
    }

    @Override
    public String getWhoochImageUrl() {
        return whoochImageUriSmall;
    }

    @Override
    public String getUserImageUrl() {
        return leaderUserImageUriSmall;
    }
    
    @Override
    public String getAlertType() {
        return " has sent you an invitation to trail...";
    }
    
    @Override
    public String getUserName() {
        return leaderUserName;
    }
    
    @Override
    public String getWhoochName() {
        return whoochName;
    }

    public OnClickListener getAcceptClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                WhoochApiCallTask task = new WhoochApiCallTask(mParentActivity.getActivityContext(),
                                                               new ConfirmTrailingRequest(), false);
                task.execute();
            }
        };
    }
    
    public OnClickListener getDeclineClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                WhoochApiCallTask task = new WhoochApiCallTask(mParentActivity.getActivityContext(),
                                                               new DeclineTrailingRequest(), false);
                task.execute();
            }
        };
    }
    
    private class ConfirmTrailingRequest implements WhoochApiCallInterface {
                
        public void preExecute() {}
        
        public HttpRequestBase getHttpRequest() {
            HttpPost request = new HttpPost(Settings.apiUrl + "/alerts/accepttrail");
            
            // Add data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("whoochId", whoochId));
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
    
    private class DeclineTrailingRequest implements WhoochApiCallInterface {
        
        public void preExecute() {}
        
        public HttpRequestBase getHttpRequest() {
            HttpPost request = new HttpPost(Settings.apiUrl + "/alerts/declinetrail");
            
            // Add data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("whoochId", whoochId));
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