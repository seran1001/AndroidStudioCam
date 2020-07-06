package com.example.twobuttons;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivityPay extends Activity {

    private WebPayJavaScriptInterface webPayJavaScriptInterface;
    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String myurl = "file:///android_asset/kakaologin.html";
        setContentView(R.layout.activity_kakaopay);
        webView = (WebView) findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("http://www.google.com");
        WebSettings webSettings = webView.getSettings();
        webView.getSettings().setJavaScriptEnabled(true);
    }
    /**
     * Read a file from the assets folder to a string.
     *
     * @param fileName includes the file path starting with the assets directory as the root
     * @return String
     */
    private String readFileToString(String fileName) {
        BufferedReader reader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open(fileName), "UTF-8"));

            // Read the file, usually looping until the end is reached.
            String myLine;
            while ((myLine = reader.readLine()) != null) {
                stringBuilder.append(myLine);
                stringBuilder.append("\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return stringBuilder.toString();
    }




}