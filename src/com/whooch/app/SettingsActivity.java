package com.whooch.app;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.urbanairship.push.PushManager;
import com.whooch.app.helpers.GCMFunctions;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;

public class SettingsActivity extends SherlockPreferenceActivity {

	View mLoadingFooterView;

	private boolean mInvitations = false;
	private boolean mFriendRequests = false;
	private boolean mReactions = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View title_view = inflater.inflate(R.layout.title_bar_generic, null);
		getSupportActionBar().setCustomView(title_view);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		TextView tvhead = (TextView) title_view
				.findViewById(R.id.header_generic_title);
		tvhead.setText("");

		addPreferencesFromResource(R.xml.settings);

		CheckBoxPreference checkMain = (CheckBoxPreference) findPreference("mainNotifications");
		CheckBoxPreference checkInvite = (CheckBoxPreference) findPreference("whoochInvitations");
		CheckBoxPreference checkFriend = (CheckBoxPreference) findPreference("friendRequests");
		CheckBoxPreference checkReactions = (CheckBoxPreference) findPreference("reactions");

		checkMain.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {

				CheckBoxPreference cbp = (CheckBoxPreference) preference;

				if (cbp.isChecked()) {
					GCMFunctions.setToken((Activity) getActivityContext());
				} else {
					GCMFunctions.clearToken((Activity) getActivityContext());
				}

				return true;
			}
		});

		checkInvite
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {

						CheckBoxPreference cbp = (CheckBoxPreference) preference;
						WhoochApiCallTask task = new WhoochApiCallTask(
								getActivityContext(), new UpdateSetting(
										"invitations", cbp.isChecked()), true);
						task.execute();

						return true;
					}
				});

		checkFriend
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {

						CheckBoxPreference cbp = (CheckBoxPreference) preference;
						WhoochApiCallTask task = new WhoochApiCallTask(
								getActivityContext(), new UpdateSetting(
										"friendrequests", cbp.isChecked()),
								true);
						task.execute();

						return true;
					}
				});

		checkReactions
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {

						CheckBoxPreference cbp = (CheckBoxPreference) preference;
						WhoochApiCallTask task = new WhoochApiCallTask(
								getActivityContext(), new UpdateSetting(
										"reactions", cbp.isChecked()), true);
						task.execute();

						return true;
					}
				});

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	@Override
	public void onResume() {
		super.onResume();

		WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(),
				new SettingsInitiate(), true);
		task.execute();

		task = new WhoochApiCallTask(getActivityContext(),
				new SetPushEnabled(), true);
		task.execute();
	}

	private Context getActivityContext() {
		return this;
	}

	private class SettingsInitiate implements WhoochApiCallInterface {

		private String mResponseString = null;

		public void preExecute() {
		}

		public HttpRequestBase getHttpRequest() {
			return new HttpGet(Settings.apiUrl + "/push/1?getPushSettings=true");
		}

		public void handleResponse(String responseString) {
			mResponseString = responseString;
		}

		public void postExecute(int statusCode) {

			if (statusCode == 200) {

				// parse the response as JSON and update the Content Array
				if (!mResponseString.equals("null")) {
					try {
						JSONObject jsonObject = new JSONObject(mResponseString);
						mInvitations = (jsonObject.getString("invitations")
								.equals("1") ? true : false);
						mFriendRequests = (jsonObject.getString(
								"friendrequests").equals("1") ? true : false);
						mReactions = (jsonObject.getString("reactions").equals(
								"1") ? true : false);

						updatePreferences();

					} catch (JSONException e) {
						e.printStackTrace();
						// TODO: error handling
					}
				} else {
					// if it is null we don't mind, there just wasn't anything
					// there
				}

			}

		}

	}

	private class SetPushEnabled implements WhoochApiCallInterface {

		private String mResponseString = null;

		public void preExecute() {
		}

		public HttpRequestBase getHttpRequest() {
			return new HttpGet(Settings.apiUrl
					+ "/push/1?getTokens=true&&platform=android");
		}

		public void handleResponse(String responseString) {
			mResponseString = responseString;
		}

		public void postExecute(int statusCode) {

			boolean apidFound = false;
			if (statusCode == 200) {

				// parse the response as JSON and update the Content Array
				if (!mResponseString.equals("null")) {
					try {

						String apid = PushManager.shared().getAPID();

						JSONObject jsonObject = new JSONObject(mResponseString);

						if (apid != null) {
							Map<String, String> map = new HashMap<String, String>();
							Iterator iter = jsonObject.keys();

							while (iter.hasNext()) {
								
								String key = (String) iter.next();
								String value = jsonObject.getString(key);

								if (apid.equals(value)) {
									apidFound = true;
								}
							}
						}


					} catch (JSONException e) {
						e.printStackTrace();
						// TODO: error handling
					}
				} else {
					// if it is null we don't mind, there just wasn't anything
					// there
				}

			}
			
			CheckBoxPreference check;
			check = (CheckBoxPreference) findPreference("mainNotifications");
			
			if (apidFound) {
				check.setChecked(true);
			} else {
				check.setChecked(false);
			}

		}
	}

	private class UpdateSetting implements WhoochApiCallInterface {

		private String mResponseString = null;
		private String mUpdateType = null;
		private String mUpdateVal = null;

		public UpdateSetting(String updateType, boolean updateVal) {
			mUpdateType = updateType;

			if (updateVal) {
				mUpdateVal = "1";
			} else {
				mUpdateVal = "0";
			}
		}

		public void preExecute() {
		}

		public HttpRequestBase getHttpRequest() {

			HttpPost request = new HttpPost(Settings.apiUrl + "/push/"
					+ mUpdateType);

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("option", mUpdateVal));

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

	private void updatePreferences() {

		CheckBoxPreference check;

		check = (CheckBoxPreference) findPreference("whoochInvitations");
		check.setChecked(mInvitations);

		check = (CheckBoxPreference) findPreference("friendRequests");
		check.setChecked(mFriendRequests);

		check = (CheckBoxPreference) findPreference("reactions");
		check.setChecked(mReactions);

	}

}