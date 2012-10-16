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
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Toast;

import com.whooch.app.InviteUserActivity;
import com.whooch.app.UploadPhotoActivity;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;

public class WhoochProfileEntry {
	
	private static final int UPLOAD_PHOTO_WHOOCH = 2;
    
    // from JSON
    public String whoochName = null;
    public String type = null;
    public String description = null;
    public String trailingCount = null;
    public String whoochImage = null;
    public String rating = null;
    public String whoochId = null;
    public String contributingList = null;
    public String trailingList = null;
    public String leaderName = null;
    public String leaderImage = null;
    public String leaderId = null;
    public String isContributing = null;
    public String isTrailing = null;
    public String isStreaming = null;
    public String updateAlerts = null;
    public String feedbackAlerts = null;
    public String updatePush = null;
    public String feedbackPush = null;
    
    // derived attributes
    public String whoochImageUriSmall = null;
    public String whoochImageUriMedium = null;
    public String whoochImageUriLarge = null;
    
    public String whoochImageUriDefault = null;
    
    public WhoochProfileEntry() {
    }
    
    public WhoochProfileEntry(JSONObject json, WindowManager windowMgr) {
        try {
            whoochName = json.getString("whoochName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            type = json.getString("type");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            description = json.getString("description");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            trailingCount = json.getString("trailingCount");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            whoochImage = json.getString("whoochImage");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            rating = json.getString("rating");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            whoochId = json.getString("whoochId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            leaderName = json.getJSONObject("leader").getString("userName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            leaderId = json.getJSONObject("leader").getString("userId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            leaderImage = json.getJSONObject("leader").getString("userImage");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            isContributing = json.getString("isContributing");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            isTrailing = json.getString("isTrailing");
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
    
    public OnClickListener getInviteUserClickListener(){
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
            	Intent i = new Intent(v.getContext(), InviteUserActivity.class);
            	i.putExtra("WHOOCH_ID", whoochId);
            	v.getContext().startActivity(i);
            }
        };
    	
    }
    
    public OnClickListener getUpdatePhotoClickListener(){
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
            	Intent i = new Intent(v.getContext(), UploadPhotoActivity.class);
            	i.putExtra("UPLOAD_TYPE", UPLOAD_PHOTO_WHOOCH);
            	i.putExtra("WHOOCH_ID", whoochId);
            	v.getContext().startActivity(i);
            }
        };
    }
    
    public OnClickListener getTrailWhoochClickListener(){
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                WhoochApiCallTask task = new WhoochApiCallTask(v.getContext(), new TrailWhooch(whoochId, v.getContext()), true);
                task.execute();
            }
        };
    }
    
	private class TrailWhooch implements WhoochApiCallInterface {

		private String mWhoochId = null;
		private String mResponseString = null;
		private Context mContext = null;

		public TrailWhooch(String whoochId, Context context) {
			mWhoochId = whoochId;
			mContext = context;
		}

		public HttpRequestBase getHttpRequest() {

			HttpPost request = new HttpPost(Settings.apiUrl
					+ "/whooch/starttrailingopen");

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("whoochId", mWhoochId));

			try {
				request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				// TODO error handling
			}

			return request;
		}

		public void handleResponse(String responseString) {
			mResponseString = responseString;
		}

		public void postExecute(int statusCode) {

			if (statusCode == 200) {
				// parse the response as JSON and update the Content Array
				if ((mResponseString != null)
						&& (!mResponseString.equals("null"))) {
					try {
						JSONObject jsonObject = new JSONObject(mResponseString);
						String trailStatus = jsonObject
								.getString("trailingStatus");

						if ((trailStatus != null)
								&& (trailStatus.equals("true"))) {
							Toast.makeText(mContext,
									"You are now trailing this whooch",
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(mContext,
									"Something went wrong, try again",
									Toast.LENGTH_SHORT).show();
						}

					} catch (JSONException e) {
						e.printStackTrace();
						// TODO: error handling
					}
				} else {
					Toast.makeText(mContext,
							"Something went wrong, try again",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(mContext,
						"Something went wrong, try again",
						Toast.LENGTH_SHORT).show();
			}
		}
	}
    
}