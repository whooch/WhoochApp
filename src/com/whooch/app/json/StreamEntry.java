package com.whooch.app.json;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.whooch.app.helpers.Settings;

public class StreamEntry implements Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -1819540536319873974L;
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
    public FeedbackEntry feedbackInfo = null;
    public String fans = null;
    public String isFan = null;
    public String fanString = null;
    
    // derived attributes
    
    public String userImageUriSmall = null;
    public String userImageUriMedium = null;
    public String userImageUriLarge = null;
    
    public String userImageUriDefault = null;
    
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
        
        try {
            fans = json.getString("fans");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
		if (Integer.parseInt(fans, 10) > 0) {
			if(Integer.parseInt(fans, 10) == 1)
			{
				fanString = "(1 fan)";
			}
			else
			{
				fanString = "(" + fans + " fans)";
			}
		}
        
        try {
            isFan = json.getString("isFan");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        // get feedback info if this post is a reaction
        if (!reactionTo.equals("null")) {
            try {
                feedbackInfo = new FeedbackEntry(json.getJSONObject("feedbackInfo"), windowMgr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
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