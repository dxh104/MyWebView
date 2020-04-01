package com.example.administer.mywebview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.http.SslError;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.administer.mywebview.R;

/**
 * Created by XHD on 2020/03/30
 */
public class WrapperWebView extends LinearLayout {

    private ProgressBar mProgressBar;
    private WebView mWebView;
    private WebSettings settings;

    private Drawable mProgressDrawable;//进度条样式
    private int mProgressWidth;//进度条宽度
    private int mProgressHeight;//进度条高度

    private int mWebViewWidth;//WebView宽度
    private int mWebViewHeight;//WebView高度

    private String mUrl;
    private int layoutCount = 0;
    private OnloadPageListener onloadPageListener;
    private String TAG = "WrapperWebView------>";

    public WrapperWebView(Context context) {
        this(context, null);
    }

    public WrapperWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public WrapperWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.WrapperWebView);

        mProgressDrawable = array.getDrawable(R.styleable.WrapperWebView_progressDrawable);

        mProgressWidth = array.getLayoutDimension(R.styleable.WrapperWebView_progressWidth, -2);
        mProgressHeight = array.getLayoutDimension(R.styleable.WrapperWebView_progressHeight, -2);

        mWebViewWidth = array.getLayoutDimension(R.styleable.WrapperWebView_webViewWidth, -2);
        mWebViewHeight = array.getLayoutDimension(R.styleable.WrapperWebView_webViewHeight, -2);
        array.recycle();
        setOrientation(VERTICAL);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (layoutCount == 0) {
            layoutCount++;

            mWebView = new WebView(getContext());
            mProgressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);

            this.addView(mProgressBar);
            this.addView(mWebView);

            mProgressBar.getLayoutParams().width = mProgressWidth;
            mProgressBar.getLayoutParams().height = mProgressHeight;
            mProgressBar.setProgressDrawable(mProgressDrawable);

            mWebView.getLayoutParams().width = mWebViewWidth;
            mWebView.getLayoutParams().height = mWebViewHeight;

            initListener();
            initWebViewSettings();
            String page = onloadPageListener.onFirstLoadPage();
            if (!TextUtils.isEmpty(page))
                loadUrl(page);
        }
    }

    //初始化WebView配置
    private void initWebViewSettings() {
        settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true); // 设置JS是否可以打开WebView新窗口
        settings.setDefaultTextEncodingName("utf-8");// 设置编码格式
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);//设置WebView缓存模式 默认断网情况下不缓存
        settings.setPluginState(WebSettings.PluginState.ON);//让WebView支持播放插件
        settings.setSupportZoom(true); // 支持缩放
        settings.setBuiltInZoomControls(true); // 支持手势缩放
        settings.setDisplayZoomControls(false); // 不显示缩放按钮
        settings.setAllowFileAccess(true);//设置在WebView内部是否允许访问文件
        if (Build.VERSION.SDK_INT >= 19) {
            settings.setLoadsImagesAutomatically(true);//支持自动加载图片
        } else {
            settings.setLoadsImagesAutomatically(false);
        }
        settings.setDatabaseEnabled(true);//数据库存储API是否可用，默认值false。
        settings.setSaveFormData(true);//WebView是否保存表单数据，默认值true。
        settings.setDomStorageEnabled(true);//DOM存储API是否可用，默认false。
        settings.setGeolocationEnabled(true);//定位是否可用，默认为true。
        settings.setAppCacheEnabled(true);//应用缓存API是否可用
        settings.setUseWideViewPort(true); // 将图片调整到适合WebView的大小
        settings.setLoadWithOverviewMode(true); // 自适应屏幕
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//播放网络视频
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);//允许WebView同时加载Https和Http
        }

        mWebView.setHorizontalScrollBarEnabled(false);//去掉webview的滚动条,水平不显示
        mWebView.setScrollbarFadingEnabled(true);//不活动的时候隐藏，活动的时候显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            settings.setMediaPlaybackRequiresUserGesture(false); //自动播放音乐
        }
        mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);//设置滚动条样式
        mWebView.setOverScrollMode(View.OVER_SCROLL_NEVER); // 取消WebView中滚动或拖动到顶部、底部时的阴影
        mWebView.requestFocus(); // 触摸焦点起作用

    }

    private void initListener() {
        //处理各种通知 & 请求事件 除此之外WebViewClient更多的处理网页的地址的解析和渲染
        mWebView.setWebViewClient(new WebViewClient() {
            //在网页上的所有加载都经过这个方法,这个函数我们可以做很多操作。
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Log.i(TAG, "shouldOverrideUrlLoading: ");
                return super.shouldOverrideUrlLoading(view, request);
            }

            //开始载入页面调用的，我们可以设定一个loading的页面，告诉用户程序在等待网络响应。
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mProgressBar.setVisibility(View.VISIBLE);
                Log.i(TAG, "开始载入页面调用onPageStarted: " + url);
            }

            //设定加载资源的操作.加载资源时响应
            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                Log.i(TAG, "加载资源时响应onLoadResource: " + url);

            }

            //加载页面的服务器出现错误时（如404）调用。
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                mProgressBar.setVisibility(View.GONE);
                Log.i(TAG, error.toString() + "加载页面的服务器出现错误时onReceivedError: " + request.toString());

            }

            //在页面加载结束时调用。我们可以关闭loading，切换程序动作。
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mProgressBar.setVisibility(View.GONE);
                Log.i(TAG, "在页面加载结束时调用onPageFinished: " + url);
            }

            //获取返回信息授权请求
            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                super.onReceivedHttpAuthRequest(view, handler, host, realm);
                Log.i(TAG, realm + "获取返回信息授权请求onReceivedHttpAuthRequest: " + handler.toString());

            }

            //处理https请求
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();    //表示等待证书响应---接受所有网站的证书，忽略SSL错误，执行访问网页
                Log.i(TAG, error.toString() + "处理https请求onReceivedSslError: " + handler.toString());

            }
        });
        //辅助 WebView 处理 Javascript 的对话框,网站图标,网站标题等等
        mWebView.setWebChromeClient(new WebChromeClient() {

            //拦截确认框
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return super.onJsConfirm(view, url, message, result);
            }

            //拦截输入框
            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }

            //拦截警告框
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            //获取网页title
            @Override
            public void onReceivedTitle(WebView view, String title) {

            }

            //获得网页的加载进度并显示
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress >= 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    if (mProgressBar.getVisibility() == View.GONE) {
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                    mProgressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
    }

    //Android调用js方法
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void androidToJs(String script) {
        if (mWebView == null)
            return;
        if (Build.VERSION.SDK_INT < 18) {
            mWebView.loadUrl(script);
        } else {
            mWebView.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    //此处为 js 返回的结果
                }
            });
        }
    }

    //加载网页
    public void loadUrl(String url) {
        mUrl = url;
        if (mWebView != null)
            mWebView.loadUrl(url);
        else
            Log.i("---------->", "loadUrl: mWebView未加载完成");
    }

    //设置ProgressDrawable
//    public void setProgressDrawable(@DrawableRes int id) {
//        if (mProgressBar != null)
//            mProgressBar.setProgressDrawable(getContext().getResources().getDrawable(id));
//    }


    //回退网页
    public boolean goBack() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return false;
    }

    public void onPause() {
        if (mWebView == null)
            return;
        mWebView.onPause();
        mWebView.pauseTimers();
        mWebView.getSettings().setJavaScriptEnabled(false);//取消支持js
    }

    public void onResume() {
        if (mWebView == null)
            return;
        mWebView.onResume();
        mWebView.resumeTimers();
        mWebView.getSettings().setJavaScriptEnabled(true);//支持js
    }

    public void onDestroy() {
        if (mWebView == null)
            return;
        removeView(mWebView);
        if (mWebView != null) {
            mWebView.setVisibility(View.GONE);
            mWebView.stopLoading();
            mWebView.getSettings().setJavaScriptEnabled(false);
            mWebView.clearHistory();
            mWebView.clearView();
            mWebView.removeAllViews();
            mWebView.destroy();
            mWebView = null;
        }
    }

    public boolean isKeyDown(int keyCode) {
        if (mWebView == null)
            return false;
        // 是否按下返回键，且WebView现在的层级，可以返回
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return false;
    }

    public WebView getmWebView() {
        return mWebView;
    }

    public String getUrl() {
        return mUrl;
    }

    public ProgressBar getmProgressBar() {
        return mProgressBar;
    }

    public WebSettings getSettings() {
        return settings;
    }

    public void setOnloadPageListener(OnloadPageListener onloadPageListener) {
        this.onloadPageListener = onloadPageListener;
    }

    //网页加载监听
    public interface OnloadPageListener {
        String onFirstLoadPage();//网页首次加载
    }
}
