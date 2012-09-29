package com.whooch.app.json;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.whooch.app.helpers.Settings;

public class StreamEntry {
    
    // from JSON
    public String content = null;
    public String image = null;
    public String isContributor = null;
    public String isFriend = null;
    public String reactionTo = null;
    public String reactionType = null;
    public String timestamp = null;
    public String type = null;
    public String userId = null;
    public String userImage = null;
    public String userName = null;
    public String whoochId = null;
    public String whoochImage = null;
    public String whoochName = null;
    public String whoochNumber = null;
    public FeedbackInfo feedbackInfo = null;
    
    // derived attributes
    public String whoochImageUriSmall = null;
    public String whoochImageUriMedium = null;
    public String whoochImageUriLarge = null;
    
    public String whoochImageUriDefault = null;
    
    public StreamEntry() {
    }
    
    public StreamEntry(JSONObject json, WindowManager windowMgr) {
        try {
            content = json.getString("content");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            image = json.getString("image");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            isContributor = json.getString("isContributor");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            isFriend = json.getString("isFriend");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            reactionTo = json.getString("reactionTo");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            reactionType = json.getString("reactionType");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            timestamp = json.getString("timestamp");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            type = json.getString("type");
        } catch (JSONException e) {
            // whooch entries won't have a type
            //e.printStackTrace();
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
        
        try {
            whoochNumber = json.getString("whoochNumber");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        // get feedback info if this post is a reaction
        if (!reactionTo.equals("null")) {
            try {
                feedbackInfo = new FeedbackInfo(json.getJSONObject("feedbackInfo"), windowMgr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
        
        // determine proper image to use based on the current screen resolution
        DisplayMetrics metrics = new DisplayMetrics();
        windowMgr.getDefaultDisplay().getMetrics(metrics);
        switch(metrics.densityDpi){
        case DisplayMetrics.DENSITY_LOW:
            whoochImageUriDefault = whoochImageUriSmall;
        case DisplayMetrics.DENSITY_MEDIUM:
            whoochImageUriDefault = whoochImageUriMedium;
        case DisplayMetrics.DENSITY_HIGH:
            whoochImageUriDefault = whoochImageUriLarge;
        }
    }
    
}