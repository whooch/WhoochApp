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
import com.whooch.app.json.StreamEntry;

public class StreamArrayAdapter extends ArrayAdapter<StreamEntry> {

	private static final int TYPE_REGULAR = 0;
	private static final int TYPE_FEEDBACK = 1;
	private static final int TYPE_MAX_COUNT = 2;

	private Context mContext;
	private ArrayList<StreamEntry> mData = null;
	private LayoutInflater mInflater;

	private boolean mIsSingleWhooch;

	public StreamArrayAdapter(Context context, ArrayList<StreamEntry> data,
			boolean isSingleWhooch) {
		super(context, 0, data);
		mContext = context;
		mData = data;
		mIsSingleWhooch = isSingleWhooch;
		mInflater = ((Activity) mContext).getLayoutInflater();
	}

	@Override
	public int getItemViewType(int position) {
		if (mData.get(position).reactionType.equals("feedback")) {
			return TYPE_FEEDBACK;
		} else {
			return TYPE_REGULAR;
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
			if (getItemViewType(position) == TYPE_REGULAR) {
				view = mInflater.inflate(R.layout.stream_entry, parent, false);
			} else if (getItemViewType(position) == TYPE_FEEDBACK) {
				view = mInflater.inflate(R.layout.stream_feedback_entry,
						parent, false);
			} else {
				// TODO: error
			}
		}

		StreamEntry whoochEntry = mData.get(position);

		ImageView iv1 = (ImageView) view.findViewById(R.id.entry_whooch_image);
		TextView tv1 = (TextView) view.findViewById(R.id.entry_whooch_title);
		if (mIsSingleWhooch) {
			tv1.setVisibility(View.GONE);
			UrlImageViewHelper.setUrlDrawable(iv1,
					whoochEntry.userImageUriLarge);
		} else {
			tv1.setText(whoochEntry.whoochName);
			UrlImageViewHelper.setUrlDrawable(iv1,
					whoochEntry.whoochImageUriLarge);
		}

		TextView tv2 = (TextView) view.findViewById(R.id.entry_posted_user);
		tv2.setText(whoochEntry.userName);

		TextView tv3 = (TextView) view.findViewById(R.id.entry_whooch_content);
		tv3.setText(WhoochHelperFunctions.getSpannedFromHtmlContent(
				whoochEntry.content, tv3, mContext));

		if (getItemViewType(position) == TYPE_FEEDBACK) {

			TextView tv5 = (TextView) view
					.findViewById(R.id.entry_feedback_content);
			tv5.setText(WhoochHelperFunctions.getSpannedFromHtmlContent(
					whoochEntry.feedbackInfo.content, tv5, mContext));

			ImageView iv2 = (ImageView) view
					.findViewById(R.id.entry_feedback_user_image);
			// TODO: figure out what's up with this preloading
			// UrlImageViewHelper.setUrlDrawable(iv2,
			// whoochEntry.feedbackInfo.userImageUriMedium,
			// R.drawable.ic_whooch_transparent);
			UrlImageViewHelper.setUrlDrawable(iv2,
					whoochEntry.feedbackInfo.userImageUriMedium);
			
			if (whoochEntry.feedbackInfo.image.equals("null")) {
				ImageView iv6 = (ImageView) view.findViewById(R.id.feedbackImagePicture);
				iv6.setVisibility(View.GONE);
			} else {
				ImageView iv6 = (ImageView) view.findViewById(R.id.imagePicture);
				iv6.setVisibility(View.VISIBLE);
			}
		}

		TextView tv4 = (TextView) view.findViewById(R.id.entry_whooch_foot);
		tv4.setText(WhoochHelperFunctions.toRelativeTime(Long
				.parseLong(whoochEntry.timestamp)));
		
		TextView tvFan = (TextView) view.findViewById(R.id.entry_whooch_foot_fans);
		if(whoochEntry.fanString != null)
		{
			tvFan.setText(whoochEntry.fanString);
			tvFan.setVisibility(View.VISIBLE);
		}
		else
		{
			tvFan.setVisibility(View.GONE);
		}

		if (whoochEntry.image.equals("null")) {
			ImageView iv3 = (ImageView) view.findViewById(R.id.imagePicture);
			iv3.setVisibility(View.GONE);
		} else {
			ImageView iv3 = (ImageView) view.findViewById(R.id.imagePicture);
			iv3.setVisibility(View.VISIBLE);
		}

		if (!whoochEntry.reactionType.equals("whooch")) {
			ImageView iv4 = (ImageView) view.findViewById(R.id.imagePlus);
			iv4.setVisibility(View.GONE);
		} else {
			ImageView iv4 = (ImageView) view.findViewById(R.id.imagePlus);
			iv4.setVisibility(View.VISIBLE);
		}

		ImageView iv5 = (ImageView) view.findViewById(R.id.imageOpenClosed);
		if (iv5 != null) {
			if (mIsSingleWhooch) {

				iv5.setVisibility(View.GONE);

			} else {
				if (whoochEntry.type.equals("open")) {
					iv5.setImageResource(R.drawable.ic_open_gr);
				} else {
					iv5.setImageResource(R.drawable.ic_closed_gr);
				}

				iv5.setVisibility(View.VISIBLE);
			}
		}
		
		return view;
	}

}