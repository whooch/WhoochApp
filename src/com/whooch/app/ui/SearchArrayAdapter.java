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
import com.whooch.app.json.ListsEntry;
import com.whooch.app.json.SearchEntry;

public class SearchArrayAdapter extends ArrayAdapter<SearchEntry> {

    private static final int TYPE_WHOOCH = 0;
    private static final int TYPE_USER = 1;
    private static final int TYPE_MAX_COUNT = 2;

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
        if (mData.get(position).searchType == "open") {
            return TYPE_WHOOCH;
        } else {
            return TYPE_USER;
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
            if (getItemViewType(position) == TYPE_WHOOCH) {
                view = mInflater.inflate(R.layout.search_whooch_entry, parent, false);
            } else if (getItemViewType(position) == TYPE_USER) {
                view = mInflater.inflate(R.layout.search_user_entry, parent, false);
            } else {
                // TODO: error
            }
        }
        
        if (getItemViewType(position) == TYPE_WHOOCH) {

            SearchEntry searchEntry = mData.get(position);
            
            ImageView iv1 = (ImageView) view.findViewById(R.id.search_whooch_image);
            UrlImageViewHelper.setUrlDrawable(iv1, searchEntry.whoochImageUriLarge);
                 
            TextView tv1 = (TextView) view.findViewById(R.id.search_whooch_title);
            tv1.setText(searchEntry.whoochName);
            
            TextView tv2 = (TextView) view.findViewById(R.id.search_whooch_leader);
            tv2.setText(searchEntry.leaderName);
           

        }
        else if (getItemViewType(position) == TYPE_USER)
        {
            SearchEntry searchEntry = mData.get(position);
            
            ImageView iv1 = (ImageView) view.findViewById(R.id.search_user_image);
            UrlImageViewHelper.setUrlDrawable(iv1, searchEntry.userImageUriLarge);
                 
            TextView tv1 = (TextView) view.findViewById(R.id.search_user_name);
            tv1.setText(searchEntry.userName);

        }
        else
        {
        	//TODO: error
        }
        
        return view;
    }

}