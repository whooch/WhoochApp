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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.whooch.app.R;

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
		if (mShowProgressDialog) {
			Activity a = (Activity) mActivityContext;
			View loader = a.findViewById(R.id.main_loader);
			if (loader != null) {
				loader.setVisibility(View.VISIBLE);
			}
		}
		
		mWhoochApiCall.preExecute();
		
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

		if (mShowProgressDialog) {
			Activity a = (Activity) mActivityContext;
			View loader = a.findViewById(R.id.main_loader);
			if (loader != null) {
				loader.setVisibility(View.GONE);
			}
		}
		
		// execute the post execute method for this task
		mWhoochApiCall.postExecute(statusCode);

		// do error handling
		if (statusCode == 407 || statusCode == 400) {
			Toast.makeText(mActivityContext,
					"bad user credentials, please log in again",
					Toast.LENGTH_LONG).show();
			// TODO: force user back to log in screen
		} else if (statusCode == -1) {
			Toast.makeText(mActivityContext, "Error 37-1", Toast.LENGTH_LONG)
					.show();
		} else if (statusCode == -2) {
			Toast.makeText(mActivityContext, "Error 37-2", Toast.LENGTH_LONG)
					.show();
		} else if (statusCode == -3) {
			Toast.makeText(mActivityContext, "Error 37-3", Toast.LENGTH_LONG)
					.show();
		} else if (statusCode == -4) {
			Toast.makeText(mActivityContext, "Error 37-4", Toast.LENGTH_LONG)
					.show();
		}
	}
}