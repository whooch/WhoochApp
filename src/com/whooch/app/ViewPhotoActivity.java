package com.whooch.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.whooch.app.helpers.Settings;

public class ViewPhotoActivity extends SherlockListActivity { 
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_photo);
        
        Intent i = getIntent();
        Bundle b = i.getExtras();
        
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
        
        ImageView iv1 = (ImageView) this.findViewById(R.id.viewAttachedImage);
		UrlImageViewHelper.setUrlDrawable(iv1, imageUri);
    }
    
    @Override
    public void onResume() {
        super.onResume();

    }
     
}