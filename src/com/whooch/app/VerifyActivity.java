package com.whooch.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochHelperFunctions;

public class VerifyActivity extends SherlockActivity {

	private EditText UsernameText;
	private EditText PasswordText;
	private Button mLoginButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.verify);
		
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View title_view = inflater.inflate(R.layout.title_bar_generic, null);
		getSupportActionBar().setCustomView(title_view);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		TextView tvhead = (TextView)title_view.findViewById(R.id.header_generic_title);
		tvhead.setText("Verify your password");

		UsernameText = (EditText) findViewById(R.id.login_username);
		
		SharedPreferences settings = getActivityContext().getSharedPreferences(
				"whooch_preferences", 0);
		String userName = settings.getString("username", null);
		
		if(userName != null)
		{
			UsernameText.setText(userName);
			UsernameText.setEnabled(false);
		}
		else
		{
			//something really went wrong, go to login
			Intent i = null;
			i = new Intent(this, LoginActivity.class);
			startActivity(i);
		}
		
		PasswordText = (EditText) findViewById(R.id.login_password);
		mLoginButton = (Button) findViewById(R.id.login_button);
		mLoginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				VerifyUserTask task = new VerifyUserTask(getActivityContext());
				task.execute();
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

	public class VerifyUserTask extends AsyncTask<Void, Void, Integer> {

		private Context mActivityContext;
		private String mUsername;
		private String mPassword;
		private String mUserId;

		public VerifyUserTask(Context ctx) {
			super();
			mActivityContext = ctx;
			mUsername = UsernameText.getText().toString();
			mPassword = PasswordText.getText().toString();
		}

		@Override
		protected void onPreExecute() {

			ConnectivityManager cm = (ConnectivityManager) mActivityContext
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			if (cm.getActiveNetworkInfo() != null
					&& cm.getActiveNetworkInfo().isConnectedOrConnecting()) {

				View loader = findViewById(R.id.main_loader);
				if (loader != null) {
					loader.setVisibility(View.VISIBLE);
				}
				
				loader = findViewById(R.id.main_action_icons);
				if (loader != null) {
					loader.setVisibility(View.GONE);
				}

			} else {

				this.cancel(true);

				Toast.makeText(mActivityContext,
						"A connection to the server is not available",
						Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected Integer doInBackground(Void... params) {

			// prepare the request
			HttpPost postRequest = new HttpPost(Settings.apiUrl
					+ "/user/verify");
			postRequest.setHeader("Authorization",
					WhoochHelperFunctions.getB64Auth(mUsername, mPassword));

			// make the request
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = null;
			try {
				response = client.execute(postRequest);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// get the response
			int statusCode = -1;
			if (response != null) {
				statusCode = response.getStatusLine().getStatusCode();
			}

			InputStream content = null;
			mUserId = null;

			try {
				if (response.getEntity() != null) {
					content = response.getEntity().getContent();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(content));

					mUserId = reader.readLine();
				} else {
					mUserId = null;
				}
			} catch (IllegalStateException e) {
				mUserId = null;
			} catch (IOException e) {
				mUserId = null;
			}

			return statusCode;
		}

		@Override
		protected void onPostExecute(Integer result) {

			View loader = findViewById(R.id.main_loader);
			if (loader != null) {
				loader.setVisibility(View.GONE);
			}
			loader = findViewById(R.id.main_action_icons);
			if (loader != null) {
				loader.setVisibility(View.VISIBLE);
			}

			if ((mUserId != null) && (mUserId.length() > 0)) {

				if (result == 200) {

					// verification successful, store username and password in
					// shared prefs, and start the stream activity
					SharedPreferences settings = getSharedPreferences(
							"whooch_preferences", 0);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString("password", mPassword);
					editor.commit();
					
					Intent i = new Intent(getApplicationContext(),
							StreamActivity.class);
					startActivity(i);

				} else if (result == 401) {
					Toast.makeText(getApplicationContext(),
							"Incorrect password", Toast.LENGTH_LONG)
							.show();
				} else {
					Toast.makeText(getApplicationContext(),
							"Incorrect password", Toast.LENGTH_LONG)
							.show();
				}
			} else {
				Toast.makeText(getApplicationContext(),
						"Incorrect password", Toast.LENGTH_LONG)
						.show();
			}
		}
	}
}