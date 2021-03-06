package com.whooch.app.json;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import com.whooch.app.WhoochProfileActivity;
import com.whooch.app.helpers.Settings;

public class SearchEntry {

	// from JSON
	public String whoochId = null;
	public String whoochName = null;
	public String whoochImage = null;
	public String leaderName = null;
	public String isTrailing = null;

	public String userId = null;
	public String userName = null;
	public String userImage = null;
	public String isFriend = null;

	public String content = null;
	public String reactionType = null;
	public FeedbackEntry feedbackInfo = null;
	public String timestamp = null;
	public String image = null;
	public String isContributor = null;
	public String whoochNumber = null;
	public String reactionTo = null;
	public String fans = null;
	public String isFan = null;
	public String fanString = null;

	// derived attributes
	public String whoochImageUriSmall = null;
	public String whoochImageUriMedium = null;
	public String whoochImageUriLarge = null;

	public String userImageUriSmall = null;
	public String userImageUriMedium = null;
	public String userImageUriLarge = null;

	public String whoochImageUriDefault = null;

	public String userImageUriDefault = null;

	public String searchType = null;

	public SearchEntry() {
	}

	public SearchEntry(JSONObject json, String mSearchType,
			WindowManager windowMgr) {

		searchType = mSearchType;

		try {
			whoochId = json.getString("whoochId");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			whoochName = json.getString("whoochName");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			whoochImage = json.getString("whoochImage");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			leaderName = json.getString("leaderName");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			isTrailing = json.getString("isTrailing");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			userId = json.getString("userId");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			userName = json.getString("userName");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			userImage = json.getString("userImage");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			isFriend = json.getString("isFriend");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			content = json.getString("content");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			reactionType = json.getString("reactionType");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// get feedback info if this post is a reaction
		if (reactionType != null) {
			if (reactionType.equals("feedback")) {
				try {
					feedbackInfo = new FeedbackEntry(
							json.getJSONObject("feedbackInfo"), windowMgr);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		}
		try {
			timestamp = json.getString("timestamp");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			image = json.getString("image");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			reactionTo = json.getString("reactionTo");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			whoochNumber = json.getString("whoochNumber");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			isContributor = json.getString("isContributor");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			fans = json.getString("fans");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			isFan = json.getString("isFan");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (fans != null) {
			try {
				if (Integer.parseInt(fans, 10) > 0) {
					if (Integer.parseInt(fans, 10) == 1) {
						fanString = "(1 fan)";
					} else {
						fanString = "(" + fans + " fans)";
					}
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
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

		// determine the URLs for the whooch image
		if (userImage != null && userId != null) {
			if (userImage.equals("defaultUser.png")) {
				userImageUriSmall = Settings.cdnUrl + "s_" + userImage;
				userImageUriMedium = Settings.cdnUrl + "m_" + userImage;
				userImageUriLarge = Settings.cdnUrl + "l_" + userImage;
			} else {
				userImageUriSmall = Settings.cdnUrl + "u" + userId + "_s"
						+ userImage;
				userImageUriMedium = Settings.cdnUrl + "u" + userId + "_m"
						+ userImage;
				userImageUriLarge = Settings.cdnUrl + "u" + userId + "_l"
						+ userImage;
			}
		}

		// determine proper image to use based on the current screen resolution
		windowMgr.getDefaultDisplay().getMetrics(metrics);
		switch (metrics.densityDpi) {
		case DisplayMetrics.DENSITY_LOW:
			userImageUriDefault = userImageUriSmall;
		case DisplayMetrics.DENSITY_MEDIUM:
			userImageUriDefault = userImageUriMedium;
		case DisplayMetrics.DENSITY_HIGH:
			userImageUriDefault = userImageUriLarge;
		}
	}

	public OnClickListener getWhoochProfileClickListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(v.getContext(),
						WhoochProfileActivity.class);
				v.getContext().startActivity(i);
			}
		};

	}

}