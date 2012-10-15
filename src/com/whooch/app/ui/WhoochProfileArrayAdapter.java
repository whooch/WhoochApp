package com.whooch.app.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.whooch.app.R;
import com.whooch.app.json.WhoochProfileEntry;

public class WhoochProfileArrayAdapter extends ArrayAdapter<WhoochProfileEntry> {

	private Context mContext;
	private ArrayList<WhoochProfileEntry> mData = null;
	private LayoutInflater mInflater;

	public WhoochProfileArrayAdapter(Context context,
			ArrayList<WhoochProfileEntry> data) {
		super(context, 0, data);
		mContext = context;
		mData = data;
		mInflater = ((Activity) mContext).getLayoutInflater();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view = convertView;
        if (view == null)
        {
        	view = mInflater.inflate(R.layout.whooch_profile_entry, parent, false);
        }
        
		WhoochProfileEntry whoochProfileEntry = mData.get(position);
		
		ImageView iv1 = (ImageView) view.findViewById(R.id.wprofile_whoochimage);
		UrlImageViewHelper.setUrlDrawable(iv1,
				whoochProfileEntry.whoochImageUriLarge);

		TextView tv1 = (TextView) view.findViewById(R.id.wprofile_whoochname);
		tv1.setText(whoochProfileEntry.whoochName);

		TextView tv2 = (TextView) view.findViewById(R.id.wprofile_description);
		tv2.setText(whoochProfileEntry.description);
		
		TextView tv3 = (TextView) view.findViewById(R.id.wprofile_whoochname);
		tv3.setText(whoochProfileEntry.whoochName);
		
		Button ibtn1 = (Button) view.findViewById(R.id.wprofile_inviteuser);
		Button ibtn2 = (Button) view.findViewById(R.id.wprofile_updatephoto);
		Button ibtn3 = (Button) view.findViewById(R.id.wprofile_trailwhooch);
		
		if(whoochProfileEntry.isContributing.equals("1"))
		{
			ibtn1.setVisibility(View.VISIBLE);
	        ibtn1.setOnClickListener(whoochProfileEntry.getInviteUserClickListener());
		}

		
        SharedPreferences settings = mContext.getSharedPreferences("whooch_preferences", 0);
        String currentUserName = settings.getString("username", null);
        
        if (whoochProfileEntry.isContributing.equals("1") && whoochProfileEntry.leaderName.equalsIgnoreCase(currentUserName)) 
        {
        	ibtn2.setVisibility(View.VISIBLE);
	        ibtn2.setOnClickListener(whoochProfileEntry.getUpdatePhotoClickListener());
		}
        
        if(whoochProfileEntry.type.equals("open") && whoochProfileEntry.isContributing.equals("0") && whoochProfileEntry.isTrailing.equals("0"))
        {
        	ibtn3.setVisibility(View.VISIBLE);
	        ibtn3.setOnClickListener(whoochProfileEntry.getTrailWhoochClickListener());
        }
        
		return view;
	}

}