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
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochHelperFunctions;

public class RegisterActivity extends SherlockActivity {

	private EditText UsernameText;
	private EditText FirstnameText;
	private EditText LastnameText;
	private EditText EmailText;
	private EditText EmailAgainText;
	private EditText PasswordText;
	private EditText PasswordAgainText;
	private Button mRegisterButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);

		UsernameText = (EditText) findViewById(R.id.reg_username);
		FirstnameText = (EditText) findViewById(R.id.reg_firstname);
		LastnameText = (EditText) findViewById(R.id.reg_lastname);
		EmailText = (EditText) findViewById(R.id.reg_email);
		EmailAgainText = (EditText) findViewById(R.id.reg_emailagain);
		PasswordText = (EditText) findViewById(R.id.reg_password);
		PasswordAgainText = (EditText) findViewById(R.id.reg_passwordagain);
		mRegisterButton = (Button) findViewById(R.id.register_button);

		mRegisterButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				VerifyRegistrationTask task = new VerifyRegistrationTask(getActivityContext());
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
		
		if ( (username != null) && (userid != null) && (password != null) ) { Intent i = new
		Intent(getApplicationContext(), StreamActivity.class);
		startActivity(i); }
		 
	}

	private Context getActivityContext() {
		return this;
	}

	public class VerifyRegistrationTask extends AsyncTask<Void, Void, Integer> {

		private ProgressDialog mProgressDialog;
		private Context mActivityContext;
		private String mUsername;
		private String mFirstname;
		private String mLastname;
		private String mEmail;
		private String mEmailAgain;
		private String mPassword;
		private String mPasswordAgain;
		private String mActivationCode;
		private String mRegistrationResponse;

		public VerifyRegistrationTask(Context ctx) {
			super();
			mActivityContext = ctx;
			mUsername = UsernameText.getText().toString();
			mFirstname = FirstnameText.getText().toString();
			mLastname = LastnameText.getText().toString();
			mEmail = EmailText.getText().toString();
			mEmailAgain = EmailAgainText.getText().toString();
			mPassword = PasswordText.getText().toString();
			mPasswordAgain = PasswordAgainText.getText().toString();
		}

		@Override
		protected void onPreExecute() {
			
			ConnectivityManager cm = (ConnectivityManager) mActivityContext
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			if (cm.getActiveNetworkInfo() != null
					&& cm.getActiveNetworkInfo().isConnectedOrConnecting()) {
				
				this.mProgressDialog = ProgressDialog.show(mActivityContext, null,
						"loading", true);
				
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

			// prepare the request
			HttpPost postRequest = new HttpPost(Settings.apiUrl
					+ "/user");

	        Intent i = getIntent();
	        Bundle b = i.getExtras();
	        mActivationCode = b.getString("activation_code");
	        
            // Add data
	        nameValuePairs.add(new BasicNameValuePair("action", "register"));
	        nameValuePairs.add(new BasicNameValuePair("username", mUsername));
	        nameValuePairs.add(new BasicNameValuePair("firstname", mFirstname));
	        nameValuePairs.add(new BasicNameValuePair("lastname", mLastname));
	        nameValuePairs.add(new BasicNameValuePair("email", mEmail));
            nameValuePairs.add(new BasicNameValuePair("emailAgain", mEmailAgain));
            nameValuePairs.add(new BasicNameValuePair("password", mPassword));
            nameValuePairs.add(new BasicNameValuePair("passwordAgain", mPasswordAgain));
            nameValuePairs.add(new BasicNameValuePair("activationCode", mActivationCode));
            
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
			mRegistrationResponse = null;

			try {
				if (response.getEntity() != null) {
					content = response.getEntity().getContent();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(content));
					
					mRegistrationResponse = reader.readLine();
				}
				else
				{
					mRegistrationResponse = null;
				}
			} catch (IllegalStateException e) {
				mRegistrationResponse = null;
			} catch (IOException e) {
				mRegistrationResponse = null;
			}

			return statusCode;
		}

		@Override
		protected void onPostExecute(Integer result) {

			this.mProgressDialog.cancel();

			mRegistrationResponse = mRegistrationResponse.replace("\"", "");
			if (mRegistrationResponse.equals("true")) {

				VerifyUserTask task = new VerifyUserTask(getActivityContext(), mUsername, mPassword);
				task.execute();
				
			} else {
				Toast.makeText(getApplicationContext(),
						mRegistrationResponse, Toast.LENGTH_LONG)
						.show();
			}
		}
	}
	
	public class VerifyUserTask extends AsyncTask<Void, Void, Integer> {

		private ProgressDialog mProgressDialog;
		private Context mActivityContext;
		private String mUsername;
		private String mPassword;
		private String mUserId;

		public VerifyUserTask(Context ctx, String username, String password) {
			super();
			mActivityContext = ctx;
			mUsername = username;
			mPassword = password;
		}

		@Override
		protected void onPreExecute() {
			
			this.mProgressDialog = ProgressDialog.show(mActivityContext, null,
					"loading", true);
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
				}
				else
				{
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
					// verification successful, store username and password in
					// shared prefs, and start the stream activity
					SharedPreferences settings = getSharedPreferences(
							"whooch_preferences", 0);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString("username", mUsername);
					mUserId = mUserId.replace("\"", "");
					editor.putString("userid", mUserId);
					editor.putString("password", mPassword);
					editor.commit();

					Intent i = new Intent(getApplicationContext(),
							StreamActivity.class);
					startActivity(i);
				} else if (result == 401) {
					Toast.makeText(getApplicationContext(),
							"Registration failed", Toast.LENGTH_LONG)
							.show();
				} else {
					Toast.makeText(getApplicationContext(),
							"Registration failed", Toast.LENGTH_LONG)
							.show();
				}
			} else {
				Toast.makeText(getApplicationContext(),
						"Registration failed", Toast.LENGTH_LONG)
						.show();
			}
		}
	}
}