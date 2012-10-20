package com.koushikdutta.urlimageviewhelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html.ImageGetter;
import android.util.Log;
import android.view.View;

/*
* NOTE: this file was not originally part of the urlimageviewhelper library. The code
* was taken from a stackoverflow answer:
*
* http://stackoverflow.com/questions/7424512/android-html-imagegetter-as-asynctask
*/

public class UrlImageGetter implements ImageGetter {
    Context c;
    View container;
    
    /***
* Construct the URLImageGetter which will execute AsyncTask and refresh the container
* @param t
* @param c
*/
    public UrlImageGetter(View t, Context c) {
        this.c = c;
        this.container = t;
    }

    public Drawable getDrawable(String source) {
        URLDrawable urlDrawable = new URLDrawable();
        
        // NOTE: the bounds are currently hardcoded to match the size of the
        // image that will be displayed inline. If the image size
        // changes the bounds values should be changed as well.
        urlDrawable.setBounds(0, 0, 35, 35);
        
        // get the actual source
        ImageGetterAsyncTask asyncTask =
            new ImageGetterAsyncTask( urlDrawable);

        asyncTask.execute(source);

        // return reference to URLDrawable where I will change with actual image from
        // the src tag
        
        // TODO: return a temporary image here as a placeholder.
        return urlDrawable;
    }

    @SuppressWarnings("deprecation")
    public class URLDrawable extends BitmapDrawable {
        // the drawable that you need to set, you could set the initial drawing
        // with the loading image if you need to
        protected Drawable drawable;
        
        @Override
        public void draw(Canvas canvas) {
            // override the draw to facilitate refresh function later
            if(drawable != null) {
                drawable.draw(canvas);
            }
        }
    }
    
    public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {
        URLDrawable urlDrawable;

        public ImageGetterAsyncTask(URLDrawable d) {
            this.urlDrawable = d;
        }

        @Override
        protected Drawable doInBackground(String... params) {
            String source = params[0];
            return fetchDrawable(source);
        }

        @Override
        protected void onPostExecute(Drawable result) {
            // set the correct bound according to the result from HTTP call
           urlDrawable.setBounds(0, 0, 20 + result.getIntrinsicWidth(), 20
                    + result.getIntrinsicHeight());
           
            // change the reference of the current drawable to the result
            // from the HTTP call
            urlDrawable.drawable = result;
          
            // redraw the image by invalidating the container
            UrlImageGetter.this.container.invalidate();
        }

        /***
* Get the Drawable from URL
* @param urlString
* @return
*/
        public Drawable fetchDrawable(String urlString) {
            
            final UrlImageCache cache = UrlImageCache.getInstance();
            Drawable drawable = cache.get(urlString);
            if (drawable == null) {
                try {
                    InputStream is = fetch(urlString);
                    drawable = Drawable.createFromStream(is, "src");
                    cache.put(urlString, drawable);
                } catch (Exception e) {
                    Log.d("UrlImageGetter", e.getMessage());
                    return new URLDrawable();
                }
            }
            
            drawable.setBounds(0, 0, 30, 30); //drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            return drawable;
        }

        private InputStream fetch(String urlString) throws MalformedURLException, IOException {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet request = new HttpGet(urlString.replace(" ", "%20"));
            HttpResponse response = httpClient.execute(request);
            return response.getEntity().getContent();
        }
    }
}