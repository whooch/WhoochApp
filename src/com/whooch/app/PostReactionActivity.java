package com.whooch.app;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;

import android.content.Intent;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.Toast;

import com.koushikdutta.urlimageviewhelper.UrlImageGetter;
import com.whooch.app.helpers.ActionBarHelper;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;
import com.whooch.app.json.ContributingEntry;

public class PostReactionActivity extends PostBaseActivity {
    
    private String mWhoochIdExtra;  
    private String mReactionToExtra;
    private String mReactionTypeExtra;
    private String mContentExtra;
    private String mUserNameExtra;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ActionBarHelper.setupActionBar(getSupportActionBar(), new ActionBarHelper.TabListener(getApplicationContext()), 1);
        
        mWhoochSelectorLayout.setVisibility(View.GONE);
        mWhoochFeedbackLayout.setVisibility(View.GONE);

        mSubmitButton.setText("React");
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new Submit(), true);
                task.execute();
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        ActionBarHelper.selectTab(getSupportActionBar(), 1);
        
        Intent i = getIntent();
        Bundle b = i.getExtras();
        
        if (b == null) {
               Toast.makeText(getApplicationContext(), "Error: bad intent", Toast.LENGTH_SHORT).show();
               finish();
        }
        
        mWhoochIdExtra = b.getString("WHOOCH_ID");   
        mReactionToExtra = b.getString("REACTION_TO");
        mReactionTypeExtra = b.getString("REACTION_TYPE");
        mContentExtra = b.getString("CONTENT");
        mUserNameExtra = b.getString("USER_NAME");

        if ( (mWhoochIdExtra == null) || (mReactionToExtra == null) || (mReactionTypeExtra == null) ||
                (mContentExtra == null)  || (mUserNameExtra == null) ) {
               Toast.makeText(getApplicationContext(), "Error: bad intent", Toast.LENGTH_SHORT).show();
               finish();
        }
        
        UrlImageGetter imageGetter = new UrlImageGetter(mPostText, getActivityContext());
        Spanned htmlSpan = Html.fromHtml(mContentExtra.replaceAll(">\\s+<", "><"), imageGetter, null);
        mReactingToText.setText(htmlSpan);
        mPostText.setText(">" + mUserNameExtra + " ");
        mPostText.setSelection(mPostText.getText().length());
    }
    
    private class Submit implements WhoochApiCallInterface {

        public HttpRequestBase getHttpRequest() {
                        
            HttpPost request = new HttpPost(Settings.apiUrl + "/whooch/add");

			ContributingEntry selectedEntry = (ContributingEntry) mWhoochSelector
					.getSelectedItem();

			MultipartEntity reqEntity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);

			try {
				if (mImageBitmap != null) {
					reqEntity.addPart("image", new StringBody("true"));
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					mImageBitmap.compress(CompressFormat.JPEG, 80, bos);

					byte[] data = bos.toByteArray();

					ByteArrayBody bab = new ByteArrayBody(data, mImageName);
					reqEntity.addPart("file", bab);
				}
				reqEntity.addPart("whoochId", new StringBody(mWhoochIdExtra));
				reqEntity.addPart("reactionTo", new StringBody(mReactionToExtra));
				reqEntity.addPart("reactionType", new StringBody(mReactionTypeExtra));
				reqEntity.addPart("content", new StringBody(mPostText.getText().toString()));

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			request.setEntity(reqEntity);
            
            return request;
        }
        
        public void handleResponse(String responseString) {
        }
        
        public void postExecute(int statusCode) {
            if (statusCode == 202) {
                finish();
            }
        }
    }

}