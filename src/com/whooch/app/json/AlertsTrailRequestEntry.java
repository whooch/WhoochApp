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

public class AlertsTrailRequestEntry extends AlertsEntry {
    
    // used to callback into Activity to refresh alerts
    AlertsActivity mParentActivity;
    
    // from JSON
    public String trailRequestId = null;
    public String userId = null;
    public String userImage = null;
    public String userName = null;
    public String whoochId = null;
    public String whoochImage = null;
    public String whoochName = null;
    
    // derived attributes
    public String whoochImageUriSmall = null;
    public String whoochImageUriMedium = null;
    public String whoochImageUriLarge = null;
    public String whoochImageUriDefault = null;
    
    public String userImageUriSmall = null;
    public String userImageUriMedium = null;
    public String userImageUriLarge = null;    
    public String userImageUriDefault = null;
    
    public AlertsTrailRequestEntry() {
        super();
    }
    
    public AlertsTrailRequestEntry(JSONObject json, WindowManager windowMgr, AlertsActivity parentActivity) {        
        super(json);
        
        mParentActivity = parentActivity;
        
        try {
            trailRequestId = json.getString("trailRequestId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        JSONObject info = new JSONObject();
        try {
            info = json.getJSONObject("trailRequestInfo");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            userId = info.getString("userId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            userImage = info.getString("userImage");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            userName = info.getString("userName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            whoochId = info.getString("whoochId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            whoochImage = info.getString("whoochImage");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            whoochName = info.getString("whoochName");
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
        if (userImage != null && userId != null) {
            if (userImage.equals("defaultUser.png")) {
            	userImageUriSmall = Settings.cdnUrl + "s_" + userImage;
            	userImageUriMedium = Settings.cdnUrl + "m_" + userImage;
            	userImageUriLarge = Settings.cdnUrl + "l_" + userImage;
            } else {
            	userImageUriSmall = Settings.cdnUrl + "w" + userId + "_s" + userImage;
            	userImageUriMedium = Settings.cdnUrl + "w" + userId + "_m" + userImage;
            	userImageUriLarge = Settings.cdnUrl + "w" + userId + "_l" + userImage;
            }
        }
        
        // determine proper image to use based on the current screen resolution
        DisplayMetrics metrics = new DisplayMetrics();
        windowMgr.getDefaultDisplay().getMetrics(metrics);
        switch(metrics.densityDpi){
        case DisplayMetrics.DENSITY_LOW:
            whoochImageUriDefault = whoochImageUriSmall;
            userImageUriDefault = userImageUriSmall;
        case DisplayMetrics.DENSITY_MEDIUM:
            whoochImageUriDefault = whoochImageUriMedium;
            userImageUriDefault = userImageUriMedium;
        case DisplayMetrics.DENSITY_HIGH:
            whoochImageUriDefault = whoochImageUriLarge;
            userImageUriDefault = userImageUriLarge;
        }
    }

    @Override
    public String getWhoochImageUrl() {
        return whoochImageUriSmall;
    }

    @Override
    public String getUserImageUrl() {
        return userImageUriSmall;
    }

    @Override
    public String getAlertType() {
        return " has sent you a request to trail...";
    }
    
    @Override
    public String getUserName() {
        return userName;
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
                                                               new ConfirmOpenTrailRequest(), false);
                task.execute();
            }
        };
    }
    
    public OnClickListener getDeclineClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                WhoochApiCallTask task = new WhoochApiCallTask(mParentActivity.getActivityContext(),
                                                               new DeclineOpenTrailRequest(), false);
                task.execute();
            }
        };
    }
    
    private class ConfirmOpenTrailRequest implements WhoochApiCallInterface {
                
        public void preExecute() {}
        
        public HttpRequestBase getHttpRequest() {
            HttpPost request = new HttpPost(Settings.apiUrl + "/whooch/starttrailingopen");
            
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
    
    private class DeclineOpenTrailRequest implements WhoochApiCallInterface {
        
        public void preExecute() {}
        
        public HttpRequestBase getHttpRequest() {
            HttpPost request = new HttpPost(Settings.apiUrl + "/alerts/removerequest");
            
            // Add data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("trailRequestId", trailRequestId));
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