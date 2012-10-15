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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.whooch.app.json.FeedbackEntry;
import com.whooch.app.ui.FeedbackArrayAdapter;

import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;

public class FeedbackActivity extends SherlockListActivity implements
		OnScrollListener {

	private PullToRefreshListView mListView;

	private ArrayList<FeedbackEntry> mFeedbackArray = new ArrayList<FeedbackEntry>();
	private FeedbackArrayAdapter mAdapter;

	boolean mFeedbackInitiated = false;
	boolean mFeedbackHasMoreUpdates = true;
	boolean mLoadMoreItemsInProgress = false;
	
	String mDeleteFeedbackId = null;

	private int mFeedbackNextPage = 1;

	View mLoadingFooterView;

	// Unique id counter to prevent Android from reusing the same dialog.
	int mNextDialogId = 0;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(Menu.NONE, 1, 0, "Received")
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		menu.add(Menu.NONE, 2, 0, "Sent")
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.stream);

		ActionBarHelper.setupActionBar(getSupportActionBar(),
				new ActionBarHelper.TabListener(getApplicationContext()), 2);

		mListView = (PullToRefreshListView) getListView();
		mListView.setOnScrollListener(this);
		mListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				WhoochApiCallTask task = new WhoochApiCallTask(
						getActivityContext(), new FeedbackInitiate(), false);
				task.execute();
			}
		});

		mAdapter = new FeedbackArrayAdapter(this, mFeedbackArray);
		setListAdapter(mAdapter);
	}

	@Override
	public void onResume() {
		super.onResume();

		ActionBarHelper.selectTab(getSupportActionBar(), 2);

		mFeedbackInitiated = false;
		mFeedbackHasMoreUpdates = true;
		mLoadMoreItemsInProgress = false;
		WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(),
				new FeedbackInitiate(), true);
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

		if (mFeedbackInitiated && mFeedbackHasMoreUpdates
				&& !mLoadMoreItemsInProgress) {

			int numPaddingItems = 0;
			if (firstVisibleItem + visibleItemCount + numPaddingItems >= totalItemCount) {

				mLoadMoreItemsInProgress = true;
				WhoochApiCallTask task = new WhoochApiCallTask(
						getActivityContext(), new FeedbackGetMoreUpdates(),
						false);
				task.execute();

				mLoadingFooterView = this.getLayoutInflater().inflate(
						R.layout.stream_loading_footer, null);
				mListView.addFooterView(mLoadingFooterView);
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle b) {

		final FeedbackEntry entry = mFeedbackArray
				.get(b.getInt("POSITION") - 1);

		// create the menu
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<Runnable> handlers = new ArrayList<Runnable>();

		names.add("React");
		handlers.add(new Runnable() {
			public void run() {

                Intent i = new Intent(getApplicationContext(), PostReactionActivity.class);
                i.putExtra("WHOOCH_ID", entry.whoochId);
                i.putExtra("REACTION_TO", entry.feedbackId);
                i.putExtra("REACTION_TYPE", "feedback");
                i.putExtra("CONTENT", entry.content);
                i.putExtra("USER_NAME", entry.userName);
                startActivity(i);
			}
		});

		if (!entry.image.equals("null")) {
			names.add("View Photo");
			handlers.add(new Runnable() {
				public void run() {
					Log.d("FeedbackActivity", "View Photo");
					Intent i = new Intent(getApplicationContext(),
							ViewPhotoActivity.class);
					i.putExtra("FEEDBACK_ID", entry.feedbackId);
					i.putExtra("IMAGE_TYPE", "feedback");
					i.putExtra("IMAGE_NAME", entry.image);
					startActivity(i);
				}
			});
		}

		names.add("Go to Whooch");
		handlers.add(new Runnable() {
			public void run() {
				Intent i = new Intent(getApplicationContext(),
						WhoochActivity.class);
				i.putExtra("WHOOCH_ID", entry.whoochId);
				startActivity(i);
			}
		});

		names.add("Remove Feedback");
		handlers.add(new Runnable() {
			public void run() {
				Log.d("FeedbackActivity", "Remove Feedback");
				
                AlertDialog.Builder builder = new AlertDialog.Builder(
    					getActivityContext());
                
                builder.setTitle("Whooch");
                builder.setMessage("Are you sure you want to remove this feedback?");
                
                builder.setNegativeButton("CANCEL",
    					new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

					}
				});
                
    			builder.setPositiveButton("OK",
    					new DialogInterface.OnClickListener() {
    						public void onClick(DialogInterface dialog, int id) {
    							mDeleteFeedbackId = entry.feedbackId;
    							WhoochApiCallTask task = new WhoochApiCallTask(
    									getActivityContext(), new RemoveFeedback(), false);
    							task.execute();
    						}
    					});
    	        
    		    
    			AlertDialog dialog = builder.create();

    			dialog.show();
			}
		});

		final String[] namesArray = names.toArray(new String[names.size()]);
		final Runnable[] handlersArray = handlers.toArray(new Runnable[handlers
				.size()]);

		return new AlertDialog.Builder(getActivityContext()).setItems(
				namesArray, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Log.d("FeedbackActivity", "Something Was Clicked");
						handlersArray[which].run();
					}
				}).create();
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	private class FeedbackInitiate implements WhoochApiCallInterface {

		private String mResponseString = null;

		public HttpRequestBase getHttpRequest() {
			return new HttpGet(Settings.apiUrl
					+ "/feedback/1?page=0&type=user&received=true");
		}

		public void handleResponse(String responseString) {
			mResponseString = responseString;
		}

		public void postExecute(int statusCode) {
			if (statusCode == 200) {
				mFeedbackArray.clear();

				// parse the response as JSON and update the Content Array
				if (!mResponseString.equals("null")) {
					try {
						JSONArray jsonArray = new JSONArray(mResponseString);

						// the newest updates are at the front of the array, so
						// loop over forwards
						for (int i = 0; i < jsonArray.length(); i++) {

							// create an object that will be used to populate
							// the List View and add it to the array
							FeedbackEntry entry = new FeedbackEntry(
									jsonArray.getJSONObject(i),
									getWindowManager());
							mFeedbackArray.add(entry);
						}
					} catch (JSONException e) {
						e.printStackTrace();
						// TODO: error handling
					}
				} else {
					// if it is null we don't mind, there just wasn't anything
					// there
				}

				mFeedbackNextPage++;

				if (mFeedbackArray.size() < 25) {
					mFeedbackHasMoreUpdates = false;
				}

			}

			mAdapter.notifyDataSetChanged();
			mListView.onRefreshComplete();

			mFeedbackInitiated = true;
		}
	}

	private class FeedbackGetMoreUpdates implements WhoochApiCallInterface {

		private String mResponseString = null;

		public HttpRequestBase getHttpRequest() {
			return new HttpGet(Settings.apiUrl + "/feedback/1?page="
					+ mFeedbackNextPage + "&type=user&received=true");
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
						FeedbackEntry entry = new FeedbackEntry(
								jsonArray.getJSONObject(i), getWindowManager());
						mFeedbackArray.add(entry);

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

			mFeedbackNextPage++;

			// if size is 10 we need to keep checking for more updates
			if (mFeedbackArray.size() < 10) {
				mFeedbackHasMoreUpdates = false;
			}

			mAdapter.notifyDataSetChanged();
			mListView.onRefreshComplete();

			// dismiss the 'loading more items' footer
			mListView.removeFooterView(mLoadingFooterView);

			// set flag to false so onScroll method can be called again
			mLoadMoreItemsInProgress = false;
		}
	}

	private class RemoveFeedback implements WhoochApiCallInterface {

		public HttpRequestBase getHttpRequest() {
			HttpPost request = new HttpPost(Settings.apiUrl + "/feedback/delete");

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("feedbackId",
					mDeleteFeedbackId));
			nameValuePairs.add(new BasicNameValuePair("type",
					"whooch"));

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

			if (statusCode == 200) {
				WhoochApiCallTask task = new WhoochApiCallTask(
						getActivityContext(), new FeedbackInitiate(), false);
				task.execute();
			}

			mDeleteFeedbackId = null;

		}
	}

}