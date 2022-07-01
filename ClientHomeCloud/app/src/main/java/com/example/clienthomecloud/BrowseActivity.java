package com.example.clienthomecloud;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

public class BrowseActivity extends Activity {
    private static final int SELECT_PICTURE = 1;

    private String selectedImagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("Image", "я начал активность");

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
//                Log.d("Image", selectedImagePath);
                try {
                    SendImage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * helper to retrieve the path of an image URI
     */
    public String getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor;
        if (Build.VERSION.SDK_INT > 23) {
            // Will return "image:x*"
            String wholeID = DocumentsContract.getDocumentId(uri);
            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];
            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";

            cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection, sel, new String[]{id}, null);
        } else {
            cursor = getContentResolver().query(uri, projection, null, null, null);
        }
        String path = null;
        try {
            int column_index = cursor
                    .getColumnIndex(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index).toString();
            cursor.close();
        } catch (NullPointerException e) {
            Log.d("Test", "Бля");
        }
        return path;
    }

    public void SendImage() throws Exception {
        Connection connection = MainActivity.mConnect;
        connection.sendData(selectedImagePath);
    }
}