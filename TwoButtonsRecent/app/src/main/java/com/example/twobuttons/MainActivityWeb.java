package com.example.twobuttons;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.Intent;
import android.os.Build;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

public class MainActivityWeb extends AppCompatActivity {
    private WebViewJavaScriptInterface webViewJavaScriptInterface;
    private WebView webView;

    private final String APP_SCHEME = "iamportapp";


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
/*
  //  @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {

        if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("javascript:")) {
            Intent intent = null;

            try {
                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME); //IntentURI처리
                Uri uri = Uri.parse(intent.getDataString());

                startActivity(new Intent(Intent.ACTION_VIEW, uri)); //해당되는 Activity 실행
                return true;
            } catch (URISyntaxException ex) {
                return false;
            } catch (ActivityNotFoundException e) {
                if (intent == null) return false;

                if (handleNotFoundPaymentScheme(intent.getScheme()))
                    return true; //설치되지 않은 앱에 대해 사전 처리(Google Play이동 등 필요한 처리)

                String packageName = intent.getPackage();
                if (packageName != null) { //packageName이 있는 경우에는 Google Play에서 검색을 기본
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                    return true;
                }

                return false;
            }
        }

        return false;
    }

    private boolean handleNotFoundPaymentScheme(String scheme) {
        return true;
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String url = intent.toString();

        if (url.startsWith(APP_SCHEME)) {
            // "iamportapp://https://pgcompany.com/foo/bar"와 같은 형태로 들어옴
            String redirectURL = url.substring(APP_SCHEME.length() + "://".length());
            webView.loadUrl(redirectURL);
        }
    }

 */
    /**
     * Open a new activity window.
     */
    public void goToSecondActivity() {
        Intent intent = new Intent(this, BasicActivity.class);
        startActivity(intent);
    }

    /**
     * Open a new activity window.
     */
    public void goToKakaoPay() {
        Intent intent = new Intent(this, MainActivityPay.class);
        startActivity(intent);
    }

    @SuppressLint("JavascriptInterface")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_web);
        WebView webView = (WebView) findViewById(R.id.webView);
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