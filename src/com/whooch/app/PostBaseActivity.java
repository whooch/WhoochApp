package com.whooch.app;

import java.util.ArrayList;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;

public class PostBaseActivity extends SherlockActivity {
    
    protected LinearLayout mUserSearchLayout;
    protected AutoCompleteTextView mSearchUsersAutoText;
    
    protected LinearLayout mWhoochSelectorLayout;
    protected Spinner mWhoochSelector;
    protected TextView mReactingToText;
    protected RelativeLayout mWhoochFeedbackLayout;
    protected ImageView mWhoochImage;
    protected TextView mWhoochName;
    
    protected EditText mPostText;
    protected TextView mCharCountText;
    protected Button mCameraButton;
    protected Button mSubmitButton;

    protected ArrayList<String> mSearchUsersArray = new ArrayList<String>();
    protected ArrayAdapter<String> mSearchUsersAdapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post);
        
        /////////////////
        // User Search //
        /////////////////
        mUserSearchLayout = (LinearLayout) findViewById(R.id.post_user_search_layout);
        
        mSearchUsersAdapter = new ArrayAdapter<String>(getActivityContext(), android.R.layout.simple_dropdown_item_1line, mSearchUsersArray);
        mSearchUsersAutoText = (AutoCompleteTextView) findViewById(R.id.post_search_users);
        mSearchUsersAutoText.setAdapter(mSearchUsersAdapter);
        mSearchUsersAutoText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String userString = ">" + mSearchUsersArray.get(position) + " ";
                int start = mPostText.getSelectionStart();
                int end = mPostText.getSelectionEnd();
                mPostText.getText().replace(Math.min(start, end), Math.max(start, end),
                        userString, 0, userString.length());
            }
        });
        
        mSearchUsersAutoText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new GetUsers(s.toString()), false);
                task.execute();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        
        ////////////////////////////////
        // Post Type Specific Content //
        ////////////////////////////////
        mWhoochSelectorLayout = (LinearLayout) findViewById(R.id.post_select_whooch_layout);
        mWhoochSelector = (Spinner) findViewById(R.id.post_whooch_spinner);
        
        mReactingToText = (TextView) findViewById(R.id.post_reacting_to);
        
        mWhoochFeedbackLayout = (RelativeLayout) findViewById(R.id.post_whooch_feedback_layout);
        mWhoochImage = (ImageView) findViewById(R.id.post_whooch_image);
        mWhoochName = (TextView) findViewById(R.id.post_whooch_title);
        
        //////////////////
        // Post Related //
        //////////////////        
        mPostText = (EditText) findViewById(R.id.post_text);
        mPostText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCharCountText.setText(String.valueOf(Settings.MAX_POST_LENGTH - s.length()));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mCharCountText = (TextView) findViewById(R.id.post_char_count);
        mCameraButton = (Button) findViewById(R.id.post_camera_button);
        mSubmitButton = (Button) findViewById(R.id.post_submit_button);
    }
    
    protected Context getActivityContext() {
        return this;
    }

    private class GetUsers implements WhoochApiCallInterface {
        
        private String mTerm = "";
        private String mResponseString = null;

        public GetUsers(String term) {
            mTerm = term;
        }
        
        public HttpRequestBase getHttpRequest() {
            return new HttpGet(Settings.apiUrl + "/user/getusers?term=" + mTerm);
        }
        
        public void handleResponse(String responseString) {
            mResponseString = responseString;
        }
        
        public void postExecute(int statusCode) {

            if (statusCode == 200) {
                mSearchUsersArray.clear();

                try {
                    Log.d("ReactActivity", "responseString: " + mResponseString);
                    JSONArray jsonArray = new JSONArray(mResponseString);

                    for (int i=0; i<jsonArray.length(); i++) {
                        mSearchUsersArray.add(jsonArray.getString(i));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    // TODO: error handling
                }
                
                mSearchUsersAdapter = new ArrayAdapter<String>(getActivityContext(), android.R.layout.simple_dropdown_item_1line, mSearchUsersArray);
                mSearchUsersAutoText.setAdapter(mSearchUsersAdapter);
                
                if (mSearchUsersArray.size() == 1 && mSearchUsersAutoText.getText().toString().equals(mSearchUsersArray.get(0))) {
                    // the user selected a name from the list, don't suggest it again
                } else {
                    mSearchUsersAutoText.showDropDown();
                }
            }
        }
    }

}