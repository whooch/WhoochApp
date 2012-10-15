package com.whooch.app;

import java.util.ArrayList;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.actionbarsherlock.app.SherlockListActivity;
import com.whooch.app.helpers.ActionBarHelper;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.json.PushSettingsEntry;
import com.whooch.app.ui.PushSettingsArrayAdapter;

public class PushSettingsActivity extends SherlockListActivity {
    
    String mUserId = null;
    private ArrayList<PushSettingsEntry> mPushSettingsArray = new ArrayList<PushSettingsEntry>();
    PushSettingsArrayAdapter mAdapter;

    View mLoadingFooterView;
    
    // Unique id counter to prevent Android from reusing the same dialog.
    int mNextDialogId = 0;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.push_settings);
        
        ActionBarHelper.setupActionBar(getSupportActionBar(), new ActionBarHelper.TabListener(getApplicationContext()), 4);
        
        mAdapter = new PushSettingsArrayAdapter(this, mPushSettingsArray);
        setListAdapter(mAdapter);
    }
    
    @Override
    public void onResume() {
        super.onResume();

        ActionBarHelper.selectTab(getSupportActionBar(), 4);

      //  WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new PushSettingsInitiate(), true);
      //  task.execute();
    }
    
    private Context getActivityContext() {
        return this;
    }
    
    @Override
    protected Dialog onCreateDialog(int id, Bundle b) {
        
        // create the menu
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<Runnable> handlers = new ArrayList<Runnable>();
        
        final String[] namesArray = names.toArray(new String[names.size()]);
        final Runnable[] handlersArray = handlers.toArray(new Runnable[handlers.size()]);
        
        return new AlertDialog.Builder(getActivityContext())
            .setItems(namesArray, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Log.d("UserProfileActivity", "Something Was Clicked");
                    handlersArray[which].run();
                }
            })
            .create();
    }
    
    private class PushSettingsInitiate implements WhoochApiCallInterface {
        
        private String mResponseString = null;
        
        public HttpRequestBase getHttpRequest() {
        	SharedPreferences settings = getActivityContext().getSharedPreferences("whooch_preferences", 0);
        	mUserId = settings.getString("userid", null);
            return new HttpGet(Settings.apiUrl + "/user/info?userId=" + mUserId);
        }
        
        public void handleResponse(String responseString) {
            mResponseString = responseString;
        }
         
        public void postExecute(int statusCode) {
            
            if (statusCode == 200) {
                
                // parse the response as JSON and update the Content Array
                if (!mResponseString.equals("null")) {
                    try {
                        JSONArray jsonArray = new JSONArray(mResponseString);
                        // create an object that will be used to populate the List View and add it to the array
                        PushSettingsEntry entry = new PushSettingsEntry(jsonArray.getJSONObject(0), getWindowManager());
                        mPushSettingsArray.add(entry);   
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