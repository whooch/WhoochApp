package com.whooch.app;

import java.util.ArrayList;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.whooch.app.helpers.ActionBarHelper;
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
    
    // Unique id counter to prevent Android from reusing the same dialog.
    int mNextDialogId = 0;
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

			menu.add(Menu.NONE, 1, 0, "Trail Whooch").setShowAsAction(
					MenuItem.SHOW_AS_ACTION_ALWAYS);

		return true;
	}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);
         
       ActionBarHelper.setupActionBar(getSupportActionBar(), new ActionBarHelper.TabListener(getApplicationContext()), 1);
        
        mAdapter = new WhoochProfileArrayAdapter(this, mWhoochProfileArray);
        setListAdapter(mAdapter);
    }
    
    @Override
    public void onResume() {
        super.onResume();

        ActionBarHelper.selectTab(getSupportActionBar(), 1);

        WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new ProfileInitiate(), true);
        task.execute();
    }
    
    private Context getActivityContext() {
        return this;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Bundle b = new Bundle();
        b.putInt("POSITION", position);
        showDialog(mNextDialogId++, b);
    }
    
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {

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
    
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }
    
    private class ProfileInitiate implements WhoochApiCallInterface {
        
        private String mResponseString = null;
        
        public HttpRequestBase getHttpRequest() {
            Intent i = getIntent();
            Bundle b = i.getExtras();
            mWhoochId = b.getString("WHOOCH_ID");
            if (mWhoochId == null) {
                Toast.makeText(getApplicationContext(), "Error: bad intent", Toast.LENGTH_SHORT).show();
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