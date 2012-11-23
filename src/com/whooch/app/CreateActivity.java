package com.whooch.app;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;

public class CreateActivity extends SherlockActivity {

	private EditText mWhoochNameText;
	private Spinner mSpinner;
	private Button mSubmitButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create);

		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View title_view = inflater.inflate(R.layout.title_bar_generic, null);
		getSupportActionBar().setCustomView(title_view);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		TextView tvhead = (TextView) title_view
				.findViewById(R.id.header_generic_title);
		tvhead.setText("Create Whooch");

		mWhoochNameText = (EditText) findViewById(R.id.create_whooch_name);
		mSpinner = (Spinner) findViewById(R.id.create_spinner);

		mSubmitButton = (Button) findViewById(R.id.create_button);
		mSubmitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mWhoochNameText.getText().toString().trim().length() <= 0) {
					Toast.makeText(getActivityContext(),
							"Please provide a name for your whooch",
							Toast.LENGTH_SHORT).show();
				} else {
					WhoochApiCallTask task = new WhoochApiCallTask(
							getActivityContext(), new CreateWhooch(), true, true);
					task.execute();
				}
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

	}

	private Context getActivityContext() {
		return this;
	}

	private class CreateWhooch implements WhoochApiCallInterface {

		private String mResponseString = null;

		public void preExecute() {
		}

		public HttpRequestBase getHttpRequest() {

			HttpPost request = new HttpPost(Settings.apiUrl + "/whooch");

			// Add data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("action", "add"));
			nameValuePairs.add(new BasicNameValuePair("whoochName",
					mWhoochNameText.getText().toString()));
			nameValuePairs.add(new BasicNameValuePair("type", mSpinner
					.getSelectedItem().toString().toLowerCase()));

			try {
				request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				// TODO error handling
			}

			return request;
		}

		public void handleResponse(String responseString) {
			mResponseString = responseString;
		}

		public void postExecute(int statusCode) {

			if (statusCode == 404) {
				Toast.makeText(getActivityContext(),
						"You already have a whooch by that name",
						Toast.LENGTH_LONG).show();
				return;
			} else if (statusCode == 409) {
				Toast.makeText(
						getActivityContext(),
						"Whooch names can only contain letters, numbers, and whitespace",
						Toast.LENGTH_LONG).show();
				return;
			}
			else if (statusCode == 400){
				Toast.makeText(getActivityContext(),
						"Something went wrong, please try again", Toast.LENGTH_LONG)
						.show();
			}

			if (!mResponseString.equals("null")) {
				String createdWhoochId = null;
				try {
					JSONObject jsonObject = new JSONObject(mResponseString);
					createdWhoochId = jsonObject.getString("whoochId");
				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(
							getActivityContext(),
							"Something went wrong, please try again",
							Toast.LENGTH_LONG).show();
				}

				if (createdWhoochId != null) {
					Intent i = new Intent(getApplicationContext(),
							WhoochActivity.class);
					i.putExtra("WHOOCH_ID", createdWhoochId);
					startActivity(i);
				} else {
					Toast.makeText(
							getActivityContext(),
							"Something went wrong, please try again",
							Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(
						getActivityContext(),
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