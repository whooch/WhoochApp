package com.whooch.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.whooch.app.helpers.GCMFunctions;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochHelperFunctions;

public class LoginActivity extends SherlockActivity {

	private EditText UsernameText;
	private EditText PasswordText;
	private EditText ActivationText;
	private Button mLoginButton;
	private Button mActivationButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		UsernameText = (EditText) findViewById(R.id.login_username);
		PasswordText = (EditText) findViewById(R.id.login_password);
		ActivationText = (EditText) findViewById(R.id.activation_code);
		mLoginButton = (Button) findViewById(R.id.login_button);
		mActivationButton = (Button) findViewById(R.id.activation_button);

		mLoginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				VerifyUserTask task = new VerifyUserTask(getActivityContext());
				task.execute();
			}
		});

		mActivationButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				VerifyActivationTask task = new VerifyActivationTask(
						getActivityContext());
				task.execute();
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();

		// if the user is already logged in, take them directly to the stream
		SharedPreferences settings = getSharedPreferences("whooch_preferences",
				0);
		String username = settings.getString("username", null);
		String userid = settings.getString("userid", null);
		String password = settings.getString("password", null);

		if ((username != null) && (userid != null) && (password != null)) {
			Intent i = new Intent(getApplicationContext(), StreamActivity.class);
			startActivity(i);
		}

	}

	private Context getActivityContext() {
		return this;
	}

	public class VerifyUserTask extends AsyncTask<Void, Void, Integer> {

		private ProgressDialog mProgressDialog;
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

				this.mProgressDialog = ProgressDialog.show(mActivityContext,
						null, "loading", true);

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

			this.mProgressDialog.cancel();

			if ((mUserId != null) && (mUserId.length() > 0)) {

				if (result == 200) {
					
					GCMFunctions.setToken((Activity) getActivityContext(), mUserId, mUsername, mPassword);


				} else if (result == 401) {
					Toast.makeText(getApplicationContext(),
							"Incorrect username or password", Toast.LENGTH_LONG)
							.show();
				} else {
					Toast.makeText(getApplicationContext(),
							"Incorrect username or password", Toast.LENGTH_LONG)
							.show();
				}
			} else {
				Toast.makeText(getApplicationContext(),
						"Incorrect username or password", Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	public class VerifyActivationTask extends AsyncTask<Void, Void, Integer> {

		private ProgressDialog mProgressDialog;
		private Context mActivityContext;
		private String mActivationCode;
		private String mActivationResponse;

		public VerifyActivationTask(Context ctx) {
			super();
			mActivityContext = ctx;
			mActivationCode = ActivationText.getText().toString();
		}

		@Override
		protected void onPreExecute() {

			ConnectivityManager cm = (ConnectivityManager) mActivityContext
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			if (cm.getActiveNetworkInfo() != null
					&& cm.getActiveNetworkInfo().isConnectedOrConnecting()) {

				this.mProgressDialog = ProgressDialog.show(mActivityContext,
						null, "loading", true);

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
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

			HttpPost postRequest = new HttpPost(Settings.apiUrl
					+ "/user/activation");

			// Add data
			nameValuePairs.add(new BasicNameValuePair("activationCode",
					mActivationCode));

			try {
				postRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				// TODO error handling
			}

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
			mActivationResponse = null;

			try {
				if (response.getEntity() != null) {
					content = response.getEntity().getContent();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(content));

					mActivationResponse = reader.readLine();
				} else {
					mActivationResponse = null;
				}
			} catch (IllegalStateException e) {
				mActivationResponse = null;
			} catch (IOException e) {
				mActivationResponse = null;
			}

			return statusCode;
		}

		@Override
		protected void onPostExecute(Integer result) {

			this.mProgressDialog.cancel();

			mActivationResponse = mActivationResponse.replace("\"", "");
			if (mActivationResponse.equals("true")) {

				Intent i = new Intent(getApplicationContext(),
						RegisterActivity.class);
				i.putExtra("activation_code", mActivationCode);
				startActivity(i);

			} else {
				Toast.makeText(getApplicationContext(),
						"Activation code not valid", Toast.LENGTH_LONG).show();
			}
		}
	}
}