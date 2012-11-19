package com.whooch.app.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.whooch.app.R;
import com.whooch.app.StreamActivity;
import com.whooch.app.VerifyActivity;

public class WhoochApiCallTask extends AsyncTask<Void, Void, Integer> {

	private Context mActivityContext;
	private WhoochApiCallInterface mWhoochApiCall;
	private boolean mShowProgressDialog;

	public WhoochApiCallTask(Context ctx, WhoochApiCallInterface whoochApiCall,
			boolean showProgressDialog) {
		super();
		mActivityContext = ctx;
		mWhoochApiCall = whoochApiCall;
		mShowProgressDialog = showProgressDialog;
	}

	@Override
	protected void onPreExecute() {
		
		Activity a = (Activity) mActivityContext;
		
		if(WhoochHelperFunctions.getScreenOrientation(a) == Configuration.ORIENTATION_PORTRAIT)
		{
			a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		else if(WhoochHelperFunctions.getScreenOrientation(a) == Configuration.ORIENTATION_LANDSCAPE)
		{
			a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		
		if (mShowProgressDialog) {
			View loader = a.findViewById(R.id.main_loader);
			if (loader != null) {
				loader.setVisibility(View.VISIBLE);
			}
			
			loader = a.findViewById(R.id.main_action_icons);
			if (loader != null) {
				loader.setVisibility(View.GONE);
			}
		}

		ConnectivityManager cm = (ConnectivityManager) mActivityContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (cm.getActiveNetworkInfo() != null
				&& cm.getActiveNetworkInfo().isConnectedOrConnecting()) {
			mWhoochApiCall.preExecute();
		} else {
			this.cancel(true);
			
			
			TextView tvE1 = (TextView) a.findViewById(R.id.empty_text1);
			TextView tvE2 = (TextView) a.findViewById(R.id.empty_text2);
			if((tvE1 != null) && (tvE2 != null))
			{
			tvE1.setText("A connection to the server could not be made at this time.");
			tvE2.setText("");
			}
			
			
			if (mShowProgressDialog) {
				View loader = a.findViewById(R.id.main_loader);
				if (loader != null) {
					loader.setVisibility(View.GONE);
				}
				loader = a.findViewById(R.id.main_action_icons);
				if (loader != null) {
					loader.setVisibility(View.VISIBLE);
				}
			}
			
			Toast.makeText(mActivityContext,
					"A connection to the server is not available",
					Toast.LENGTH_LONG).show();
		}

	}

	@Override
	protected Integer doInBackground(Void... params) {

		// get the http request and add authentication headers
		HttpRequestBase httpRequest = mWhoochApiCall.getHttpRequest();

		// TESTING
		Log.d("WhoochApiCallTask", httpRequest.getURI().toString());

		SharedPreferences settings = mActivityContext.getSharedPreferences(
				"whooch_preferences", 0);
		String username = settings.getString("username", null);
		String password = settings.getString("password", null);
		httpRequest.setHeader("Authorization",
				WhoochHelperFunctions.getB64Auth(username, password));

		// make the request
		HttpClient client = new DefaultHttpClient();
		HttpResponse response = null;
		try {
			response = client.execute(httpRequest);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// get the response
		int statusCode = -1;
		String responseString = "";
		if (response != null) {
			statusCode = response.getStatusLine().getStatusCode();

			if (statusCode >= 200 && statusCode < 300 || statusCode == 304) {
				InputStream content = null;
				StringBuilder builder = new StringBuilder();
				try {
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						content = response.getEntity().getContent();
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(content));
						String line;
						while ((line = reader.readLine()) != null) {
							builder.append(line);
						}
					}
				} catch (IllegalStateException e) {
					e.printStackTrace();
					statusCode = -2;
					return statusCode;
				} catch (IOException e) {
					e.printStackTrace();
					statusCode = -3;
					return statusCode;
				}

				responseString = builder.toString();

				// handle the response
				mWhoochApiCall.handleResponse(responseString);
			}
		}

		return statusCode;
	}

	@Override
	protected void onPostExecute(Integer statusCode) {
		
		Activity a = (Activity) mActivityContext;
		
		a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

		if (mShowProgressDialog) {
			View loader = a.findViewById(R.id.main_loader);
			if (loader != null) {
				loader.setVisibility(View.GONE);
			}
			loader = a.findViewById(R.id.main_action_icons);
			if (loader != null) {
				loader.setVisibility(View.VISIBLE);
			}
		}

		// do error handling
		if (statusCode == 407) {

			Intent i = null;
			i = new Intent(a, VerifyActivity.class);
			a.startActivity(i);

		}
		else if(statusCode == 401)
		{
			Intent i = null;
			i = new Intent(a, StreamActivity.class);
			a.startActivity(i);
			
			Toast.makeText(mActivityContext,
					"You no longer have access to this whooch",
					Toast.LENGTH_LONG).show();
		}
		else if((statusCode == 200) || (statusCode == 201) || (statusCode == 202))
		{
			// execute the post execute method for this task
			mWhoochApiCall.postExecute(statusCode);
		}
		else {
			Toast.makeText(mActivityContext,
					"Something went wrong, please try again", Toast.LENGTH_SHORT)
					.show();
		}
	}

}