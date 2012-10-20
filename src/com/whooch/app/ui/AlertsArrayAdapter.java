package com.whooch.app.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.whooch.app.R;
import com.whooch.app.json.AlertsEntry;

public class AlertsArrayAdapter extends ArrayAdapter<AlertsEntry> {

    private static final int TYPE_REGULAR = 0;
    private static final int TYPE_EMPTY = 1;
    private static final int TYPE_MAX_COUNT = 2;

    private Context mContext; 
    private ArrayList<AlertsEntry> mData = null;
    private LayoutInflater mInflater;

    public AlertsArrayAdapter(Context context, ArrayList<AlertsEntry> data) {
        super(context, 0, data);
        mContext = context;
        mData = data;
        mInflater = ((Activity) mContext).getLayoutInflater();
    }
    
    @Override
    public int getItemViewType(int position) {
        if (mData.get(position).alertType == null) {
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
                view = mInflater.inflate(R.layout.alerts_entry, parent, false);
            } else if (getItemViewType(position) == TYPE_EMPTY) {
                view = mInflater.inflate(R.layout.alerts_empty_entry, parent, false);
            } else {
                // TODO: error
            }
        }
        
        if (getItemViewType(position) == TYPE_REGULAR) {

            AlertsEntry alertsEntry = mData.get(position);

            ImageView iv1 = (ImageView) view.findViewById(R.id.alerts_entry_image);
            UrlImageViewHelper.setUrlDrawable(iv1, alertsEntry.getUserImageUrl());

            TextView tv1 = (TextView) view.findViewById(R.id.alerts_entry_message_text);
            tv1.setText(alertsEntry.getMessage());

            ImageButton ibtn1 = (ImageButton) view.findViewById(R.id.alerts_entry_accept_button);
            ibtn1.setOnClickListener(alertsEntry.getAcceptClickListener());

            ImageButton ibtn2 = (ImageButton) view.findViewById(R.id.alerts_entry_decline_button);
            ibtn2.setOnClickListener(alertsEntry.getDeclineClickListener());
        }
        
        return view;
    }

}