package com.whooch.app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
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
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.koushikdutta.urlimageviewhelper.UrlImageGetter;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.whooch.app.helpers.ActionBarHelper;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;
import com.whooch.app.helpers.WhoochHelperFunctions;
import com.whooch.app.json.StreamEntry;
import com.whooch.app.ui.StreamArrayAdapter;

import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;

public class StreamActivity extends SherlockListActivity implements
		OnScrollListener {

	private PullToRefreshListView mListView;

	private ArrayList<StreamEntry> mStreamArray = new ArrayList<StreamEntry>();
	private StreamArrayAdapter mAdapter;

	private String mLatestTimestamp = "0";
	private String mOldestTimestamp = "0";

	boolean mStreamInitiated = false;
	boolean mStreamHasMoreUpdates = true;
	boolean mLoadMoreItemsInProgress = false;

	private String mShowConvoId = null;
	private String mShowConvoNumber = null;
	private StreamEntry mShowConvoCurrentUpdate = null;

	private String mDeleteWhoochId = null;
	private String mDeleteWhoochNumber = null;

	View mLoadingFooterView;

	// Unique id counter to prevent Android from reusing the same dialog.
	int mNextDialogId = 0;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// SubMenu sub = menu.addSubMenu("Settings");
		// sub.setIcon(R.drawable.abs__ic_menu_moreoverflow_holo_dark);

		menu.add(Menu.NONE, 1, 0, "Update")
				.setIcon(android.R.drawable.ic_menu_edit)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		menu.add(Menu.NONE, 2, 0, "Search")
				.setIcon(android.R.drawable.ic_menu_search)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		menu.add(Menu.NONE, 3, 0, "Create")
				.setIcon(android.R.drawable.ic_input_add)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i = null;
		switch (item.getItemId()) {
		case 1:
			i = new Intent(getActivityContext(), PostStandardActivity.class);
			i.putExtra("UPDATE_TYPE", "regular");
			startActivity(i);
			return true;
		case 2:
			i = new Intent(getActivityContext(), SearchActivity.class);
			startActivity(i);
			return true;
		case 3:
			i = new Intent(getActivityContext(), CreateActivity.class);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.stream);

		ActionBarHelper.setupActionBar(getSupportActionBar(),
				new ActionBarHelper.TabListener(getApplicationContext()), 0);

		mListView = (PullToRefreshListView) getListView();
		mListView.setOnScrollListener(this);
		mListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				WhoochApiCallTask task = new WhoochApiCallTask(
						getActivityContext(), new StreamGetNewUpdates(), false);
				task.execute();
			} 
		});

		mAdapter = new StreamArrayAdapter(this, mStreamArray, false);
		setListAdapter(mAdapter);
		
		ActionBarHelper.selectTab(getSupportActionBar(), 0);

		if (savedInstanceState == null) {

				WhoochApiCallTask task = new WhoochApiCallTask(
						getActivityContext(), new StreamInitiate(), false);
				task.execute();
				
		}
	}
	
	 @Override
	 public void onConfigurationChanged(Configuration newConfig) {
	  // TODO Auto-generated method stub
	  super.onConfigurationChanged(newConfig);
	 }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Serialize state object and write it to bundle
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(mStreamArray);
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		outState.putByteArray("StreamList", bos.toByteArray());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onRestoreInstanceState(Bundle savedState) {
		
		if (savedState != null) {

			if (savedState.containsKey("StreamList")) {

				try {
					
					View loader = findViewById(R.id.main_loader);
					if (loader != null) {
						loader.setVisibility(View.GONE);
					}
					
					ObjectInputStream objectIn = new ObjectInputStream(
							new ByteArrayInputStream(
									savedState.getByteArray("StreamList")));
					Object obj;
					obj = objectIn.readObject();

					setStreamArray((ArrayList<StreamEntry>) obj);
					
					if (mStreamArray.size() > 0) {
						mLatestTimestamp = mStreamArray.get(0).timestamp;
						mOldestTimestamp = mStreamArray
								.get(mStreamArray.size() - 1).timestamp;
					}
					
					mAdapter.notifyDataSetChanged();
					mListView.onRefreshComplete();
					mStreamInitiated = true;
					
					
				} catch (OptionalDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				WhoochApiCallTask task = new WhoochApiCallTask(
						getActivityContext(), new StreamInitiate(), false);
				task.execute();
			}
		} else {
			WhoochApiCallTask task = new WhoochApiCallTask(
					getActivityContext(), new StreamInitiate(), false);
			task.execute();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

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
	protected Dialog onCreateDialog(int id, Bundle b) {

		final StreamEntry entry = mStreamArray.get(b.getInt("POSITION") - 1);

		// create the menu
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<Runnable> handlers = new ArrayList<Runnable>();

		SharedPreferences settings = getActivityContext().getSharedPreferences(
				"whooch_preferences", 0);
		String currentUserName = settings.getString("username", null);

		if (entry.isContributor.equals("1")
				&& !entry.userName.equalsIgnoreCase(currentUserName)) {
			names.add("React");
			handlers.add(new Runnable() {
				public void run() {
					Log.d("StreamActivity", "React");
					Intent i = new Intent(getApplicationContext(),
							PostReactionActivity.class);
					i.putExtra("WHOOCH_ID", entry.whoochId);
					i.putExtra("REACTION_TO", entry.whoochNumber);
					i.putExtra("REACTION_TYPE", "whooch");
					i.putExtra("CONTENT", entry.content);
					i.putExtra("USER_NAME", entry.userName);
					startActivity(i);
				}
			});
		}

		if (entry.isContributor.equals("0") && entry.type.equals("open")) {
			names.add("Send Feedback");
			handlers.add(new Runnable() {
				public void run() {
					Log.d("StreamActivity", "Send Feedback");
					Intent i = new Intent(getApplicationContext(),
							PostFeedbackActivity.class);
					i.putExtra("WHOOCH_ID", entry.whoochId);
					i.putExtra("WHOOCH_NUMBER", entry.whoochNumber);
					i.putExtra("WHOOCH_IMAGE", entry.whoochImageUriLarge);
					i.putExtra("WHOOCH_NAME", entry.whoochName);
					startActivity(i);
				}
			});
		}

		if (entry.reactionType.equals("whooch")) {
			names.add("Show Conversation");
			handlers.add(new Runnable() {
				public void run() {
					Log.d("StreamActivity", "Show Conversation");
					mShowConvoId = entry.whoochId;
					mShowConvoNumber = entry.reactionTo;
					mShowConvoCurrentUpdate = entry;

					WhoochApiCallTask task = new WhoochApiCallTask(
							getActivityContext(), new ShowConversation(), true);
					task.execute();
				}
			});
		}

		if (!entry.image.equals("null")) {
			names.add("View Photo");
			handlers.add(new Runnable() {
				public void run() {
					Log.d("StreamActivity", "View Photo");
					Intent i = new Intent(getApplicationContext(),
							ViewPhotoActivity.class);
					i.putExtra("WHOOCH_ID", entry.whoochId);
					i.putExtra("WHOOCH_NUMBER", entry.whoochNumber);
					i.putExtra("IMAGE_TYPE", "whooch");
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

		if (entry.userName.equalsIgnoreCase(currentUserName)) {
			names.add("Delete Update");
			handlers.add(new Runnable() {
				public void run() {
					Log.d("StreamActivity", "Delete Update");

					AlertDialog.Builder builder = new AlertDialog.Builder(
							getActivityContext());

					builder.setTitle("Whooch");
					builder.setMessage("Are you sure you want to delete this update?");

					builder.setNegativeButton("CANCEL",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

								}
							});

					builder.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									mDeleteWhoochId = entry.whoochId;
									mDeleteWhoochNumber = entry.whoochNumber;

									WhoochApiCallTask task = new WhoochApiCallTask(
											getActivityContext(),
											new DeleteUpdate(), true);
									task.execute();
								}
							});

					AlertDialog dialog = builder.create();

					dialog.show();
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
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

		// the onScroll method is called A LOT. make sure we don't fire off the
		// same update multiple times.
		if (mStreamInitiated && mStreamHasMoreUpdates
				&& !mLoadMoreItemsInProgress) {

			// numPaddingItems is used to determine how early we should start
			// loading more items
			// if numPaddingItems == 0, don't start loading until the user
			// actually reaches the end
			// of the list
			// if numPaddingItems >= 1, start loading when we are
			// numPaddingItems away from the bottom
			// of the list
			int numPaddingItems = 0;
			if (firstVisibleItem + visibleItemCount + numPaddingItems >= totalItemCount) {

				// The user is approaching the end of the listview, so take this
				// time to load
				// some additional items.
				mLoadMoreItemsInProgress = true;
				WhoochApiCallTask task = new WhoochApiCallTask(
						getActivityContext(), new StreamGetMoreUpdates(), false);
				task.execute();

				// display a footer at the end of the listview indicating that
				// we are loading additional items
				mLoadingFooterView = this.getLayoutInflater().inflate(
						R.layout.stream_loading_footer, null);
				mListView.addFooterView(mLoadingFooterView);
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	private class StreamInitiate implements WhoochApiCallInterface {

		private String mResponseString = null;

		public HttpRequestBase getHttpRequest() {
			return new HttpGet(Settings.apiUrl + "/stream/1?count=25");
		}

		public void handleResponse(String responseString) {
			mResponseString = responseString;
		}

		public void postExecute(int statusCode) {

			if (statusCode == 200) {

				mStreamArray.clear();

				// parse the response as JSON and update the Content Array
				if (!mResponseString.equals("null")) {
					try {
						JSONArray jsonArray = new JSONArray(mResponseString);

						// the newest updates are at the front of the array, so
						// loop over forwards
						for (int i = 0; i < jsonArray.length(); i++) {

							// create an object that will be used to populate
							// the List View and add it to the array
							StreamEntry entry = new StreamEntry(
									jsonArray.getJSONObject(i),
									getWindowManager());
							mStreamArray.add(entry);

							// TODO: not sure if this is helping, also need to
							// figure out what size image to load
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
					// if it is null we don't mind, there just wasn't anything
					// there
				}

				if (mStreamArray.size() > 0) {
					mLatestTimestamp = mStreamArray.get(0).timestamp;
					mOldestTimestamp = mStreamArray
							.get(mStreamArray.size() - 1).timestamp;
				}
			}
			
			mAdapter.notifyDataSetChanged();
			mListView.onRefreshComplete();

			mStreamInitiated = true;

		}
	}

	private class StreamGetNewUpdates implements WhoochApiCallInterface {

		private String mResponseString = null;

		public HttpRequestBase getHttpRequest() {
			return new HttpGet(Settings.apiUrl + "/stream/1?boundary="
					+ mLatestTimestamp + "&after=true");
		}

		public void handleResponse(String responseString) {
			mResponseString = responseString;
		}

		public void postExecute(int statusCode) {

			// parse the response as JSON and update the Content Array
			if (!mResponseString.equals("null")) {
				try {
					JSONArray jsonArray = new JSONArray(mResponseString);

					// the latest updates are at the end of the array, so we'll
					// loop over the array forwards and
					// prepend the each update as we parse it, from oldest to
					// newest.
					for (int i = 0; i < jsonArray.length(); i++) {

						// create an object that will be used to populate the
						// List View and prepend it to the array
						StreamEntry entry = new StreamEntry(
								jsonArray.getJSONObject(i), getWindowManager());
						mStreamArray.add(0, entry);

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
				// if it is null we don't mind, there just wasn't anything there
			}

			if (mStreamArray.size() > 0) {
				mLatestTimestamp = mStreamArray.get(0).timestamp;
				mOldestTimestamp = mStreamArray.get(mStreamArray.size() - 1).timestamp;
			}

			mAdapter.notifyDataSetChanged();
			mListView.onRefreshComplete();
		}
	}

	private class StreamGetMoreUpdates implements WhoochApiCallInterface {

		private String mResponseString = null;

		public HttpRequestBase getHttpRequest() {
			return new HttpGet(Settings.apiUrl + "/stream/1?boundary="
					+ mOldestTimestamp + "&before=true");
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
						StreamEntry entry = new StreamEntry(
								jsonArray.getJSONObject(i), getWindowManager());
						mStreamArray.add(entry);

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
				// if it is null then we should stop trying to get more updates
				mStreamHasMoreUpdates = false;
			}

			if (mStreamArray.size() > 0) {
				mLatestTimestamp = mStreamArray.get(0).timestamp;
				mOldestTimestamp = mStreamArray.get(mStreamArray.size() - 1).timestamp;
			}

			mAdapter.notifyDataSetChanged();
			mListView.onRefreshComplete();

			// dismiss the 'loading more items' footer
			mListView.removeFooterView(mLoadingFooterView);

			// set flag to false so onScroll method can be called again
			mLoadMoreItemsInProgress = false;
		}
	}

	private class ShowConversation implements WhoochApiCallInterface {

		private String mResponseString = null;

		public HttpRequestBase getHttpRequest() {
			return new HttpGet(Settings.apiUrl + "/whooch/" + mShowConvoId
					+ "?single=1&whoochNumber=" + mShowConvoNumber);
		}

		public void handleResponse(String responseString) {
			mResponseString = responseString;
		}

		public void postExecute(int statusCode) {

			if (statusCode == 200) {

				// parse the response as JSON and update the Content Array
				if (!mResponseString.equals("null")) {
					try {
						JSONArray jsonArray = new JSONArray(mResponseString);

						// create an object that will be used to populate the
						// List View and add it to the array
						StreamEntry entry = new StreamEntry(
								jsonArray.getJSONObject(0), getWindowManager());

						AlertDialog.Builder builder = new AlertDialog.Builder(
								getActivityContext());

						builder.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										mShowConvoId = null;
										mShowConvoNumber = null;
										dialog.dismiss();
									}
								});

						LayoutInflater inflater = (LayoutInflater) getActivityContext()
								.getSystemService(
										Context.LAYOUT_INFLATER_SERVICE);

						View view = inflater.inflate(
								R.layout.show_conversation, null);

						ImageView iv1A = (ImageView) view
								.findViewById(R.id.entry_whooch_imageA);
						UrlImageViewHelper.setUrlDrawable(iv1A,
								entry.whoochImageUriLarge);

						TextView tv1A = (TextView) view
								.findViewById(R.id.entry_whooch_titleA);
						tv1A.setText(entry.whoochName);

						TextView tv2A = (TextView) view
								.findViewById(R.id.entry_posted_userA);
						tv2A.setText(entry.userName);

						TextView tv3A = (TextView) view
								.findViewById(R.id.entry_whooch_contentA);
						UrlImageGetter imageGetter = new UrlImageGetter(tv3A,
								getActivityContext());

						Spanned htmlSpan = Html.fromHtml(
								entry.content.replaceAll(">\\s+<", "><"),
								imageGetter, null);

						tv3A.setText(htmlSpan);

						TextView tv4A = (TextView) view
								.findViewById(R.id.entry_whooch_footA);
						tv4A.setText(WhoochHelperFunctions.toRelativeTime(Long
								.parseLong(entry.timestamp)));

						ImageView iv1B = (ImageView) view
								.findViewById(R.id.entry_whooch_imageB);
						UrlImageViewHelper.setUrlDrawable(iv1B,
								mShowConvoCurrentUpdate.whoochImageUriLarge);

						TextView tv1B = (TextView) view
								.findViewById(R.id.entry_whooch_titleB);
						tv1B.setText(mShowConvoCurrentUpdate.whoochName);

						TextView tv2B = (TextView) view
								.findViewById(R.id.entry_posted_userB);
						tv2B.setText(mShowConvoCurrentUpdate.userName);

						TextView tv3B = (TextView) view
								.findViewById(R.id.entry_whooch_contentB);
						imageGetter = new UrlImageGetter(tv3B,
								getActivityContext());

						htmlSpan = Html.fromHtml(
								mShowConvoCurrentUpdate.content.replaceAll(
										">\\s+<", "><"), imageGetter, null);

						tv3B.setText(htmlSpan);

						TextView tv4B = (TextView) view
								.findViewById(R.id.entry_whooch_footB);
						tv4B.setText(WhoochHelperFunctions.toRelativeTime(Long
								.parseLong(mShowConvoCurrentUpdate.timestamp)));

						builder.setView(view);

						AlertDialog dialog = builder.create();

						dialog.show();

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

	private class DeleteUpdate implements WhoochApiCallInterface {

		public HttpRequestBase getHttpRequest() {
			HttpPost request = new HttpPost(Settings.apiUrl + "/whooch/delete");

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("whoochId",
					mDeleteWhoochId));
			nameValuePairs.add(new BasicNameValuePair("whoochNumber",
					mDeleteWhoochNumber));

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
						getActivityContext(), new StreamInitiate(), false);
				task.execute();
			}

			mDeleteWhoochId = null;
			mDeleteWhoochNumber = null;

		}
	}
	
    private void setStreamArray(ArrayList<StreamEntry> temp)
    {
    	mStreamArray.clear();
    	for(int i=0; i<temp.size(); i++)
    	{
    		mStreamArray.add(temp.get(i));
    	}
    }

}