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

import android.content.Intent;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.whooch.app.helpers.ActionBarHelper;
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
	private String mUpdateType = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent i = getIntent();
		Bundle b = i.getExtras();

		if (b.getString("UPDATE_TYPE").equals("whooch")) {
			mWhoochIdExtra = b.getString("WHOOCH_ID");
			mWhoochImageExtra = b.getString("WHOOCH_IMAGE");
			mWhoochNameExtra = b.getString("WHOOCH_NAME");
			mUpdateType = "whooch";

			mWhoochSelectorLayout.setVisibility(View.GONE);

	        if ( (mWhoochIdExtra == null) || 
	                (mWhoochImageExtra == null)  || (mWhoochNameExtra == null) ) {
	               Toast.makeText(getApplicationContext(), "Error: bad intent", Toast.LENGTH_SHORT).show();
	               finish();
	               return;
	        }
	        
	        UrlImageViewHelper.setUrlDrawable(mWhoochImage, mWhoochImageExtra);
	        mWhoochName.setText(mWhoochNameExtra);
		} else {
			// add the invalid "No Whooch Selected" entry. This will be rendered
			// by
			// the adapter.
			mUpdateType = "regular";
			mContributingArray.add(new ContributingEntry());

			mWhoochSelectorAdapter = new ContributingArrayAdapter(this,
					android.R.layout.simple_spinner_item, mContributingArray);
			mWhoochSelector.setAdapter(mWhoochSelectorAdapter);
			mWhoochFeedbackLayout.setVisibility(View.GONE);
		}

		ActionBarHelper.setupActionBar(getSupportActionBar(),
				new ActionBarHelper.TabListener(getApplicationContext()), 1);

		mReactingToText.setVisibility(View.GONE);

		mSubmitButton.setText("Update");
		mSubmitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if ((mUpdateType == "regular")
						&& (mWhoochSelector.getSelectedItemPosition() == 0)) {
					// User selected the placeholder item
					Toast.makeText(getActivityContext(),
							"Please select a Whooch", Toast.LENGTH_SHORT)
							.show();
				} else {
					WhoochApiCallTask task = new WhoochApiCallTask(
							getActivityContext(), new Submit(), true);
					task.execute();
				}
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		ActionBarHelper.selectTab(getSupportActionBar(), 1);

		if (mUpdateType == "regular") {
			WhoochApiCallTask task = new WhoochApiCallTask(
					getActivityContext(), new GetContributingList(), false);
			task.execute();
		}
	}

	private class GetContributingList implements WhoochApiCallInterface {

		private String mResponseString = null;

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
		}
	}

	private class Submit implements WhoochApiCallInterface {

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
				// TODO: take user to the whooch they just posted in? Would need
				// to go
				// to the correct activity but also remove the PostStandard
				// activity from
				// the backstack.
				finish();
			}

		}
	}

}