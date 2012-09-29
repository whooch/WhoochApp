package com.whooch.app;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.whooch.app.helpers.ActionBarHelper;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;
import com.whooch.app.json.ContributingEntry;
import com.whooch.app.ui.ContributingArrayAdapter;

public class PostStandardActivity extends PostBaseActivity {
    
    private ArrayList<ContributingEntry> mContributingArray = new ArrayList<ContributingEntry>();
    private ContributingArrayAdapter mWhoochSelectorAdapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ActionBarHelper.setupActionBar(getSupportActionBar(), new ActionBarHelper.TabListener(getApplicationContext()), 4);
    
        mReactingToText.setVisibility(View.GONE);
        mWhoochFeedbackLayout.setVisibility(View.GONE);
    
        // add the invalid "No Whooch Selected" entry.  This will be rendered by the adapter.
        mContributingArray.add(new ContributingEntry());
        
        mWhoochSelectorAdapter = new ContributingArrayAdapter(this, android.R.layout.simple_spinner_item , mContributingArray);
        mWhoochSelector.setAdapter(mWhoochSelectorAdapter);
        
        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new CreateWhooch(), true);
                //task.execute();
            }
        });

        mSubmitButton.setText("Update");
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mWhoochSelector.getSelectedItemPosition() == 0) {
                    // User selected the placeholder item
                    Toast.makeText(getActivityContext(), "Please select a Whooch", Toast.LENGTH_SHORT).show();
                } else {
                    WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new Submit(), true);
                    task.execute();
                }
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        ActionBarHelper.selectTab(getSupportActionBar(), 4);
        
        WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new GetContributingList(), false);
        task.execute();
    }
    
    private class GetContributingList implements WhoochApiCallInterface {
        
        private String mResponseString = null;

        public HttpRequestBase getHttpRequest() {
            return new HttpGet(Settings.apiUrl + "/user/contributinglist");
        }
        
        public void handleResponse(String responseString) {
            mResponseString = responseString;
        }
        
        public void postExecute(int statusCode) {
            if (statusCode == 200) {
                mContributingArray.clear();
                mContributingArray.add(new ContributingEntry()); // No Whooch Selected
                
                // parse the response as JSON and update the Content Array
                if (!mResponseString.equals("null")) {
                    try {
                        JSONArray jsonArray = new JSONArray(mResponseString);
                        for (int i=0; i<jsonArray.length(); i++) {
                            ContributingEntry entry = new ContributingEntry(jsonArray.getJSONObject(i), getWindowManager());
                            mContributingArray.add(entry);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        // TODO: error handling
                    }
                } else {
                    // if it is null we don't mind, there just wasn't anything there
                }
            }
            
            mWhoochSelectorAdapter.notifyDataSetChanged();
        }
    }
    
    private class Submit implements WhoochApiCallInterface {

        public HttpRequestBase getHttpRequest() {
            
            HttpPost request = new HttpPost(Settings.apiUrl + "/whooch/add");
            
            ContributingEntry selectedEntry = (ContributingEntry) mWhoochSelector.getSelectedItem();
            
            // Add data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();    
            nameValuePairs.add(new BasicNameValuePair("whoochId", selectedEntry.whoochId));
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
                // TODO: take user to the whooch they just posted in?  Would need to go
                // to the correct activity but also remove the PostStandard activity from
                // the backstack.
                finish();
            }
        }
    }

}