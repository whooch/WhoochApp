package com.whooch.app;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;
import com.whooch.app.json.WhoochProfileEntry;

public class WhoochSettingsActivity extends SherlockPreferenceActivity {

	private String mWhoochId = null;
	private String mWhoochName = null;
	private boolean hasFeedback = false;
	private boolean hasStreaming = false;
	private CheckBoxPreference updatePref = null;
	private CheckBoxPreference streamPref = null;
	private CheckBoxPreference feedbackPref = null;

	View mLoadingFooterView;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
	
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View title_view = inflater
				.inflate(R.layout.title_bar_generic, null);
		getSupportActionBar().setCustomView(title_view);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		TextView tvhead = (TextView) title_view
				.findViewById(R.id.header_generic_title);
		tvhead.setText("");

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

		Intent i = getIntent();
		Bundle b = i.getExtras();
		mWhoochId = b.getString("WHOOCH_ID");
		mWhoochName = b.getString("WHOOCH_NAME");
		hasFeedback = b.getBoolean("HAS_FEEDBACK");
		hasStreaming = b.getBoolean("HAS_STREAMING");

		if (mWhoochId == null || mWhoochName == null) {
			Toast.makeText(getApplicationContext(),
					"Something went wrong, please try again",
					Toast.LENGTH_SHORT).show();
			finish();
		}

		addPreferencesFromResource(R.xml.whoochsettings);

		PreferenceCategory pc = (PreferenceCategory) findPreference("WhoochSettingsTitle");
		pc.setTitle(mWhoochName + " Settings");

		updatePref = new CheckBoxPreference(getActivityContext());
		updatePref.setTitle("Updates");
		updatePref.setKey("wpupdates");
		updatePref.setSummary("Notify me when this whooch is updated");

		updatePref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {

						CheckBoxPreference cbp = (CheckBoxPreference) preference;
						WhoochApiCallTask task = new WhoochApiCallTask(
								getActivityContext(), new UpdateWhoochSetting(
										"updates", cbp.isChecked()), true);
						task.execute();

						return true;
					}
				});

		if (hasFeedback) {
			feedbackPref = new CheckBoxPreference(getActivityContext());
			feedbackPref.setTitle("Feedback");
			feedbackPref.setKey("wpfeedback");
			feedbackPref
					.setSummary("Notify me when this whooch receives new feedback");

			feedbackPref
					.setOnPreferenceClickListener(new OnPreferenceClickListener() {

						public boolean onPreferenceClick(Preference preference) {

							CheckBoxPreference cbp = (CheckBoxPreference) preference;
							WhoochApiCallTask task = new WhoochApiCallTask(
									getActivityContext(),
									new UpdateWhoochSetting("feedback", cbp
											.isChecked()), true);
							task.execute();

							return true;
						}
					});
		}

		if (hasStreaming) {
			streamPref = new CheckBoxPreference(getActivityContext());
			streamPref.setTitle("Streaming");
			streamPref.setKey("wpstreaming");
			streamPref
					.setSummary("I want updates from this whooch to appear in my stream");

			streamPref
					.setOnPreferenceClickListener(new OnPreferenceClickListener() {

						public boolean onPreferenceClick(Preference preference) {

							CheckBoxPreference cbp = (CheckBoxPreference) preference;
							if (cbp.isChecked()) {
								WhoochApiCallTask task = new WhoochApiCallTask(
										getActivityContext(),
										new UpdateWhoochStreaming(
												"startstreaming"), true);
								task.execute();
							} else {
								WhoochApiCallTask task = new WhoochApiCallTask(
										getActivityContext(),
										new UpdateWhoochStreaming(
												"stopstreaming"), true);
								task.execute();
							}
							return true;
						}
					});

		}

		WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(),
				new SettingsInitiate(), true);
		task.execute();

	}

	private PreferenceActivity getActivityContext() {
		return this;
	}

	private class UpdateWhoochSetting implements WhoochApiCallInterface {

		private String mResponseString = null;
		private String mUpdateType = null;
		private String mUpdateVal = null;

		public UpdateWhoochSetting(String updateType, boolean updateVal) {
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

		}

	}

	private class UpdateWhoochStreaming implements WhoochApiCallInterface {

		private String mResponseString = null;
		private String mUpdateType = null;

		public UpdateWhoochStreaming(String updateType) {
			mUpdateType = updateType;
		}

		public void preExecute() {
		}

		public HttpRequestBase getHttpRequest() {

			HttpPost request = new HttpPost(Settings.apiUrl + "/stream/"
					+ mUpdateType);

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
			SharedPreferences settings = getSharedPreferences(
					"whooch_preferences", 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("streaming_updated", true);
			editor.commit();
			
		}

	}

	private class SettingsInitiate implements WhoochApiCallInterface {

		private String mResponseString = null;

		public void preExecute() {
		}

		public HttpRequestBase getHttpRequest() {
			Intent i = getIntent();
			Bundle b = i.getExtras();
			mWhoochId = b.getString("WHOOCH_ID");
			if (mWhoochId == null) {
				Toast.makeText(getApplicationContext(),
						"Something went wrong, please try again",
						Toast.LENGTH_SHORT).show();
				finish();
			}

			return new HttpGet(Settings.apiUrl + "/whooch/" + mWhoochId
					+ "?info=true");
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
						// create an object that will be used to populate the
						// List View and add it to the array

						WhoochProfileEntry entry = new WhoochProfileEntry(
								jsonObject, getWindowManager());

						if (findPreference("wpupdates") == null) {
						getActivityContext().getPreferenceScreen()
								.addPreference(updatePref);
						}
						CheckBoxPreference check = (CheckBoxPreference) findPreference("wpupdates");
						if (entry.updatePush.equals("1")) {
							check.setChecked(true);
						} else {
							check.setChecked(false);
						}

						if (hasFeedback) {
							if (findPreference("wpfeedback") == null) {
							getActivityContext().getPreferenceScreen()
									.addPreference(feedbackPref);
							}
							check = (CheckBoxPreference) findPreference("wpfeedback");
							if (entry.feedbackPush.equals("1")) {
								check.setChecked(true);
							} else {
								check.setChecked(false);
							}
						}

						if (hasStreaming) {
							if (findPreference("wpstreaming") == null) {
								getActivityContext().getPreferenceScreen()
										.addPreference(streamPref);
							}
							check = (CheckBoxPreference) findPreference("wpstreaming");
							if (entry.isStreaming.equals("1")) {
								check.setChecked(true);
							} else {
								check.setChecked(false);
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

		}

	}
}