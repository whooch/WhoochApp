package com.whooch.app;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.whooch.app.helpers.ActionBarHelper;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;
import com.whooch.app.json.FriendsEntry;
import com.whooch.app.ui.FriendsArrayAdapter;

public class InviteUserActivity extends SherlockListActivity {
    
    String mUserId = null;
    private ArrayList<FriendsEntry> mFriendsArray = new ArrayList<FriendsEntry>();
    ArrayAdapter<FriendsEntry> mAdapter;
    protected Spinner mFriendsSelector;
    private Button mInviteButton;

    View mLoadingFooterView;
    
    // Unique id counter to prevent Android from reusing the same dialog.
    int mNextDialogId = 0;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.whooch_invite_user);
        
        ActionBarHelper.setupActionBar(getSupportActionBar(), new ActionBarHelper.TabListener(getApplicationContext()), 1);

        // add the invalid "No Whooch Selected" entry.  This will be rendered by the adapter.
        mFriendsArray.add(new FriendsEntry());
        
        mAdapter = new FriendsArrayAdapter(this, android.R.layout.simple_spinner_item, mFriendsArray);

        mFriendsSelector = (Spinner) findViewById(R.id.whooch_invite_spinner);
        mFriendsSelector.setAdapter(mAdapter);
        
        mInviteButton = (Button) findViewById(R.id.whooch_user_invite_button);
        mInviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFriendsSelector.getSelectedItemPosition() == 0) {
                    // User selected the placeholder item
                    Toast.makeText(getActivityContext(), "Select a friend to invite", Toast.LENGTH_SHORT).show();
                } else {
                    WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new Invite(), true);
                    task.execute();
                }
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();

        ActionBarHelper.selectTab(getSupportActionBar(), 1);

        WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new InviteUserInitiate(), true);
        task.execute();
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
                    Log.d("InviteUserActivity", "Something Was Clicked");
                    handlersArray[which].run();
                }
            })
            .create();
    }
    
    private class InviteUserInitiate implements WhoochApiCallInterface {
        
        private String mResponseString = null;
        
        public void preExecute() {}
        
        public HttpRequestBase getHttpRequest() {
            return new HttpGet(Settings.apiUrl + "/friends/1");
        }
        
        public void handleResponse(String responseString) {
            mResponseString = responseString;
        }
         
        public void postExecute(int statusCode) {
            
            if (statusCode == 200) {
                mFriendsArray.clear();
                mFriendsArray.add(new FriendsEntry());
                
                // parse the response as JSON and update the Content Array
                if (!mResponseString.equals("null")) {
                	
                	try {
						JSONArray jsonArray = new JSONArray(mResponseString);
						for (int i=0; i<jsonArray.length(); i++) {
							JSONObject jsonObject = jsonArray.getJSONObject(i);
							FriendsEntry entry = new FriendsEntry(jsonObject, getWindowManager());
							mFriendsArray.add(entry);
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.e("testlog", e.toString());
					}
                	
                } else {
                    // if it is null we don't mind, there just wasn't anything there
                }
                
            }
            
        }
        
    }
    
    private class Invite implements WhoochApiCallInterface {

        public void preExecute() {}
        
        public HttpRequestBase getHttpRequest() {
            
            HttpPost request = new HttpPost(Settings.apiUrl + "/whooch/add/fake");
            
            FriendsEntry selectedEntry = (FriendsEntry) mFriendsSelector.getSelectedItem();
            
            // Add data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();    
            nameValuePairs.add(new BasicNameValuePair("userId", selectedEntry.userId));

            Log.e("invite id", selectedEntry.userId);
            /*try {
                request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                // TODO error handling
            }*/

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