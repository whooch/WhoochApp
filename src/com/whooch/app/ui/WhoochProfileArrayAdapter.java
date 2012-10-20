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
import android.widget.LinearLayout;
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
		
		
		ImageView iv2 = (ImageView) view.findViewById(R.id.wprofile_leaderimage);
		UrlImageViewHelper.setUrlDrawable(iv2,
				whoochProfileEntry.leaderImageUriDefault);

		TextView tv2 = (TextView) view.findViewById(R.id.wprofile_leadername);
		tv2.setText(whoochProfileEntry.leaderName);
		
		ImageView ivType = (ImageView) view.findViewById(R.id.wprofile_typeimage);
		TextView tvType = (TextView) view.findViewById(R.id.wprofile_type);
        if(whoochProfileEntry.type.equals("open"))
        {
        	tvType.setText("Open");
        	ivType.setImageResource(R.drawable.ic_open_gr);
        	
        	LinearLayout ll1 = (LinearLayout) view.findViewById(R.id.wprofile_open_content);
        	ll1.setVisibility(View.VISIBLE);
        	
        	TextView tvTrail = (TextView) view.findViewById(R.id.wprofile_users_trailing);
        	tvTrail.setText(whoochProfileEntry.trailingCount);
        	
        	TextView tvRating = (TextView) view.findViewById(R.id.wprofile_rating);
        	tvRating.setText(whoochProfileEntry.rating);
        }
        else
        {
        	tvType.setText("Closed");
        	ivType.setImageResource(R.drawable.ic_closed_gr);
        }
		

		TextView tv3 = (TextView) view.findViewById(R.id.wprofile_description);
		if(whoochProfileEntry.description.length() > 0)
		{
			tv3.setText(whoochProfileEntry.description);
		}
		else
		{
			tv3.setText("No description provided");
		}
		
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
        
        if(whoochProfileEntry.type.equals("open") && whoochProfileEntry.isContributing.equals("0"))
        {
        	if(whoochProfileEntry.isTrailing.equals("0"))
        	{
        		ibtn3.setText("Trail whooch");
        		ibtn3.setVisibility(View.VISIBLE);
        		ibtn3.setOnClickListener(whoochProfileEntry.getTrailWhoochClickListener());
        	}
        	else
        	{
        		ibtn3.setText("Stop trailing whooch");
        		ibtn3.setVisibility(View.VISIBLE);
        		ibtn3.setOnClickListener(whoochProfileEntry.getStopTrailWhoochClickListener());
        	}
        }
        
		return view;
	}

}