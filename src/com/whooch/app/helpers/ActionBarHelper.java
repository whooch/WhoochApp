package com.whooch.app.helpers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.whooch.app.AlertsActivity;
import com.whooch.app.CreateActivity;
import com.whooch.app.ListsActivity;
import com.whooch.app.PostFeedbackActivity;
import com.whooch.app.PostStandardActivity;
import com.whooch.app.StreamActivity;

public class ActionBarHelper {

    private static final String[] TAB_NAMES = {"Stream", "Lists", "Create", "Alerts", "Post", "Feedback"};
    
    public static void setupActionBar(ActionBar actionBar, ActionBar.TabListener tabListener, int selected) {
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(true);
        
        for (int i=0; i<TAB_NAMES.length; ++i) {
            Tab tab = actionBar.newTab();
            tab.setText(TAB_NAMES[i]);
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

        private Context mApplicationContext = null;
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
                if (!tab.getText().toString().equals(lastUnselectedTab)) {
                    
                    if (tab.getText().toString().equals(TAB_NAMES[0])) {
                        Intent i = new Intent(mApplicationContext, StreamActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mApplicationContext.startActivity(i);
                        
                    } else if (tab.getText().toString().equals(TAB_NAMES[1])) {
                        Intent i = new Intent(mApplicationContext, ListsActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mApplicationContext.startActivity(i);
                        
                    } else if (tab.getText().toString().equals(TAB_NAMES[2])) {
                        Intent i = new Intent(mApplicationContext, CreateActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mApplicationContext.startActivity(i);
                        
                    } else if (tab.getText().toString().equals(TAB_NAMES[3])) {
                        Intent i = new Intent(mApplicationContext, AlertsActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mApplicationContext.startActivity(i);
                    
                    } else if (tab.getText().toString().equals(TAB_NAMES[4])) {
                        Intent i = new Intent(mApplicationContext, PostStandardActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mApplicationContext.startActivity(i);
                    
                    } else if (tab.getText().toString().equals(TAB_NAMES[5])) {
                        Intent i = new Intent(mApplicationContext, PostFeedbackActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mApplicationContext.startActivity(i);
                    }
                }
            }
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction transaction) {
               Log.i("ActionBarHelper", "onUnselected called for tab: " + tab.getText());
               mTabDebounce = true;
               lastUnselectedTab = tab.getText().toString();
        }
    }
    
}