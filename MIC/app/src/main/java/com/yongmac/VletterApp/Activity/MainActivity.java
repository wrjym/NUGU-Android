package com.yongmac.VletterApp.Activity;


import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.yongmac.VletterApp.AndroidBridge;
import com.yongmac.VletterApp.R;

public class MainActivity extends Activity{

    private WebView mWebView;   //웹뷰
    private WebSettings mWebSettings;   //웹뷰세팅
    public String HostingURL= "http://13.209.89.216:8080/NUGU/";
    private long lastTimeBackPressed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = (WebView)findViewById(R.id.webview) ;
        mWebView.setWebViewClient(new WebViewClient());


        mWebSettings = mWebView.getSettings();
        //자바스크립트 사용 허용
        //웹뷰 내에서 자바스크립트 실행해 자바스크릅에서 안드로이드 함수
        //실행시킬려면 필수로 세팅필요
        mWebSettings.setJavaScriptEnabled(true);
        //웹 확대축소버튼 없애기
        mWebSettings.setDisplayZoomControls(false);
        //캐시 사용 여부 설정
        mWebSettings.setAppCacheEnabled(false);

        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setVerticalScrollbarOverlay(false);

        mWebView.setWebChromeClient(new WebChromeClient());

        mWebView.loadUrl(HostingURL);


        AndroidBridge ab = new AndroidBridge(mWebView, MainActivity.this);
        mWebView.addJavascriptInterface(ab,"Android");


    }

    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()){
            mWebView.goBack();

        } else {
            if(System.currentTimeMillis() - lastTimeBackPressed < 1500){
                finish();
                return;
            }
            lastTimeBackPressed = System.currentTimeMillis();
            Toast.makeText(this,"'뒤로' 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }

    }





}