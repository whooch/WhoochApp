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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Toast;

import com.whooch.app.AlertsActivity;
import com.whooch.app.LoginActivity;
import com.whooch.app.PushSettingsActivity;
import com.whooch.app.UploadPhotoActivity;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;

public class UserProfileEntry {
	
	private static final int UPLOAD_PHOTO_USER = 1;
    
    // from JSON
    public String userId = null;
    public String userName = null;
    public String firstName = null;
    public String lastName = null;
    public String location = null;
    public String bio = null;
    public String isFriend = null;
    public String isSubscriber = null;
    public String userImage = null;
    
    // derived attributes
    public String userImageUriSmall = null;
    public String userImageUriMedium = null;
    public String userImageUriLarge = null;
    
    public String userImageUriDefault = null;
    
    public UserProfileEntry() {
    }
    
    public UserProfileEntry(JSONObject json, WindowManager windowMgr) {
        try {
            userId = json.getString("userId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            userName = json.getString("userName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            firstName = json.getString("firstName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            lastName = json.getString("lastName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            bio = json.getString("bio");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            isFriend = json.getString("isFriend");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            isSubscriber = json.getString("isSubscriber");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            userImage = json.getString("userImage");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        // determine the URLs for the whooch image
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
    
    public OnClickListener getPushSettingsClickListener(){
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
            	Intent i = new Intent(v.getContext(), PushSettingsActivity.class);
            	v.getContext().startActivity(i);
            }
        };
    	
    }
    
    public OnClickListener getFriendRequestClickListener(){
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
            	//send friend request
                WhoochApiCallTask task = new WhoochApiCallTask(v.getContext(), new SendFriendRequest(userId, v.getContext()), true);
                task.execute();
            }
        };
    	
    }
    
    public OnClickListener getUpdatePhotoClickListener(){
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
            	Intent i = new Intent(v.getContext(), UploadPhotoActivity.class);
            	i.putExtra("UPLOAD_TYPE", UPLOAD_PHOTO_USER);
            	v.getContext().startActivity(i);
            }
        };
    }
    
    public OnClickListener getAlertsClickListener(){
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), AlertsActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                v.getContext().startActivity(i);
            }
        };
    }
    
	private class SendFriendRequest implements WhoochApiCallInterface {

		private String mUserId = null;
		private Context mContext = null;


		public SendFriendRequest(String userId, Context context) {
			mUserId = userId;
			mContext = context;
		}

		public HttpRequestBase getHttpRequest() {

			HttpPost request = new HttpPost(Settings.apiUrl
					+ "/friends/request");

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("userId", mUserId));

			try {
				request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				// TODO error handling
			}

			return request;
		}

		public void handleResponse(String responseString) {
			
		}

		public void postExecute(int statusCode) {

			// parse the response as JSON and update the Content Array
			if (statusCode == 200) {
				Toast.makeText(mContext, "Friend request sent",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mContext,
						"Something went wrong, try again", Toast.LENGTH_SHORT)
						.show();
			}

		}
	}
    
}