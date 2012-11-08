package com.whooch.app.helpers;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.whooch.app.CreateActivity;
import com.whooch.app.FeedbackActivity;
import com.whooch.app.ListsActivity;
import com.whooch.app.PostStandardActivity;
import com.whooch.app.R;
import com.whooch.app.ReactionsActivity;
import com.whooch.app.SearchActivity;
import com.whooch.app.StreamActivity;
import com.whooch.app.UserProfileActivity;
import com.whooch.app.json.NavigationEntry;
import com.whooch.app.ui.NavigationArrayAdapter;

public class ActionBarHelper {

	protected static Activity mActivityContext = null;

	private static int currentItem = 0;

	private static OnNavigationListener mOnNavigationListener = new OnNavigationListener() {
		public boolean onNavigationItemSelected(int itemPosition, long itemId) {

			if (currentItem != itemPosition) {

				if (itemPosition == 0) {
					Intent i = new Intent(mActivityContext,
							StreamActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mActivityContext.startActivity(i);

				} else if (itemPosition == 1) {
					Intent i = new Intent(mActivityContext,
							ListsActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mActivityContext.startActivity(i);

				} else if (itemPosition == 2) {
					Intent i = new Intent(mActivityContext,
							FeedbackActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mActivityContext.startActivity(i);

				} else if (itemPosition == 3) {
					Intent i = new Intent(mActivityContext,
							ReactionsActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mActivityContext.startActivity(i);

				} else if (itemPosition == 4) {
					Intent i = new Intent(mActivityContext,
							UserProfileActivity.class);
					SharedPreferences settings = mActivityContext
							.getSharedPreferences("whooch_preferences", 0);
					String userId = settings.getString("userid", null);
					i.putExtra("USER_ID", userId);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mActivityContext.startActivity(i);

				}	
			}
			
			return true;
		}
	};

	public static void setupActionBar(ActionBar actionBar, Activity activityContext, int selected) {
		
		mActivityContext = activityContext;
		
		currentItem = selected;

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		ArrayList<NavigationEntry> navArray = new ArrayList<NavigationEntry>();
		navArray.add(new NavigationEntry(1));
		navArray.add(new NavigationEntry(2));
		navArray.add(new NavigationEntry(3));
		navArray.add(new NavigationEntry(4));
		navArray.add(new NavigationEntry(5));
		NavigationArrayAdapter list = new NavigationArrayAdapter(
				mActivityContext, navArray); 
		actionBar.setListNavigationCallbacks(list, mOnNavigationListener);

		actionBar.setSelectedNavigationItem(selected);

		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);

		LayoutInflater inflater = (LayoutInflater) mActivityContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view = inflater.inflate(R.layout.title_bar, null);

		actionBar.setCustomView(view);

		Button btn1 = (Button) view.findViewById(R.id.main_create);
		btn1.setOnClickListener(getCreateWhoochClickListener());

		ImageButton ibtn2 = (ImageButton) view.findViewById(R.id.main_search);
		ibtn2.setOnClickListener(getSearchClickListener());

		ImageButton ibtn3 = (ImageButton) view.findViewById(R.id.main_update);
		ibtn3.setOnClickListener(getUpdateClickListener());

	}

	public static OnClickListener getCreateWhoochClickListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(v.getContext(), CreateActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				v.getContext().startActivity(i);
			}
		};

	}

	public static OnClickListener getSearchClickListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(v.getContext(), SearchActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				v.getContext().startActivity(i);
			}
		};
	}

	public static OnClickListener getUpdateClickListener() {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(v.getContext(),
						PostStandardActivity.class);
				i.putExtra("UPDATE_TYPE", "regular");
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				v.getContext().startActivity(i);
			}
		};
	}

}