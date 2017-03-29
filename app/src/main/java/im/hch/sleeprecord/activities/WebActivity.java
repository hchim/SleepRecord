package im.hch.sleeprecord.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import butterknife.BindView;
import butterknife.ButterKnife;
import im.hch.sleeprecord.R;

public class WebActivity extends AppCompatActivity {
    public static final String URL_EXTRA = "extra_url";

    @BindView(R.id.webview) WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        ButterKnife.bind(this);

        webView.getSettings().setJavaScriptEnabled(true);

        Bundle bundle = getIntent().getExtras();
        String url = bundle.getString(URL_EXTRA);
        webView.loadUrl(url);
    }
}
