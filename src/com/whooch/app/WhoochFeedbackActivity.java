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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;
import com.whooch.app.helpers.WhoochHelperFunctions;
import com.whooch.app.json.FeedbackEntry;
import com.whooch.app.ui.FeedbackArrayAdapter;

public class WhoochFeedbackActivity extends SherlockListActivity implements
		OnScrollListener {

	private ListView mListView;

	private ArrayList<FeedbackEntry> mFeedbackArray = new ArrayList<FeedbackEntry>();
	private FeedbackArrayAdapter mAdapter;

	boolean mFeedbackInitiated = false;
	boolean mFeedbackHasMoreUpdates = true;
	boolean mLoadMoreItemsInProgress = false;
	private int mFeedbackNextPage = 1;
	private String mWhoochId = null;

	String mDeleteFeedbackId = null;
	private int mLastSelectedPosition = -1;
	
	private FeedbackEntry mDialogCurrentUpdate = null;

	View mLoadingFooterView;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.stream);
		
		Intent i = getIntent();
		Bundle b = i.getExtras();
		
		mWhoochId = b.getString("WHOOCH_ID");

		LayoutInflater inflater = (LayoutInflater) getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
	
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		
		View whoochTitle = inflater.inflate(
				R.layout.whooch_title_bar, null);
		getSupportActionBar().setCustomView(whoochTitle);
		
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		
		ImageView iv1 = (ImageView) findViewById(R.id.wheader_whooch_image);
		UrlImageViewHelper.setUrlDrawable(iv1,
				b.getString("WHOOCH_IMAGE"));

		TextView tv1 = (TextView) findViewById(R.id.wheader_whooch_title);
		tv1.setText(b.getString("WHOOCH_NAME"));

		TextView tv2 = (TextView) findViewById(R.id.wheader_whooch_leader);
		tv2.setText("Feedback");

		LinearLayout ll1 = (LinearLayout) findViewById(R.id.wheader_whoochinfo);
		ll1.setVisibility(View.VISIBLE);
		
		ll1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	finish();
            }
        });

		mListView = getListView();
		mListView.setOnScrollListener(this);
		
		//Add and remove loading footer before setting adapter
		//Footer won't show up unless one is present when adapter is set
		mLoadingFooterView = this.getLayoutInflater().inflate(
				R.layout.stream_loading_footer, null);
		mListView.addFooterView(mLoadingFooterView);

		mAdapter = new FeedbackArrayAdapter(this, mFeedbackArray, true);
		setListAdapter(mAdapter);
		
		mListView.removeFooterView(mLoadingFooterView);

		if (savedInstanceState == null) {

			WhoochApiCallTask task = new WhoochApiCallTask(
					getActivityContext(), new FeedbackInitiate(), true);
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
			out.writeObject(mFeedbackArray);
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		outState.putByteArray("FeedbackList", bos.toByteArray());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onRestoreInstanceState(Bundle savedState) {

		if (savedState != null) {

			if (savedState.containsKey("FeedbackList")
					&& savedState.containsKey("FeedbackType")) {

				try {

					View loader = findViewById(R.id.main_loader);
					if (loader != null) {
						loader.setVisibility(View.GONE);
					}

					ObjectInputStream objectIn = new ObjectInputStream(
							new ByteArrayInputStream(
									savedState.getByteArray("FeedbackList")));
					Object obj;
					obj = objectIn.readObject();

					setFeedbackArray((ArrayList<FeedbackEntry>) obj);

					mFeedbackInitiated = true;
					mFeedbackNextPage++;

					if (mFeedbackArray.size() < 25) {
						mFeedbackHasMoreUpdates = false;
					}
					
					if (mFeedbackArray.isEmpty()) {
						TextView tvE1 = (TextView) findViewById(R.id.empty_text1);
						tvE1.setText("No feedback has been sent to this whooch.");
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
						getActivityContext(), new FeedbackInitiate(), true);
				task.execute();
			}
		} else {
			WhoochApiCallTask task = new WhoochApiCallTask(
					getActivityContext(), new FeedbackInitiate(), true);
			task.execute();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mFeedbackInitiated) {
			WhoochApiCallTask task = new WhoochApiCallTask(
					getActivityContext(), new FeedbackInitiate(), false);
			task.execute();
		}
	}

	private Context getActivityContext() {
		return this;
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

	@SuppressWarnings("deprecation")
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) 
	{
		final FeedbackEntry entry = mFeedbackArray.get(position);
		mLastSelectedPosition = position;
		mDialogCurrentUpdate = entry;

		// create the menu
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<Runnable> handlers = new ArrayList<Runnable>();

			names.add(getResources().getString(R.string.modal_react));
			handlers.add(new Runnable() {
				public void run() {

					Intent i = new Intent(getApplicationContext(),
							PostReactionActivity.class);
					i.putExtra("WHOOCH_ID", entry.whoochId);
					i.putExtra("REACTION_TO", entry.feedbackId);
					i.putExtra("REACTION_TYPE", "feedback");
					i.putExtra("CONTENT", entry.content);
					i.putExtra("USER_NAME", entry.userName);
					i.putExtra("WHOOCH_NAME", entry.whoochName);
					i.putExtra("WHOOCH_IMAGE", entry.whoochImageUriLarge);
					startActivity(i);
				}
			});

		if (!entry.image.equals("null")) {
			names.add(getResources().getString(R.string.modal_image));
			handlers.add(new Runnable() {
				public void run() {
					Log.d("FeedbackActivity", "View Photo");
					Intent i = new Intent(getApplicationContext(),
							ViewPhotoActivity.class);
					i.putExtra("FEEDBACK_ID", entry.feedbackId);
					i.putExtra("IMAGE_TYPE", "feedback");
					i.putExtra("IMAGE_NAME", entry.image);
					i.putExtra("WHOOCH_NAME", entry.whoochName);
					i.putExtra("WHOOCH_IMAGE", entry.whoochImageUriMedium);
					i.putExtra("USER_NAME", entry.userName);
					startActivity(i);
				}
			});
		}

		names.add(getResources().getString(R.string.modal_removefeedback));
		handlers.add(new Runnable() {
			public void run() {
				Log.d("FeedbackActivity", "Remove Feedback");

				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivityContext());

				builder.setTitle("Whooch");
				builder.setMessage("Are you sure you want to remove this feedback?");

				builder.setNegativeButton("No",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

							}
						});

				builder.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								mDeleteFeedbackId = entry.feedbackId;
								WhoochApiCallTask task = new WhoochApiCallTask(
										getActivityContext(),
										new RemoveFeedback(), true);
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

		assembleUpdateDialog(namesArray, handlersArray);
	}
	
	private void assembleUpdateDialog(final String[] namesArray, final Runnable[] handlersArray)
	{
		Builder builder = new AlertDialog.Builder(getActivityContext());
		
		LayoutInflater inflater = (LayoutInflater) getActivityContext()
				.getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);

		View view = inflater.inflate(
				R.layout.stream_entry, null);
		
		builder.setCustomTitle(view);
		
		builder.setItems(
				namesArray, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						handlersArray[which].run();
					}
				});
		
		ImageView iv1 = (ImageView) view
				.findViewById(R.id.entry_whooch_image);
		UrlImageViewHelper.setUrlDrawable(iv1,
				mDialogCurrentUpdate.userImageUriLarge);

		TextView tv1 = (TextView) view
				.findViewById(R.id.entry_whooch_title);
		tv1.setVisibility(View.GONE);

		TextView tv2 = (TextView) view
				.findViewById(R.id.entry_posted_user);
		tv2.setText(mDialogCurrentUpdate.userName);

		TextView tv3 = (TextView) view
				.findViewById(R.id.entry_whooch_content);
		tv3.setText(WhoochHelperFunctions
				.getSpannedFromHtmlContent(
						mDialogCurrentUpdate.content, tv3,
						getActivityContext()));
		tv3.setMovementMethod(LinkMovementMethod.getInstance());

		TextView tv4 = (TextView) view
				.findViewById(R.id.entry_whooch_foot);
		tv4.setText(WhoochHelperFunctions.toRelativeTime(Long
				.parseLong(mDialogCurrentUpdate.timestamp)));
		
		LinearLayout ll1 = (LinearLayout) view.findViewById(R.id.entry_update_extras);
		ll1.setVisibility(View.GONE);
		
		AlertDialog dialog = builder.create();

		dialog.show();
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	private class FeedbackInitiate implements WhoochApiCallInterface {

		private String mResponseString = null;

		public void preExecute() {

			mFeedbackInitiated = false;
			mFeedbackHasMoreUpdates = true;
			mLoadMoreItemsInProgress = false;
			mFeedbackNextPage = 1;

		}

		public HttpRequestBase getHttpRequest() {
			return new HttpGet(Settings.apiUrl
					+ "/feedback/" + mWhoochId + "?page=0&type=whooch");
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

						if (jsonArray.length() < 25) {
							mFeedbackHasMoreUpdates = false;
						}

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
					mFeedbackHasMoreUpdates = false;
				}

			}
			
			if (mFeedbackArray.isEmpty()) {
				TextView tvE1 = (TextView) findViewById(R.id.empty_text1);
				tvE1.setText("No feedback has been sent to this whooch.");
			}

			mAdapter.notifyDataSetChanged();

			mFeedbackInitiated = true;
		}
	}

	private class FeedbackGetMoreUpdates implements WhoochApiCallInterface {

		private String mResponseString = null;

		public void preExecute() {
		}

		public HttpRequestBase getHttpRequest() {
			return new HttpGet(Settings.apiUrl + "/feedback/" + mWhoochId + "?page="
					+ mFeedbackNextPage + "&type=whooch");
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
						mFeedbackHasMoreUpdates = false;
					}

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
				mFeedbackHasMoreUpdates = false;
			}

			mFeedbackNextPage++;

			mAdapter.notifyDataSetChanged();

			// dismiss the 'loading more items' footer
			mListView.removeFooterView(mLoadingFooterView);

			// set flag to false so onScroll method can be called again
			mLoadMoreItemsInProgress = false;
		}
	}

	private class RemoveFeedback implements WhoochApiCallInterface {

		public void preExecute() {
		}

		public HttpRequestBase getHttpRequest() {
			HttpPost request = new HttpPost(Settings.apiUrl
					+ "/feedback/delete");

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("feedbackId",
					mDeleteFeedbackId));

				nameValuePairs.add(new BasicNameValuePair("type", "whooch"));


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
				mFeedbackArray.remove(mLastSelectedPosition);
				
				if (mFeedbackArray.isEmpty()) {
					TextView tvE1 = (TextView) findViewById(R.id.empty_text1);
					tvE1.setText("No feedback has been sent to this whooch.");
				}
				
				mAdapter.notifyDataSetChanged();
			}

			mDeleteFeedbackId = null;

		}
	}

	private void setFeedbackArray(ArrayList<FeedbackEntry> temp) {
		mFeedbackArray.clear();
		for (int i = 0; i < temp.size(); i++) {
			mFeedbackArray.add(temp.get(i));
		}
	}

}