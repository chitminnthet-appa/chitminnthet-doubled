package com.doubled.control

import android.app.Activity
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.view.WindowManager

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        db = AppDatabase(this)
        webView = WebView(this)
        setContentView(webView)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.settings.setSupportZoom(false)
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()
        webView.addJavascriptInterface(DatabaseBridge(db), "AndroidDB")
        webView.loadUrl("file:///android_asset/index.html")
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}

private class DatabaseBridge(private val db: AppDatabase) {
    @JavascriptInterface fun getAgents(): String = db.getAgents().toString()
    @JavascriptInterface fun saveAgent(name: String, brake: String, commission: String, rate: String) {
        db.saveAgent(name, brake.toDoubleOrNull() ?: 0.0, commission.toDoubleOrNull() ?: 0.0, rate.toDoubleOrNull() ?: 0.0)
    }
    @JavascriptInterface fun deleteAgent(name: String) { db.deleteAgent(name) }
    @JavascriptInterface fun renameAgent(oldName: String, newName: String, brake: String, commission: String, rate: String) {
        db.renameAgent(oldName, newName, brake.toDoubleOrNull() ?: 0.0, commission.toDoubleOrNull() ?: 0.0, rate.toDoubleOrNull() ?: 0.0)
    }
    @JavascriptInterface fun getEntries(): String = db.getEntries().toString()
    @JavascriptInterface fun saveEntry(session: String, agent: String, type: String, digits: String, amount: Double, date: String) {
        db.saveEntry(session, agent, type, digits, amount, date)
    }
    @JavascriptInterface fun getResults(): String = db.getResults().toString()
    @JavascriptInterface fun saveResult(session: String, date: String, result: String) { db.saveResult(session, date, result) }
}
