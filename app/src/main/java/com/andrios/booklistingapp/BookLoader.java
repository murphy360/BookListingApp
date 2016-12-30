package com.andrios.booklistingapp;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.ArrayList;

/**
 * Created by Corey on 11/27/2016.
 */

public class BookLoader extends AsyncTaskLoader<ArrayList<Book>> {

    private String mUrl;
    final static String TAG = "BookLoader";

    public BookLoader(Context context, String url) {
        super(context);
        this.mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public ArrayList<Book> loadInBackground() {
        ArrayList<Book> books = QueryUtils.extractBooks(mUrl);
        return books;
    }
}