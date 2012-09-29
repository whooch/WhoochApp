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
import com.whooch.app.helpers.Settings;
import com.whooch.app.json.ContributingEntry;

public class ContributingArrayAdapter extends ArrayAdapter<ContributingEntry> {
    
    private static final int TYPE_REGULAR = 0;
    private static final int TYPE_FIRST = 1;
    private static final int TYPE_MAX_COUNT = 2;
    
    private Context mContext; 
    private ArrayList<ContributingEntry> mData = null;
    private LayoutInflater mInflater;
        
    public ContributingArrayAdapter(Context context, int textViewResourceId, ArrayList<ContributingEntry> data) {
        super(context, textViewResourceId, data);
        mContext = context;
        mData = data;
        mInflater = ((Activity) mContext).getLayoutInflater();
    }
    
    @Override
    public int getItemViewType(int position) {
        if (mData.get(position).whoochId == null) {
            return TYPE_FIRST;
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
        return getDropDownView(position, convertView, parent);
    }
    
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) { 
        
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.lists_entry, parent, false);
        }
        
        ContributingEntry contributingEntry = mData.get(position);
        
        ImageView iv1 = (ImageView) view.findViewById(R.id.lists_entry_whooch_image);
        TextView tv1 = (TextView) view.findViewById(R.id.lists_entry_whooch_title);
        ImageView iv2 = (ImageView) view.findViewById(R.id.lists_entry_open_closed_image);

        if (getItemViewType(position) == TYPE_FIRST) {
            UrlImageViewHelper.setUrlDrawable(iv1, Settings.defaultWhoochImageUriMedium);
            tv1.setText("No Whooch Selected");
            iv2.setVisibility(View.INVISIBLE);
        } else {
            UrlImageViewHelper.setUrlDrawable(iv1, contributingEntry.whoochImageUriMedium);
            tv1.setText(contributingEntry.whoochName);
            iv2.setVisibility(View.VISIBLE);
            if (contributingEntry.type.equals("open")) {
                iv2.setImageResource(R.drawable.ic_open_gr);
            } else {
                iv2.setImageResource(R.drawable.ic_closed_gr);
            }
        }
        
        return view;
    }

}