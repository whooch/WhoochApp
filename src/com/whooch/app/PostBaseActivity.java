package com.whooch.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;

public class PostBaseActivity extends SherlockActivity {

	private static final int REQUEST_CODE_LIBRARY = 1;
	private static final int REQUEST_CODE_CAMERA = 2;
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
	protected Button mSubmitButton;

	protected Bitmap mImageBitmap = null;
	protected String mImageName = null;
	protected ImageButton mImageView;
	protected String mImagePath = null;
	protected boolean mCameraIntent = false;

	protected ArrayList<String> mSearchUsersArray = new ArrayList<String>();
	protected ArrayAdapter<String> mSearchUsersAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post);

		// ///////////////
		// User Search //
		// ///////////////
		mUserSearchLayout = (LinearLayout) findViewById(R.id.post_user_search_layout);

		mImageView = (ImageButton) findViewById(R.id.imageButton1);
		
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	getPhoto(view);
            }
        });
		
		mSearchUsersAdapter = new ArrayAdapter<String>(getActivityContext(),
				android.R.layout.simple_dropdown_item_1line, mSearchUsersArray);
		mSearchUsersAutoText = (AutoCompleteTextView) findViewById(R.id.post_search_users);
		mSearchUsersAutoText.setAdapter(mSearchUsersAdapter);
		mSearchUsersAutoText
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						String userString = ">"
								+ mSearchUsersArray.get(position) + " ";
						int start = mPostText.getSelectionStart();
						int end = mPostText.getSelectionEnd();
						mPostText.getText().replace(Math.min(start, end),
								Math.max(start, end), userString, 0,
								userString.length());
					}
				});

		mSearchUsersAutoText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				WhoochApiCallTask task = new WhoochApiCallTask(
						getActivityContext(), new GetUsers(s.toString()), false);
				task.execute();
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		// //////////////////////////////
		// Post Type Specific Content //
		// //////////////////////////////
		mWhoochSelectorLayout = (LinearLayout) findViewById(R.id.post_select_whooch_layout);
		mWhoochSelector = (Spinner) findViewById(R.id.post_whooch_spinner);

		mReactingToText = (TextView) findViewById(R.id.post_reacting_to);

		mWhoochFeedbackLayout = (RelativeLayout) findViewById(R.id.post_whooch_feedback_layout);
		mWhoochImage = (ImageView) findViewById(R.id.post_whooch_image);
		mWhoochName = (TextView) findViewById(R.id.post_whooch_title);

		// ////////////////
		// Post Related //
		// ////////////////
		mPostText = (EditText) findViewById(R.id.post_text);
		mPostText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mCharCountText.setText(String.valueOf(Settings.MAX_POST_LENGTH
						- s.length()));
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		mCharCountText = (TextView) findViewById(R.id.post_char_count);
		mSubmitButton = (Button) findViewById(R.id.post_submit_button);
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
	    if(mImagePath != null)
	    {
	    	File photo = null;
	    	photo = new File(mImagePath);
	    	photo.delete();
	    	mImagePath = null;
	    }
		
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		
	    if(mImagePath != null && mCameraIntent == false)
	    {
	    	File photo = null;
	    	photo = new File(mImagePath);
	    	photo.delete();
	    	mImagePath = null;
	    }
		
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		
	    if(mImagePath != null && mCameraIntent == false)
	    {
	    	File photo = null;
	    	photo = new File(mImagePath);
	    	photo.delete();
	    	mImagePath = null;
	    }
		
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

					for (int i = 0; i < jsonArray.length(); i++) {
						mSearchUsersArray.add(jsonArray.getString(i));
					}

				} catch (JSONException e) {
					e.printStackTrace();
					// TODO: error handling
				}

				mSearchUsersAdapter = new ArrayAdapter<String>(
						getActivityContext(),
						android.R.layout.simple_dropdown_item_1line,
						mSearchUsersArray);
				mSearchUsersAutoText.setAdapter(mSearchUsersAdapter);

				if (mSearchUsersArray.size() == 1
						&& mSearchUsersAutoText.getText().toString()
								.equals(mSearchUsersArray.get(0))) {
					// the user selected a name from the list, don't suggest it
					// again
				} else {
					mSearchUsersAutoText.showDropDown();
				}
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mCameraIntent = false;
		
		if (requestCode == REQUEST_CODE_CAMERA
				&& resultCode == Activity.RESULT_OK) {
			handleCameraPhoto(data);
		} else if (requestCode == REQUEST_CODE_LIBRARY
				&& resultCode == Activity.RESULT_OK) {
			handleLibraryPhoto(data);
		} else {
			// do nothing
		}
	}

	private void dispatchTakePictureIntent() {
		if ((isIntentAvailable(PostBaseActivity.this, "android.media.action.IMAGE_CAPTURE") == true) &&
				Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			
			File photo = null;
			
		    if(mImagePath != null)
		    {
		    	photo = new File(mImagePath);
		    	photo.delete();
		    	mImagePath = null;
		    }
			
						
			mImageName = "image" + String.valueOf(System.currentTimeMillis())
					+ ".jpg";
					
			   Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
			    photo = new File(Environment.getExternalStorageDirectory(),  mImageName);
			    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
			            Uri.fromFile(photo));
			    

			mImagePath = photo.getAbsolutePath();
			
			mCameraIntent = true;
			startActivityForResult(takePictureIntent, REQUEST_CODE_CAMERA);
		
		} else {
			Toast.makeText(getApplicationContext(), "Camera is not available",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void dispatchGetPictureIntent() {
		Intent intent = new Intent();

		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		
		mCameraIntent = true;
		startActivityForResult(Intent.createChooser(intent, "Choose Picture"),
				REQUEST_CODE_LIBRARY);
	}

	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	private void handleCameraPhoto(Intent intent) {
		
		if(mImagePath != null)
		{
		File photo = new File(mImagePath);
        Uri selectedImage = Uri.fromFile(photo);
        getContentResolver().notifyChange(selectedImage, null);
        ContentResolver cr = getContentResolver();
        try {
             mImageBitmap = android.provider.MediaStore.Images.Media
             .getBitmap(cr, selectedImage);
             mImageView.setImageBitmap(Bitmap.createScaledBitmap(mImageBitmap, 150,
     				150, false));

        } catch (Exception e) {
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT)
                    .show();
        }
		}
		else
		{
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT)
            .show();
		}

	}

	private void handleLibraryPhoto(Intent intent) {
		Uri imageUri = intent.getData();
		String imagePath = getPath(imageUri);
		mImageBitmap = BitmapFactory.decodeFile(imagePath);
		mImageView.setImageBitmap(Bitmap.createScaledBitmap(mImageBitmap, 150,
				150, false));
		mImageName = imagePath.substring(imagePath.lastIndexOf("/") + 1,
				imagePath.length());
	}

	public void getPhoto(View view) {

		if (mImageBitmap != null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					view.getContext());

			builder.setMessage("Remove photo?").setTitle("Whooch");

			builder.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							mImageView
									.setImageResource(android.R.drawable.ic_menu_camera);
							mImageBitmap = null;
							mImageName = null;
						}
					});

			builder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {

						}
					});

			AlertDialog dialog = builder.create();

			dialog.show();
		} else {
			selectPhotoRetrievalType(view.getContext());
		}
	}

	public void selectPhotoRetrievalType(Context context) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setTitle("Upload Image");

		builder.setItems(R.array.image_retrieval_type,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						if (which == 0) {
							dispatchTakePictureIntent();
						} else {
							dispatchGetPictureIntent();
						}
					}
				});

		AlertDialog dialog = builder.create();

		dialog.show();
	}

	public String getPath(Uri uri) {

		String[] filePathColumn = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(uri, filePathColumn, null,
				null, null);
		cursor.moveToFirst();
		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		String picturePath = cursor.getString(columnIndex);
		cursor.close();
		return picturePath;

	}

}