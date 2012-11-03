package com.whooch.app;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;
import com.whooch.app.json.FriendsEntry;
import com.whooch.app.ui.FriendsArrayAdapter;

public class InviteUserActivity extends SherlockListActivity {

	String mUserId = null;
	String mWhoochId = null;
	String mWhoochName = null;
	String mWhoochImage = null;
	String mWhoochType = null;
	private ArrayList<FriendsEntry> mFriendsArray = new ArrayList<FriendsEntry>();
	ArrayAdapter<FriendsEntry> mAdapter;
	protected Spinner mFriendsSelector;
	private Spinner mInviteTypeSelector;
	private Button mInviteButton;
	private String mUserName = null;
	private String mInviteType = "contribute";

	// private AutoCompleteTextView mSearchUsersAutoText;
	// private ArrayList<FriendsEntry> mSearchUsersArray = new
	// ArrayList<FriendsEntry>();
	// private ArrayAdapter<FriendsEntry> mSearchUsersAdapter;

	View mLoadingFooterView;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.whooch_invite_user);

		SharedPreferences settings = getSharedPreferences("whooch_preferences",
				0);
		mUserId = settings.getString("userid", null);

		Intent i = getIntent();
		Bundle b = i.getExtras();
		mWhoochId = b.getString("WHOOCH_ID");
		mWhoochName = b.getString("WHOOCH_NAME");
		mWhoochImage = b.getString("WHOOCH_IMAGE");
		mWhoochType = b.getString("WHOOCH_TYPE");


		
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
		tv2.setText("Invite user");

		LinearLayout ll1 = (LinearLayout) findViewById(R.id.wheader_whoochinfo);
		ll1.setVisibility(View.VISIBLE);
		
		ll1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	finish();
            }
        });

		// add the invalid "No Whooch Selected" entry. This will be rendered by
		// the adapter.
		mFriendsArray.add(new FriendsEntry());

		mAdapter = new FriendsArrayAdapter(this,
				android.R.layout.simple_spinner_item, mFriendsArray);

		mFriendsSelector = (Spinner) findViewById(R.id.whooch_invite_spinner);
		mFriendsSelector.setAdapter(mAdapter);

		if (mWhoochType.equals("open")) {
			mInviteTypeSelector = (Spinner) findViewById(R.id.whooch_invite_type_spinner_open);
		} else {
			mInviteTypeSelector = (Spinner) findViewById(R.id.whooch_invite_type_spinner_closed);
		}
		
		mInviteTypeSelector.setVisibility(View.VISIBLE);

		mInviteTypeSelector.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parentView,
							View selectedItemView, int position, long id) {
						if (position == 0) {
							mInviteType = "contribute";
						} else {
							mInviteType = "trail";
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> parentView) {

					}

				});

		mInviteButton = (Button) findViewById(R.id.whooch_user_invite_button);
		mInviteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mFriendsSelector.getSelectedItemPosition() == 0) {
					// User selected the placeholder item
					Toast.makeText(getActivityContext(),
							"Select a friend to invite", Toast.LENGTH_SHORT)
							.show();
				} else {
					WhoochApiCallTask task = new WhoochApiCallTask(
							getActivityContext(), new Invite(), true);
					task.execute();
				}
			}
		});

		/*
		 * mSearchUsersAutoText = (AutoCompleteTextView)
		 * findViewById(R.id.whooch_invite_search_users);
		 * 
		 * mSearchUsersAdapter = new FriendsArrayAdapter( getActivityContext(),
		 * android.R.layout.simple_spinner_item, mSearchUsersArray);
		 * 
		 * mSearchUsersAutoText.setAdapter(mSearchUsersAdapter);
		 * mSearchUsersAutoText .setOnItemClickListener(new
		 * AdapterView.OnItemClickListener() { public void
		 * onItemClick(AdapterView<?> parent, View view, int position, long id)
		 * { if (!mSearchUsersArray.get(position).equals( "No users found")) { }
		 * else { mSearchUsersAutoText.setText(""); } } });
		 * 
		 * mSearchUsersAutoText.addTextChangedListener(new TextWatcher() {
		 * 
		 * @Override public void beforeTextChanged(CharSequence s, int start,
		 * int count, int after) { }
		 * 
		 * @Override public void onTextChanged(CharSequence s, int start, int
		 * before, int count) {
		 * 
		 * mSearchUsersArray.clear(); //Start at index of 1 since first entry is
		 * default message for(int i=1; i < mFriendsArray.size(); i++) {
		 * if(mFriendsArray
		 * .get(i).userName.toLowerCase().contains(s.toString().toLowerCase()))
		 * {
		 * 
		 * mSearchUsersArray.add(mFriendsArray.get(i)); Log.e("test",
		 * mSearchUsersArray.get(0).userName); } }
		 * 
		 * mSearchUsersAdapter = new FriendsArrayAdapter( getActivityContext(),
		 * android.R.layout.simple_spinner_item, mSearchUsersArray);
		 * 
		 * 
		 * mSearchUsersAutoText.setAdapter(mSearchUsersAdapter);
		 * mSearchUsersAutoText.showDropDown();
		 * 
		 * }
		 * 
		 * @Override public void afterTextChanged(Editable s) { } });
		 */

	}

	@Override
	public void onResume() {
		super.onResume();

		WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(),
				new InviteUserInitiate(), true);
		task.execute();
	}

	private Context getActivityContext() {
		return this;
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle b) {

		// create the menu
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<Runnable> handlers = new ArrayList<Runnable>();

		final String[] namesArray = names.toArray(new String[names.size()]);
		final Runnable[] handlersArray = handlers.toArray(new Runnable[handlers
				.size()]);

		return new AlertDialog.Builder(getActivityContext()).setItems(
				namesArray, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Log.d("InviteUserActivity", "Something Was Clicked");
						handlersArray[which].run();
					}
				}).create();
	}

	private class InviteUserInitiate implements WhoochApiCallInterface {

		private String mResponseString = null;

		public void preExecute() {

		}

		public HttpRequestBase getHttpRequest() {
			return new HttpGet(Settings.apiUrl + "/whooch/" + mWhoochId
					+ "?potentialcontributeinvites=true");
		}

		public void handleResponse(String responseString) {
			mResponseString = responseString;
		}

		public void postExecute(int statusCode) {

			if (statusCode == 200) {
				mFriendsArray.clear();
				mFriendsArray.add(new FriendsEntry());

				// parse the response as JSON and update the Content Array
				if (!mResponseString.equals("null")) {

					try {
						JSONArray jsonArray = new JSONArray(mResponseString);
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject jsonObject = jsonArray.getJSONObject(i);
							FriendsEntry entry = new FriendsEntry(jsonObject,
									getWindowManager());
							mFriendsArray.add(entry);
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.e("testlog", e.toString());
					}

				} else {
					// if it is null we don't mind, there just wasn't anything
					// there
				}

			}

			LinearLayout lll = (LinearLayout) findViewById(R.id.whooch_invite_layout);
			lll.setVisibility(View.VISIBLE);
		}

	}

	private class Invite implements WhoochApiCallInterface {

		public void preExecute() {
		}

		public HttpRequestBase getHttpRequest() {
			
			String requestString = null;
			
			if(mInviteType.equals("contribute"))
			{
				requestString = "sendcontributeinvites";
			}
			else if(mInviteType.equals("trail") && mWhoochType.equals("closed"))
			{
				requestString = "sendtrailinvites";
			}
			else
			{
				requestString = "sendtrailrequests";
			}
			
			HttpPost request = new HttpPost(Settings.apiUrl
					+ "/whooch/" + requestString);

			MultipartEntity reqEntity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);

			FriendsEntry selectedEntry = (FriendsEntry) mFriendsSelector
					.getSelectedItem();

			mUserName = selectedEntry.userName;

			try {
				reqEntity.addPart("whoochId", new StringBody(mWhoochId));

				reqEntity.addPart("inviteList[]", new StringBody(
						selectedEntry.userId));

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			request.setEntity(reqEntity);

			return request;
		}

		public void handleResponse(String responseString) {
		}

		public void postExecute(int statusCode) {
			if (statusCode == 200) {
				Toast.makeText(getApplicationContext(),
						"Invitation sent to " + mUserName, Toast.LENGTH_SHORT)
						.show();
				finish();
			}
		}
	}

}