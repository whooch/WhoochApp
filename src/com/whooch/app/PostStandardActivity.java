package com.whooch.app;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;
import com.whooch.app.json.ContributingEntry;
import com.whooch.app.ui.ContributingArrayAdapter;

public class PostStandardActivity extends PostBaseActivity {

	private ArrayList<ContributingEntry> mContributingArray = new ArrayList<ContributingEntry>();
	private ContributingArrayAdapter mWhoochSelectorAdapter;
	private String mWhoochIdExtra = null;
	private String mWhoochImageExtra = null;
	private String mWhoochNameExtra = null;

	private Spinner mSelectorSpinner = null;
	private ProgressBar mSelectorProgress = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent i = getIntent();
		Bundle b = i.getExtras();

		mSelectorSpinner = (Spinner) findViewById(R.id.post_whooch_spinner);
		mSelectorProgress = (ProgressBar) findViewById(R.id.spinner_loader);

		mLayoutType = "regular";

		if (b.getString("UPDATE_TYPE").equals("whooch")) {
			mWhoochIdExtra = b.getString("WHOOCH_ID");
			mWhoochImageExtra = b.getString("WHOOCH_IMAGE");
			mWhoochNameExtra = b.getString("WHOOCH_NAME");
			mUpdateType = "whooch";

			if ((mWhoochIdExtra == null) || (mWhoochImageExtra == null)
					|| (mWhoochNameExtra == null)) {
				Toast.makeText(getApplicationContext(), "Something went wrong, please try again",
						Toast.LENGTH_SHORT).show();
				finish();
				return;
			}

		} else {
			// add the invalid "No Whooch Selected" entry. This will be rendered
			// by
			// the adapter.

			getSupportActionBar().setDisplayShowHomeEnabled(true);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			
			LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View title_view = inflater.inflate(R.layout.title_bar_generic, null);
			getSupportActionBar().setCustomView(title_view);
			getSupportActionBar().setDisplayShowCustomEnabled(true);
			TextView tvhead = (TextView)title_view.findViewById(R.id.header_generic_title);
			tvhead.setText("Update");
			
			mWhoochSelectorLayout.setVisibility(View.VISIBLE);

			mUpdateType = "regular";
			
			mContributingArray.add(new ContributingEntry());

			mWhoochSelectorAdapter = new ContributingArrayAdapter(this,
					android.R.layout.simple_spinner_item, mContributingArray);
			mWhoochSelector.setAdapter(mWhoochSelectorAdapter);

			WhoochApiCallTask task = new WhoochApiCallTask(
					getActivityContext(), new GetContributingList(), false);
			task.execute();
		}

		mSubmitButton.setText("Update");
		mSubmitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if ((mUpdateType == "regular")
						&& (mWhoochSelector.getSelectedItemPosition() == 0)) {
					// User selected the placeholder item
					Toast.makeText(getActivityContext(),
							"Please select a whooch", Toast.LENGTH_SHORT)
							.show();
				} else if (mPostText.getText().toString().trim().length() <= 0) {
					Toast.makeText(getActivityContext(),
							"You need to say something", Toast.LENGTH_SHORT)
							.show();
				} else {
					WhoochApiCallTask task = new WhoochApiCallTask(
							getActivityContext(), new Submit(), true, true);
					task.execute();
				}
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

	}

	private class GetContributingList implements WhoochApiCallInterface {

		private String mResponseString = null;

		public void preExecute() {
			mSelectorProgress.setVisibility(View.VISIBLE);
		}

		public HttpRequestBase getHttpRequest() {
			return new HttpGet(Settings.apiUrl + "/user/contributinglist");
		}

		public void handleResponse(String responseString) {
			mResponseString = responseString;
		}

		public void postExecute(int statusCode) {
			if (statusCode == 200) {
				mContributingArray.clear();
				mContributingArray.add(new ContributingEntry()); // No Whooch
																	// Selected

				// parse the response as JSON and update the Content Array
				if (!mResponseString.equals("null")) {
					try {
						JSONArray jsonArray = new JSONArray(mResponseString);
						for (int i = 0; i < jsonArray.length(); i++) {
							ContributingEntry entry = new ContributingEntry(
									jsonArray.getJSONObject(i),
									getWindowManager());
							mContributingArray.add(entry);
						}
					} catch (JSONException e) {
						e.printStackTrace();
						// TODO: error handling
					}
				} else {
					// if it is null we don't mind, there just wasn't anything
					// there
				}
			}

			mWhoochSelectorAdapter.notifyDataSetChanged();

			if (mSpinnerId != null) {

				for (int i = 0; i < mWhoochSelectorAdapter.getCount(); i++) {
					ContributingEntry entry = (ContributingEntry) mWhoochSelectorAdapter.getItem(i);
					if (entry.whoochId != null) {
						if (entry.whoochId.equals(mSpinnerId)) {
							mWhoochSelector.setSelection(i);
							break;
						}
					}
				}

			}

			mSelectorProgress.setVisibility(View.GONE);
			mSelectorSpinner.setVisibility(View.VISIBLE);
		}
	}

	private class Submit implements WhoochApiCallInterface {

		public void preExecute() {
		}

		public HttpRequestBase getHttpRequest() {

			String whoochId = null;

			if (mUpdateType == "whooch") {
				whoochId = mWhoochIdExtra;
			} else {
				ContributingEntry selectedEntry = (ContributingEntry) mWhoochSelector
						.getSelectedItem();

				whoochId = selectedEntry.whoochId;
			}

			HttpPost request = new HttpPost(Settings.apiUrl + "/whooch/add");

			MultipartEntity reqEntity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);

			try {
				if (mImageBitmap != null) {
					reqEntity.addPart("image", new StringBody("true"));
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					mImageBitmap.compress(CompressFormat.JPEG, 80, bos);

					byte[] data = bos.toByteArray();

					ByteArrayBody bab = new ByteArrayBody(data, mImageName);
					reqEntity.addPart("file", bab);
				}
				reqEntity.addPart("whoochId", new StringBody(whoochId));
				reqEntity.addPart("content", new StringBody(mPostText.getText()
						.toString()));

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
			if (statusCode == 202) {
				if (mUpdateType == "whooch") {
					finish();
				} else {
					Intent i = new Intent(getApplicationContext(),
							StreamActivity.class);
					startActivity(i);
					finish();
				}
			}
			else if(statusCode == 409)
			{
				Toast.makeText(getApplicationContext(), "You already said that",
						Toast.LENGTH_SHORT).show();
			}
			else {
				Toast.makeText(getActivityContext(),
						"Something went wrong, please try again", Toast.LENGTH_LONG)
						.show();
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