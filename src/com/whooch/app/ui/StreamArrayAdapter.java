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
import com.whooch.app.json.StreamEntry;

public class StreamArrayAdapter extends ArrayAdapter<StreamEntry> {

    private static final int TYPE_REGULAR = 0;
    private static final int TYPE_FEEDBACK = 1;
    private static final int TYPE_MAX_COUNT = 2;
    
    private Context mContext; 
    private ArrayList<StreamEntry> mData = null;
    private LayoutInflater mInflater;
    
    private boolean mIsSingleWhooch;
    
    public StreamArrayAdapter(Context context, ArrayList<StreamEntry> data, boolean isSingleWhooch) {
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
        if (view == null)
        {
            if (getItemViewType(position) == TYPE_REGULAR) {
                view = mInflater.inflate(R.layout.stream_entry, parent, false);
            } else if (getItemViewType(position) == TYPE_FEEDBACK) {
                view = mInflater.inflate(R.layout.stream_feedback_entry, parent, false);
            } else {
                // TODO: error
            }
        }
        
        StreamEntry whoochEntry = mData.get(position);
        
        ImageView iv1 = (ImageView) view.findViewById(R.id.entry_whooch_image);
        TextView tv1 = (TextView) view.findViewById(R.id.entry_whooch_title);
        if (mIsSingleWhooch) {
            tv1.setVisibility(View.GONE);
            

            // TODO: figure out what's up with this preloading
            //UrlImageViewHelper.setUrlDrawable(iv1, whoochEntry.whoochImageUriLarge, R.drawable.ic_whooch_transparent);
            UrlImageViewHelper.setUrlDrawable(iv1, whoochEntry.userImageUriLarge);
        } else {
            tv1.setText(whoochEntry.whoochName);
            
            // TODO: figure out what's up with this preloading
            //UrlImageViewHelper.setUrlDrawable(iv1, whoochEntry.whoochImageUriLarge, R.drawable.ic_whooch_transparent);
            UrlImageViewHelper.setUrlDrawable(iv1, whoochEntry.whoochImageUriLarge);
        }
        
        TextView tv2 = (TextView) view.findViewById(R.id.entry_posted_user);
        tv2.setText(whoochEntry.userName);
        
        TextView tv3 = (TextView) view.findViewById(R.id.entry_whooch_content);
        UrlImageGetter imageGetter = new UrlImageGetter(tv3, mContext); 
        
        Spanned htmlSpan = Html.fromHtml(whoochEntry.content.replaceAll(">\\s+<", "><"), imageGetter, null);
       
        while(imageGetter.isImageLoaded())
        {
        }
        tv3.setText(htmlSpan);

        if (getItemViewType(position) == TYPE_FEEDBACK) {
            
            TextView tv5 = (TextView) view.findViewById(R.id.entry_feedback_content);
            imageGetter = new UrlImageGetter(tv5, mContext);
            
            htmlSpan = Html.fromHtml(whoochEntry.feedbackInfo.content.replaceAll(">\\s+<", "><"), imageGetter, null);
            tv5.setText(htmlSpan);
            
            ImageView iv2 = (ImageView) view.findViewById(R.id.entry_feedback_user_image);
            // TODO: figure out what's up with this preloading
            //UrlImageViewHelper.setUrlDrawable(iv2, whoochEntry.feedbackInfo.userImageUriMedium, R.drawable.ic_whooch_transparent);
            UrlImageViewHelper.setUrlDrawable(iv2, whoochEntry.feedbackInfo.userImageUriMedium);
        }
        
        TextView tv4 = (TextView) view.findViewById(R.id.entry_whooch_foot);
        tv4.setText(WhoochHelperFunctions.toRelativeTime(Long.parseLong(whoochEntry.timestamp)));
        
        if(whoochEntry.image.equals("null"))
        {
        	ImageView iv3 = (ImageView) view.findViewById(R.id.imagePicture);
        	iv3.setVisibility(View.GONE);
        }
        
        if(!whoochEntry.reactionType.equals("whooch"))
        {
        	ImageView iv4 = (ImageView) view.findViewById(R.id.imagePlus);
        	iv4.setVisibility(View.GONE);
        }
        
        return view;
    }

}