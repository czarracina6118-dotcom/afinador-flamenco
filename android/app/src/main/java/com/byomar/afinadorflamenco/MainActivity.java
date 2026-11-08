package com.byomar.afinadorflamenco;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.WindowManager;
import androidx.webkit.WebViewAssetLoader;
import java.io.ByteArrayInputStream;

public class MainActivity extends Activity {
    private static final String HOST = "appassets.androidplatform.net";
    private static final int MIC_PERMISSION = 1;
    private WebView webView;
    private PermissionRequest pending;

    private boolean isLocal(Uri uri) {
        return "https".equals(uri.getScheme()) && HOST.equals(uri.getHost())
                && (uri.getPort() == -1 || uri.getPort() == 443);
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        webView = new WebView(this);
        webView.setBackgroundColor(Color.BLACK);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setAllowContentAccess(false);
        // Keep controls outside system bars, including Android 15 edge-to-edge.
        webView.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
        webView.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (isLocal(request.getUrl())) {
                    WebResourceResponse response = loader.shouldInterceptRequest(request.getUrl());
                    if (response != null) return response;
                }
                // This tuner has no remote content or network dependency.
                return new WebResourceResponse("text/plain", "UTF-8", 404, "Not Found",
                        java.util.Collections.emptyMap(), new ByteArrayInputStream(new byte[0]));
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return !isLocal(request.getUrl());
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onPermissionRequest(PermissionRequest request) {
                String[] resources = request.getResources();
                if (!isLocal(request.getOrigin()) || resources.length != 1
                        || !PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resources[0])) {
                    request.deny();
                    return;
                }
                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    request.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});
                } else if (pending == null) {
                    pending = request;
                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIC_PERMISSION);
                } else {
                    request.deny();
                }
            }
            @Override public void onPermissionRequestCanceled(PermissionRequest request) {
                if (pending == request) pending = null;
            }
            @Override public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                new AlertDialog.Builder(MainActivity.this).setTitle("Afinador Flamenco")
                        .setMessage(message).setPositiveButton("Aceptar", (dialog, which) -> result.confirm())
                        .setOnCancelListener(dialog -> result.cancel()).show();
                return true;
            }
        });
        setContentView(webView);
        webView.loadUrl("https://" + HOST + "/assets/index.html");
    }

    @Override public void onRequestPermissionsResult(int code, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(code, permissions, results);
        if (code != MIC_PERMISSION || pending == null) return;
        PermissionRequest request = pending;
        pending = null;
        if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            request.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});
        } else {
            request.deny();
        }
    }

    @Override protected void onPause() {
        // The Android permission dialog can pause the Activity; keep its request alive.
        if (webView != null && pending == null) {
            webView.evaluateJavascript("if(typeof stop==='function') stop();", null);
        }
        if (webView != null) webView.onPause();
        super.onPause();
    }

    @Override protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
    }

    @Override protected void onStop() {
        if (pending != null) { pending.deny(); pending = null; }
        if (webView != null) webView.evaluateJavascript("if(typeof stop==='function') stop();", null);
        super.onStop();
    }

    @Override protected void onDestroy() {
        if (pending != null) { pending.deny(); pending = null; }
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
