package com.whooch.app.helpers;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

import com.urbanairship.push.PushManager;
import com.whooch.app.LoginActivity;
import com.whooch.app.StreamActivity;

public class GCMFunctions {

	public static void setTokenLogin(Activity a, String userId, String userName, String password) {
		WhoochApiCallTask task = new WhoochApiCallTask(a, new SetTokenLogin(a, userId, userName, password), true, true);
		task.execute();
	}

	public static void clearTokenLogout(Activity a) {
		WhoochApiCallTask task = new WhoochApiCallTask(a, new ClearTokenLogout(a),
				true);
		task.execute();
	}
	
	public static void setToken(Activity a) {
		WhoochApiCallTask task = new WhoochApiCallTask(a, new SetToken(a), true);
		task.execute();
	}

	public static void clearToken(Activity a) {
		WhoochApiCallTask task = new WhoochApiCallTask(a, new ClearToken(a),
				true);
		task.execute();
	}

	private static class SetTokenLogin implements WhoochApiCallInterface {

		Activity mActivity = null;
		String mUserId = null;
		String mUsername = null;
		String mPassword = null;

		public void preExecute() {
			
			// verification successful, store username and password in
			// shared prefs, and start the stream activity
			SharedPreferences settings = mActivity.getSharedPreferences(
					"whooch_preferences", 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("username", mUsername);
			mUserId = mUserId.replace("\"", "");
			editor.putString("userid", mUserId);
			editor.putString("password", mPassword);
			editor.commit();
			
		}
		
		SetTokenLogin(Activity a, String userId, String userName, String password)
		{
			mActivity = a;
			mUsername = userName;
			mUserId = userId;
			mPassword = password;
		} 

		public HttpRequestBase getHttpRequest() {
			HttpPost request = new HttpPost(Settings.apiUrl + "/push");

			String apid = PushManager.shared().getAPID();

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("action", "addtoken"));
			nameValuePairs.add(new BasicNameValuePair("token", apid));
			nameValuePairs.add(new BasicNameValuePair("platform", "android"));

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
			
			//take user to stream regardless of success/fail
			Intent i = new Intent(mActivity.getApplicationContext(),
					StreamActivity.class);
			mActivity.startActivity(i);

		}
	}

	private static class ClearTokenLogout implements WhoochApiCallInterface {
		
		Activity mActivity = null;

		public void preExecute() {

		}
		
		ClearTokenLogout(Activity a)
		{
			mActivity = a;
		}

		public HttpRequestBase getHttpRequest() {
			HttpPost request = new HttpPost(Settings.apiUrl + "/push");

			String apid = PushManager.shared().getAPID();

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("action", "removetoken"));
			nameValuePairs.add(new BasicNameValuePair("token", apid));
			nameValuePairs.add(new BasicNameValuePair("platform", "android"));

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
				SharedPreferences settings = mActivity
						.getSharedPreferences("whooch_preferences", 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("username", null);
				editor.putString("userid", null);
				editor.putString("password", null);
				editor.commit();

				Intent i = null;
				i = new Intent(mActivity, LoginActivity.class);
				mActivity.startActivity(i);
			}

		}
	}
	
	
	private static class SetToken implements WhoochApiCallInterface {

		Activity mActivity = null;

		public void preExecute() {

		}
		
		SetToken(Activity a)
		{
			mActivity = a;
		}

		public HttpRequestBase getHttpRequest() {
			HttpPost request = new HttpPost(Settings.apiUrl + "/push");

			String apid = PushManager.shared().getAPID();

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("action", "addtoken"));
			nameValuePairs.add(new BasicNameValuePair("token", apid));
			nameValuePairs.add(new BasicNameValuePair("platform", "android"));

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
			

		}
	}
	
	private static class ClearToken implements WhoochApiCallInterface {
		
		Activity mActivity = null;

		public void preExecute() {

		}
		
		ClearToken(Activity a)
		{
			mActivity = a;
		}

		public HttpRequestBase getHttpRequest() {
			HttpPost request = new HttpPost(Settings.apiUrl + "/push");

			String apid = PushManager.shared().getAPID();

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("action", "removetoken"));
			nameValuePairs.add(new BasicNameValuePair("token", apid));
			nameValuePairs.add(new BasicNameValuePair("platform", "android"));

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


		}
	}

}