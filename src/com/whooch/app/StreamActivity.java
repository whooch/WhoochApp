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
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.whooch.app.helpers.ActionBarHelper;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;
import com.whooch.app.json.StreamEntry;
import com.whooch.app.ui.StreamArrayAdapter;

import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;

public class StreamActivity extends SherlockListActivity implements OnScrollListener {
    
    private PullToRefreshListView mListView;
    
    private ArrayList<StreamEntry> mStreamArray = new ArrayList<StreamEntry>();
    private StreamArrayAdapter mAdapter;
    
    private String mLatestTimestamp = "0";
    private String mOldestTimestamp = "0";
    
    boolean mStreamInitiated = false;
    boolean mStreamHasMoreUpdates = true;
    boolean mLoadMoreItemsInProgress = false;
    
    View mLoadingFooterView;
    
    // Unique id counter to prevent Android from reusing the same dialog.
    int mNextDialogId = 0;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stream);
        
        ActionBarHelper.setupActionBar(getSupportActionBar(), new ActionBarHelper.TabListener(getApplicationContext()), 0);
        
        mListView = (PullToRefreshListView) getListView();
        mListView.setOnScrollListener(this);
        mListView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new StreamGetNewUpdates(), false);
                task.execute();
            }
        });
        
        mAdapter = new StreamArrayAdapter(this, mStreamArray, false);
        setListAdapter(mAdapter);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        ActionBarHelper.selectTab(getSupportActionBar(), 0);
        
        mStreamInitiated = false;
        mStreamHasMoreUpdates = true;
        mLoadMoreItemsInProgress = false;
        WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new StreamInitiate(), true);
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
    protected Dialog onCreateDialog(int id, Bundle b) {

        final StreamEntry entry = mStreamArray.get(b.getInt("POSITION")-1);
        
        // create the menu
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<Runnable> handlers = new ArrayList<Runnable>();
        
        SharedPreferences settings = getActivityContext().getSharedPreferences("whooch_preferences", 0);
        String currentUserName = settings.getString("username", null);
        
        if (entry.isContributor.equals("1") && !entry.userName.equalsIgnoreCase(currentUserName)) {
            names.add("React");
            handlers.add(new Runnable() {
                public void run() {
                    Log.d("StreamActivity", "React");
                    Intent i = new Intent(getApplicationContext(), PostReactionActivity.class);
                    i.putExtra("WHOOCH_ID", entry.whoochId);
                    i.putExtra("WHOOCH_NUMBER", entry.whoochNumber);
                    i.putExtra("CONTENT", entry.content);
                    i.putExtra("USER_NAME", entry.userName);
                    startActivity(i);
                }
            });
        }
        
        if (entry.isContributor.equals("0") && entry.type.equals("open")) {
            names.add("Send Feedback");
            handlers.add(new Runnable() {
                public void run() {
                    Log.d("StreamActivity", "Send Feedback");
                    Intent i = new Intent(getApplicationContext(), PostFeedbackActivity.class);
                    i.putExtra("WHOOCH_ID", entry.whoochId);
                    i.putExtra("WHOOCH_NUMBER", entry.whoochNumber);
                    i.putExtra("WHOOCH_IMAGE", entry.whoochImageUriLarge);
                    i.putExtra("WHOOCH_NAME", entry.whoochName);
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
        
        names.add("Go to Whooch");
        handlers.add(new Runnable() {
            public void run() {
                Intent i = new Intent(getApplicationContext(), WhoochActivity.class);
                i.putExtra("WHOOCH_ID", entry.whoochId);
                startActivity(i);
            }
        });

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
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        
        // the onScroll method is called A LOT.  make sure we don't fire off the
        // same update multiple times.
        if (mStreamInitiated && mStreamHasMoreUpdates && !mLoadMoreItemsInProgress) {
    
            // numPaddingItems is used to determine how early we should start loading more items
            //  if numPaddingItems == 0, don't start loading until the user actually reaches the end
            //    of the list
            //  if numPaddingItems >= 1, start loading when we are numPaddingItems away from the bottom
            //    of the list
            int numPaddingItems = 0;
            if (firstVisibleItem + visibleItemCount + numPaddingItems >= totalItemCount) {

                // The user is approaching the end of the listview, so take this time to load
                // some additional items.
                mLoadMoreItemsInProgress = true;
                WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new StreamGetMoreUpdates(), false);
                task.execute();
                
                // display a footer at the end of the listview indicating that we are loading additional items
                mLoadingFooterView = this.getLayoutInflater().inflate(R.layout.stream_loading_footer, null);
                mListView.addFooterView(mLoadingFooterView);
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }
    
    private class StreamInitiate implements WhoochApiCallInterface {
        
        private String mResponseString = null;
        
        public HttpRequestBase getHttpRequest() {
            return new HttpGet(Settings.apiUrl + "/stream/1?count=25");
        }
        
        public void handleResponse(String responseString) {
            mResponseString = responseString;
        }
        
        public void postExecute(int statusCode) {
            
            if (statusCode == 200) {
                mStreamArray.clear();
                
                // parse the response as JSON and update the Content Array
                if (!mResponseString.equals("null")) {
                    try {
                        JSONArray jsonArray = new JSONArray(mResponseString);
                        
                        // the newest updates are at the front of the array, so loop over forwards
                        for (int i=0; i<jsonArray.length(); i++) {
                            
                            // create an object that will be used to populate the List View and add it to the array
                            StreamEntry entry = new StreamEntry(jsonArray.getJSONObject(i), getWindowManager());
                            mStreamArray.add(entry);
                            
                            // TODO: not sure if this is helping, also need to figure out what size image to load
                            // preload the image that will be displayed
                            //UrlImageViewHelper.loadUrlDrawable(getApplicationContext(), entry.whoochImageUriDefault, R.drawable.ic_whooch_transparent);
                        }
        
                    } catch (JSONException e) {
                        e.printStackTrace();
                        // TODO: error handling
                    }
                } else {
                    // if it is null we don't mind, there just wasn't anything there
                }
                
                if (mStreamArray.size() > 0) {
                    mLatestTimestamp = mStreamArray.get(0).timestamp;
                    mOldestTimestamp = mStreamArray.get(mStreamArray.size() - 1).timestamp;
                }
            }
            
            mAdapter.notifyDataSetChanged();
            mListView.onRefreshComplete();
            
            mStreamInitiated = true;
        }
    }
    
    private class StreamGetNewUpdates implements WhoochApiCallInterface {
        
        private String mResponseString = null;
        
        public HttpRequestBase getHttpRequest() {
            return new HttpGet(Settings.apiUrl + "/stream/1?boundary=" + mLatestTimestamp + "&after=true");
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
                        mStreamArray.add(0, entry);
                        
                        // preload the image that will be displayed
                        //UrlImageViewHelper.loadUrlDrawable(getApplicationContext(), entry.whoochImageUriDefault, R.drawable.ic_whooch_transparent);
                    }
    
                } catch (JSONException e) {
                    e.printStackTrace();
                    // TODO: error handling
                }
            } else {
                // if it is null we don't mind, there just wasn't anything there
            }
            
            if (mStreamArray.size() > 0) {
                mLatestTimestamp = mStreamArray.get(0).timestamp;
                mOldestTimestamp = mStreamArray.get(mStreamArray.size() - 1).timestamp;
            }
            
            mAdapter.notifyDataSetChanged();
            mListView.onRefreshComplete();
        }
    }

    private class StreamGetMoreUpdates implements WhoochApiCallInterface {
        
        private String mResponseString = null;
        
        public HttpRequestBase getHttpRequest() {
            return new HttpGet(Settings.apiUrl + "/stream/1?boundary=" + mOldestTimestamp + "&before=true");
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
                        mStreamArray.add(entry);
                        
                        // preload the image that will be displayed
                        //UrlImageViewHelper.loadUrlDrawable(getApplicationContext(), entry.whoochImageUriDefault, R.drawable.ic_whooch_transparent);
                    }
    
                } catch (JSONException e) {
                    e.printStackTrace();
                    // TODO: error handling
                }
            } else {
                // if it is null then we should stop trying to get more updates
                mStreamHasMoreUpdates = false;
            }
            
            if (mStreamArray.size() > 0) {
                mLatestTimestamp = mStreamArray.get(0).timestamp;
                mOldestTimestamp = mStreamArray.get(mStreamArray.size() - 1).timestamp;
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