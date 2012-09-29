package com.whooch.app.json;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.whooch.app.helpers.Settings;

public class ListsEntry {
    
    // from JSON
    public String leaderName = null;
    public String type = null;
    public String whoochId = null;
    public String whoochImage = null;
    public String whoochName = null;
    
    // derived attributes
    public String whoochImageUriSmall = null;
    public String whoochImageUriMedium = null;
    public String whoochImageUriLarge = null;
    
    public String whoochImageUriDefault = null;
    
    public ListsEntry() {
    }
    
    public ListsEntry(JSONObject json, WindowManager windowMgr) {
        try {
            leaderName = json.getString("leaderName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            type = json.getString("type");
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
            whoochImageUriSmall = Settings.cdnUrl + "w" + whoochId + "_s" + whoochImage;
            whoochImageUriMedium = Settings.cdnUrl + "w" + whoochId + "_m" + whoochImage;
            whoochImageUriLarge = Settings.cdnUrl + "w" + whoochId + "_l" + whoochImage;
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