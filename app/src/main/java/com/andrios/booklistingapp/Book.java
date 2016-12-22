package com.andrios.booklistingapp;

import java.util.ArrayList;

/**
 * Created by Corey on 12/21/2016.
 */

public class Book extends Object {


    private String id;
    private String title;
    private ArrayList<String> authorList;
    private String publisher;
    private String publishDate;
    private String description;
    private String smallThumbUrl;
    private String thumbUrl;

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
}
