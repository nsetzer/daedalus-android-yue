package com.github.nicksetzer.daedalus.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.webkit.WebViewAssetLoader;

import com.github.nicksetzer.daedalus.Log;

public class DaedalusWebViewClient extends WebViewClient {

    Activity m_activity;
    boolean m_devel;

    WebViewAssetLoader m_assetLoader;

    public DaedalusWebViewClient(Activity activity, boolean devel) {
        m_activity = activity;
        m_devel = devel;

        m_assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(activity))
                .addPathHandler("/res/", new WebViewAssetLoader.ResourcesPathHandler(activity))
                .build();

    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view,
                                                      WebResourceRequest request) {
        Log.error("asset: " + request.getUrl());
        return m_assetLoader.shouldInterceptRequest(request.getUrl());
    }
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        android.util.Log.e("daedalus-js", "ssl error handled");

        if (m_devel) {
            handler.proceed();
            return;
        }

        final SslErrorHandler fHandler = handler;
        final AlertDialog.Builder builder = new AlertDialog.Builder(m_activity);
        String message = "SSL Certificate error.";
        switch (error.getPrimaryError()) {
            case SslError.SSL_UNTRUSTED:
                message = "The certificate authority is not trusted.";
                break;
            case SslError.SSL_EXPIRED:
                message = "The certificate has expired.";
                break;
            case SslError.SSL_IDMISMATCH:
                message = "The certificate Hostname mismatch.";
                break;
            case SslError.SSL_NOTYETVALID:
                message = "The certificate is not yet valid.";
                break;
        }
        message += " Do you want to continue anyway?";

        builder.setTitle("SSL Certificate Error");
        builder.setMessage(message);
        builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fHandler.proceed();
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fHandler.cancel();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

}
