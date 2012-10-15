package com.whooch.app.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.whooch.app.R;
import com.whooch.app.json.UserProfileEntry;

public class UserProfileArrayAdapter extends ArrayAdapter<UserProfileEntry> {

	private Context mContext;
	private ArrayList<UserProfileEntry> mData = null;
	private LayoutInflater mInflater;
	private String mProfileType = null;

	public UserProfileArrayAdapter(Context context,
			ArrayList<UserProfileEntry> data, String profileType) {
		super(context, 0, data);
		mContext = context;
		mData = data;
		mInflater = ((Activity) mContext).getLayoutInflater();
		mProfileType = profileType;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view = convertView;
		if (view == null) {
			if(mProfileType == "local")
			{
				view = mInflater
						.inflate(R.layout.user_profile_entry, parent, false);
			}
			else
			{
				view = mInflater
						.inflate(R.layout.user_profile_entry_foreign, parent, false);
			}
		}

		UserProfileEntry userProfileEntry = mData.get(position);

		ImageView iv1 = (ImageView) view.findViewById(R.id.profile_userimage);
		UrlImageViewHelper.setUrlDrawable(iv1,
				userProfileEntry.userImageUriLarge);

		TextView tv1 = (TextView) view.findViewById(R.id.profile_username);
		tv1.setText(userProfileEntry.userName);

		TextView tv2 = (TextView) view.findViewById(R.id.profile_location);
		tv2.setText(userProfileEntry.location);

		TextView tv3 = (TextView) view.findViewById(R.id.profile_name);
		tv3.setText(userProfileEntry.firstName + " "
				+ userProfileEntry.lastName);

		TextView tv4 = (TextView) view.findViewById(R.id.profile_bio);
		tv4.setText(userProfileEntry.bio);

		if (mProfileType == "local") {

			Button ibtn1 = (Button) view
					.findViewById(R.id.profile_pushsettings);
			ibtn1.setOnClickListener(userProfileEntry
					.getPushSettingsClickListener());

			Button ibtn2 = (Button) view.findViewById(R.id.profile_updatephoto);
			ibtn2.setOnClickListener(userProfileEntry
					.getUpdatePhotoClickListener());

			Button ibtn3 = (Button) view.findViewById(R.id.profile_signout);
			ibtn3.setOnClickListener(userProfileEntry.getSignOutClickListener());
			
			Button ibtn4 = (Button) view.findViewById(R.id.profile_alerts);
			ibtn4.setOnClickListener(userProfileEntry.getAlertsClickListener());

		}
		else
		{
			if(userProfileEntry.isFriend.equals("0"))
			{
				Button ibtn1 = (Button) view
						.findViewById(R.id.profile_friendrequest);
				ibtn1.setVisibility(View.VISIBLE);
				ibtn1.setOnClickListener(userProfileEntry
						.getFriendRequestClickListener());
				
			}
		}

		return view;
	}
	
}