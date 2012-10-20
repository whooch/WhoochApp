package com.whooch.app;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;

import android.content.Intent;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.whooch.app.helpers.ActionBarHelper;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;
import com.whooch.app.helpers.WhoochHelperFunctions;

public class PostReactionActivity extends PostBaseActivity {
    
    private String mWhoochIdExtra;  
    private String mReactionToExtra;
    private String mReactionTypeExtra;
    private String mContentExtra;
    private String mUserNameExtra;
    private String mWhoochNameExtra;
    private String mWhoochImageExtra;
    private TextView mReactingToText;
    private ImageView mReactingToImage;
    private TextView mReactingToWhoochName;
    private TextView mReactingToUserName;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ActionBarHelper.setupActionBar(getSupportActionBar(), new ActionBarHelper.TabListener(getApplicationContext()), 1);
        
        mLayoutType = "reaction";
        
		mReactingToText = (TextView) findViewById(R.id.post_reacting_to);
		mReactingToImage = (ImageView) findViewById(R.id.post_react_whooch_image);
		mReactingToWhoochName = (TextView) findViewById(R.id.post_react_whooch_title);
		mReactingToUserName = (TextView) findViewById(R.id.post_react_whooch_contributor);
        
        mReactLayout.setVisibility(View.VISIBLE);

        mSubmitButton.setText("React");
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
				if (mPostText.getText().toString().trim().length() <= 0)
				{
					Toast.makeText(getActivityContext(),
							"You need to say something", Toast.LENGTH_SHORT)
							.show();
				}
				else
				{
					WhoochApiCallTask task = new WhoochApiCallTask(getActivityContext(), new Submit(), true);
					task.execute();
				}
            }
        });
        
        Intent i = getIntent();
        Bundle b = i.getExtras();
        
        if (b == null) {
               Toast.makeText(getApplicationContext(), "Error: bad intent", Toast.LENGTH_SHORT).show();
               finish();
        }
        
        mUserNameExtra = b.getString("USER_NAME");
        mPostText.setText(">" + mUserNameExtra + " ");
        mPostText.setSelection(mPostText.getText().length());
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
        mWhoochNameExtra = b.getString("WHOOCH_NAME");
        mWhoochImageExtra = b.getString("WHOOCH_IMAGE");
        
        if ( (mWhoochIdExtra == null) || (mReactionToExtra == null) || (mReactionTypeExtra == null) ||
                (mContentExtra == null)  || (mUserNameExtra == null) || (mWhoochNameExtra == null) || (mWhoochImageExtra == null) ) {
               Toast.makeText(getApplicationContext(), "Error: bad intent", Toast.LENGTH_SHORT).show();
               finish();
        }
        
        mReactingToWhoochName.setText(mWhoochNameExtra);
        mReactingToUserName.setText(mUserNameExtra);
        UrlImageViewHelper.setUrlDrawable(mReactingToImage, mWhoochImageExtra);
        mReactingToText.setText(WhoochHelperFunctions.getSpannedFromHtmlContent(mContentExtra, mPostText, getActivityContext()));

    }
    
    private class Submit implements WhoochApiCallInterface {

        public void preExecute() {}
        
        public HttpRequestBase getHttpRequest() {
                        
            HttpPost request = new HttpPost(Settings.apiUrl + "/whooch/add");

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