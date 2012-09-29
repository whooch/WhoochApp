package com.whooch.app;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochHelperFunctions;

public class LoginActivity extends SherlockActivity {
    
    private EditText UsernameText;
    private EditText PasswordText;
    private Button mLoginButton;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        UsernameText = (EditText) findViewById(R.id.login_username);
        PasswordText = (EditText) findViewById(R.id.login_password);
        mLoginButton = (Button) findViewById(R.id.login_button);
        
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VerifyUserTask task = new VerifyUserTask(getActivityContext());
                task.execute();
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        // if the user is already logged in, take them directly to the stream
        SharedPreferences settings = getSharedPreferences("whooch_preferences", 0);
        String username = settings.getString("username", null);
        String password = settings.getString("password", null);

        if ( (username != null) && (password != null) ) {
            Intent i = new Intent(getApplicationContext(), StreamActivity.class);
            startActivity(i);
        }
    }
    
    private Context getActivityContext() {
        return this;
    }
    
    public class VerifyUserTask extends AsyncTask<Void, Void, Integer> {
        
        private ProgressDialog mProgressDialog;
        private Context mActivityContext;
        private String mUsername;
        private String mPassword;
    
        public VerifyUserTask(Context ctx) {
            super();
            mActivityContext = ctx;
            mUsername = UsernameText.getText().toString();
            mPassword = PasswordText.getText().toString();
        }
        
        @Override
        protected void onPreExecute() {
            this.mProgressDialog = ProgressDialog.show(mActivityContext, null, "loading", true);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            
            // prepare the request
            HttpPost postRequest = new HttpPost(Settings.apiUrl + "/user/verify");
            postRequest.setHeader("Authorization", WhoochHelperFunctions.getB64Auth(mUsername, mPassword));
            
            // make the request
            HttpClient client = new DefaultHttpClient();
            HttpResponse response = null;
            try {
                response = client.execute(postRequest);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            // get the response
            int statusCode = -1;
            if (response != null) {
                statusCode = response.getStatusLine().getStatusCode();
            }
            
            return statusCode;
        }

        @Override
        protected void onPostExecute(Integer result) {
            
            this.mProgressDialog.cancel();
            
            if (result == 200) {
                // verification successful, store username and password in shared prefs, and start the stream activity                
                SharedPreferences settings = getSharedPreferences("whooch_preferences", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("username", mUsername);
                editor.putString("password", mPassword);
                editor.commit();
                
                Intent i = new Intent(getApplicationContext(), StreamActivity.class);
                startActivity(i);
            } else if (result == 401) {
                Toast.makeText(getApplicationContext(), "Incorrect username or password", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Error 37", Toast.LENGTH_LONG).show();
            }
        }
    }
}