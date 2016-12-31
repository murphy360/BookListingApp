package com.andrios.booklistingapp;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.graphics.Bitmap;

/**
 * Created by Corey on 11/27/2016.
 */

public class ImageLoader extends AsyncTaskLoader<Bitmap> {

    private String mUrl;
    final static String TAG = "BookLoader";

    public ImageLoader(Context context, String url) {
        super(context);
        this.mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public Bitmap loadInBackground() {
        Bitmap bitmap = QueryUtils.downloadImage(mUrl);
        return bitmap;
    }
}