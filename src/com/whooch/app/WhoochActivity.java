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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.koushikdutta.urlimageviewhelper.UrlImageGetter;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.whooch.app.helpers.ActionBarHelper;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;
import com.whooch.app.helpers.WhoochHelperFunctions;
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
    
    private String mShowConvoId = null;
    private String mShowConvoNumber = null;
    private StreamEntry mShowConvoCurrentUpdate = null;
    
    private String mDeleteWhoochId = null;
    private String mDeleteWhoochNumber = null;
    
    private String mLatestWhoochNum = "0";
    private String mOldestWhoochNum = "0";
    
    private String mWhoochId;
    private String mWhoochName;
    private String mWhoochImage;

    View mLoadingFooterView;
    
    // Unique id counter to prevent Android from reusing the same dialog.
    int mNextDialogId = 0;
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(Menu.NONE, 1, 0, "Update").setIcon(android.R.drawable.ic_menu_edit)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		menu.add(Menu.NONE, 2, 0, "Search").setIcon(android.R.drawable.ic_menu_search)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		menu.add(Menu.NONE, 3, 0, "Create").setIcon(android.R.drawable.ic_input_add)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		return true;
	}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.whooch);
        
        ActionBarHelper.setupActionBar(getSupportActionBar(), new ActionBarHelper.TabListener(getApplicationContext()), 1);
        
        mListView = (PullToRefreshListView) getListView();
        mListView.setOnScrollListener(this);
        mListView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
              //  WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new WhoochGetNewUpdates(), false);
              //  task.execute();
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
        
    	Button ibtn1 = (Button) findViewById(R.id.whooch_options);
    	if(ibtn1 != null)
    	{
        	//unable to find ID - let it go for now
            ibtn1.setOnClickListener(getWhoochProfileClickListener());
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
                    
                    Intent i = new Intent(getApplicationContext(), PostReactionActivity.class);
                    i.putExtra("WHOOCH_ID", entry.whoochId);
                    i.putExtra("REACTION_TO", entry.whoochNumber);
                    i.putExtra("REACTION_TYPE", "whooch");
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
                    Log.d("WhoochActivity", "Show Conversation");
                    mShowConvoId = entry.whoochId;
                    mShowConvoNumber = entry.reactionTo;
                    mShowConvoCurrentUpdate = entry;
                    
                    WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new ShowConversation(), true);
                    task.execute();
                }
            });
        }
        
        if (!entry.image.equals("null")) {
            names.add("View Photo");
            handlers.add(new Runnable() {
                public void run() {
                    Log.d("WhoochActivity", "View Photo");
                    Intent i = new Intent(getApplicationContext(), ViewPhotoActivity.class);
                    i.putExtra("WHOOCH_ID", entry.whoochId);
                    i.putExtra("WHOOCH_NUMBER", entry.whoochNumber);
                    i.putExtra("IMAGE_TYPE", "whooch");
                    i.putExtra("IMAGE_NAME", entry.image);
                    startActivity(i);
                }
            });
        }
        
        if (entry.userName.equalsIgnoreCase(currentUserName)) {
            names.add("Delete Update");
            handlers.add(new Runnable() {
                public void run() {
                    Log.d("WhoochActivity", "Delete Update");
                    
                    AlertDialog.Builder builder = new AlertDialog.Builder(
        					getActivityContext());
                    
                    builder.setTitle("Whooch");
                    builder.setMessage("Are you sure you want to delete this update?");
                    
                    builder.setNegativeButton("CANCEL",
        					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {

						}
					});
                    
        			builder.setPositiveButton("OK",
        					new DialogInterface.OnClickListener() {
        						public void onClick(DialogInterface dialog, int id) {
        							mDeleteWhoochId = entry.whoochId;
        							mDeleteWhoochNumber = entry.whoochNumber;
        							
        		                    WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new DeleteUpdate(), true);
        		                    task.execute();
        						}
        					});
        	        
        		    
        			AlertDialog dialog = builder.create();

        			dialog.show();
                }
            });
        }

        if(names.size() > 0)
        {
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
        else
        {
        	return null;
        }
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
                        
                        StreamEntry entry = new StreamEntry(jsonArray.getJSONObject(0), getWindowManager());
                        mWhoochName = entry.whoochName;
                        mWhoochImage = entry.whoochImageUriLarge;
                        if(entry.isContributor.equals("0"))
                        {
                        	Button ibtn1 = (Button) findViewById(R.id.whooch_send_feedback);
                        	ibtn1.setVisibility(View.VISIBLE);
                            ibtn1.setOnClickListener(getSendFeedbackClickListener());
                        }
                        else
                        {
                        	Button ibtn1 = (Button) findViewById(R.id.whooch_update);
                        	ibtn1.setVisibility(View.VISIBLE);
                        	ibtn1.setOnClickListener(getWhoochUpdateClickListener());
                        }
                        
                        // the newest updates are at the front of the array, so loop over forwards
                        for (int i=0; i<jsonArray.length(); i++) {
                            
                            // create an object that will be used to populate the List View and add it to the array
                            entry = new StreamEntry(jsonArray.getJSONObject(i), getWindowManager());
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
    
    public OnClickListener getWhoochProfileClickListener(){
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
            	Intent i = new Intent(v.getContext(), WhoochProfileActivity.class);
            	i.putExtra("WHOOCH_ID", mWhoochId);
            	v.getContext().startActivity(i);
            }
        };
    	
    }
    
    public OnClickListener getSendFeedbackClickListener(){
        return new OnClickListener() {
            @Override
            public void onClick(View v) {            	
                Intent i = new Intent(getApplicationContext(), PostFeedbackActivity.class);
                i.putExtra("WHOOCH_ID", mWhoochId);
                i.putExtra("WHOOCH_IMAGE", mWhoochImage);
                i.putExtra("WHOOCH_NAME", mWhoochName);
                v.getContext().startActivity(i);
            }
        };
    	
    }
    
    public OnClickListener getWhoochUpdateClickListener(){
        return new OnClickListener() {
            @Override
            public void onClick(View v) {            	
                Intent i = new Intent(getApplicationContext(), PostStandardActivity.class);
                i.putExtra("WHOOCH_ID", mWhoochId);
                i.putExtra("WHOOCH_IMAGE", mWhoochImage);
                i.putExtra("WHOOCH_NAME", mWhoochName);
                i.putExtra("UPDATE_TYPE", "whooch");
                v.getContext().startActivity(i);
            }
        };
    	
    }
    
 private class ShowConversation implements WhoochApiCallInterface {
        
        private String mResponseString = null;
        
        public HttpRequestBase getHttpRequest() {
            return new HttpGet(Settings.apiUrl + "/whooch/" + mShowConvoId + "?single=1&whoochNumber=" + mShowConvoNumber);
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
                        StreamEntry entry = new StreamEntry(jsonArray.getJSONObject(0), getWindowManager());
                        
            			AlertDialog.Builder builder = new AlertDialog.Builder(
            					getActivityContext());

            			builder.setPositiveButton("OK",
            					new DialogInterface.OnClickListener() {
            						public void onClick(DialogInterface dialog, int id) {
            		                    mShowConvoId = null;
            		                    mShowConvoNumber = null;
            							dialog.dismiss();
            						}
            					});

            			LayoutInflater inflater = (LayoutInflater)getActivityContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            			
            			View view = inflater.inflate(R.layout.show_conversation, null);
            			
            			ImageView iv1A = (ImageView) view.findViewById(R.id.entry_whooch_imageA);
            			UrlImageViewHelper.setUrlDrawable(iv1A, entry.whoochImageUriLarge);
            			
            	        TextView tv1A = (TextView) view.findViewById(R.id.entry_whooch_titleA);
            	        tv1A.setText(entry.whoochName);
            	        
            	        TextView tv2A = (TextView) view.findViewById(R.id.entry_posted_userA);
            	        tv2A.setText(entry.userName);
            	        
            	        TextView tv3A = (TextView) view.findViewById(R.id.entry_whooch_contentA);
            	        UrlImageGetter imageGetter = new UrlImageGetter(tv3A, getActivityContext()); 
            	        
            	        Spanned htmlSpan = Html.fromHtml(entry.content.replaceAll(">\\s+<", "><"), imageGetter, null);
            	       
            	        tv3A.setText(htmlSpan);
            	        
            	        TextView tv4A = (TextView) view.findViewById(R.id.entry_whooch_footA);
            	        tv4A.setText(WhoochHelperFunctions.toRelativeTime(Long.parseLong(entry.timestamp)));
            	        
            			ImageView iv1B = (ImageView) view.findViewById(R.id.entry_whooch_imageB);
            			UrlImageViewHelper.setUrlDrawable(iv1B, mShowConvoCurrentUpdate.whoochImageUriLarge);
            			
            	        TextView tv1B = (TextView) view.findViewById(R.id.entry_whooch_titleB);
            	        tv1B.setText(mShowConvoCurrentUpdate.whoochName);
            			
            	        TextView tv2B = (TextView) view.findViewById(R.id.entry_posted_userB);
            	        tv2B.setText(mShowConvoCurrentUpdate.userName);
            	        
            	        TextView tv3B = (TextView) view.findViewById(R.id.entry_whooch_contentB);
            	        imageGetter = new UrlImageGetter(tv3B, getActivityContext()); 
            	        
            	        htmlSpan = Html.fromHtml(mShowConvoCurrentUpdate.content.replaceAll(">\\s+<", "><"), imageGetter, null);
            	       
            	        tv3B.setText(htmlSpan);
            	        
            	        TextView tv4B = (TextView) view.findViewById(R.id.entry_whooch_footB);
            	        tv4B.setText(WhoochHelperFunctions.toRelativeTime(Long.parseLong(mShowConvoCurrentUpdate.timestamp)));
            	        
            		    builder.setView(view);



            			AlertDialog dialog = builder.create();

            			dialog.show();
        
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
    
    private class DeleteUpdate implements WhoochApiCallInterface {
        
        public HttpRequestBase getHttpRequest() {
            HttpPost request = new HttpPost(Settings.apiUrl + "/whooch/delete");
            
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();    
            nameValuePairs.add(new BasicNameValuePair("whoochId", mDeleteWhoochId));
            nameValuePairs.add(new BasicNameValuePair("whoochNumber", mDeleteWhoochNumber));
            
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
            
        	if (statusCode == 200) {
                WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new WhoochInitiate(), false);
                task.execute();
        	}
        	
        	mDeleteWhoochId = null;
        	mDeleteWhoochNumber = null;
            
        }
    }
    
}