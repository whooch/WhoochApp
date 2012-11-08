package com.whooch.app;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;
import com.whooch.app.helpers.WhoochHelperFunctions;
import com.whooch.app.json.SearchEntry;
import com.whooch.app.json.StreamEntry;
import com.whooch.app.ui.SearchArrayAdapter;

public class SearchActivity extends SherlockListActivity implements
		OnScrollListener {

	private ListView mListView;

	private ArrayList<SearchEntry> mSearchArray = new ArrayList<SearchEntry>();
	private SearchArrayAdapter mAdapter;

	boolean mSearchInitiated = false;
	boolean mSearchHasMoreUpdates = true;
	boolean mLoadMoreItemsInProgress = false;

	private String mShowConvoId = null;
	private String mShowConvoNumber = null;
	private SearchEntry mShowConvoCurrentUpdate = null;
	private int mLastSelectedPosition = -1;

	private int mSearchNextPage = 1;
	private EditText mSearchQuery = null;
	private String mSearchType = null;

	private Spinner mSearchSpinner = null;

	View mLoadingFooterView;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);

		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View title_view = inflater.inflate(R.layout.title_bar_generic, null);
		getSupportActionBar().setCustomView(title_view);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		TextView tvhead = (TextView) title_view
				.findViewById(R.id.header_generic_title);
		tvhead.setText("Search");

		mSearchQuery = (EditText) findViewById(R.id.search_query);

		mAdapter = new SearchArrayAdapter(this, mSearchArray);
		setListAdapter(mAdapter);

		EditText et1 = (EditText) findViewById(R.id.search_query);
		et1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					if (mSearchQuery.getText().toString().trim().length() > 0) {
						WhoochApiCallTask task = new WhoochApiCallTask(
								getActivityContext(), new SearchInitiate(),
								true);
						task.execute();
					} else {
						Toast.makeText(getActivityContext(),
								"Please provide a keyword to search for",
								Toast.LENGTH_LONG).show();
					}
					return true;

				}
				return false;
			}
		});

		Uri data = getIntent().getData();

		mSearchSpinner = (Spinner) findViewById(R.id.search_spinner);

		if (data != null) {
			mSearchType = "hash";
			mSearchSpinner.setSelection(2);

			mSearchQuery.setText(data.toString().replaceAll(
					"com.whooch.updatesearch://(.+)", "$1"));
			mSearchQuery.setSelection(mSearchQuery.getText().length());

			WhoochApiCallTask task = new WhoochApiCallTask(
					getActivityContext(), new SearchInitiate(), true);
			task.execute();
		} else {
			mSearchType = "open";
			mSearchSpinner.setSelection(0);
		}

		mSearchSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView,
					View selectedItemView, int position, long id) {
				if (position == 0) {
					mSearchType = "open";
				} else if (position == 1) {
					mSearchType = "user";
				} else {
					mSearchType = "hash";
				}

				if (mSearchQuery.getText().toString().trim().length() > 0) {
					WhoochApiCallTask task = new WhoochApiCallTask(
							getActivityContext(), new SearchInitiate(), true);
					task.execute();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {

			}

		});

	}

	@Override
	public void onResume() {
		super.onResume();

		mSearchInitiated = false;
		mSearchHasMoreUpdates = true;
		mLoadMoreItemsInProgress = false;
	}

	private Context getActivityContext() {
		return this;
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
						getActivityContext(), new SearchGetMoreUpdates(), true);
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

		final SearchEntry entry = mSearchArray.get(position);
		mLastSelectedPosition = position;
		mShowConvoCurrentUpdate = entry;

		if (mSearchType == "open") {

			Intent i = new Intent(getApplicationContext(), WhoochActivity.class);
			i.putExtra("WHOOCH_ID", entry.whoochId);
			startActivity(i);

		} else if (mSearchType == "user") {

			Intent i = new Intent(getApplicationContext(),
					UserProfileActivity.class);
			i.putExtra("USER_ID", entry.userId);
			i.putExtra("FORCE_FOREIGN", "true");
			startActivity(i);

		} else {

			// create the menu
			ArrayList<String> names = new ArrayList<String>();
			ArrayList<Runnable> handlers = new ArrayList<Runnable>();

			SharedPreferences settings = getActivityContext()
					.getSharedPreferences("whooch_preferences", 0);
			String currentUserName = settings.getString("username", null);

			if (entry.isContributor.equals("1")
					&& !entry.userName.equalsIgnoreCase(currentUserName)) {
				names.add(getResources().getString(R.string.modal_react));
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
						i.putExtra("WHOOCH_NAME", entry.whoochName);
						i.putExtra("WHOOCH_IMAGE", entry.whoochImageUriLarge);
						startActivity(i);
					}
				});
			}

			// All updates in search are open, so just check contribute status
			if (entry.isContributor.equals("0")) {
				names.add(getResources().getString(R.string.modal_feedback));
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
				names.add(getResources().getString(R.string.modal_showconversation));
				handlers.add(new Runnable() {
					public void run() {
						Log.d("StreamActivity", "Show Conversation");
						mShowConvoId = entry.whoochId;
						mShowConvoNumber = entry.reactionTo;

						WhoochApiCallTask task = new WhoochApiCallTask(
								getActivityContext(), new ShowConversation(),
								true);
						task.execute();
					}
				});
			}

			if (!entry.image.equals("null")) {
				names.add(getResources().getString(R.string.modal_image));
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

			names.add(getResources().getString(R.string.modal_whooch));
			handlers.add(new Runnable() {
				public void run() {
					Intent i = new Intent(getApplicationContext(),
							WhoochActivity.class);
					i.putExtra("WHOOCH_ID", entry.whoochId);
					startActivity(i);
				}
			});

			final String[] namesArray = names.toArray(new String[names.size()]);
			final Runnable[] handlersArray = handlers
					.toArray(new Runnable[handlers.size()]);

			assembleUpdateDialog(namesArray, handlersArray);

		}

	}

	private void assembleUpdateDialog(final String[] namesArray,
			final Runnable[] handlersArray) {
		Builder builder = new AlertDialog.Builder(getActivityContext());

		LayoutInflater inflater = (LayoutInflater) getActivityContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view = inflater.inflate(R.layout.stream_entry, null);

		builder.setCustomTitle(view);

		builder.setItems(namesArray, new DialogInterface.OnClickListener() {
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

	private class SearchInitiate implements WhoochApiCallInterface {

		private String mResponseString = null;

		public void preExecute() {

			mSearchArray.clear();
			mAdapter.notifyDataSetChanged();

		}

		public HttpRequestBase getHttpRequest() {
			return new HttpGet(Settings.apiUrl
					+ "/search/1?page=0&type="
					+ mSearchType
					+ "&query="
					+ URLEncoder.encode(mSearchQuery.getText().toString()
							.trim()));
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
						JSONObject jsonObject = new JSONObject(mResponseString);
							JSONArray jsonArray = jsonObject
									.getJSONArray(mSearchType);
							// the newest updates are at the front of the array,
							// so
							// loop over forwards

							for (int i = 0; i < jsonArray.length(); i++) {

								// create an object that will be used to
								// populate
								// the List View and add it to the array
								SearchEntry entry = new SearchEntry(
										jsonArray.getJSONObject(i),
										mSearchType, getWindowManager());

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
			
			if(mSearchArray.isEmpty())
			{
				if (mSearchType.equals("user")) {
					TextView tvE1 = (TextView) findViewById(R.id.empty_text1);
					tvE1.setText("Sorry, we could not find any users.");
				}
				else if(mSearchType.equals("open"))
				{
					TextView tvE1 = (TextView) findViewById(R.id.empty_text1);
					tvE1.setText("Sorry, we could not find any open whooches.");
				}
				else
				{
					TextView tvE1 = (TextView) findViewById(R.id.empty_text1);
					tvE1.setText("Sorry, we could not find any updates.");
				}
			}

			mAdapter.notifyDataSetChanged();

			mSearchInitiated = true;
		}
	}

	private class SearchGetMoreUpdates implements WhoochApiCallInterface {

		private String mResponseString = null;

		public void preExecute() {
		}

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

				SearchEntry entry = mSearchArray.get(lastPosition);

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

	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		int itemId = item.getItemId();
		switch (itemId) {
		case android.R.id.home:
			finish();
			break;

		}

		return true;
	}
}