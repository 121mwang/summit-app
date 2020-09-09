package me.myles.summitbrowser;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button send;
    TextView URL;
    WebView webView;
    RelativeLayout loading;

    final String number = "2058902792";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS},1);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        send = (Button)findViewById(R.id.send);
        URL = (TextView)findViewById(R.id.URL);
        webView = (WebView)findViewById(R.id.webView);
        loading = (RelativeLayout) findViewById(R.id.loadingPanel);


        URL.setText("");

        send.setOnClickListener(this);

        webView.setWebViewClient(new WebViewClient() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if(ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED) {
                    sendSMS("" + request.getUrl());
                    Log.d("Success", "Succ");
                    MMSTask mms = new MMSTask();
                    mms.setContext(MainActivity.this);
                    mms.execute(MainActivity.this);
                    setLoading(true);
                }
                return true;
            }
        });

        setLoading(false);
    }

    public void onClick(View view){
        sendSMS("" + URL.getText());
        if(ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED) {
            Log.d("Success", "Succ");
            MMSTask mms = new MMSTask();
            mms.setContext(this);
            mms.execute(this);
            setLoading(true);
        }
    }

    public void sendSMS(String message){
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, message, null, null);
        URL.setText("");
    }
    public String getHTML(Bitmap image){
        int pich = image.getHeight();
        int picw = image.getWidth();

        String html = "";
        Log.d("Dimensions", pich + " " + picw);
        for (int x = 0; x < pich; x++){
            String threes = "";
            for (int y = 0; y < picw; y++){

                int color = image.getPixel(y, x);

                int red = Color.red(color);


                if (red != 0){
                    threes = threes + (char)red;
                }


                int green = Color.green(color);

                if (green != 0){
                    threes = threes + (char)green;
                }


                int blue = Color.blue(color);

                if (blue != 0){
                    threes = threes + (char)blue;
                }
                //Log.d("Pixels" , red + " " + green + " " + blue);
            }
            //Log.d("Threes", threes);
            html = html + threes;
        }

        Log.d("html", html.substring(0, 6));
        return html;
    }

    public void loadHTML(String html){
        final String html2 = html;
        //largeLog("lolol", html);

        //Toast.makeText(this, html, Toast.LENGTH_LONG).show();
        webView.post(new Runnable() {
            @Override
            public void run() {
                //Log.d("HTML", "Loading html!");

                webView.getSettings().setJavaScriptEnabled(true);
                //webView.loadDataWithBaseURL(null, readFromFile(MainActivity.this), "text/html", "utf-8", null);
                webView.loadDataWithBaseURL(null, html2, "text/html", "UTF-8", null);
            }
        });

    }

    public static void largeLog(String tag, String content) {
        if (content.length() > 4000) {
            Log.d(tag, content.substring(0, 4000));
            largeLog(tag, content.substring(4000));
        } else {
            Log.d(tag, content);
        }
    }

    public void setLoading(boolean load){
        if (load){
            loading.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
        }
        else{
            loading.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
        }
    }



}