package com.whooch.app.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.whooch.app.CreateActivity;
import com.whooch.app.FeedbackActivity;
import com.whooch.app.ListsActivity;
import com.whooch.app.PostStandardActivity;
import com.whooch.app.R;
import com.whooch.app.ReactionsActivity;
import com.whooch.app.SearchActivity;
import com.whooch.app.StreamActivity;
import com.whooch.app.UserProfileActivity;

public class ActionBarHelper {

    private static final String[] TAB_NAMES = {"Stream", "Whooch", "Feedback", "Reactions", "You"};
    protected static Context mApplicationContext = null;
    
    public static void setupActionBar(ActionBar actionBar, ActionBar.TabListener tabListener, int selected) {
    	
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(true);
    	
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowCustomEnabled(true);

		LayoutInflater inflater = (LayoutInflater) mApplicationContext.getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);

		View view = inflater.inflate(
				R.layout.title_bar, null);
        
        
        actionBar.setCustomView(view);
        
		Button btn1 = (Button) view.findViewById(R.id.main_create);
		btn1.setOnClickListener(getCreateWhoochClickListener());

		ImageButton ibtn2 = (ImageButton) view.findViewById(R.id.main_search);
		ibtn2.setOnClickListener(getSearchClickListener());
		
		ImageButton ibtn3 = (ImageButton) view.findViewById(R.id.main_update);
		ibtn3.setOnClickListener(getUpdateClickListener());
        
        for (int i=0; i<TAB_NAMES.length; ++i) {
            Tab tab = actionBar.newTab();
            tab.setTag(TAB_NAMES[i]);
            
            if(TAB_NAMES[i].equals("Stream"))
            {
            	tab.setCustomView(R.layout.stream_tab);
            }
            else if(TAB_NAMES[i].equals("Whooch"))
            {
            	tab.setCustomView(R.layout.whooch_tab);
            }
            else if(TAB_NAMES[i].equals("Feedback"))
            {
            	tab.setCustomView(R.layout.feedback_tab);
            }
            else if(TAB_NAMES[i].equals("Reactions"))
            {
            	tab.setCustomView(R.layout.reactions_tab);
            }
            else if(TAB_NAMES[i].equals("You"))
            {
            	tab.setCustomView(R.layout.you_tab);
            }
            
            tab.setTabListener(tabListener);
            if (selected == i) {
                actionBar.addTab(tab, true);
            } else {
                actionBar.addTab(tab, false);
            }
        }
        
    }
    
    public static void selectTab(ActionBar actionBar, int index) {
        if (actionBar.getSelectedTab().getPosition() != index) {
            Tab tab = actionBar.getTabAt(index);
            actionBar.selectTab(tab);
        }
    }
    
    public static class TabListener implements ActionBar.TabListener {

        private boolean mTabDebounce = false;
        private String lastUnselectedTab = "";
        
        public TabListener(Context appContext) {
            mApplicationContext = appContext;
            mTabDebounce = false;
        }
        
        @Override
        public void onTabReselected(Tab tab, FragmentTransaction transaction) {
               Log.i("ActionBarHelper", "onTabReselected called for tab: " + tab.getText());
               
               mTabDebounce = true; // no tab has been unselected, but this is an explicit selection.
               
               onTabSelected(tab, transaction);
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction transaction) {
            Log.i("ActionBarHelper", "onTabSelected called for tab: " + tab.getText());
            
            // check to prevent an activity from firing when the action bar is first created
            if (mTabDebounce) {
                
                // check to prevent strange behavior when the back button is used, where the 
                // tab from the previous activity is unselected and then selected right after.
                if (!tab.getTag().toString().equals(lastUnselectedTab)) {
                    
                    if (tab.getTag().toString().equals(TAB_NAMES[0])) {
                        Intent i = new Intent(mApplicationContext, StreamActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mApplicationContext.startActivity(i);
                        
                    } else if (tab.getTag().toString().equals(TAB_NAMES[1])) {
                        Intent i = new Intent(mApplicationContext, ListsActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mApplicationContext.startActivity(i);
                        
                    } else if (tab.getTag().toString().equals(TAB_NAMES[2])) {
                        Intent i = new Intent(mApplicationContext, FeedbackActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mApplicationContext.startActivity(i);
                        
                    } else if (tab.getTag().toString().equals(TAB_NAMES[3])) {
                        Intent i = new Intent(mApplicationContext, ReactionsActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mApplicationContext.startActivity(i);
                        
                    } else if (tab.getTag().toString().equals(TAB_NAMES[4])) {
                        Intent i = new Intent(mApplicationContext, UserProfileActivity.class);
                    	SharedPreferences settings = mApplicationContext.getSharedPreferences("whooch_preferences", 0);
                    	String userId = settings.getString("userid", null);
                    	i.putExtra("USER_ID", userId);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mApplicationContext.startActivity(i);
                        
                    }
                }
            }
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction transaction) {
               Log.i("ActionBarHelper", "onUnselected called for tab: " + tab.getTag());
               mTabDebounce = true;
               lastUnselectedTab = tab.getTag().toString();
        }
    }
    
    public static OnClickListener getCreateWhoochClickListener(){
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
            	Intent i = new Intent(v.getContext(), CreateActivity.class);
            	i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            	v.getContext().startActivity(i);
            }
        };
    	
    }
    
    public static OnClickListener getSearchClickListener(){
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
            	Intent i = new Intent(v.getContext(), SearchActivity.class);
            	i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            	v.getContext().startActivity(i);
            }
        };
    }
        
    public static OnClickListener getUpdateClickListener(){
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
              	Intent i = new Intent(v.getContext(), PostStandardActivity.class);
                i.putExtra("UPDATE_TYPE", "regular");
              	i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              	v.getContext().startActivity(i);
            }
        };
    }
    
}