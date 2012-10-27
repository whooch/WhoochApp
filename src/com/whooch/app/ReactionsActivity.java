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
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.whooch.app.helpers.ActionBarHelper;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;
import com.whooch.app.helpers.WhoochHelperFunctions;
import com.whooch.app.json.StreamEntry;
import com.whooch.app.ui.StreamArrayAdapter;

public class ReactionsActivity extends SherlockListActivity implements
		OnScrollListener {

	private ListView mListView;

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
	
    private Button mReceivedButton = null;
    private Button mSentButton = null;

	View mLoadingFooterView;

	// Unique id counter to prevent Android from reusing the same dialog.
	int mNextDialogId = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback_reactions);

		ActionBarHelper.setupActionBar(getSupportActionBar(),
				new ActionBarHelper.TabListener(getApplicationContext()), 3);

		mListView = getListView();
		mListView.setOnScrollListener(this);

		// Add and remove loading footer before setting adapter
		// Footer won't show up unless one is present when adapter is set
		mLoadingFooterView = this.getLayoutInflater().inflate(
				R.layout.stream_loading_footer, null);
		mListView.addFooterView(mLoadingFooterView);

		mAdapter = new StreamArrayAdapter(this, mWhoochArray, false);
		setListAdapter(mAdapter);

		mListView.removeFooterView(mLoadingFooterView);

		ActionBarHelper.selectTab(getSupportActionBar(), 3);

		if (savedInstanceState == null) {
			WhoochApiCallTask task = new WhoochApiCallTask(
					getActivityContext(), new ReactionsInitiate(), true);
			task.execute();
		}
		
		mReceivedButton = (Button) findViewById(R.id.received_action);
		mSentButton = (Button) findViewById(R.id.sent_action);
		
		mReceivedButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				if (mReactionsType == "sent") {
					mReceivedButton.setSelected(true);
		            mSentButton.setSelected(false);
		            
					mReactionsType = "received";
					mWhoochArray.clear();
					mAdapter.notifyDataSetChanged();
					WhoochApiCallTask task = new WhoochApiCallTask(
							getActivityContext(),
							new ReactionsInitiate(), true);
					task.execute();
				}
	
			}
			
		});
		
		mSentButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				if (mReactionsType == "received") {
					mSentButton.setSelected(true);
		            mReceivedButton.setSelected(false);
		            
					mReactionsType = "sent";
					mWhoochArray.clear();
					mAdapter.notifyDataSetChanged();
					WhoochApiCallTask task = new WhoochApiCallTask(
							getActivityContext(),
							new ReactionsInitiate(), true);
					task.execute();
				}
				
			}
			
		});
		
        mReceivedButton.setSelected(true);
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

		if (mReactionsInitiated) {
			WhoochApiCallTask task = new WhoochApiCallTask(
					getActivityContext(), new ReactionsInitiate(), false);
			task.execute();
		}
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

		final StreamEntry entry = mWhoochArray.get(b.getInt("POSITION"));
		mLastSelectedPosition = b.getInt("POSITION");
		mShowConvoCurrentUpdate = entry;

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
					i.putExtra("USER_NAME", entry.userName);
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
					i.putExtra("WHOOCH_NAME", entry.whoochName);
					i.putExtra("WHOOCH_IMAGE", entry.whoochImageUriMedium);
					i.putExtra("USER_NAME", entry.userName);
					startActivity(i);
				}
			});
		}
		
		if (!entry.userName.equalsIgnoreCase(currentUserName)
				&& entry.isFan.equals("0")) {
			names.add("I'm a fan of this update");
			handlers.add(new Runnable() {
				public void run() {
					WhoochApiCallTask task = new WhoochApiCallTask(
							getActivityContext(), new AddFan(), true);
					task.execute();
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

		return assembleUpdateDialog(namesArray, handlersArray);
	}

	private Dialog assembleUpdateDialog(final String[] namesArray,
			final Runnable[] handlersArray) {
		Builder dialog = new AlertDialog.Builder(getActivityContext());

		LayoutInflater inflater = (LayoutInflater) getActivityContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view = inflater.inflate(R.layout.stream_entry, null);

		dialog.setCustomTitle(view);

		dialog.setItems(namesArray, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				handlersArray[which].run();
			}
		});

		ImageView iv1 = (ImageView) view.findViewById(R.id.entry_whooch_image);
		UrlImageViewHelper.setUrlDrawable(iv1,
				mShowConvoCurrentUpdate.whoochImageUriLarge);

		TextView tv1 = (TextView) view.findViewById(R.id.entry_whooch_title);
		tv1.setText(mShowConvoCurrentUpdate.whoochName);

		TextView tv2 = (TextView) view.findViewById(R.id.entry_posted_user);
		tv2.setText(mShowConvoCurrentUpdate.userName);

		TextView tv3 = (TextView) view.findViewById(R.id.entry_whooch_content);
		tv3.setText(WhoochHelperFunctions.getSpannedFromHtmlContent(
				mShowConvoCurrentUpdate.content, tv3, getActivityContext()));
		tv3.setMovementMethod(LinkMovementMethod.getInstance());

		TextView tv4 = (TextView) view.findViewById(R.id.entry_whooch_foot);
		tv4.setText(WhoochHelperFunctions.toRelativeTime(Long
				.parseLong(mShowConvoCurrentUpdate.timestamp)));
		
		TextView tvFan = (TextView) view.findViewById(R.id.entry_whooch_foot_fans);
		if(mShowConvoCurrentUpdate.fanString != null)
		{
			tvFan.setText(mShowConvoCurrentUpdate.fanString);
			tvFan.setVisibility(View.VISIBLE);
		}
		else
		{
			tvFan.setVisibility(View.GONE);
		}

		LinearLayout ll1 = (LinearLayout) view
				.findViewById(R.id.entry_update_extras);
		ll1.setVisibility(View.GONE);

		return dialog.create();
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

			mReactionsInitiated = true;
		}
	}

	private class ReactionsGetMoreUpdates implements WhoochApiCallInterface {

		private String mResponseString = null;

		public void preExecute() {
		}

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
								.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

						View view = inflater.inflate(R.layout.show_conversation_title, null);
						
						ImageView iv1 = (ImageView) view
								.findViewById(R.id.show_conversation_whoochimage);
						UrlImageViewHelper.setUrlDrawable(iv1,
								entry.whoochImageUriLarge);

						TextView tv1A = (TextView) view
								.findViewById(R.id.show_conversation_whoochname);
						tv1A.setText(entry.whoochName);

						builder.setCustomTitle(view);



						view = inflater.inflate(
								R.layout.show_conversation, null);
						
						ImageView iv1A = (ImageView) view
								.findViewById(R.id.entry_user_imageA);
						UrlImageViewHelper.setUrlDrawable(iv1A,
								entry.userImageUriLarge);

						TextView tv2A = (TextView) view
								.findViewById(R.id.entry_posted_userA);
						tv2A.setText(entry.userName);

						TextView tv3A = (TextView) view
								.findViewById(R.id.entry_whooch_contentA);
						tv3A.setText(WhoochHelperFunctions
								.getSpannedFromHtmlContent(entry.content, tv3A,
										getActivityContext()));

						TextView tv4A = (TextView) view
								.findViewById(R.id.entry_whooch_footA);
						tv4A.setText(WhoochHelperFunctions.toRelativeTime(Long
								.parseLong(entry.timestamp)));

						TextView tvFanA = (TextView) view.findViewById(R.id.entry_whooch_foot_fansA);
						if(entry.fanString != null)
						{
							tvFanA.setText(entry.fanString);
							tvFanA.setVisibility(View.VISIBLE);
						}
						else
						{
							tvFanA.setVisibility(View.GONE);
						}

						ImageView iv1B = (ImageView) view
								.findViewById(R.id.entry_user_imageB);
						UrlImageViewHelper.setUrlDrawable(iv1B,
								mShowConvoCurrentUpdate.userImageUriLarge);

						TextView tv2B = (TextView) view
								.findViewById(R.id.entry_posted_userB);
						tv2B.setText(mShowConvoCurrentUpdate.userName);

						TextView tv3B = (TextView) view
								.findViewById(R.id.entry_whooch_contentB);
						tv3B.setText(WhoochHelperFunctions
								.getSpannedFromHtmlContent(
										mShowConvoCurrentUpdate.content, tv3B,
										getActivityContext()));

						TextView tv4B = (TextView) view
								.findViewById(R.id.entry_whooch_footB);
						tv4B.setText(WhoochHelperFunctions.toRelativeTime(Long
								.parseLong(mShowConvoCurrentUpdate.timestamp)));

						TextView tvFanB = (TextView) view.findViewById(R.id.entry_whooch_foot_fansB);
						if(mShowConvoCurrentUpdate.fanString != null)
						{
							tvFanB.setText(mShowConvoCurrentUpdate.fanString);
							tvFanB.setVisibility(View.VISIBLE);
						}
						else
						{
							tvFanB.setVisibility(View.GONE);
						}

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

		public void preExecute() {
		}

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
	
	private class AddFan implements WhoochApiCallInterface {

		private int fanCount = 0;
		private int lastPosition = -1;

		public void preExecute() {
			fanCount = Integer.parseInt(mShowConvoCurrentUpdate.fans, 10);
			lastPosition = mLastSelectedPosition;
		}

		public HttpRequestBase getHttpRequest() {
			HttpPost request = new HttpPost(Settings.apiUrl + "/whooch/addfan");

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("whoochId",
					mShowConvoCurrentUpdate.whoochId));
			nameValuePairs.add(new BasicNameValuePair("whoochNumber",
					mShowConvoCurrentUpdate.whoochNumber));

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

				fanCount++;
				
				StreamEntry entry = mWhoochArray.get(lastPosition);
				
				if (fanCount == 1) {
					entry.fanString = "(1 fan)";
				} else {
					entry.fanString = "(" + fanCount + " fans)";
				}
				
				mAdapter.notifyDataSetChanged();
				
				Toast.makeText(getActivityContext(),
						"You are now a fan of this update",
						Toast.LENGTH_LONG).show();

			} else {
				Toast.makeText(getActivityContext(),
						"Something went wrong, please try again",
						Toast.LENGTH_LONG).show();
			}

		}
	}

	private void setReactionsArray(ArrayList<StreamEntry> temp) {
		mWhoochArray.clear();
		for (int i = 0; i < temp.size(); i++) {
			mWhoochArray.add(temp.get(i));
		}
	}

}