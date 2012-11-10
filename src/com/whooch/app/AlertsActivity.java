package com.whooch.app;

import java.util.ArrayList;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;
import com.whooch.app.json.AlertsContributingEntry;
import com.whooch.app.json.AlertsEntry;
import com.whooch.app.json.AlertsFriendEntry;
import com.whooch.app.json.AlertsTrailRequestEntry;
import com.whooch.app.json.AlertsTrailingEntry;
import com.whooch.app.ui.AlertsArrayAdapter;

public class AlertsActivity extends SherlockListActivity {
    
    private AlertsArrayAdapter mAdapter;

    private ArrayList<AlertsEntry> mAlertsArray = new ArrayList<AlertsEntry>();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alerts);
        
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View title_view = inflater.inflate(R.layout.title_bar_generic, null);
		getSupportActionBar().setCustomView(title_view);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		TextView tvhead = (TextView)title_view.findViewById(R.id.header_generic_title);
		tvhead.setText("Alerts");
		 
        mAdapter = new AlertsArrayAdapter(this, mAlertsArray);
        setListAdapter(mAdapter);

    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new AlertsInitiate(), true);
        task.execute();
    }
    
    public Context getActivityContext() {
        return this;
    }

    public void refreshAlertsCallback() {
        WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new AlertsInitiate(), true);
        task.execute();
    }
    
    private class AlertsInitiate implements WhoochApiCallInterface {
        
        private String mResponseString = null;
        
		public void preExecute() {
			
		}
        
        public HttpRequestBase getHttpRequest() {
            return new HttpGet(Settings.apiUrl + "/alerts/1");
        }
        
        public void handleResponse(String responseString) {
            mResponseString = responseString;
        }
        
        public void postExecute(int statusCode) {
            
            mAlertsArray.clear();
            
            // parse the response as JSON and update the Content Array
            if (!mResponseString.equals("null")) {
                try {
                    JSONArray jsonArray = new JSONArray(mResponseString);
                    
                    for (int i=0; i<jsonArray.length(); i++) {
                        
                        JSONObject jsonAlert = jsonArray.getJSONObject(i);
                        String alertType = jsonAlert.getString("alertType");
                        
                        AlertsEntry entry = null;
                    
                        if (alertType.equals("friend")) {
                            entry = new AlertsFriendEntry(jsonArray.getJSONObject(i), getWindowManager(), AlertsActivity.this);
                        } else if (alertType.equals("trailing")) {
                            entry = new AlertsTrailingEntry(jsonArray.getJSONObject(i), getWindowManager(), AlertsActivity.this);
                        } else if (alertType.equals("contributing")) {
                            entry = new AlertsContributingEntry(jsonArray.getJSONObject(i), getWindowManager(), AlertsActivity.this);
                        } else if (alertType.equals("trailrequest")) {
                            entry = new AlertsTrailRequestEntry(jsonArray.getJSONObject(i), getWindowManager(), AlertsActivity.this);
                        } else {
                            // TODO: error handling
                            Log.d("AlertsActivity", "received unhandled alert type: " + alertType);
                        }
                        
                        if (entry != null)
                        {
                            mAlertsArray.add(entry);
                        }
                    }
    
                } catch (JSONException e) {
                    e.printStackTrace();
                    // TODO: error handling
                    
                }
            } 
            
            if(mAlertsArray.isEmpty())
            {
				TextView tvE1 = (TextView)findViewById(R.id.empty_text1);
				tvE1.setText("You do not have any alerts.");	
            }
            
            mAdapter.notifyDataSetChanged();
        }

    }
    
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

	    int itemId = item.getItemId();
	    switch (itemId) {
	    case android.R.id.home:
	        finish();
	        break;

	    }

	    return true;
	}

}