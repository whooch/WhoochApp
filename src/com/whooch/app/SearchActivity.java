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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.whooch.app.helpers.ActionBarHelper;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;
import com.whooch.app.json.SearchEntry;
import com.whooch.app.ui.SearchArrayAdapter;

import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;

public class SearchActivity extends SherlockListActivity implements
		OnScrollListener {

	private PullToRefreshListView mListView;

	private ArrayList<SearchEntry> mSearchArray = new ArrayList<SearchEntry>();
	private SearchArrayAdapter mAdapter;

	boolean mSearchInitiated = false;
	boolean mSearchHasMoreUpdates = true;
	boolean mLoadMoreItemsInProgress = false;

	private int mSearchNextPage = 1;
	private EditText mSearchQuery = null;
	private String mSearchType = "open";

	View mLoadingFooterView;

	// Unique id counter to prevent Android from reusing the same dialog.
	int mNextDialogId = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);

		ActionBarHelper.setupActionBar(getSupportActionBar(),
				new ActionBarHelper.TabListener(getApplicationContext()), 1);

		mListView = (PullToRefreshListView) getListView();
		mListView.setOnScrollListener(this);
		mListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				WhoochApiCallTask task = new WhoochApiCallTask(
						getActivityContext(), new SearchInitiate(), false);
				task.execute();
			}
		});

		mSearchQuery = (EditText) findViewById(R.id.search_query);

		RadioButton searchWhoochRB = (RadioButton) findViewById(R.id.search_whooch_rb);
		searchWhoochRB.setChecked(true);

		mAdapter = new SearchArrayAdapter(this, mSearchArray);
		setListAdapter(mAdapter);

		Button ibtn1 = (Button) findViewById(R.id.search_submit);

		// unable to find ID - let it go for now
		ibtn1.setOnClickListener(getSearchSubmitClickListener());
	}

	@Override
	public void onResume() {
		super.onResume();

		ActionBarHelper.selectTab(getSupportActionBar(), 1);

		mSearchInitiated = false;
		mSearchHasMoreUpdates = true;
		mLoadMoreItemsInProgress = false;
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

		if (mSearchInitiated && mSearchHasMoreUpdates
				&& !mLoadMoreItemsInProgress) {

			int numPaddingItems = 0;
			if (firstVisibleItem + visibleItemCount + numPaddingItems >= totalItemCount) {

				mLoadMoreItemsInProgress = true;
				WhoochApiCallTask task = new WhoochApiCallTask(
						getActivityContext(), new SearchGetMoreUpdates(), false);
				task.execute();

				mLoadingFooterView = this.getLayoutInflater().inflate(
						R.layout.stream_loading_footer, null);
				mListView.addFooterView(mLoadingFooterView);
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle b) {

		final SearchEntry entry = mSearchArray.get(b.getInt("POSITION") - 1);

		// create the menu
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<Runnable> handlers = new ArrayList<Runnable>();

		if (mSearchType == "open") {

			if (entry.isTrailing.equals("0")) {
				names.add("Trail whooch");
				handlers.add(new Runnable() {
					public void run() {
						WhoochApiCallTask task = new WhoochApiCallTask(
								getActivityContext(), new TrailWhooch(
										entry.whoochId), true);
						task.execute();
					}
				});
			}

			names.add("Go to whooch");
			handlers.add(new Runnable() {
				public void run() {
					Intent i = new Intent(getApplicationContext(),
							WhoochActivity.class);
					i.putExtra("WHOOCH_ID", entry.whoochId);
					startActivity(i);
				}
			});

		} else {

			SharedPreferences settings = getActivityContext()
					.getSharedPreferences("whooch_preferences", 0);
			String currentUserName = settings.getString("username", null);

			if (entry.isFriend.equals("0")
					&& !entry.userName.equalsIgnoreCase(currentUserName)) {
				names.add("Send Friend Request");
				handlers.add(new Runnable() {
					public void run() {
						WhoochApiCallTask task = new WhoochApiCallTask(
								getActivityContext(), new SendFriendRequest(
										entry.userId), true);
						task.execute();
					}
				});
			}

			names.add("Go to profile");
			handlers.add(new Runnable() {
				public void run() {
					Intent i = new Intent(getApplicationContext(),
							UserProfileActivity.class);
					i.putExtra("USER_ID", entry.userId);
					startActivity(i);
				}
			});

		}

		final String[] namesArray = names.toArray(new String[names.size()]);
		final Runnable[] handlersArray = handlers.toArray(new Runnable[handlers
				.size()]);

		return new AlertDialog.Builder(getActivityContext()).setItems(
				namesArray, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Log.d("StreamActivity", "Something Was Clicked");
						handlersArray[which].run();
					}
				}).create();
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	private class SearchInitiate implements WhoochApiCallInterface {

		private String mResponseString = null;

		public HttpRequestBase getHttpRequest() {
			return new HttpGet(Settings.apiUrl + "/search/1?page=0&type="
					+ mSearchType + "&query="
					+ mSearchQuery.getText().toString().trim());
		}

		public void handleResponse(String responseString) {
			mResponseString = responseString;
		}

		public void postExecute(int statusCode) {

			if (statusCode == 200) {
				mSearchArray.clear();

				// parse the response as JSON and update the Content Array
				if (!mResponseString.equals("null")) {

					try {

						JSONArray jsonArray = new JSONObject(mResponseString)
								.getJSONArray(mSearchType);
						// the newest updates are at the front of the array, so
						// loop over forwards
						for (int i = 0; i < jsonArray.length(); i++) {

							// create an object that will be used to populate
							// the List View and add it to the array
							SearchEntry entry = new SearchEntry(
									jsonArray.getJSONObject(i), mSearchType,
									getWindowManager());

							mSearchArray.add(entry);

						}
					} catch (JSONException e) {
						e.printStackTrace();
						// TODO: error handling
					}
				} else {
					// if it is null we don't mind, there just wasn't anything
					// there
				}

				mSearchNextPage++;

				// if it is less than 25 then we should stop trying to get more
				// updates
				if (mSearchArray.size() < 25) {
					mSearchHasMoreUpdates = false;
				}
			}

			mAdapter.notifyDataSetChanged();
			mListView.onRefreshComplete();

			mSearchInitiated = true;
		}
	}

	private class SearchGetMoreUpdates implements WhoochApiCallInterface {

		private String mResponseString = null;

		public HttpRequestBase getHttpRequest() {
			return new HttpGet(Settings.apiUrl + "/search/1?page="
					+ mSearchNextPage + "&type=" + mSearchType + "&query="
					+ mSearchQuery);
		}

		public void handleResponse(String responseString) {
			mResponseString = responseString;
		}

		public void postExecute(int statusCode) {

			// parse the response as JSON and update the Content Array
			if (!mResponseString.equals("null")) {
				try {
					JSONArray jsonArray = new JSONArray(mResponseString);

					// the newest updates are at the front of the array, so loop
					// over forwards
					for (int i = 0; i < jsonArray.length(); i++) {

						// create an object that will be used to populate the
						// List View and add it to the array
						SearchEntry entry = new SearchEntry(
								jsonArray.getJSONObject(i), mSearchType,
								getWindowManager());
						mSearchArray.add(entry);

						// preload the image that will be displayed
						// UrlImageViewHelper.loadUrlDrawable(getApplicationContext(),
						// entry.whoochImageUriDefault,
						// R.drawable.ic_whooch_transparent);
					}

				} catch (JSONException e) {
					e.printStackTrace();
					// TODO: error handling
				}
			} else {

			}

			mSearchNextPage++;

			// if it is less than 10 then we should stop trying to get more
			// updates
			if (mSearchArray.size() < 10) {
				mSearchHasMoreUpdates = false;
			}

			mAdapter.notifyDataSetChanged();
			mListView.onRefreshComplete();

			// dismiss the 'loading more items' footer
			mListView.removeFooterView(mLoadingFooterView);

			// set flag to false so onScroll method can be called again
			mLoadMoreItemsInProgress = false;
		}
	}

	public OnClickListener getSearchSubmitClickListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				WhoochApiCallTask task = new WhoochApiCallTask(
						getActivityContext(), new SearchInitiate(), true);
				task.execute();
			}
		};

	}

	public void onRadioButtonClicked(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();

		// Check which radio button was clicked
		switch (view.getId()) {
		case R.id.search_whooch_rb:
			if (checked)
				mSearchType = "open";
			break;
		case R.id.search_user_rb:
			if (checked)
				mSearchType = "user";
			break;
		}
	}

	private class TrailWhooch implements WhoochApiCallInterface {

		private String mWhoochId = null;
		private String mResponseString = null;

		public TrailWhooch(String whoochId) {
			mWhoochId = whoochId;
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
							Toast.makeText(getApplicationContext(),
									"You are now trailing this whooch",
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(getApplicationContext(),
									"Something went wrong, try again",
									Toast.LENGTH_SHORT).show();
						}

					} catch (JSONException e) {
						e.printStackTrace();
						// TODO: error handling
					}
				} else {
					Toast.makeText(getApplicationContext(),
							"Something went wrong, try again",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(getApplicationContext(),
						"Something went wrong, try again",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class SendFriendRequest implements WhoochApiCallInterface {

		private String mUserId = null;


		public SendFriendRequest(String userId) {
			mUserId = userId;
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
				Toast.makeText(getApplicationContext(), "Friend request sent",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(),
						"Something went wrong, try again", Toast.LENGTH_SHORT)
						.show();
			}

		}
	}
}