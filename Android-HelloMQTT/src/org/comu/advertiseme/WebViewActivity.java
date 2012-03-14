package org.comu.advertiseme;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
 
public class WebViewActivity extends Activity {
 
	private WebView webView;
 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
		Intent i = getIntent();

		// Receiving the Data
		String videoUrl = i.getStringExtra("url");
		
		webView = (WebView) findViewById(R.id.webView1);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl(videoUrl.toString());
	}
	}