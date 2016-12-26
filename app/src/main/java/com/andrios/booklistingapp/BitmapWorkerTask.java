package com.andrios.booklistingapp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by Corey on 12/22/2016.
 */

class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;
    private Context context;
    private Book book;
    private int data = 0;
    private String imageUrl;
    private String id;

    private static final String TAG = "BitmapWorkerTask: ";

    public BitmapWorkerTask(ImageView imageView, Context context, Book book) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
        this.context = context;
        this.book = book;
    }

    //TODO MAKE THIS WORK: https://developer.android.com/training/displaying-bitmaps/process-bitmap.html
    // Decode image in background.
    @Override
    protected Bitmap doInBackground(Integer... params) {
        Bitmap b;
        Log.d(TAG, "doInBackground: Book ID: " + book.getFilePath(context));
        File file = new File(book.getFilePath(context));
        if (file.exists()) {
            Log.d(TAG, "doInBackground: File Exists");
            b = decodeSampledBitmapFromFile(file, 100, 100);
        } else {
            Log.d(TAG, "doInBackground: File Doesn'd Exist");
            b = QueryUtils.downloadImage(book.getImageUrl());
            writeBitmapToFile(b, book.getFilePath(context));
        }
        return b;
    }

    @Override
    protected void onPreExecute() {
        File file = new File(book.getFilePath(context));
        if (!file.exists()) {
            final ImageView imageView = imageViewReference.get();
            Resources r = context.getResources();
            Bitmap b = BitmapFactory.decodeResource(r, R.drawable.placeholder_book);
            imageView.setImageDrawable(new AsyncDrawable(r, b, this));
        }
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        Log.d(TAG, "onPostExecute: ");
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask =
                    getBitmapWorkerTask(imageView);

            Log.d(TAG, "onPostExecute: this == workertask" + this.equals(bitmapWorkerTask));
            Log.d(TAG, "imageView != null " + imageView.equals(null));
            //TODO Examples had me checking this:  if (this == bitmapWorkerTask && imageView != null) {


            imageView.setImageBitmap(bitmap);


        }
    }

    public static Bitmap decodeSampledBitmapFromFile(File file, int reqWidth, int reqHeight) {


        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath());

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }


    private void writeBitmapToFile(Bitmap bitmap, String filename) {
        Log.d(TAG, "writeBitmapToFile: ");
        FileOutputStream out = null;
        File f = new File(filename);
        try {
            out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
            Log.d(TAG, "writeBitmapToFile: Last Try");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        f = new File(filename);
        Log.d(TAG, "writeBitmapToFile: Does file exist?" + f.exists());
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }
}