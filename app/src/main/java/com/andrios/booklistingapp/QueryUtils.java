package com.andrios.booklistingapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Helper methods related to requesting and receiving Book data from USGS.
 */
public final class QueryUtils {

    private static final String TAG = "QueryUtils: ";


    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    public void DownloadFromUrl(String imageURL, String fileName) {  //this is the downloader method
        try {
            URL url = new URL("http://yoursite.com/&quot; + imageURL");
            File file = new File(fileName);

            long startTime = System.currentTimeMillis();
            Log.d("ImageManager", "download begining");
            Log.d("ImageManager", "download url:" + url);
            Log.d("ImageManager", "downloaded file name:" + fileName);
                        /* Open a connection to that URL. */
            URLConnection ucon = url.openConnection();

                        /*
                         * Define InputStreams to read from the URLConnection.
                         */
            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            FileOutputStream fos = new FileOutputStream(file);
            int current = 0;
            while ((current = bis.read()) != -1) {
                fos.write(current);
            }

            fos.close();


        } catch (IOException e) {
            Log.d("ImageManager", "Error: " + e);
        }

    }

    public static Bitmap downloadImage(String requestUrl) {
        URL url = createUrl(requestUrl);
        Bitmap b = null;
        try {
            Log.d(TAG, "downloadImage: MakeImageHttpRequest");
            b = makeImageHttpRequest(url);
        } catch (IOException e) {
            Log.e(TAG, "Error closing input stream", e);
        }
        return b;
    }

    /**
     * Return a list of {@link Book} objects that has been built up from
     * parsing a JSON response.
     * @param requestUrl
     */
    public static ArrayList<Book> extractBooks(String requestUrl) {
        Log.d(TAG, "extractBooks: ");
        // Create an empty ArrayList that we can start adding Books to
        ArrayList<Book> books = new ArrayList<>();

        // Try to parse the SAMPLE_JSON_RESPONSE. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            JSONObject response = new JSONObject(fetchJsonResponse(requestUrl));
            Log.d(TAG, "extractBooks: " + response.toString());
            JSONArray bookObjects = response.getJSONArray("items");
            Log.d(TAG, bookObjects.toString());
            for(int i = 0; i<bookObjects.length(); i++){
                JSONObject bookObject = bookObjects.getJSONObject(i);
                JSONObject volumeInfo = bookObject.getJSONObject("volumeInfo");
                String id = bookObject.getString("id");
                String title = volumeInfo.getString("title");
                ArrayList<String> authorList = new ArrayList<>();
                if (volumeInfo.has("authors") && !volumeInfo.isNull("authors")) {
                    JSONArray authors = volumeInfo.getJSONArray("authors");
                    for(int j = 0; j < authors.length(); j++){
                        authorList.add(authors.getString(j));
                    }
                }else{
                   authorList.add("No Authors Listed");
                }

                String publisher;
                if (volumeInfo.has("publisher") && !volumeInfo.isNull("publisher")) {
                    publisher = volumeInfo.getString("publisher");
                }else{
                    publisher = "Publisher not Listed";
                }
                String publishDate = volumeInfo.getString("publishedDate");

                String description;
                if (volumeInfo.has("description") && !volumeInfo.isNull("description")) {
                    description = volumeInfo.getString("description");
                }else{
                    description = "No Description Listed";
                }


                JSONObject imageLinks;
                String smallThumbUrl;
                String thumbUrl;

                if (volumeInfo.has("imageLinks") && !volumeInfo.isNull("imageLinks")) {
                    imageLinks = volumeInfo.getJSONObject("imageLinks");
                    smallThumbUrl = imageLinks.getString("smallThumbnail");
                    thumbUrl = imageLinks.getString("thumbnail");
                }else{
                    smallThumbUrl = "empty";
                    thumbUrl = "empty";
                }

                books.add(new Book(id, title, authorList, publisher, publishDate, description, smallThumbUrl, thumbUrl));
                Log.d(TAG, "extractBooks: Added Book" + books.get(i).getTitle());
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e(TAG, "Problem parsing the book JSON results", e);
        }

        // Return the list of books
        return books;
    }


    public static String fetchJsonResponse(String requestUrl) {


        Log.d(TAG, "fetchJsonResponse: ");
        // Create URL object
        URL url = createUrl(requestUrl);
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(TAG, "Error closing input stream", e);
        }
        return jsonResponse;
    }
    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error with creating URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        Log.d(TAG, "makeHttpRequest: ");
        String jsonResponse = "";
        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();

                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(TAG, "Problem retrieving the Book JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static Bitmap makeImageHttpRequest(URL url) throws IOException {
        Log.d(TAG, "makeImageHttpRequest: ");

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        Bitmap bitmap = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();

                bitmap = BitmapFactory.decodeStream(inputStream);
            } else {
                Log.e(TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(TAG, "Problem retrieving the Book JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return bitmap;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }



}
