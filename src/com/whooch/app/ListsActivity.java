package com.whooch.app;

import java.util.ArrayList;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.whooch.app.helpers.ActionBarHelper;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;
import com.whooch.app.json.ListsEntry;
import com.whooch.app.ui.ListsArrayAdapter;

public class ListsActivity extends SherlockActivity {

    private ListView mWorkingListView;
    
    private ArrayList<ListsEntry> mLeadingArray = new ArrayList<ListsEntry>();
    private ArrayList<ListsEntry> mContributingArray = new ArrayList<ListsEntry>();
    private ArrayList<ListsEntry> mTrailingArray = new ArrayList<ListsEntry>();
    
    private ArrayList<ListsEntry> mWorkingListArray = new ArrayList<ListsEntry>();
    private ListsArrayAdapter mWorkingAdapter = null;
    
    private Button mLeadingButton = null;
    private Button mContributingButton = null;
    private Button mTrailingButton = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lists);
        
        ActionBarHelper.setupActionBar(getSupportActionBar(), new ActionBarHelper.TabListener(getApplicationContext()), 1);
        
        // set up tabs
        mWorkingListView = (ListView) findViewById(R.id.lists_main);
        mWorkingAdapter = new ListsArrayAdapter(this, mWorkingListArray);
        mWorkingListView.setAdapter(mWorkingAdapter);
        mWorkingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), WhoochActivity.class);
                i.putExtra("WHOOCH_ID", mWorkingListArray.get(position).whoochId);
                startActivity(i);
            }
        });
        
		WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new LoadList(1), true);
		task.execute();
		
		mLeadingButton = (Button) findViewById(R.id.leading_action);
		mContributingButton = (Button) findViewById(R.id.contributing_action);
		mTrailingButton = (Button) findViewById(R.id.trailing_action);
		
		mLeadingButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				 if (!mLeadingButton.isSelected()){
			            mLeadingButton.setSelected(true);
			            mContributingButton.setSelected(false);
			            mTrailingButton.setSelected(false);
			            
						if(mLeadingArray.isEmpty())
						{
							mWorkingListArray.clear();
							mWorkingAdapter.notifyDataSetChanged();
							WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new LoadList(1), true);
							task.execute();
						}
						else
						{
							setWhoochList(mLeadingArray);
							mWorkingAdapter.notifyDataSetChanged();
						}
			        } 	
			}
			
		});
		
		mContributingButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				 if (!mContributingButton.isSelected()){
			            mLeadingButton.setSelected(false);
			            mContributingButton.setSelected(true);
			            mTrailingButton.setSelected(false);
			            
						if(mContributingArray.isEmpty())
						{
							mWorkingListArray.clear();
							mWorkingAdapter.notifyDataSetChanged();
							WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new LoadList(2), true);
							task.execute();
						}
						else
						{
							setWhoochList(mContributingArray);
							mWorkingAdapter.notifyDataSetChanged();
						}
			        } 
			}
			
		});
		
		mTrailingButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				 if (!mTrailingButton.isSelected()){
			            mLeadingButton.setSelected(false);
			            mContributingButton.setSelected(false);
			            mTrailingButton.setSelected(true);
			            
						if(mTrailingArray.isEmpty())
						{
							mWorkingListArray.clear();
							mWorkingAdapter.notifyDataSetChanged();
							WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new LoadList(3), true);
							task.execute();
						}
						else
						{
							setWhoochList(mTrailingArray);
							mWorkingAdapter.notifyDataSetChanged();
						}
			        } 
			}
			
		});
		
        mLeadingButton.setSelected(true);
	
    }
    
    @Override
    public void onResume() {
        super.onResume();
        ActionBarHelper.selectTab(getSupportActionBar(), 1);
    }
    
    private Context getActivityContext() {
        return this;
    }
    
    private class LoadList implements WhoochApiCallInterface {
        
        private String mResponseString = null;
        private int mListId;
        private ArrayList<ListsEntry> mLocalArray = null;

        public void preExecute() {}
        
        LoadList(int listId) {
            mListId = listId;
            
            switch (mListId) {
            case 1:
                mLocalArray = mLeadingArray;
                break;
            case 2:
                mLocalArray = mContributingArray;
                break;
            case 3:
                mLocalArray = mTrailingArray;
                break;
            }
        }
                
        public HttpRequestBase getHttpRequest() {
            return new HttpGet(Settings.apiUrl + "/list/" + mListId);
        }
        
        public void handleResponse(String responseString) {
            mResponseString = responseString;
        }
        
        public void postExecute(int statusCode) {
            
            // parse the response as JSON and update the Content Array
            if (!mResponseString.equals("null")) {
                // parse the response as JSON and update the Content Array
                try {
                    JSONArray jsonArray = new JSONArray(mResponseString);
                    
                    mLocalArray.clear();
                    for (int i=0; i<jsonArray.length(); i++) {
                        
                        // create the class that will be used to populate the List View
                        ListsEntry entry = new ListsEntry(jsonArray.getJSONObject(i), getWindowManager());
                        mLocalArray.add(entry);
                        
                        // pre-load the image that will be displayed
                        UrlImageViewHelper.loadUrlDrawable(getApplicationContext(), entry.whoochImageUriDefault);
                    }
                    
                } catch (JSONException e) {
                    e.printStackTrace();
                    // TODO: error handling
                }
            } else {
                // if it is null we don't mind, there just wasn't anything there
            }
            
            // if the list is empty, add an empty entry.  The adapter will see this and display that there
            // are no items in the list
            if (mLocalArray.size() == 0) {
                mLocalArray.add(new ListsEntry());
            }
            
            setWhoochList(mLocalArray);
            mWorkingAdapter.notifyDataSetChanged();
        }
    }
    
    private void setWhoochList(ArrayList<ListsEntry> temp)
    {
    	mWorkingListArray.clear();
    	for(int i=0; i<temp.size(); i++)
    	{
    		mWorkingListArray.add(temp.get(i));
    	}
    }

}