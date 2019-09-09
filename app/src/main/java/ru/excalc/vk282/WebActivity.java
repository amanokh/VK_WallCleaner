package ru.excalc.vk282;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;
import com.vk.sdk.payments.VKIInAppBillingService;

public class WebActivity extends AppCompatActivity {
    private WebView webview;
    private WebViewClient webViewClient;
    private ProgressBar pageLoading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        webview = (WebView) findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.clearCache(true);

        webViewClient = new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                super.onPageStarted(view, url, favicon);
                pageLoading = findViewById(R.id.pageLoading);
                pageLoading.setVisibility(View.VISIBLE);

            }
            @Override
            public void onPageFinished(WebView view, String url){
                super.onPageFinished(view,url);
                pageLoading.setVisibility(View.GONE);
                if (url.contains("access_token=")){
                    webview.setVisibility(View.GONE);
                    String token = url.split("access_token=")[1].split("&")[0];
                    String url1 = url.split("#")[1];
                    VKAccessToken vk_token = VKAccessToken.tokenFromUrlString(url1);
                    VKSdk.logout();
                    vk_token.save();
                    startActivity(new Intent(getApplicationContext(), LoadingActivity.class));
                }else if(url.contains("error=access_denied")) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
            }
        };
        webview.setWebViewClient(webViewClient);
        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        String url = "https://oauth.vk.com/authorize?client_id=6652234&display=page&redirect_uri=https://oauth.vk.com/blank.html&scope=wall&response_type=token&v=5.84";
        webview.loadUrl(url);
    }

}
