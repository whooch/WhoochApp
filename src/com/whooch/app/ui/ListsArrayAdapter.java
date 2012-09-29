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

public class ListsArrayAdapter extends ArrayAdapter<ListsEntry> {

    private static final int TYPE_REGULAR = 0;
    private static final int TYPE_EMPTY = 1;
    private static final int TYPE_MAX_COUNT = 2;
    
    Context mContext;
    ArrayList<ListsEntry> mData = null;
    private LayoutInflater mInflater;
    
    public ListsArrayAdapter(Context context, ArrayList<ListsEntry> data) {
        super(context, 0, data);
        mContext = context;
        mData = data;
        mInflater = ((Activity) mContext).getLayoutInflater();
    }
    
    @Override
    public int getItemViewType(int position) {
        if (mData.get(position).whoochId == null) {
            return TYPE_EMPTY;
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
                view = mInflater.inflate(R.layout.lists_entry, parent, false);
            } else if (getItemViewType(position) == TYPE_EMPTY) {
                view = mInflater.inflate(R.layout.lists_empty_entry, parent, false);
            } else {
                // TODO: error
            }
        }
        
        if (getItemViewType(position) == TYPE_REGULAR) {
            
            ListsEntry listEntry = mData.get(position);
        
            ImageView iv1 = (ImageView) view.findViewById(R.id.lists_entry_whooch_image);
            UrlImageViewHelper.setUrlDrawable(iv1, listEntry.whoochImageUriDefault);
            
            TextView tv1 = (TextView) view.findViewById(R.id.lists_entry_whooch_title);
            tv1.setText(listEntry.whoochName);
            
            ImageView iv2 = (ImageView) view.findViewById(R.id.lists_entry_open_closed_image);
            if (listEntry.type.equals("open")) {
                iv2.setImageResource(R.drawable.ic_open_gr);
            } else {
                iv2.setImageResource(R.drawable.ic_closed_gr);
            }
        }
        
        return view;
    }

}