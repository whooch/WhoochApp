package com.whooch.app;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.view.View;

import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;

public class WhoochSettingsActivity extends PreferenceActivity {
    
    private String mWhoochId = null;
    private boolean mHasFeedback = false;
    private String mWhoochName = null;

    View mLoadingFooterView;
    
    // Unique id counter to prevent Android from reusing the same dialog.
    int mNextDialogId = 0;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);        
        addPreferencesFromResource(R.xml.whoochsettings);
        
		Intent i = getIntent();
		Bundle b = i.getExtras();
		mWhoochId = b.getString("WHOOCH_ID");
		mWhoochName = b.getString("WHOOCH_NAME");
		mHasFeedback = b.getBoolean("FEEDBACK");
		
		PreferenceCategory pc=(PreferenceCategory)findPreference("WhoochSettingsTitle");
		pc.setTitle(mWhoochName + " Settings");
		
		CheckBoxPreference checkUpdate = (CheckBoxPreference) findPreference("updates");
		if (b.getString("UPDATE_PUSH").equals("1"))
		{
			checkUpdate.setChecked(true);
		}
		else
		{
			checkUpdate.setChecked(false);
		}
		
		CheckBoxPreference checkUpdates = (CheckBoxPreference) findPreference("updates");
		checkUpdates.setOnPreferenceClickListener(new OnPreferenceClickListener() {

		    public boolean onPreferenceClick(Preference preference) {
		    	
		    	CheckBoxPreference cbp = (CheckBoxPreference)preference;
				WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(),
						new UpdateWhoochSetting("updates", cbp.isChecked()), true);
				task.execute();
		    	
		        return true; 
		    }
		});
		
		if(mHasFeedback)
		{
		    CheckBoxPreference checkboxPref = new CheckBoxPreference(this);
		    checkboxPref.setTitle("Feedback");
		    checkboxPref.setKey("feedback");
		    checkboxPref.setSummary("Notify me when this whooch receives new feedback");
		  
			checkboxPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			    public boolean onPreferenceClick(Preference preference) {
			    	
			    	CheckBoxPreference cbp = (CheckBoxPreference)preference;
					WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(),
							new UpdateWhoochSetting("feedback", cbp.isChecked()), true);
					task.execute();
			    	
			        return true; 
			    }
			});
			
			if (b.getString("FEEDBACK_PUSH").equals("1"))
			{
				checkboxPref.setChecked(true);
			}
			else
			{
				checkboxPref.setChecked(false);
			}
			
			this.getPreferenceScreen().addPreference(checkboxPref);
		}
        
    }
    
    @Override
    public void onResume() {
        super.onResume();

    }
    
    private Context getActivityContext() {
        return this;
    }
    
	private class UpdateWhoochSetting implements WhoochApiCallInterface {

		private String mResponseString = null;
		private String mUpdateType = null;
		private String mUpdateVal = null;
		
		public UpdateWhoochSetting(String updateType, boolean updateVal)
		{
			mUpdateType = updateType;
			
			if(updateVal)
			{
				mUpdateVal = "1";
			}
			else
			{
				mUpdateVal = "0";
			}
		}

		public void preExecute() {
		}

		public HttpRequestBase getHttpRequest() {
			
			HttpPost request = new HttpPost(Settings.apiUrl + "/push/" + mUpdateType);
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("option",
					mUpdateVal));
			
			nameValuePairs.add(new BasicNameValuePair("whoochId",
					mWhoochId));

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


		}

	}
}