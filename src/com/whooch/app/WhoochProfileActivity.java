package com.whooch.app;

import java.util.ArrayList;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;
import com.whooch.app.json.WhoochProfileEntry;
import com.whooch.app.ui.WhoochProfileArrayAdapter;

public class WhoochProfileActivity extends SherlockListActivity implements OnScrollListener {
    
    String mWhoochId = null;
    private ArrayList<WhoochProfileEntry> mWhoochProfileArray = new ArrayList<WhoochProfileEntry>();
    WhoochProfileArrayAdapter mAdapter;

    View mLoadingFooterView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.whooch_profile);
         
        LayoutInflater inflater = (LayoutInflater) getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
	
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		
		View whoochTitle = inflater.inflate(
				R.layout.whooch_title_bar, null);
		getSupportActionBar().setCustomView(whoochTitle);
		
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		
		
		LinearLayout ll1 = (LinearLayout)findViewById(R.id.wheader_whoochinfo);
		ll1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	finish();
            }
        });
        
        mAdapter = new WhoochProfileArrayAdapter(this, mWhoochProfileArray);
        setListAdapter(mAdapter);
    }
    
    @Override
    public void onResume() {
        super.onResume();

        WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new ProfileInitiate(), true);
        task.execute();
    }
    
    private Context getActivityContext() {
        return this;
    }
    
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {

    }
    
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }
    
    private class ProfileInitiate implements WhoochApiCallInterface {
        
        private String mResponseString = null;
        
        public void preExecute() {}
        
        public HttpRequestBase getHttpRequest() {
            Intent i = getIntent();
            Bundle b = i.getExtras();
            mWhoochId = b.getString("WHOOCH_ID");
            if (mWhoochId == null) {
                Toast.makeText(getApplicationContext(), "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
                finish();
            }

            return new HttpGet(Settings.apiUrl + "/whooch/" + mWhoochId + "?info=true");
        }
        
        public void handleResponse(String responseString) {
            mResponseString = responseString;
        }
         
        public void postExecute(int statusCode) {
        	
            if (statusCode == 200) { 
            	mWhoochProfileArray.clear();
                // parse the response as JSON and update the Content Array
                if (!mResponseString.equals("null")) {
                    try {
                        JSONObject jsonObject = new JSONObject(mResponseString);
                        // create an object that will be used to populate the List View and add it to the array
                        
                        WhoochProfileEntry entry = new WhoochProfileEntry(jsonObject, getWindowManager());
                        
						ImageView iv1 = (ImageView) findViewById(R.id.wheader_whooch_image);
						UrlImageViewHelper.setUrlDrawable(iv1,
								entry.whoochImageUriMedium);

						TextView tv1 = (TextView) findViewById(R.id.wheader_whooch_title);
						tv1.setText(entry.whoochName);

						TextView tv2 = (TextView) findViewById(R.id.wheader_whooch_leader);
						tv2.setText("Options");

						LinearLayout ll1 = (LinearLayout) findViewById(R.id.wheader_whoochinfo);
						ll1.setVisibility(View.VISIBLE);
                       
                        mWhoochProfileArray.add(entry);   
                        mAdapter.notifyDataSetChanged();
                        
                    } catch (JSONException e) {
                        e.printStackTrace();
                        // TODO: error handling
                    }
                } else {
                    // if it is null we don't mind, there just wasn't anything there
                }
                
            }
        
        }
        
    }
    
}