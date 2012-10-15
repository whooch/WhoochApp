package com.whooch.app;

import java.util.ArrayList;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.whooch.app.helpers.ActionBarHelper;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;
import com.whooch.app.json.UserProfileEntry;
import com.whooch.app.ui.UserProfileArrayAdapter;

public class UserProfileActivity extends SherlockListActivity implements
		OnScrollListener {

	boolean mProfileInitiated = false;
	String mUserId = null;
	private ArrayList<UserProfileEntry> mUserProfileArray = new ArrayList<UserProfileEntry>();
	UserProfileArrayAdapter mAdapter;
	private String mProfileType = null;
	View mLoadingFooterView;

	// Unique id counter to prevent Android from reusing the same dialog.
	int mNextDialogId = 0;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		if (mProfileType == "local") {
			
			menu.add(Menu.NONE, 1, 0, "Alerts").setShowAsAction(
					MenuItem.SHOW_AS_ACTION_ALWAYS);
			
			menu.add(Menu.NONE, 2, 0, "Push Settings").setShowAsAction(
					MenuItem.SHOW_AS_ACTION_ALWAYS);

			menu.add(Menu.NONE, 3, 0, "Update Photo").setShowAsAction(
					MenuItem.SHOW_AS_ACTION_ALWAYS);

			menu.add(Menu.NONE, 4, 0, "Sign Out").setShowAsAction(
					MenuItem.SHOW_AS_ACTION_ALWAYS);
		} else {
			menu.add(Menu.NONE, 1, 0, "Send Friend Request").setShowAsAction(
					MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		return true;
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
			mProfileType = "local";
		} else {
			mProfileType = "foreign";
		}

		ActionBarHelper.setupActionBar(getSupportActionBar(),
				new ActionBarHelper.TabListener(getApplicationContext()), 4);

		mAdapter = new UserProfileArrayAdapter(this, mUserProfileArray,
				mProfileType);
		setListAdapter(mAdapter);
	}

	@Override
	public void onResume() {
		super.onResume();

		ActionBarHelper.selectTab(getSupportActionBar(), 4);

		WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(),
				new ProfileInitiate(), true);
		task.execute();
	}

	private Context getActivityContext() {
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Bundle b = new Bundle();
		b.putInt("POSITION", position);
		showDialog(mNextDialogId++, b);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	private class ProfileInitiate implements WhoochApiCallInterface {

		private String mResponseString = null;

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