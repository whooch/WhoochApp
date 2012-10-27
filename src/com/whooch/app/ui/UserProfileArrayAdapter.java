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
	private String mProfileType;

	public UserProfileArrayAdapter(Context context, String profileType,
			ArrayList<UserProfileEntry> data) {
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
			view = mInflater
					.inflate(R.layout.user_profile_entry, parent, false);
		}

		UserProfileEntry entry = mData.get(position);

		ImageView iv1 = (ImageView) view.findViewById(R.id.profile_userimage);
		UrlImageViewHelper.setUrlDrawable(iv1, entry.userImageUriLarge);

		TextView tv1 = (TextView) view.findViewById(R.id.profile_username);
		tv1.setText(entry.userName);

		TextView tv2 = (TextView) view.findViewById(R.id.profile_location);
		if (entry.location.length() > 0) {
			tv2.setText(entry.location);
		} else {
			tv2.setText("No location provided");
		}

		TextView tv3 = (TextView) view.findViewById(R.id.profile_name);

		if ((entry.firstName.length() > 0) || (entry.lastName.length() > 0)) {
			tv3.setText(entry.firstName + " " + entry.lastName);
		} else {
			tv3.setText("No name provided");
		}

		TextView tv4 = (TextView) view.findViewById(R.id.profile_bio);
		tv4.setText(entry.bio);

		if (entry.bio.length() > 0) {
			tv4.setText(entry.bio);
		} else {
			tv4.setText("No information provided");
		}

		if (mProfileType == "local") {
			Button ibtn1 = (Button) view.findViewById(R.id.profile_button);
			ibtn1.setText("Update profile image");
			ibtn1.setVisibility(View.VISIBLE);
			ibtn1.setOnClickListener(entry.getUpdatePhotoClickListener());

			Button ibtn2 = (Button) view.findViewById(R.id.alerts_button);
			ibtn2.setVisibility(View.VISIBLE);
			ibtn2.setOnClickListener(entry.getAlertsClickListener());
		} else {
			if (mProfileType.equals("foreign")) {
				if (entry.isFriend.equals("0")) {
					Button ibtn1 = (Button) view
							.findViewById(R.id.profile_button);
					ibtn1.setText("Send friend request");
					ibtn1.setOnClickListener(entry
							.getFriendRequestClickListener());
				} else {
					Button ibtn1 = (Button) view
							.findViewById(R.id.profile_button);
					ibtn1.setText("Remove friend");
					ibtn1.setOnClickListener(entry
							.getFriendRemoveClickListener());
				}
			} else {
				Button ibtn1 = (Button) view
						.findViewById(R.id.profile_button);
				ibtn1.setVisibility(View.GONE);
			}
		}

		return view;
	}

}