package me.myles.summitbrowser;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

class MMSTask extends AsyncTask<Activity, Void, Void> {

    final String RECEIVER_NUM = "+16097792343";
    int old_id = 0;
    MainActivity activity;

    public void setContext(MainActivity activity){
        this.activity = activity;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
    }

    @Override
    protected Void doInBackground(Activity... activities) {
        while (true){
            //Log.d("Hello", "Looping");
            ContentResolver contentResolver = activities[0].getContentResolver();
            Uri uri = Uri.parse("content://mms");
            Cursor query = contentResolver.query(uri, null, null, null, null);
            Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://mms/part"), null, null, null, null);
            smsInboxCursor.moveToLast();
            query.moveToFirst();

            //Log.d("IDlol", query.getString(0));
            int id = smsInboxCursor.getInt(0);

            String add="";
            String[] projection =  new String[] { "address", "contact_id", "charset", "type" };
            final String selection = "type=137"; // "type="+ PduHeaders.FROM,

            Uri.Builder builder = Uri.parse("content://mms").buildUpon();
            builder.appendPath(String.valueOf(query.getInt(0))).appendPath("addr");

            Cursor cursor = contentResolver.query(
                    builder.build(),
                    projection,
                    selection,
                    null, null);

            if (cursor.moveToFirst()) {
                add =  cursor.getString(0);
            }

            //Log.d("Address", "" + (add == RECEIVER_NUM));
            if (old_id != id){
                // Getting the bitmap image
                Uri partURI = Uri.parse("content://mms/part/" + id);
                InputStream is = null;
                Bitmap bitmap = null;
                try {
                    is = contentResolver.openInputStream(partURI);
                    bitmap = BitmapFactory.decodeStream(is);
                }
                catch (IOException e) {
                    //Toast.makeText(activities[0], e.getMessage(), Toast.LENGTH_LONG).show();
                }
                finally {
                    if (is != null) {
                        try {
                            is.close();
                        }
                        catch (IOException e) {//Toast.makeText(activities[0], e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }



                if (ContextCompat.checkSelfPermission(activities[0], Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveImage(bitmap);
                    Log.d("Permission", "There is write permission");
                    Log.d("Done", "Saved to file");

                    old_id = id;

                    // Call main activity to convert and draw html
                    activity.loadHTML(activity.getHTML(bitmap));
//                    activity.loadHTML("<!DOCTYPE html>\n" +
//                            "<html>\n" +
//                            "<body>\n" +
//                            "\n" +
//                            "<h1>My First Heading</h1>\n" +
//                            "\n" +
//                            "<p>My first paragraph.</p>\n" +
//                            "\n" +
//                            "</body>\n" +
//                            "</html>");

                } else {
                    // Request permission from the user
                    ActivityCompat.requestPermissions(activities[0], new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 112);
                }
            }
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }





    private void saveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        File myDir = new File(root);
        myDir.mkdirs();

        String fname = "test.png";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}