package com.whooch.app;

import java.util.ArrayList;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.whooch.app.helpers.ActionBarHelper;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;
import com.whooch.app.helpers.WhoochHelperFunctions;
import com.whooch.app.json.UserProfileEntry;
import com.whooch.app.ui.UserProfileArrayAdapter;

public class UserProfileActivity extends SherlockListActivity implements
		OnScrollListener {

	boolean mProfileInitiated = false;
	String mUserId = null;
	private String mProfileType = null;
	View mLoadingFooterView;

	private ArrayList<UserProfileEntry> mUserProfileArray = new ArrayList<UserProfileEntry>();
	UserProfileArrayAdapter mAdapter;

	// Unique id counter to prevent Android from reusing the same dialog.
	int mNextDialogId = 0;

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		WhoochHelperFunctions whoochHelper = new WhoochHelperFunctions();
		if (mProfileType == "local"
				&& (whoochHelper.getScreenOrientation(this) == Configuration.ORIENTATION_PORTRAIT)) {

			menu.add(Menu.NONE, 1, 0, "Settings").setShowAsAction(
					MenuItem.SHOW_AS_ACTION_ALWAYS);

			menu.add(Menu.NONE, 2, 0, "Sign Out").setShowAsAction(
					MenuItem.SHOW_AS_ACTION_ALWAYS);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i = null;
		switch (item.getItemId()) {
		case 1:
			i = new Intent(getActivityContext(), SettingsActivity.class);
			startActivity(i);
			return true;
		case 2:
			SharedPreferences settings = getActivityContext()
					.getSharedPreferences("whooch_preferences", 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("username", null);
			editor.putString("userid", null);
			editor.putString("password", null);
			editor.commit();

			i = new Intent(getActivityContext(), LoginActivity.class);
			startActivity(i);
			return true;
	    case android.R.id.home:
	        finish();
	        return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_profile);

		SharedPreferences settings = getActivityContext().getSharedPreferences(
				"whooch_preferences", 0);
		String userId = settings.getString("userid", null);

		Intent i = getIntent();
		Bundle b = i.getExtras();
		mUserId = b.getString("USER_ID");

		if (mUserId.equals(userId)) {
			if(b.containsKey("FORCE_FOREIGN"))
			{
				mProfileType = "localForeign";
			}
			else
			{
				mProfileType = "local";
			}	
		} else {
			mProfileType = "foreign";
		}

		if (mProfileType.equals("local")) {
			ActionBarHelper
					.setupActionBar(getSupportActionBar(),
							new ActionBarHelper.TabListener(
									getApplicationContext()), 4);
		}
		else
		{
			getSupportActionBar().setDisplayShowHomeEnabled(true);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			
			LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View title_view = inflater.inflate(R.layout.title_bar_generic, null);
			getSupportActionBar().setCustomView(title_view);
			getSupportActionBar().setDisplayShowCustomEnabled(true);
			TextView tvhead = (TextView)title_view.findViewById(R.id.header_generic_title);
			tvhead.setText("Profile");
		}
		
		
		mAdapter = new UserProfileArrayAdapter(this, mProfileType,
				mUserProfileArray);
		setListAdapter(mAdapter);

	}

	@Override
	public void onResume() {
		super.onResume();

		if (mProfileType.equals("local")) 
		{
			ActionBarHelper.selectTab(getSupportActionBar(), 4);
		}

		WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(),
				new ProfileInitiate(), true);
		task.execute();

	}

	public Context getActivityContext() {
		return this;
	}

	private class ProfileInitiate implements WhoochApiCallInterface {

		private String mResponseString = null;

		public void preExecute() {
		}

		public HttpRequestBase getHttpRequest() {
			return new HttpGet(Settings.apiUrl + "/user/info?userId=" + mUserId);
		}

		public void handleResponse(String responseString) {
			mResponseString = responseString;
		}

		public void postExecute(int statusCode) {

			if (statusCode == 200) {
				mUserProfileArray.clear();

				// parse the response as JSON and update the Content Array
				if (!mResponseString.equals("null")) {
					try {

						JSONArray jsonArray = new JSONArray(mResponseString);
						// create an object that will be used to populate the
						// List View and add it to the array

						UserProfileEntry entry = new UserProfileEntry(
								jsonArray.getJSONObject(0), getWindowManager());
						mUserProfileArray.add(entry);

						mAdapter.notifyDataSetChanged();

						if (mProfileType == "local") {
							WhoochApiCallTask task = new WhoochApiCallTask(
									getActivityContext(), new GetAlertCount(),
									true);
							task.execute();
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

	private class GetAlertCount implements WhoochApiCallInterface {

		private String mResponseString = null;

		public void preExecute() {
		}

		public HttpRequestBase getHttpRequest() {
			return new HttpGet(Settings.apiUrl + "/alerts/1");
		}

		public void handleResponse(String responseString) {
			mResponseString = responseString;
		}

		public void postExecute(int statusCode) {

			Button ibtn1 = (Button) findViewById(R.id.alerts_button);

			if (statusCode == 200) {

				// parse the response as JSON and update the Content Array
				if (!mResponseString.equals("null")) {
					try {
						JSONArray jsonArray = new JSONArray(mResponseString);

						int alertCount = 0;

						for (int i = 0; i < jsonArray.length(); i++) {

							JSONObject jsonAlert = jsonArray.getJSONObject(i);
							String alertType = jsonAlert.getString("alertType");

							if (alertType.equals("friend")
									|| alertType.equals("trailing")
									|| alertType.equals("contributing")
									|| alertType.equals("trailrequest")) {

								alertCount++;

							}

						}

						ibtn1.setText("Alerts (" + alertCount + ")");

					} catch (JSONException e) {
						e.printStackTrace();
						// TODO: error handling
						ibtn1.setText("Alerts");
					}
				} else {
					ibtn1.setText("Alerts");
				}
			} else {
				ibtn1.setText("Alerts");
			}

		}

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}
	
}