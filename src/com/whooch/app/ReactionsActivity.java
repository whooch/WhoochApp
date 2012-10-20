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

public class ReactionsActivity extends SherlockListActivity implements
		OnScrollListener {

	private PullToRefreshListView mListView;

	private ArrayList<StreamEntry> mWhoochArray = new ArrayList<StreamEntry>();
	private StreamArrayAdapter mAdapter;

	boolean mReactionsInitiated = false;
	boolean mReactionsHasMoreUpdates = true;
	boolean mLoadMoreItemsInProgress = false;
	private int mReactionsNextPage = 1;

	private String mShowConvoId = null;
	private String mShowConvoNumber = null;
	private StreamEntry mShowConvoCurrentUpdate = null;

	private String mDeleteWhoochId = null;
	private String mDeleteWhoochNumber = null;
	private int mLastSelectedPosition = -1;

	private String mReactionsType = "received";

	View mLoadingFooterView;

	// Unique id counter to prevent Android from reusing the same dialog.
	int mNextDialogId = 0;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(Menu.NONE, 1, 0, "Received").setShowAsAction(
				MenuItem.SHOW_AS_ACTION_ALWAYS);

		menu.add(Menu.NONE, 2, 0, "Sent").setShowAsAction(
				MenuItem.SHOW_AS_ACTION_ALWAYS);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			if (mReactionsType == "sent") {
				mReactionsType = "received";
				mWhoochArray.clear();
				mAdapter.notifyDataSetChanged();
				WhoochApiCallTask task = new WhoochApiCallTask(
						getActivityContext(), new ReactionsInitiate(), true);
				task.execute();
			}
			return true;
		case 2:
			if (mReactionsType == "received") {
				mReactionsType = "sent";
				mWhoochArray.clear();
				mAdapter.notifyDataSetChanged();
				WhoochApiCallTask task = new WhoochApiCallTask(
						getActivityContext(), new ReactionsInitiate(), true);
				task.execute();
			}
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
				new ActionBarHelper.TabListener(getApplicationContext()), 3);

		mListView = (PullToRefreshListView) getListView();
		mListView.setOnScrollListener(this);
		mListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				WhoochApiCallTask task = new WhoochApiCallTask(
						getActivityContext(), new ReactionsInitiate(), false);
				task.execute();
			}
		});

		mAdapter = new StreamArrayAdapter(this, mWhoochArray, false);
		setListAdapter(mAdapter);

		ActionBarHelper.selectTab(getSupportActionBar(), 3);

		if (savedInstanceState == null) {
			WhoochApiCallTask task = new WhoochApiCallTask(
					getActivityContext(), new ReactionsInitiate(), true);
			task.execute();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Serialize state object and write it to bundle
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(mWhoochArray);
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		outState.putByteArray("ReactionsList", bos.toByteArray());
		outState.putString("ReactionsType", mReactionsType);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onRestoreInstanceState(Bundle savedState) {

		if (savedState != null) {

			if (savedState.containsKey("ReactionsList")
					&& savedState.containsKey("ReactionsType")) {

				try {

					View loader = findViewById(R.id.main_loader);
					if (loader != null) {
						loader.setVisibility(View.GONE);
					}

					mReactionsType = savedState.getString("ReactionsType");

					ObjectInputStream objectIn = new ObjectInputStream(
							new ByteArrayInputStream(
									savedState.getByteArray("ReactionsList")));
					Object obj;
					obj = objectIn.readObject();

					setReactionsArray((ArrayList<StreamEntry>) obj);

					mReactionsInitiated = true;
					mReactionsNextPage++;

					if (mWhoochArray.size() < 25) {
						mReactionsHasMoreUpdates = false;
					}

					mAdapter.notifyDataSetChanged();
					mListView.onRefreshComplete();

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
						getActivityContext(), new ReactionsInitiate(), true);
				task.execute();
			}
		} else {
			WhoochApiCallTask task = new WhoochApiCallTask(
					getActivityContext(), new ReactionsInitiate(), true);
			task.execute();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		ActionBarHelper.selectTab(getSupportActionBar(), 3);
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

		if (mReactionsInitiated && mReactionsHasMoreUpdates
				&& !mLoadMoreItemsInProgress) {

			int numPaddingItems = 0;
			if (firstVisibleItem + visibleItemCount + numPaddingItems >= totalItemCount) {

				mLoadMoreItemsInProgress = true;
				WhoochApiCallTask task = new WhoochApiCallTask(
						getActivityContext(), new ReactionsGetMoreUpdates(),
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

		final StreamEntry entry = mWhoochArray.get(b.getInt("POSITION") - 1);
		mLastSelectedPosition = b.getInt("POSITION") - 1;

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
					Log.d("ReactionsActivity", "React");
					Intent i = new Intent(getApplicationContext(),
							PostReactionActivity.class);
					i.putExtra("WHOOCH_ID", entry.whoochId);
					i.putExtra("REACTION_TO", entry.whoochNumber);
					i.putExtra("REACTION_TYPE", "whooch");
					i.putExtra("CONTENT", entry.content);
					i.putExtra("USER_NAME", entry.userName);
	                i.putExtra("WHOOCH_NAME", entry.whoochName);
	                i.putExtra("WHOOCH_IMAGE", entry.whoochImageUriLarge);
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
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	private class ReactionsInitiate implements WhoochApiCallInterface {

		private String mResponseString = null;

        public void preExecute() {
        	
        	mReactionsInitiated = false;
        	mReactionsHasMoreUpdates = true;
        	mLoadMoreItemsInProgress = false;
        	mReactionsNextPage = 1;
        	
        }
        
		public HttpRequestBase getHttpRequest() {
			return new HttpGet(Settings.apiUrl
					+ "/reactions/1?page=0&type=user&" + mReactionsType
					+ "=true");
		}

		public void handleResponse(String responseString) {
			mResponseString = responseString;
		}

		public void postExecute(int statusCode) {

			if (statusCode == 200) {
				mWhoochArray.clear();

				// parse the response as JSON and update the Content Array
				if (!mResponseString.equals("null")) {
					try {
						JSONArray jsonArray = new JSONArray(mResponseString);
						
						if (jsonArray.length() < 25) {
							mReactionsHasMoreUpdates = false;
						}

						// the newest updates are at the front of the array, so
						// loop over forwards
						for (int i = 0; i < jsonArray.length(); i++) {

							// create an object that will be used to populate
							// the List View and add it to the array
							StreamEntry entry = new StreamEntry(
									jsonArray.getJSONObject(i),
									getWindowManager());
							mWhoochArray.add(entry);
						}
					} catch (JSONException e) {
						e.printStackTrace();
						// TODO: error handling
					}
				} else {
						mReactionsHasMoreUpdates = false;
				}

			}

			mAdapter.notifyDataSetChanged();
			mListView.onRefreshComplete();

			mReactionsInitiated = true;
		}
	}

	private class ReactionsGetMoreUpdates implements WhoochApiCallInterface {

		private String mResponseString = null;

        public void preExecute() {}
        
		public HttpRequestBase getHttpRequest() {
			return new HttpGet(Settings.apiUrl + "/reactions/1?page="
					+ mReactionsNextPage + "&type=user&" + mReactionsType
					+ "=true");
		}

		public void handleResponse(String responseString) {
			mResponseString = responseString;
		}

		public void postExecute(int statusCode) {

			// parse the response as JSON and update the Content Array
			if (!mResponseString.equals("null")) {
				try {
					JSONArray jsonArray = new JSONArray(mResponseString);

					if (jsonArray.length() < 10) {
						mReactionsHasMoreUpdates = false;
					}
					// the newest updates are at the front of the array, so loop
					// over forwards
					for (int i = 0; i < jsonArray.length(); i++) {

						// create an object that will be used to populate the
						// List View and add it to the array
						StreamEntry entry = new StreamEntry(
								jsonArray.getJSONObject(i), getWindowManager());
						mWhoochArray.add(entry);

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
				mReactionsHasMoreUpdates = false;
			}

			mReactionsNextPage++;

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

        public void preExecute() {
        	
        }
        
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
						tv3A.setText(WhoochHelperFunctions.getSpannedFromHtmlContent(entry.content, tv3A, getActivityContext()));

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
						tv3B.setText(WhoochHelperFunctions.getSpannedFromHtmlContent(mShowConvoCurrentUpdate.content, tv3B, getActivityContext()));

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

        public void preExecute() {}
        
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
				mWhoochArray.remove(mLastSelectedPosition);
				mAdapter.notifyDataSetChanged();
			}
			
			mDeleteWhoochId = null;
			mDeleteWhoochNumber = null;

		}
	}

	private void setReactionsArray(ArrayList<StreamEntry> temp) {
		mWhoochArray.clear();
		for (int i = 0; i < temp.size(); i++) {
			mWhoochArray.add(temp.get(i));
		}
	}

}