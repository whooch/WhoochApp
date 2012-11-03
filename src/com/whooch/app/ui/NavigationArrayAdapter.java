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

import com.whooch.app.R;
import com.whooch.app.json.NavigationEntry;

public class NavigationArrayAdapter extends ArrayAdapter<NavigationEntry> {
    
    private Context mContext; 
    private ArrayList<NavigationEntry> mData = null;
    private LayoutInflater mInflater;
        
    public NavigationArrayAdapter(Context context, ArrayList<NavigationEntry> data) {
        super(context, 0, data);
        mData = data;
        mContext = context;
        mInflater = ((Activity) mContext).getLayoutInflater();
    }
    
    @Override
    public int getItemViewType(int position) {
    	return 0;
    }
    
    @Override
    public int getViewTypeCount() {
    	return 0;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getDropDownView(position, convertView, parent);
    }
    
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) { 
        
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.navigation_entry, parent, false);
        }
        
        NavigationEntry navigationEntry = mData.get(position);
        
        ImageView iv1 = (ImageView) view.findViewById(R.id.navigation_image);
        TextView tv1 = (TextView) view.findViewById(R.id.navigation_name);

        
        tv1.setText(navigationEntry.navigationName);
        
		if (navigationEntry.navigationType == 1) {
			iv1.setImageResource(R.drawable.ic_home_w);
		} else if (navigationEntry.navigationType == 2) {
			iv1.setImageResource(R.drawable.ic_whooch_w);
		} else if (navigationEntry.navigationType == 3) {
			iv1.setImageResource(R.drawable.ic_feedback_w);
		} else if (navigationEntry.navigationType == 4) {
			iv1.setImageResource(R.drawable.ic_reactions_w);
		} else if (navigationEntry.navigationType == 5) {
			iv1.setImageResource(R.drawable.ic_profile_w);
		}
        
        return view;
    }

}