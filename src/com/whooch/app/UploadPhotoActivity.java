package com.whooch.app;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;

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
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.whooch.app.helpers.Settings;
import com.whooch.app.helpers.WhoochApiCallInterface;
import com.whooch.app.helpers.WhoochApiCallTask;

public class UploadPhotoActivity extends Activity {

	private static final int REQUEST_CODE_LIBRARY = 1;
	private static final int REQUEST_CODE_CAMERA = 2;
	private static final int UPLOAD_PHOTO_USER = 1;
	private static final int UPLOAD_PHOTO_WHOOCH = 2;
	private Bitmap mImageBitmap = null;
	private String mImageName = null;
	private String mImagePath = null;
	public boolean mCameraIntent = true;
	private ImageView mImageView;
	private Button mUploadPhotoButton;
	private int mUploadPhotoType = 0;

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload_photo);
		
		Intent i = getIntent();
        Bundle b = i.getExtras();
        mUploadPhotoType = b.getInt("UPLOAD_TYPE");
		
		mImageView = (ImageView) findViewById(R.id.imageView1);
		mUploadPhotoButton = (Button) findViewById(R.id.upload_photo_button);

		mUploadPhotoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mImageBitmap != null) {
					WhoochApiCallTask task = new WhoochApiCallTask(view.getContext(), new UploadPhoto(), true);
					task.execute();
				} else { 
					Toast.makeText(getApplicationContext(),
							"Click the box above to first select a photo",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		mImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getPhoto(view);
			}
		});
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mCameraIntent = false;
		if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
			handleCameraPhoto(data);
		}
		else if (requestCode == REQUEST_CODE_LIBRARY && resultCode == Activity.RESULT_OK)
		{
			handleLibraryPhoto(data);
		}
		else
		{
			//do nothing
		}
	}

	private void dispatchTakePictureIntent() {
		if ((isIntentAvailable(UploadPhotoActivity.this, "android.media.action.IMAGE_CAPTURE") == true) &&
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
			startActivityForResult(Intent.createChooser(intent, "Choose Picture"), REQUEST_CODE_LIBRARY);
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
		mImageView.setImageBitmap(Bitmap.createScaledBitmap(mImageBitmap, 150, 150, false));
		mImageName = imagePath.substring(imagePath.lastIndexOf("/") + 1, imagePath.length());
	}

	public void getPhoto(View view) {

		if (mImageBitmap != null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					view.getContext());

			builder.setMessage("Remove photo?").setTitle("Whooch");

			builder.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							mImageView.setImageResource(android.R.drawable.alert_light_frame);
							mImageBitmap = null;
							mImageName =  null;
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
		    
		    builder.setItems(R.array.image_retrieval_type, new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int which) {
		            	 
		               if(which == 0)
		               {
		            	   dispatchTakePictureIntent();
		               }
		               else
		               {
		            	   dispatchGetPictureIntent();
		               }
		           }
		    });

			AlertDialog dialog = builder.create();

			dialog.show();
	}

	private class UploadPhoto implements WhoochApiCallInterface {

		public HttpRequestBase getHttpRequest() {
			
			String apiString = null;
			
			if(mUploadPhotoType == UPLOAD_PHOTO_USER)
			{
				apiString = Settings.apiUrl + "/user/updateimage";
			}
			else
			{
				apiString = Settings.apiUrl + "/whooch/updateimage";
			}

			HttpPost postRequest = new HttpPost(apiString);

			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				mImageBitmap.compress(CompressFormat.JPEG, 80, bos);

				byte[] data = bos.toByteArray();

				ByteArrayBody bab = new ByteArrayBody(data, mImageName);
				MultipartEntity reqEntity = new MultipartEntity(
						HttpMultipartMode.BROWSER_COMPATIBLE);
				reqEntity.addPart("file", bab);
				
				if(mUploadPhotoType == UPLOAD_PHOTO_WHOOCH)
				{
					Intent i = getIntent();
			        Bundle b = i.getExtras();
			        String whoochId = b.getString("WHOOCH_ID");
			        
		            reqEntity.addPart("whoochId", new StringBody(whoochId));
				}
				
				postRequest.setEntity(reqEntity);

			} catch (Exception e) {
				// handle exception here
				Log.e(e.getClass().getName(), e.getMessage());
			}

			return postRequest;
		}

		public void handleResponse(String response) {
			
		}

		public void postExecute(int statusCode) {

			if (statusCode == 200) {
				finish();
			} else {
				Toast.makeText(getApplicationContext(),
						"Error uploading image, please try again",
						Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	 public String getPath(Uri uri) 
	    {
	       
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(uri,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            return picturePath;

	    }


}