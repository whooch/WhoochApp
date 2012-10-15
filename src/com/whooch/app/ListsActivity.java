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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.whooch.app.helpers.ActionBarHelper;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;
import com.whooch.app.json.ListsEntry;
import com.whooch.app.ui.ListsArrayAdapter;

public class ListsActivity extends SherlockActivity {
    
    private TabHost mTabs;
    private ListView mLeadingListView;
    private ListView mContributingListView;
    private ListView mTrailingListView;
    
    private ListsArrayAdapter mLeadingAdapter;
    private ListsArrayAdapter mContributingAdapter;
    private ListsArrayAdapter mTrailingAdapter;
    
    private ArrayList<ListsEntry> mLeadingArray = new ArrayList<ListsEntry>();
    private ArrayList<ListsEntry> mContributingArray = new ArrayList<ListsEntry>();
    private ArrayList<ListsEntry> mTrailingArray = new ArrayList<ListsEntry>();
    
    private Button mLeadingButton;
    private Button mContributingButton;
    private Button mTrailingButton;
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

	//	SubMenu sub = menu.addSubMenu("Settings");
	//	sub.setIcon(R.drawable.abs__ic_menu_moreoverflow_holo_dark);

		menu.add(Menu.NONE, 1, 0, "Leading")
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		menu.add(Menu.NONE, 2, 0, "Contributing")
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		menu.add(Menu.NONE, 3, 0, "Trailing")
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		return true;
	}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lists);
        
        ActionBarHelper.setupActionBar(getSupportActionBar(), new ActionBarHelper.TabListener(getApplicationContext()), 1);
        
        // set up tabs
        mLeadingListView = (ListView) findViewById(R.id.lists_leading);
        mLeadingAdapter = new ListsArrayAdapter(this, mLeadingArray);
        mLeadingListView.setAdapter(mLeadingAdapter);
        mLeadingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), WhoochActivity.class);
                i.putExtra("WHOOCH_ID", mLeadingArray.get(position).whoochId);
                startActivity(i);
            }
        });
        
        mContributingListView = (ListView) findViewById(R.id.lists_contributing);
        mContributingAdapter = new ListsArrayAdapter(this, mContributingArray);
        mContributingListView.setAdapter(mContributingAdapter);
        mContributingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), WhoochActivity.class);
                i.putExtra("WHOOCH_ID", mContributingArray.get(position).whoochId);
                startActivity(i);
            }
        });
        
        mTrailingListView = (ListView) findViewById(R.id.lists_trailing);
        mTrailingAdapter = new ListsArrayAdapter(this, mTrailingArray);
        mTrailingListView.setAdapter(mTrailingAdapter);
        mTrailingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), WhoochActivity.class);
                i.putExtra("WHOOCH_ID", mTrailingArray.get(position).whoochId);
                startActivity(i);
            }
        });
        
        mTabs = (TabHost) findViewById(R.id.tabhost);
        mTabs.setup();
        
        TabHost.TabSpec spec = mTabs.newTabSpec("Leading");
        spec.setIndicator("Leading");
        spec.setContent(new TabContentFactory() {
            public View createTabContent(String tag) {
                WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new LoadList(1), true);
                task.execute();
                return mLeadingListView;
            }
        });
        mTabs.addTab(spec);
        
        spec = mTabs.newTabSpec("Contributing");
        spec.setIndicator("Contributing");
        spec.setContent(new TabContentFactory() {
            public View createTabContent(String tag) {
                WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new LoadList(2), true);
                task.execute();
                return mContributingListView;
            }
        });
        mTabs.addTab(spec);
        
        spec = mTabs.newTabSpec("Trailing");
        spec.setIndicator("Trailing");
        spec.setContent(new TabContentFactory() {
            public View createTabContent(String tag) {
                WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new LoadList(3), true);
                task.execute();
                return mTrailingListView;
            }
        });
        mTabs.addTab(spec);
        
        mLeadingButton = (Button) findViewById(R.id.lists_leading_button);
        mContributingButton = (Button) findViewById(R.id.lists_contributing_button);
        mTrailingButton = (Button) findViewById(R.id.lists_trailing_button);
        
        mLeadingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLeadingButton.setSelected(true);
                mContributingButton.setSelected(false);
                mTrailingButton.setSelected(false);
                mTabs.setCurrentTab(0);
            }
        });
        
        mContributingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLeadingButton.setSelected(false);
                mContributingButton.setSelected(true);
                mTrailingButton.setSelected(false);
                mTabs.setCurrentTab(1);
            }
        });
        
        mTrailingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLeadingButton.setSelected(false);
                mContributingButton.setSelected(false);
                mTrailingButton.setSelected(true);
                mTabs.setCurrentTab(2);
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        ActionBarHelper.selectTab(getSupportActionBar(), 1);
        mLeadingButton.setSelected(true);
        mContributingButton.setSelected(false);
        mTrailingButton.setSelected(false);
    }
    
    private Context getActivityContext() {
        return this;
    }
    
    private class LoadList implements WhoochApiCallInterface {
        
        private String mResponseString = null;
        private int mListId;
        private ArrayList<ListsEntry> mWorkingListArray = null;
        private ListsArrayAdapter mWorkingAdapter = null;

        LoadList(int listId) {
            mListId = listId;
            
            switch (mListId) {
            case 1:
                mWorkingListArray = mLeadingArray;
                mWorkingAdapter = mLeadingAdapter;
                break;
            case 2:
                mWorkingListArray = mContributingArray;
                mWorkingAdapter = mContributingAdapter;
                break;
            case 3:
                mWorkingListArray = mTrailingArray;
                mWorkingAdapter = mTrailingAdapter;
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
                    
                    mWorkingListArray.clear();
                    for (int i=0; i<jsonArray.length(); i++) {
                        
                        // create the class that will be used to populate the List View
                        ListsEntry entry = new ListsEntry(jsonArray.getJSONObject(i), getWindowManager());
                        mWorkingListArray.add(entry);
                        
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
            if (mWorkingListArray.size() == 0) {
                mWorkingListArray.add(new ListsEntry());
            }
            
            mWorkingAdapter.notifyDataSetChanged();
        }
    }

}