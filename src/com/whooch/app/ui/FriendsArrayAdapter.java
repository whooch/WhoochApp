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
import com.whooch.app.json.FriendsEntry;

public class FriendsArrayAdapter extends ArrayAdapter<FriendsEntry> {
    
    private static final int TYPE_REGULAR = 0;
    private static final int TYPE_FIRST = 1;
    private static final int TYPE_MAX_COUNT = 2;
    
    private Context mContext; 
    private ArrayList<FriendsEntry> mData = null;
    private LayoutInflater mInflater;
        
    public FriendsArrayAdapter(Context context, int textViewResourceId, ArrayList<FriendsEntry> data) {
        super(context, textViewResourceId, data);
        mContext = context;
        mData = data;
        mInflater = ((Activity) mContext).getLayoutInflater();
    }
    
    @Override
    public int getItemViewType(int position) {
        if (mData.get(position).userId == null) {
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
            view = mInflater.inflate(R.layout.friends_entry, parent, false);
        }
        
        FriendsEntry friendsEntry = mData.get(position);
        
        ImageView iv1 = (ImageView) view.findViewById(R.id.friends_entry_user_image);
        TextView tv1 = (TextView) view.findViewById(R.id.friends_entry_user_name);

        if (getItemViewType(position) == TYPE_FIRST) {
            UrlImageViewHelper.setUrlDrawable(iv1, Settings.defaultWhoochImageUriLarge);
            
            tv1.setText("");
			if (mData.size() == 1) {
				tv1.setHint("You have no friends to invite");
			} else {
	            tv1.setText("Select a friend to invite");
			}
        } else {
            UrlImageViewHelper.setUrlDrawable(iv1, friendsEntry.userImageUriMedium);
            tv1.setText(friendsEntry.userName);
        }
        
        return view;
    }

}