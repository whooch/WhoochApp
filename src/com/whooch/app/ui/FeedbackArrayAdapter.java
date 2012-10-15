package com.whooch.app.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageGetter;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.whooch.app.R;
import com.whooch.app.helpers.WhoochHelperFunctions;
import com.whooch.app.json.FeedbackEntry;

public class FeedbackArrayAdapter extends ArrayAdapter<FeedbackEntry> {
    
    private Context mContext; 
    private ArrayList<FeedbackEntry> mData = null;
    private LayoutInflater mInflater;
    
    public FeedbackArrayAdapter(Context context, ArrayList<FeedbackEntry> data) {
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
        	view = mInflater.inflate(R.layout.stream_entry, parent, false);
        }
        
        FeedbackEntry feedbackEntry = mData.get(position);
        
        ImageView iv1 = (ImageView) view.findViewById(R.id.entry_whooch_image);
        // TODO: figure out what's up with this preloading
        //UrlImageViewHelper.setUrlDrawable(iv1, whoochEntry.whoochImageUriLarge, R.drawable.ic_whooch_transparent);
        UrlImageViewHelper.setUrlDrawable(iv1, feedbackEntry.whoochImageUriLarge);
        
        TextView tv1 = (TextView) view.findViewById(R.id.entry_whooch_title);

        tv1.setText(feedbackEntry.whoochName);
        
        TextView tv2 = (TextView) view.findViewById(R.id.entry_posted_user);
        tv2.setText(feedbackEntry.userName);
        
        TextView tv3 = (TextView) view.findViewById(R.id.entry_whooch_content);
        UrlImageGetter imageGetter = new UrlImageGetter(tv3, mContext);
        Spanned htmlSpan = Html.fromHtml(feedbackEntry.content.replaceAll(">\\s+<", "><"), imageGetter, null);
        tv3.setText(htmlSpan);
        
        TextView tv4 = (TextView) view.findViewById(R.id.entry_whooch_foot);
        tv4.setText(WhoochHelperFunctions.toRelativeTime(Long.parseLong(feedbackEntry.timestamp)));
        
        if(feedbackEntry.image.equals("null"))
        {
        	ImageView iv2 = (ImageView) view.findViewById(R.id.imagePicture);
        	iv2.setVisibility(View.GONE);
        }
        
    	ImageView iv3 = (ImageView) view.findViewById(R.id.imagePlus);
    	iv3.setVisibility(View.GONE);
        
        return view;
    }

}