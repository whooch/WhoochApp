package com.whooch.app;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.whooch.app.helpers.ActionBarHelper;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;

public class PostFeedbackActivity extends PostBaseActivity {
    
    private String mWhoochIdExtra;
    private String mWhoochNumberExtra;
    private String mWhoochImageExtra;
    private String mWhoochNameExtra;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ActionBarHelper.setupActionBar(getSupportActionBar(), new ActionBarHelper.TabListener(getApplicationContext()), 5);
        
        mUserSearchLayout.setVisibility(View.GONE);
        mWhoochSelectorLayout.setVisibility(View.GONE);
        mReactingToText.setVisibility(View.GONE);
            
        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new CreateWhooch(), true);
                //task.execute();
            }
        });

        mSubmitButton.setText("Send");
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new Submit(), true);
                task.execute();
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        ActionBarHelper.selectTab(getSupportActionBar(), 5);
        
        Intent i = getIntent();
        Bundle b = i.getExtras();
        
        if (b == null) {
               Toast.makeText(getApplicationContext(), "Error: bad intent", Toast.LENGTH_SHORT).show();
               finish();
               return;
        }
        
        mWhoochIdExtra = b.getString("WHOOCH_ID");   
        mWhoochNumberExtra = b.getString("WHOOCH_NUMBER");
        mWhoochImageExtra = b.getString("WHOOCH_IMAGE");
        mWhoochNameExtra = b.getString("WHOOCH_NAME");

        if ( (mWhoochIdExtra == null) || (mWhoochNumberExtra == null) || 
                (mWhoochImageExtra == null)  || (mWhoochNameExtra == null) ) {
               Toast.makeText(getApplicationContext(), "Error: bad intent", Toast.LENGTH_SHORT).show();
               finish();
               return;
        }
        
        UrlImageViewHelper.setUrlDrawable(mWhoochImage, mWhoochImageExtra);
        mWhoochName.setText(mWhoochNameExtra);
    }
    
    private class Submit implements WhoochApiCallInterface {
                        
        public HttpRequestBase getHttpRequest() {
            
            HttpPost request = new HttpPost(Settings.apiUrl + "/whooch/add");

            // Add data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();    
            nameValuePairs.add(new BasicNameValuePair("whoochId", mWhoochIdExtra));
            nameValuePairs.add(new BasicNameValuePair("reactionTo", mWhoochNumberExtra));
            nameValuePairs.add(new BasicNameValuePair("reactionType", "feedback"));
            nameValuePairs.add(new BasicNameValuePair("content", mPostText.getText().toString()));

            try {
                request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                // TODO error handling
            }

            return request;
        }
        
        public void handleResponse(String responseString) {
        }
        
        public void postExecute(int statusCode) {
            if (statusCode == 202) {
                finish();
            }
        }
    }

}