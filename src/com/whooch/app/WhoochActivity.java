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
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;
import com.whooch.app.helpers.WhoochHelperFunctions;
import com.whooch.app.json.StreamEntry;
import com.whooch.app.json.WhoochProfileEntry;
import com.whooch.app.ui.StreamArrayAdapter;

public class WhoochActivity extends SherlockListActivity implements
		OnScrollListener {

	private ListView mListView;

	private ArrayList<StreamEntry> mWhoochArray = new ArrayList<StreamEntry>();
	private StreamArrayAdapter mAdapter;

	private String mLatestWhoochNum = "0";
	private String mOldestWhoochNum = "0";

	boolean mWhoochInitiated = false;
	boolean mWhoochHasMoreUpdates = true;
	boolean mLoadMoreItemsInProgress = false;

	private String mShowConvoId = null;
	private String mShowConvoNumber = null;
	private StreamEntry mShowConvoCurrentUpdate = null;

	private String mDeleteWhoochId = null;
	private String mDeleteWhoochNumber = null;
	private int mLastSelectedPosition = -1;

	private String mWhoochId;
	private String mWhoochName;
	private String mWhoochImage;

	private String mLeaderName;

	private String mWhoochType = null;

	private boolean mIsContributor = false;

	View mLoadingFooterView;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.whooch);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		getSupportActionBar().setDisplayShowCustomEnabled(true);

		View whoochTitle = inflater.inflate(R.layout.whooch_title_bar, null);
		getSupportActionBar().setCustomView(whoochTitle);

		getSupportActionBar().setDisplayShowHomeEnabled(false);

		LinearLayout ll1 = (LinearLayout) findViewById(R.id.wheader_whoochinfo);
		ll1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		mListView = getListView();
		mListView.setOnScrollListener(this);

		// Add and remove loading footer before setting adapter
		// Footer won't show up unless one is present when adapter is set
		mLoadingFooterView = this.getLayoutInflater().inflate(
				R.layout.stream_loading_footer, null);
		mListView.addFooterView(mLoadingFooterView);

		mAdapter = new StreamArrayAdapter(this, mWhoochArray, true);
		setListAdapter(mAdapter);

		mListView.removeFooterView(mLoadingFooterView);

		Intent i = getIntent();
		Bundle b = i.getExtras();
		mWhoochId = b.getString("WHOOCH_ID");
		if (mWhoochId == null) {
			Toast.makeText(getApplicationContext(),
					"Something went wrong, please try again",
					Toast.LENGTH_SHORT).show();
			finish();
		}

		if (savedInstanceState == null) {

			WhoochApiCallTask task = new WhoochApiCallTask(
					getActivityContext(), new WhoochInitiate(), true);
			task.execute();
			task = new WhoochApiCallTask(getActivityContext(),
					new GetWhoochInfo(), false);
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
		outState.putByteArray("WhoochList", bos.toByteArray());

		outState.putString("WhoochId", mWhoochId);
		outState.putString("WhoochName", mWhoochName);
		outState.putString("WhoochImage", mWhoochImage);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onRestoreInstanceState(Bundle savedState) {

		if (savedState != null) {

			if (savedState.containsKey("WhoochList")
					&& savedState.containsKey("WhoochId")
					&& savedState.containsKey("WhoochName")
					&& savedState.containsKey("WhoochImage")) {

				try {

					mWhoochId = savedState.getString("WhoochId");
					mWhoochName = savedState.getString("WhoochName");
					mWhoochImage = savedState.getString("WhoochImage");

					WhoochApiCallTask task = new WhoochApiCallTask(
							getActivityContext(), new GetWhoochInfo(), false);
					task.execute();

					ObjectInputStream objectIn = new ObjectInputStream(
							new ByteArrayInputStream(
									savedState.getByteArray("WhoochList")));
					Object obj;
					obj = objectIn.readObject();

					setWhoochArray((ArrayList<StreamEntry>) obj);

					if (mWhoochArray.size() < 25) {
						mWhoochHasMoreUpdates = false;
					}

					if (mWhoochArray.size() > 0) {
						mLatestWhoochNum = mWhoochArray.get(0).whoochNumber;
						mOldestWhoochNum = mWhoochArray
								.get(mWhoochArray.size() - 1).whoochNumber;
					}
					
					if (mWhoochArray.isEmpty()) {
						TextView tvE1 = (TextView) findViewById(R.id.empty_text1);
						tvE1.setText("No updates have been shared yet.");
					}

					mAdapter.notifyDataSetChanged();
					mWhoochInitiated = true;

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
						getActivityContext(), new WhoochInitiate(), true);
				task.execute();
				task = new WhoochApiCallTask(getActivityContext(),
						new GetWhoochInfo(), false);
				task.execute();
			}
		} else {
			WhoochApiCallTask task = new WhoochApiCallTask(
					getActivityContext(), new WhoochInitiate(), true);
			task.execute();
			task = new WhoochApiCallTask(getActivityContext(),
					new GetWhoochInfo(), false);
			task.execute();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mWhoochInitiated) {
			WhoochApiCallTask task = new WhoochApiCallTask(
					getActivityContext(), new WhoochGetNewUpdates(), true);
			task.execute();
		}

	}

	private Context getActivityContext() {
		return this;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

		if (mWhoochInitiated && mWhoochHasMoreUpdates
				&& !mLoadMoreItemsInProgress) {

			int numPaddingItems = 0;
			if (firstVisibleItem + visibleItemCount + numPaddingItems >= totalItemCount) {

				mLoadMoreItemsInProgress = true;
				WhoochApiCallTask task = new WhoochApiCallTask(
						getActivityContext(), new WhoochGetMoreUpdates(), false);
				task.execute();

				mLoadingFooterView = this.getLayoutInflater().inflate(
						R.layout.stream_loading_footer, null);
				mListView.addFooterView(mLoadingFooterView);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		final StreamEntry entry = mWhoochArray.get(position);
		mLastSelectedPosition = position;
		mShowConvoCurrentUpdate = entry;

		// create the menu
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<Runnable> handlers = new ArrayList<Runnable>();

		SharedPreferences settings = getActivityContext().getSharedPreferences(
				"whooch_preferences", 0);
		String currentUserName = settings.getString("username", null);

		if (entry.isContributor.equals("1")
				&& !entry.userName.equalsIgnoreCase(currentUserName)) {
			names.add(getResources().getString(R.string.modal_react));
			handlers.add(new Runnable() {
				public void run() {

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

		if (entry.reactionType.equals("whooch")) {
			names.add(getResources().getString(R.string.modal_showconversation));
			handlers.add(new Runnable() {
				public void run() {
					Log.d("WhoochActivity", "Show Conversation");
					mShowConvoId = entry.whoochId;
					mShowConvoNumber = entry.reactionTo;

					WhoochApiCallTask task = new WhoochApiCallTask(
							getActivityContext(), new ShowConversation(), true);
					task.execute();
				}
			});
		}

		if (!entry.image.equals("null")) {
			names.add(getResources().getString(R.string.modal_image));
			handlers.add(new Runnable() {
				public void run() {
					Log.d("WhoochActivity", "View Photo");
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

		if (entry.reactionType.equals("feedback")) {

			if (!entry.feedbackInfo.image.equals("null")) {
				names.add(getResources().getString(R.string.modal_feedbackimage));
				handlers.add(new Runnable() {
					public void run() {
						Log.d("StreamActivity", "View Feeedback Photo");
						Intent i = new Intent(getApplicationContext(),
								ViewPhotoActivity.class);
						i.putExtra("FEEDBACK_ID", entry.feedbackInfo.feedbackId);
						i.putExtra("IMAGE_TYPE", "feedback");
						i.putExtra("IMAGE_NAME", entry.feedbackInfo.image);
						i.putExtra("WHOOCH_NAME", entry.whoochName);
						i.putExtra("WHOOCH_IMAGE", entry.whoochImageUriMedium);
						i.putExtra("USER_NAME", entry.feedbackInfo.userName);
						startActivity(i);
					}
				});
			}

		}

		if (!entry.userName.equalsIgnoreCase(currentUserName)
				&& entry.isFan.equals("0")) {
			names.add(getResources().getString(R.string.modal_fan));
			handlers.add(new Runnable() {
				public void run() {
					WhoochApiCallTask task = new WhoochApiCallTask(
							getActivityContext(), new AddFan(), true);
					task.execute();
				}
			});
		}

		if (entry.userName.equalsIgnoreCase(currentUserName)) {
			names.add(getResources().getString(R.string.modal_delete));
			handlers.add(new Runnable() {
				public void run() {
					Log.d("WhoochActivity", "Delete Update");

					AlertDialog.Builder builder = new AlertDialog.Builder(
							getActivityContext());

					LayoutInflater inflater = (LayoutInflater) getActivityContext()
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View titleView = inflater.inflate(R.layout.default_alert_title, null);
					View contentView = inflater.inflate(R.layout.default_alert_message, null);
					TextView tvDefAlert = (TextView)contentView.findViewById(R.id.default_alert_content);
					tvDefAlert.setText("Are you sure you want to delete this update?");
					builder.setCustomTitle(titleView);
					builder.setView(contentView);

					builder.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

								}
							});

					builder.setPositiveButton("Yes",
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

		if (names.size() > 0) {
			final String[] namesArray = names.toArray(new String[names.size()]);
			final Runnable[] handlersArray = handlers
					.toArray(new Runnable[handlers.size()]);

			assembleUpdateDialog(namesArray, handlersArray);
		}
		else
		{
			assembleUpdateDialog(null, null);
		}

	}

	private void assembleUpdateDialog(final String[] namesArray,
			final Runnable[] handlersArray) {
		Builder builder = new AlertDialog.Builder(getActivityContext());

		LayoutInflater inflater = (LayoutInflater) getActivityContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view = inflater.inflate(R.layout.stream_entry, null);

		builder.setCustomTitle(view);

		if ((namesArray != null) && (handlersArray != null)) {
			builder.setItems(namesArray, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					handlersArray[which].run();
				}
			});
		} else {
			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
		}

		ImageView iv1 = (ImageView) view.findViewById(R.id.entry_whooch_image);
		UrlImageViewHelper.setUrlDrawable(iv1,
				mShowConvoCurrentUpdate.userImageUriLarge);

		TextView tv1 = (TextView) view.findViewById(R.id.entry_whooch_title);
		tv1.setVisibility(View.GONE);

		TextView tv2 = (TextView) view.findViewById(R.id.entry_posted_user);
		tv2.setText(mShowConvoCurrentUpdate.userName);

		TextView tv3 = (TextView) view.findViewById(R.id.entry_whooch_content);
		tv3.setText(WhoochHelperFunctions.getSpannedFromHtmlContent(
				mShowConvoCurrentUpdate.content, tv3, getActivityContext()));
		tv3.setMovementMethod(LinkMovementMethod.getInstance());

		TextView tv4 = (TextView) view.findViewById(R.id.entry_whooch_foot);
		tv4.setText(WhoochHelperFunctions.toRelativeTime(Long
				.parseLong(mShowConvoCurrentUpdate.timestamp)));

		TextView tvFan = (TextView) view
				.findViewById(R.id.entry_whooch_foot_fans);
		if (mShowConvoCurrentUpdate.fanString != null) {
			tvFan.setText(mShowConvoCurrentUpdate.fanString);
			tvFan.setVisibility(View.VISIBLE);
		} else {
			tvFan.setVisibility(View.GONE);
		}

		LinearLayout ll1 = (LinearLayout) view
				.findViewById(R.id.entry_update_extras);
		ll1.setVisibility(View.GONE);

		AlertDialog dialog = builder.create();

		dialog.show();
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	private class WhoochInitiate implements WhoochApiCallInterface {

		private String mResponseString = null;

		public void preExecute() {

			mLatestWhoochNum = "0";
			mOldestWhoochNum = "0";

			mWhoochInitiated = false;
			mWhoochHasMoreUpdates = true;
			mLoadMoreItemsInProgress = false;

		}

		public HttpRequestBase getHttpRequest() {
			return new HttpGet(Settings.apiUrl + "/whooch/" + mWhoochId
					+ "?count=25");
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
							mWhoochHasMoreUpdates = false;
						}

						StreamEntry entry = new StreamEntry(
								jsonArray.getJSONObject(0), getWindowManager());

						// the newest updates are at the front of the array, so
						// loop over forwards
						for (int i = 0; i < jsonArray.length(); i++) {

							// create an object that will be used to populate
							// the List View and add it to the array
							entry = new StreamEntry(jsonArray.getJSONObject(i),
									getWindowManager());
							mWhoochArray.add(entry);
						}
					} catch (JSONException e) {
						e.printStackTrace();
						// TODO: error handling
					}
				} else {
					mWhoochHasMoreUpdates = false;
				}

				if (mWhoochArray.size() > 0) {
					mLatestWhoochNum = mWhoochArray.get(0).whoochNumber;
					mOldestWhoochNum = mWhoochArray
							.get(mWhoochArray.size() - 1).whoochNumber;
				}
			}

			if (mWhoochArray.isEmpty()) {
				TextView tvE1 = (TextView) findViewById(R.id.empty_text1);
				tvE1.setText("No updates have been shared yet.");
			}

			mAdapter.notifyDataSetChanged();

			mWhoochInitiated = true;
		}
	}

	private class WhoochGetNewUpdates implements WhoochApiCallInterface {

		private String mResponseString = null;

		public void preExecute() {
		}

		public HttpRequestBase getHttpRequest() {
			return new HttpGet(Settings.apiUrl + "/whooch/" + mWhoochId
					+ "?boundary=" + mLatestWhoochNum + "&after=true");
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
						mWhoochArray.add(0, entry);
					}

				} catch (JSONException e) {
					e.printStackTrace();
					// TODO: error handling
				}
			} else {
				// if it is null we don't mind, there just wasn't anything there
			}

			if (mWhoochArray.size() > 0) {
				mLatestWhoochNum = mWhoochArray.get(0).whoochNumber;
				mOldestWhoochNum = mWhoochArray.get(mWhoochArray.size() - 1).whoochNumber;
			}

			mAdapter.notifyDataSetChanged();
		}
	}

	private class WhoochGetMoreUpdates implements WhoochApiCallInterface {

		private String mResponseString = null;

		public void preExecute() {
		}

		public HttpRequestBase getHttpRequest() {
			return new HttpGet(Settings.apiUrl + "/whooch/" + mWhoochId
					+ "?boundary=" + mOldestWhoochNum + "&before=true");
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
						mWhoochHasMoreUpdates = false;
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
				// if it is null then we should stop trying to get more updates
				mWhoochHasMoreUpdates = false;
			}

			if (mWhoochArray.size() > 0) {
				mLatestWhoochNum = mWhoochArray.get(0).whoochNumber;
				mOldestWhoochNum = mWhoochArray.get(mWhoochArray.size() - 1).whoochNumber;
			}

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
								.getSystemService(
										Context.LAYOUT_INFLATER_SERVICE);

						View view = inflater.inflate(
								R.layout.show_conversation_title, null);

						ImageView iv1 = (ImageView) view
								.findViewById(R.id.show_conversation_whoochimage);
						UrlImageViewHelper.setUrlDrawable(iv1,
								entry.whoochImageUriLarge);

						TextView tv1A = (TextView) view
								.findViewById(R.id.show_conversation_whoochname);
						tv1A.setText(entry.whoochName);

						builder.setCustomTitle(view);

						view = inflater.inflate(R.layout.show_conversation,
								null);

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
						
						tv3A.setMovementMethod(LinkMovementMethod.getInstance());

						TextView tv4A = (TextView) view
								.findViewById(R.id.entry_whooch_footA);
						tv4A.setText(WhoochHelperFunctions.toRelativeTime(Long
								.parseLong(entry.timestamp)));

						TextView tvFanA = (TextView) view
								.findViewById(R.id.entry_whooch_foot_fansA);
						if (entry.fanString != null) {
							tvFanA.setText(entry.fanString);
							tvFanA.setVisibility(View.VISIBLE);
						} else {
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

						tv3B.setMovementMethod(LinkMovementMethod.getInstance());
						
						TextView tv4B = (TextView) view
								.findViewById(R.id.entry_whooch_footB);
						tv4B.setText(WhoochHelperFunctions.toRelativeTime(Long
								.parseLong(mShowConvoCurrentUpdate.timestamp)));

						TextView tvFanB = (TextView) view
								.findViewById(R.id.entry_whooch_foot_fansB);
						if (mShowConvoCurrentUpdate.fanString != null) {
							tvFanB.setText(mShowConvoCurrentUpdate.fanString);
							tvFanB.setVisibility(View.VISIBLE);
						} else {
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
					Toast.makeText(getActivityContext(),
							"The original update has been deleted",
							Toast.LENGTH_LONG).show();
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
				
				if (mWhoochArray.isEmpty()) {
					TextView tvE1 = (TextView) findViewById(R.id.empty_text1);
					tvE1.setText("No updates have been shared yet.");
				}
				
				mAdapter.notifyDataSetChanged();
			}

			mDeleteWhoochId = null;
			mDeleteWhoochNumber = null;

		}
	}

	private class GetWhoochInfo implements WhoochApiCallInterface {

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

						if (entry.isContributing.equals("0")) {
							mIsContributor = false;
						} else {
							mIsContributor = true;
						}

						if (entry.type.equals("open")) {
							mWhoochType = "open";
						} else {
							mWhoochType = "closed";
						}

						mWhoochName = entry.whoochName;
						mWhoochImage = entry.whoochImageUriLarge;
						mLeaderName = entry.leaderName;

						ImageButton ib1 = (ImageButton) findViewById(R.id.wheader_update_button);
						Button b1 = (Button) findViewById(R.id.wheader_options_button);

						if (mIsContributor) {
							ib1.setImageResource(R.drawable.ic_update_w);
							ib1.setVisibility(View.VISIBLE);
						} else if (mWhoochType == "open") {
							ib1.setImageResource(R.drawable.ic_feedback_w);
							ib1.setVisibility(View.VISIBLE);
						} else {
							ib1.setVisibility(View.GONE);

							LayoutParams params = (LayoutParams) b1
									.getLayoutParams();
							params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
							b1.setLayoutParams(params);
						}

						ib1.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								if (mIsContributor) {
									Intent i = new Intent(getActivityContext(),
											PostStandardActivity.class);
									i.putExtra("WHOOCH_ID", mWhoochId);
									i.putExtra("WHOOCH_IMAGE", mWhoochImage);
									i.putExtra("WHOOCH_NAME", mWhoochName);
									i.putExtra("UPDATE_TYPE", "whooch");
									i.putExtra("USER_NAME", mLeaderName);
									startActivity(i);
								} else if (mWhoochType == "open") {
									Intent i = new Intent(
											getApplicationContext(),
											PostFeedbackActivity.class);
									i.putExtra("WHOOCH_ID", mWhoochId);
									i.putExtra("WHOOCH_IMAGE", mWhoochImage);
									i.putExtra("WHOOCH_NAME", mWhoochName);
									i.putExtra("USER_NAME", mLeaderName);
									startActivity(i);
								}
							}
						});

						b1.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent i = new Intent(getActivityContext(),
										WhoochProfileActivity.class);
								i.putExtra("WHOOCH_ID", mWhoochId);
								startActivity(i);
							}
						});
						b1.setVisibility(View.VISIBLE);

						ImageView iv1 = (ImageView) findViewById(R.id.wheader_whooch_image);
						UrlImageViewHelper.setUrlDrawable(iv1,
								entry.whoochImageUriMedium);

						TextView tv1 = (TextView) findViewById(R.id.wheader_whooch_title);
						tv1.setText(entry.whoochName);

						TextView tv2 = (TextView) findViewById(R.id.wheader_whooch_leader);
						tv2.setText(entry.leaderName);

						LinearLayout ll1 = (LinearLayout) findViewById(R.id.wheader_whoochinfo);
						ll1.setVisibility(View.VISIBLE);

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
				
				entry.isFan = "1";

				mAdapter.notifyDataSetChanged();

				Toast.makeText(getActivityContext(),
						"You are now a fan of this update", Toast.LENGTH_LONG)
						.show();

			} else {
				Toast.makeText(getActivityContext(),
						"Something went wrong, please try again",
						Toast.LENGTH_LONG).show();
			}

		}
	}

	private void setWhoochArray(ArrayList<StreamEntry> temp) {
		mWhoochArray.clear();
		for (int i = 0; i < temp.size(); i++) {
			mWhoochArray.add(temp.get(i));
		}
	}

}