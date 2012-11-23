package com.whooch.app.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.whooch.app.R;
import com.whooch.app.helpers.WhoochHelperFunctions;
import com.whooch.app.json.SearchEntry;

public class SearchArrayAdapter extends ArrayAdapter<SearchEntry> {

	private static final int TYPE_WHOOCH = 0;
	private static final int TYPE_USER = 1;
	private static final int TYPE_UPDATE = 2;
	private static final int TYPE_FEEDBACK = 3;
	private static final int TYPE_MAX_COUNT = 4;

	private Context mContext;
	private ArrayList<SearchEntry> mData = null;
	private LayoutInflater mInflater;

	public SearchArrayAdapter(Context context, ArrayList<SearchEntry> data) {
		super(context, 0, data);
		mContext = context;
		mData = data;
		mInflater = ((Activity) mContext).getLayoutInflater();
	}

	@Override
	public int getItemViewType(int position) {
		
		if (mData.get(position).searchType.equals("open")) {
			return TYPE_WHOOCH;
		} else if (mData.get(position).searchType.equals("user")) {
			return TYPE_USER;
		} else if ((mData.get(position).reactionType.equals("feedback")) && (mData.get(position).feedbackInfo != null)) {
			return TYPE_FEEDBACK;
		} else {
			return TYPE_UPDATE;
		}
	}

	@Override
	public int getViewTypeCount() {
		return TYPE_MAX_COUNT;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view = convertView;
		if (view == null) {
			if (getItemViewType(position) == TYPE_WHOOCH) {
				view = mInflater.inflate(R.layout.search_whooch_entry, parent,
						false);
			} else if (getItemViewType(position) == TYPE_USER) {
				view = mInflater.inflate(R.layout.search_user_entry, parent,
						false);
			} else if (getItemViewType(position) == TYPE_UPDATE) {
				view = mInflater.inflate(R.layout.stream_entry, parent, false);
			} else {
				view = mInflater.inflate(R.layout.stream_feedback_entry,
						parent, false);
			}
		}

		if (getItemViewType(position) == TYPE_WHOOCH) {

			SearchEntry searchEntry = mData.get(position);

			ImageView iv1 = (ImageView) view
					.findViewById(R.id.search_whooch_image);
			UrlImageViewHelper.setUrlDrawable(iv1,
					searchEntry.whoochImageUriLarge);

			TextView tv1 = (TextView) view
					.findViewById(R.id.search_whooch_title);
			tv1.setText(searchEntry.whoochName);

			TextView tv2 = (TextView) view
					.findViewById(R.id.search_whooch_leader);
			tv2.setText(searchEntry.leaderName);

		} else if (getItemViewType(position) == TYPE_USER) {
			SearchEntry searchEntry = mData.get(position);

			ImageView iv1 = (ImageView) view
					.findViewById(R.id.search_user_image);
			UrlImageViewHelper.setUrlDrawable(iv1,
					searchEntry.userImageUriLarge);

			TextView tv1 = (TextView) view.findViewById(R.id.search_user_name);
			tv1.setText(searchEntry.userName);

		} else {
			SearchEntry searchEntry = mData.get(position);

			ImageView iv1 = (ImageView) view
					.findViewById(R.id.entry_whooch_image);
			TextView tv1 = (TextView) view
					.findViewById(R.id.entry_whooch_title);
			tv1.setText(searchEntry.whoochName);

			UrlImageViewHelper.setUrlDrawable(iv1,
					searchEntry.whoochImageUriLarge);

			TextView tv2 = (TextView) view.findViewById(R.id.entry_posted_user);
			tv2.setText(searchEntry.userName);

			TextView tv3 = (TextView) view
					.findViewById(R.id.entry_whooch_content);
			tv3.setText(WhoochHelperFunctions.getSpannedFromHtmlContent(
					searchEntry.content, tv3, mContext));

			if (getItemViewType(position) == TYPE_FEEDBACK) {

				if(searchEntry.feedbackInfo != null)
				{
				TextView tv5 = (TextView) view
						.findViewById(R.id.entry_feedback_content);
				tv5.setText(WhoochHelperFunctions.getSpannedFromHtmlContent(
						searchEntry.feedbackInfo.content, tv5, mContext));

				ImageView iv2 = (ImageView) view
						.findViewById(R.id.entry_feedback_user_image);
				// TODO: figure out what's up with this preloading
				// UrlImageViewHelper.setUrlDrawable(iv2,
				// whoochEntry.feedbackInfo.userImageUriMedium,
				// R.drawable.ic_whooch_transparent);
				UrlImageViewHelper.setUrlDrawable(iv2,
						searchEntry.feedbackInfo.userImageUriMedium);
				}
			}

			TextView tv4 = (TextView) view.findViewById(R.id.entry_whooch_foot);
			tv4.setText(WhoochHelperFunctions.toRelativeTime(Long
					.parseLong(searchEntry.timestamp)));
			
			TextView tvFan = (TextView) view.findViewById(R.id.entry_whooch_foot_fans);
			if(searchEntry.fanString != null)
			{
				tvFan.setText(searchEntry.fanString);
				tvFan.setVisibility(View.VISIBLE);
			}
			else
			{
				tvFan.setVisibility(View.GONE);
			}

			if (searchEntry.image.equals("null")) {
				ImageView iv3 = (ImageView) view
						.findViewById(R.id.imagePicture);
				iv3.setVisibility(View.GONE);
			} else {
				ImageView iv3 = (ImageView) view
						.findViewById(R.id.imagePicture);
				iv3.setVisibility(View.VISIBLE);
			}

			if (!searchEntry.reactionType.equals("whooch")) {
				ImageView iv4 = (ImageView) view.findViewById(R.id.imagePlus);
				iv4.setVisibility(View.GONE);
			} else {
				ImageView iv4 = (ImageView) view.findViewById(R.id.imagePlus);
				iv4.setVisibility(View.VISIBLE);
			}

			ImageView iv5 = (ImageView) view.findViewById(R.id.imageOpenClosed);
			if (iv5 != null) {
				iv5.setVisibility(View.GONE);
			}

		}

		return view;
	}
}