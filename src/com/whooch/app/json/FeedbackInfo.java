package com.whooch.app.json;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.whooch.app.helpers.Settings;

public class FeedbackInfo {
    
    // from JSON
    public String content = null;
    public String feedbackId = null;
    public String timestamp = null;
    public String userId = null;
    public String userImage = null;
    public String userName = null;
    public String whoochId = null;
    public String whoochImage = null;
    public String whoochName = null;
    
    // derived attributes
    public String userImageUriSmall = null;
    public String userImageUriMedium = null;
    public String userImageUriLarge = null;
    
    public String userImageUriDefault = null;
    
    public FeedbackInfo() {
    }
    
    public FeedbackInfo(JSONObject json, WindowManager windowMgr) {
        try {
            content = json.getString("content");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            feedbackId = json.getString("feedbackId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            timestamp = json.getString("timestamp");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
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
        
        // determine the URLs for the user image
        if (whoochImage != null && whoochId != null) {
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
    
}