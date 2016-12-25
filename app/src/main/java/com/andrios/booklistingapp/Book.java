package com.andrios.booklistingapp;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Corey on 12/21/2016.
 */

public class Book extends Object {

private static final String TAG = "Book: ";
    private String id;
    private String title;
    private ArrayList<String> authorList;
    private String publisher;
    private String publishDate;
    private String description;
    private String smallThumbUrl;
    private String thumbUrl;
    private String filePath;

    public Book(String id, String title, ArrayList<String> authorList, String publisher, String publishDate, String description, String smallThumbUrl, String thumbUrl) {


        this.id = id;
        this.title = title;
        this.authorList = authorList;
        this.publisher = publisher;
        this.publishDate = publishDate;
        this.description = description;
        this.smallThumbUrl = smallThumbUrl;
        this.thumbUrl = thumbUrl;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return thumbUrl;
    }


    public String getFilePath(Context context) {
        Log.d(TAG, "getFilePath: " +  context.getCacheDir() + "/" + id);
        return context.getCacheDir() + "/" + id;
    }


}
