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
import android.view.View;

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
    int cur_id = 0;
    public void setContext(MainActivity activity){
        this.activity = activity;
    }

    @Override
    protected void onPostExecute(Void result) {
        activity.setLoading(false);
        super.onPostExecute(result);
    }

    @Override
    protected Void doInBackground(Activity... activities) {
        while (true){
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ContentResolver contentResolver = activities[0].getContentResolver();
            Uri uri = Uri.parse("content://mms");
            Cursor query = contentResolver.query(uri, null, null, null, null);
            Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://mms/part"), null, null, null, null);
            smsInboxCursor.moveToLast();
            query.moveToFirst();

            //Log.d("IDlol", query.getString(0));
            int id = smsInboxCursor.getInt(0);
            Log.d("Type", "" + id);
            try {
                Log.d("Text", "" + (smsInboxCursor.getString(smsInboxCursor.getColumnIndex("name")).equals("body.txt")));
            }
            catch (Exception e){
                Log.d("Text", "Image");

            }
            if (old_id == 0){
                old_id = id - 1;
            }
            if (cur_id == 0){
                cur_id = id;
            }

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
            Log.d("Type", smsInboxCursor.getString(smsInboxCursor.getColumnIndex("name")) + smsInboxCursor.getString(0));
            while (smsInboxCursor.getString(smsInboxCursor.getColumnIndex("name")).equals("body.txt")){
                smsInboxCursor.move(-1);
                id = smsInboxCursor.getInt(0);
                Log.d("Type", "hit");
            }
            if (old_id != id && !smsInboxCursor.getString(smsInboxCursor.getColumnIndex("name")).equals("body.txt")){
                Log.d("Type", "" + id);

                Bitmap bitmap = getImage(contentResolver,"" + id);

                if (ContextCompat.checkSelfPermission(activities[0], Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    //saveImage(bitmap);

                    Log.d("Permission", "There is write permission");
                    Log.d("Done", "Saved to file");

                    old_id = id;

                    String baseHTML = activity.getHTML(bitmap);
                    String html;
                    try {
                        int size = Integer.parseInt(baseHTML.charAt(0) + "");
                        int baseIndex = Integer.parseInt(baseHTML.charAt(1) + "");

                        String[] htmls = new String[size];
                        htmls[baseIndex] = baseHTML.substring(6);

                        for (int i = 0; i < size - 1; i++){
                            smsInboxCursor.move(-1);
                            int sliceId = smsInboxCursor.getInt(0);

                            Bitmap slice = getImage(contentResolver, "" + sliceId);

                            String sliceHTML = activity.getHTML(slice);
                            int idx = Integer.parseInt(sliceHTML.charAt(1) + "");
                            htmls[idx] = sliceHTML.substring(6);
                        }

                        StringBuffer sb = new StringBuffer();
                        for(int i = 0; i < htmls.length; i++) {
                            sb.append(htmls[i]);
                        }

                        html = sb.toString();
                    }
                    catch (Exception e){
                        html = baseHTML;
                    }


                    // Call main activity to convert and draw html
                    activity.loadHTML(html);

                    break;
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
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    private Bitmap getImage(ContentResolver contentResolver, String _id){
        Uri partURI = Uri.parse("content://mms/part/" + _id);
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
        return bitmap;

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