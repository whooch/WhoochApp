package com.whooch.app.json;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.whooch.app.InviteUserActivity;
import com.whooch.app.R;
import com.whooch.app.UploadPhotoActivity;
import com.whooch.app.UserProfileActivity;
import com.whooch.app.WhoochFeedbackActivity;
import com.whooch.app.WhoochSettingsActivity;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;
import com.whooch.app.ui.FriendsArrayAdapter;

public class WhoochProfileEntry {

	private static final int UPLOAD_PHOTO_WHOOCH = 2;

	// from JSON
	public String whoochName = null;
	public String type = null;
	public String description = null;
	public String trailingCount = null;
	public String whoochImage = null;
	public String rating = null;
	public String whoochId = null;
	public String contributingList = null;
	public String trailingList = null;
	public String leaderName = null;
	public String leaderImage = null;
	public String leaderId = null;
	public String isContributing = null;
	public String isTrailing = null;
	public String isStreaming = null;
	public String updateAlerts = null;
	public String feedbackAlerts = null;
	public String updatePush = null;
	public String feedbackPush = null;
	public String didUserRate = null;

	// derived attributes
	public String whoochImageUriSmall = null;
	public String whoochImageUriMedium = null;
	public String whoochImageUriLarge = null;

	public String whoochImageUriDefault = null;

	public String leaderImageUriDefault = null;
	
	private ArrayList<FriendsEntry> mContributorsArray = new ArrayList<FriendsEntry>();

	public WhoochProfileEntry() {
	}

	public WhoochProfileEntry(JSONObject json, WindowManager windowMgr) {
		try {
			whoochName = json.getString("whoochName");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			type = json.getString("type");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			description = json.getString("description");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			trailingCount = json.getString("trailingCount");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			whoochImage = json.getString("whoochImage");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			rating = json.getString("rating");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			whoochId = json.getString("whoochId");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			leaderName = json.getJSONObject("leader").getString("userName");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			leaderId = json.getJSONObject("leader").getString("userId");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			leaderImage = json.getJSONObject("leader").getString("userImage");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			isContributing = json.getString("isContributing");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			isTrailing = json.getString("isTrailing");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			updatePush = json.getString("updatePush");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			feedbackPush = json.getString("feedbackPush");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			didUserRate = json.getString("didUserRate");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			contributingList = json.getString("contributingList");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// determine the URLs for the whooch image
		if (whoochImage != null && whoochId != null) {
			if (whoochImage.equals("defaultWhooch.png")) {
				whoochImageUriSmall = Settings.cdnUrl + "s_" + whoochImage;
				whoochImageUriMedium = Settings.cdnUrl + "m_" + whoochImage;
				whoochImageUriLarge = Settings.cdnUrl + "l_" + whoochImage;
			} else {
				whoochImageUriSmall = Settings.cdnUrl + "w" + whoochId + "_s"
						+ whoochImage;
				whoochImageUriMedium = Settings.cdnUrl + "w" + whoochId + "_m"
						+ whoochImage;
				whoochImageUriLarge = Settings.cdnUrl + "w" + whoochId + "_l"
						+ whoochImage;
			}
		}

		// determine proper image to use based on the current screen resolution
		DisplayMetrics metrics = new DisplayMetrics();
		windowMgr.getDefaultDisplay().getMetrics(metrics);
		switch (metrics.densityDpi) {
		case DisplayMetrics.DENSITY_LOW:
			whoochImageUriDefault = whoochImageUriSmall;
		case DisplayMetrics.DENSITY_MEDIUM:
			whoochImageUriDefault = whoochImageUriMedium;
		case DisplayMetrics.DENSITY_HIGH:
			whoochImageUriDefault = whoochImageUriLarge;
		}

		if (leaderImage.equals("defaultUser.png")) {
			leaderImageUriDefault = Settings.cdnUrl + "s_" + leaderImage;
		} else {
			leaderImageUriDefault = Settings.cdnUrl + "u" + leaderId + "_s"
					+ leaderImage;
		}

		Log.e("test", leaderImageUriDefault);
	}

	public OnClickListener getInviteUserClickListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(v.getContext(), InviteUserActivity.class);
				i.putExtra("WHOOCH_ID", whoochId);
				i.putExtra("WHOOCH_NAME", whoochName);
				i.putExtra("WHOOCH_IMAGE", whoochImageUriLarge);
				i.putExtra("WHOOCH_TYPE", type);
				i.putExtra("WHOOCH_LEADER", leaderName);
				v.getContext().startActivity(i);
			}
		};
	}

	public OnClickListener getWhoochSettingsClickListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(v.getContext(),
						WhoochSettingsActivity.class);
				i.putExtra("WHOOCH_ID", whoochId);
				i.putExtra("WHOOCH_NAME", whoochName);
				if (isContributing.equals("1")) {
					i.putExtra("FEEDBACK_PUSH", feedbackPush);
					i.putExtra("FEEDBACK", true);
				} else {
					i.putExtra("FEEDBACK", false);
				}

				i.putExtra("UPDATE_PUSH", updatePush);

				v.getContext().startActivity(i);
			}
		};
	}

	public OnClickListener getUpdatePhotoClickListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(v.getContext(), UploadPhotoActivity.class);
				i.putExtra("UPLOAD_TYPE", UPLOAD_PHOTO_WHOOCH);
				i.putExtra("WHOOCH_ID", whoochId);
				i.putExtra("WHOOCH_NAME", whoochName);
				i.putExtra("WHOOCH_LEADER", leaderName);
				i.putExtra("WHOOCH_IMAGE", whoochImageUriMedium);
				v.getContext().startActivity(i);
			}
		};
	}

	public OnClickListener getTrailWhoochClickListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				WhoochApiCallTask task = new WhoochApiCallTask(v.getContext(),
						new TrailWhooch(whoochId, "start", v.getContext()),
						true);
				task.execute();
			}
		};
	}

	public OnClickListener getStopTrailWhoochClickListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				WhoochApiCallTask task = new WhoochApiCallTask(v.getContext(),
						new TrailWhooch(whoochId, "stop", v.getContext()), true);
				task.execute();
			}
		};
	}

	public OnClickListener getViewWhoochFeedbackClickListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(v.getContext(),
						WhoochFeedbackActivity.class);
				i.putExtra("WHOOCH_ID", whoochId);
				i.putExtra("WHOOCH_NAME", whoochName);
				i.putExtra("WHOOCH_LEADER", leaderName);
				i.putExtra("WHOOCH_IMAGE", whoochImageUriMedium);
				v.getContext().startActivity(i);
			}
		};
	}

	public OnClickListener getShowContributorsClickListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				final Context context = v.getContext();
				
				AlertDialog.Builder builder = new AlertDialog.Builder(
						v.getContext());

				builder.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
							}
						});

				LayoutInflater inflater = (LayoutInflater) v.getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				ArrayAdapter<FriendsEntry> contributorsAdapter 	= new FriendsArrayAdapter(v.getContext(),
						android.R.layout.simple_spinner_item, mContributorsArray);
				
				View view = inflater.inflate(R.layout.contributor_list, null);
				
				ListView lv1 = (ListView)view.findViewById(android.R.id.list);
				lv1.setAdapter(contributorsAdapter);
				
				lv1.setOnItemClickListener(new OnItemClickListener(){

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						// TODO Auto-generated method stub

						
						
						Intent i = new Intent(context,
								UserProfileActivity.class);
						i.putExtra("USER_ID", mContributorsArray.get(position).userId);
						i.putExtra("FORCE_FOREIGN", "true");
						context.startActivity(i);
						
					}
					
				});
		

				Activity a = (Activity) v.getContext();
				JSONArray jsonArray;
				try {
					mContributorsArray.clear();
					jsonArray = new JSONArray(contributingList);
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						FriendsEntry entry = new FriendsEntry(jsonObject,
								a.getWindowManager());
						mContributorsArray.add(entry);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				

				View viewHeading = inflater.inflate(R.layout.whooch_heading, null);
				
				ImageView ivh = (ImageView)viewHeading.findViewById(R.id.heading_whooch_image);
				UrlImageViewHelper.setUrlDrawable(ivh,
						whoochImageUriMedium);
				
				TextView tvh = (TextView)viewHeading.findViewById(R.id.heading_whooch_name);
				tvh.setText("Contributors");

				builder.setCustomTitle(viewHeading);

				builder.setView(view);

				AlertDialog dialog = builder.create();

				dialog.show();
			}
		};
	}

	public OnClickListener getRateWhoochClickListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {

				final Context context = v.getContext();
				AlertDialog.Builder builder = new AlertDialog.Builder(
						v.getContext());

				builder.setTitle("Rate whooch");
				builder.setMessage("Do you support this whooch?");

				builder.setNegativeButton("No",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								WhoochApiCallTask task = new WhoochApiCallTask(
										context, new RateWhooch(whoochId, "0",
												context), true);
								task.execute();
							}
						});

				builder.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								WhoochApiCallTask task = new WhoochApiCallTask(
										context, new RateWhooch(whoochId, "1",
												context), true);
								task.execute();
							}
						});

				AlertDialog dialog = builder.create();

				dialog.show();
			}
		};
	}

	private class TrailWhooch implements WhoochApiCallInterface {

		private String mWhoochId = null;
		private String mResponseString = null;
		private Context mContext = null;
		private String mAction = null;

		public void preExecute() {
		}

		public TrailWhooch(String whoochId, String action, Context context) {
			mWhoochId = whoochId;
			mContext = context;
			mAction = action;
		}

		public HttpRequestBase getHttpRequest() {

			HttpPost request = new HttpPost(Settings.apiUrl + "/whooch/"
					+ mAction + "trailingopen");

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("whoochId", mWhoochId));

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

			if (statusCode == 200) {
				// parse the response as JSON and update the Content Array
				if ((mResponseString != null)
						&& (!mResponseString.equals("null"))) {
					try {
						JSONObject jsonObject = new JSONObject(mResponseString);
						String trailStatus = jsonObject
								.getString("trailingStatus");

						if (trailStatus != null) {
							Activity a = (Activity) mContext;
							Intent intent = a.getIntent();
							a.finish();
							a.startActivity(intent);
						} else {
							Toast.makeText(mContext,
									"Something went wrong, please try again",
									Toast.LENGTH_SHORT).show();
						}

					} catch (JSONException e) {
						e.printStackTrace();
						// TODO: error handling
					}
				} else {
					Toast.makeText(mContext, "Something went wrong, please try again",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(mContext, "Something went wrong, please try again",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class RateWhooch implements WhoochApiCallInterface {

		private String mWhoochId = null;
		private String mResponseString = null;
		private Context mContext = null;
		private String mRecommend = null;

		public void preExecute() {
		}

		public RateWhooch(String whoochId, String recommend, Context context) {
			mWhoochId = whoochId;
			mContext = context;
			mRecommend = recommend;
		}

		public HttpRequestBase getHttpRequest() {

			HttpPost request = new HttpPost(Settings.apiUrl
					+ "/whooch/recommend");

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("whoochId", mWhoochId));
			nameValuePairs.add(new BasicNameValuePair("recommend", mRecommend));

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

			if (statusCode == 200) {
				// parse the response as JSON and update the Content Array
				if ((mResponseString != null)
						&& (!mResponseString.equals("null"))) {
					try {
						JSONObject jsonObject = new JSONObject(mResponseString);
						String rating = jsonObject.getString("recommend");

						if (rating != null) {
							Activity a = (Activity) mContext;
							Intent intent = a.getIntent();
							a.finish();
							a.startActivity(intent);
						} else {
							Toast.makeText(mContext,
									"Something went wrong, please try again",
									Toast.LENGTH_SHORT).show();
						}

					} catch (JSONException e) {
						e.printStackTrace();
						// TODO: error handling
					}
				} else {
					Toast.makeText(mContext, "Something went wrong, please try again",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(mContext, "Something went wrong, please try again",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

}