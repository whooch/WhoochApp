package com.whooch.app.ui;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.whooch.app.json.PushSettingsEntry;

public class PushSettingsArrayAdapter extends ArrayAdapter<PushSettingsEntry> {

	private ArrayList<PushSettingsEntry> mData = null;

	public PushSettingsArrayAdapter(Context context,
			ArrayList<PushSettingsEntry> data) {
		super(context, 0, data);
		mData = data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {


		PushSettingsEntry pushSettingsEntry = mData.get(position);


        
		return convertView;
	}

}