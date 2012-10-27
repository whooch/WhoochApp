package com.whooch.app.json;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.whooch.app.helpers.Settings;

public class FriendsEntry {
    
    // from JSON
    public String userName = null;
    public String userImage = null;
    public String userId = null;
    
    // derived attributes
    public String userImageUriSmall = null;
    public String userImageUriMedium = null;
    public String userImageUriLarge = null;
    
    public String userImageUriDefault = null;
    
    public FriendsEntry() {
    }
    
    public FriendsEntry(JSONObject json, WindowManager windowMgr) {
        try {
            userName = json.getString("userName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            userImage = json.getString("userImage");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            userId = json.getString("userId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        
        // determine the URLs for the user image
        if (userImage != null && userId != null) {
            if (userImage.equals("defaultUser.png")) {
                userImageUriSmall = Settings.cdnUrl + "s_" + userImage;
                userImageUriMedium = Settings.cdnUrl + "m_" + userImage;
                userImageUriLarge = Settings.cdnUrl + "l_" + userImage;
            } else {
                userImageUriSmall = Settings.cdnUrl + "u" + userId + "_s" + userImage;
                userImageUriMedium = Settings.cdnUrl + "u" + userId + "_m" + userImage;
                userImageUriLarge = Settings.cdnUrl + "u" + userId + "_l" + userImage;
            }
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