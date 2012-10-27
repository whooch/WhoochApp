package com.whooch.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.whooch.app.helpers.Settings;

public class ViewPhotoActivity extends SherlockListActivity { 
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_photo);
        
        
        LayoutInflater inflater = (LayoutInflater) getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
	
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		
		View whoochTitle = inflater.inflate(
				R.layout.whooch_title_bar, null);
		getSupportActionBar().setCustomView(whoochTitle);
		
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		
		
        Intent i = getIntent();
        Bundle b = i.getExtras();
        
        if (b == null) {
               Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
               finish();
        }

		
		ImageView iv1 = (ImageView) findViewById(R.id.wheader_whooch_image);
		UrlImageViewHelper.setUrlDrawable(iv1,
				b.getString("WHOOCH_IMAGE"));

		TextView tv1 = (TextView) findViewById(R.id.wheader_whooch_title);
		tv1.setText(b.getString("WHOOCH_NAME"));

		TextView tv2 = (TextView) findViewById(R.id.wheader_whooch_leader);
		tv2.setText(b.getString("USER_NAME"));

		LinearLayout ll1 = (LinearLayout) findViewById(R.id.wheader_whoochinfo);
		ll1.setVisibility(View.VISIBLE);
		
		ll1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	finish();
            }
        });

        
        String imageType = b.getString("IMAGE_TYPE");
        String imageName = null;
        String imageUri = null;
        
        if(imageType.equals("feedback"))
        {    
        	String feedbackId = b.getString("FEEDBACK_ID");
        	imageName = b.getString("IMAGE_NAME");
            imageUri = Settings.cdnUrl + "f" + feedbackId + "_" + imageName;
        }
        else
        {
        	String whoochId = b.getString("WHOOCH_ID");
        	String whoochNumber = b.getString("WHOOCH_NUMBER");
        	imageName = b.getString("IMAGE_NAME");
            imageUri = Settings.cdnUrl + whoochId + "_" + whoochNumber + "_" + imageName;
        }
        
        Log.e("photourl", imageUri);
        
        ImageView ivMain = (ImageView) this.findViewById(R.id.viewAttachedImage);
		UrlImageViewHelper.setUrlDrawable(ivMain, imageUri);
    }
    
    @Override
    public void onResume() {
        super.onResume();

    }

     
}