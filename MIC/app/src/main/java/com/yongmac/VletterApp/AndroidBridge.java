package com.yongmac.VletterApp;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.yongmac.VletterApp.Activity.BoardActivity;
import com.yongmac.VletterApp.Activity.LetterActivity;
import com.yongmac.VletterApp.Activity.MainActivity;

public class AndroidBridge {
    private WebView wv;
    private MainActivity mContext;
    private String TAG = "AndroidBridge";
    MyApplication myApp;
    final public Handler handler = new Handler();

    public AndroidBridge(WebView wv, MainActivity mContext) {
        this.wv = wv;
        this.mContext = mContext;
    }


    @JavascriptInterface
    public void sendVLetter(final String data) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d("sendVLetter","오긴 왔다.");
                Log.d("data-----",data);
                Intent intent = new Intent(mContext.getApplicationContext(), LetterActivity.class);
//                intent.putExtra("id",data);
                myApp = (MyApplication)mContext.getApplicationContext();
                myApp.setId(data);
                mContext.startActivity(intent);
            }
        });
    }

    //확인
    @JavascriptInterface
    public void sendBoard(final String data) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d("sendBoard","오긴 왔다.");
                Log.d("data-----",data);
                Intent intent = new Intent(mContext.getApplicationContext(), BoardActivity.class);
                myApp = (MyApplication)mContext.getApplicationContext();
                myApp.setId(data);
                mContext.startActivity(intent);
            }
        });
    }


}


