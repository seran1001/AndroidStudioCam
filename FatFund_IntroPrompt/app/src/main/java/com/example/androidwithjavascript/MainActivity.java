package com.example.androidwithjavascript;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    private WebViewJavaScriptInterface webViewJavaScriptInterface;
    private WebView webView;

    /**
     * Read a file from the assets folder to a string.
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

    /**
     * Open a new activity window.
     */
    public void goToSecondActivity() {
        Intent intent = new Intent(this, BasicActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.webView);

        // JavaScript is disabled by default. Enable it.
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        // Enable JavaScript calling Java methods.
        webViewJavaScriptInterface = new WebViewJavaScriptInterface(this);
        // "app" is the name of the JavaScript object that calls the methods in the interface.
        webView.addJavascriptInterface(webViewJavaScriptInterface, "app");

        // Do not allow flickering.
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        // Enable debugging in Chrome. This only works on SDK KitKat or newer.
        WebView.setWebContentsDebuggingEnabled(true);

        // Add support for the JavaScript alert function.
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                //Required functionality here
                return super.onJsAlert(view, url, message, result);
            }
        });

        // Get the HTML file as a string.
        String indexFile = readFileToString("index.html");

        // Perform search and replace operations.
        indexFile = indexFile.replace("INSERT_TITLE_HERE", "Hello, World!");

        // Load the web page with all assets that are referenced in the HTML file.
        webView.loadDataWithBaseURL("file:///android_asset/", indexFile, "text/html", "utf-8", null);

        // Call a JavaScript function from the native app.
        webView.loadUrl("javascript:alert('This alert function was called from the native app.')");
    }
}
