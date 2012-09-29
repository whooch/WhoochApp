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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.whooch.app.helpers.ActionBarHelper;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;
import com.whooch.app.json.StreamEntry;
import com.whooch.app.ui.StreamArrayAdapter;

import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;

public class WhoochActivity extends SherlockListActivity implements OnScrollListener {
    
    private PullToRefreshListView mListView;
    
    private ArrayList<StreamEntry> mWhoochArray = new ArrayList<StreamEntry>();
    private StreamArrayAdapter mAdapter;
    
    boolean mWhoochInitiated = false;
    boolean mWhoochHasMoreUpdates = true;
    boolean mLoadMoreItemsInProgress = false;
    
    private String mLatestWhoochNum = "0";
    private String mOldestWhoochNum = "0";
    
    private String mWhoochId;

    View mLoadingFooterView;
    
    // Unique id counter to prevent Android from reusing the same dialog.
    int mNextDialogId = 0;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stream);
        
        ActionBarHelper.setupActionBar(getSupportActionBar(), new ActionBarHelper.TabListener(getApplicationContext()), 1);
        
        mListView = (PullToRefreshListView) getListView();
        mListView.setOnScrollListener(this);
        mListView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new WhoochGetNewUpdates(), false);
                task.execute();
            }
        });
        
        mAdapter = new StreamArrayAdapter(this, mWhoochArray, true);
        setListAdapter(mAdapter);
        
        Intent i = getIntent();
        Bundle b = i.getExtras();
        mWhoochId = b.getString("WHOOCH_ID");
        if (mWhoochId == null) {
            Toast.makeText(getApplicationContext(), "Error: bad intent", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();

        ActionBarHelper.selectTab(getSupportActionBar(), 1);
        
        mWhoochInitiated = false;
        mWhoochHasMoreUpdates = true;
        mLoadMoreItemsInProgress = false;
        WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new WhoochInitiate(), true);
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
                
        if (mWhoochInitiated && mWhoochHasMoreUpdates && !mLoadMoreItemsInProgress) {
            
            int numPaddingItems = 0;
            if (firstVisibleItem + visibleItemCount + numPaddingItems >= totalItemCount) {
                
                mLoadMoreItemsInProgress = true;
                WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new WhoochGetMoreUpdates(), false);
                task.execute();
                
                mLoadingFooterView = this.getLayoutInflater().inflate(R.layout.stream_loading_footer, null);
                mListView.addFooterView(mLoadingFooterView);
            }
        }
    }
    
    @Override
    protected Dialog onCreateDialog(int id, Bundle b) {

        final StreamEntry entry = mWhoochArray.get(b.getInt("POSITION")-1);
        
        // create the menu
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<Runnable> handlers = new ArrayList<Runnable>();
        
        SharedPreferences settings = getActivityContext().getSharedPreferences("whooch_preferences", 0);
        String currentUserName = settings.getString("username", null);
        
        if (entry.isContributor.equals("1") && !entry.userName.equalsIgnoreCase(currentUserName)) {
            names.add("React");
            handlers.add(new Runnable() {
                public void run() {
                    Intent i = new Intent(getApplicationContext(), PostStandardActivity.class);
                    
                    // for now just pass necessary fields.  If this grows, it could be
                    // changed to pass the entire StreamEntry object.
                    i.putExtra("WHOOCH_ID", entry.whoochId);
                    i.putExtra("WHOOCH_NUMBER", entry.whoochNumber);
                    i.putExtra("CONTENT", entry.content);
                    i.putExtra("USER_NAME", entry.userName);
                    startActivity(i);
                }
            });
        }
        
        if (entry.reactionType.equals("whooch")) {
            names.add("Show Conversation");
            handlers.add(new Runnable() {
                public void run() {
                    Log.d("StreamActivity", "Show Conversation");
                }
            });
        }
        
        if (entry.userName.equalsIgnoreCase(currentUserName)) {
            names.add("Delete Update");
            handlers.add(new Runnable() {
                public void run() {
                    Log.d("StreamActivity", "Delete Update");
                }
            });
        }
        
        if (!entry.image.equals("null")) {
            names.add("View Photo");
            handlers.add(new Runnable() {
                public void run() {
                    Log.d("StreamActivity", "View Photo");
                }
            });
        }

        final String[] namesArray = names.toArray(new String[names.size()]);
        final Runnable[] handlersArray = handlers.toArray(new Runnable[handlers.size()]);
        
        return new AlertDialog.Builder(getActivityContext())
            .setItems(namesArray, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Log.d("StreamActivity", "Something Was Clicked");
                    handlersArray[which].run();
                }
            })
            .create();
    }
    
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }
    
    private class WhoochInitiate implements WhoochApiCallInterface {
        
        private String mResponseString = null;
        
        public HttpRequestBase getHttpRequest() {
            return new HttpGet(Settings.apiUrl + "/whooch/" + mWhoochId + "?count=25");
        }
        
        public void handleResponse(String responseString) {
            mResponseString = responseString;
        }
        
        public void postExecute(int statusCode) {
            
            if (statusCode == 200) {
                mWhoochArray.clear();
                
                // parse the response as JSON and update the Content Array
                if (!mResponseString.equals("null")) {
                    try {
                        JSONArray jsonArray = new JSONArray(mResponseString);
                        
                        // the newest updates are at the front of the array, so loop over forwards
                        for (int i=0; i<jsonArray.length(); i++) {
                            
                            // create an object that will be used to populate the List View and add it to the array
                            StreamEntry entry = new StreamEntry(jsonArray.getJSONObject(i), getWindowManager());
                            mWhoochArray.add(entry);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        // TODO: error handling
                    }
                } else {
                    // if it is null we don't mind, there just wasn't anything there
                }
                
                if (mWhoochArray.size() > 0) {
                    mLatestWhoochNum = mWhoochArray.get(0).whoochNumber;
                    mOldestWhoochNum = mWhoochArray.get(mWhoochArray.size() - 1).whoochNumber;
                }
            }
            
            mAdapter.notifyDataSetChanged();
            mListView.onRefreshComplete();
            
            mWhoochInitiated = true;
        }
    }
    
    private class WhoochGetNewUpdates implements WhoochApiCallInterface {
        
        private String mResponseString = null;
        
        public HttpRequestBase getHttpRequest() {
            return new HttpGet(Settings.apiUrl + "/whooch/" + mWhoochId + "?boundary=" + mLatestWhoochNum + "&after=true");
        }
        
        public void handleResponse(String responseString) {
            mResponseString = responseString;
        }
        
        public void postExecute(int statusCode) {
            
            // parse the response as JSON and update the Content Array
            if (!mResponseString.equals("null")) {
                try {
                    JSONArray jsonArray = new JSONArray(mResponseString);
                    
                    // the latest updates are at the end of the array, so we'll loop over the array forwards and
                    // prepend the each update as we parse it, from oldest to newest.
                    for (int i=0; i<jsonArray.length(); i++) {
                        
                        // create an object that will be used to populate the List View and prepend it to the array
                        StreamEntry entry = new StreamEntry(jsonArray.getJSONObject(i), getWindowManager());
                        mWhoochArray.add(0, entry);
                    }
    
                } catch (JSONException e) {
                    e.printStackTrace();
                    // TODO: error handling
                }
            } else {
                // if it is null we don't mind, there just wasn't anything there
            }
            
            if (mWhoochArray.size() > 0) {
                mLatestWhoochNum = mWhoochArray.get(0).whoochNumber;
                mOldestWhoochNum = mWhoochArray.get(mWhoochArray.size() - 1).whoochNumber;
            }
            
            mAdapter.notifyDataSetChanged();
            mListView.onRefreshComplete();
        }
    }

    private class WhoochGetMoreUpdates implements WhoochApiCallInterface {
        
        private String mResponseString = null;
        
        public HttpRequestBase getHttpRequest() {
            return new HttpGet(Settings.apiUrl + "/whooch/" + mWhoochId + "?boundary=" + mOldestWhoochNum + "&before=true");
        }
        
        public void handleResponse(String responseString) {
            mResponseString = responseString;
        }
        
        public void postExecute(int statusCode) {
            
            // parse the response as JSON and update the Content Array
            if (!mResponseString.equals("null")) {
                try {
                    JSONArray jsonArray = new JSONArray(mResponseString);
                    
                    // the newest updates are at the front of the array, so loop over forwards
                    for (int i=0; i<jsonArray.length(); i++) {
                        
                        // create an object that will be used to populate the List View and add it to the array
                        StreamEntry entry = new StreamEntry(jsonArray.getJSONObject(i), getWindowManager());
                        mWhoochArray.add(entry);
                        
                        // preload the image that will be displayed
                        //UrlImageViewHelper.loadUrlDrawable(getApplicationContext(), entry.whoochImageUriDefault, R.drawable.ic_whooch_transparent);
                    }
    
                } catch (JSONException e) {
                    e.printStackTrace();
                    // TODO: error handling
                }
            } else {
                // if it is null then we should stop trying to get more updates
                mWhoochHasMoreUpdates = false;
            }
            
            if (mWhoochArray.size() > 0) {
                mLatestWhoochNum = mWhoochArray.get(0).whoochNumber;
                mOldestWhoochNum = mWhoochArray.get(mWhoochArray.size() - 1).whoochNumber;
            }
            
            mAdapter.notifyDataSetChanged();
            mListView.onRefreshComplete();

            // dismiss the 'loading more items' footer
            mListView.removeFooterView(mLoadingFooterView);
            
            // set flag to false so onScroll method can be called again
            mLoadMoreItemsInProgress = false;
        }
    }
    
}