package com.example.administer.mywebview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.example.administer.mywebview.widget.WrapperWebView;

public class WebActivity extends AppCompatActivity {

    private WrapperWebView wrapperWebView;
    private Button btnOpenPage2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        initView();
        Intent intent = getIntent();
        final String pageUrl = intent.getStringExtra("pageUrl");
        wrapperWebView.setOnloadPageListener(new WrapperWebView.OnloadPageListener() {
            @Override
            public String onFirstLoadPage() {
                return pageUrl;
            }
        });
        btnOpenPage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wrapperWebView.loadUrl("https://www.douyu.com/");
            }
        });
    }

    private void initView() {
        wrapperWebView = (WrapperWebView) findViewById(R.id.wrapperWebView);
        btnOpenPage2 = (Button) findViewById(R.id.btn_openPage2);
    }

    @Override
    protected void onPause() {
        wrapperWebView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        wrapperWebView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        wrapperWebView.onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (wrapperWebView.isKeyDown(keyCode) == true)
            return true;
        else
            return super.onKeyDown(keyCode, event);
    }
}
