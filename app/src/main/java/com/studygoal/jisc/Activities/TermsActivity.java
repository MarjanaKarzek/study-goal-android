package com.studygoal.jisc.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.R;

/**
 * Created by Marjana Karzek on 06/09/17.
 */

public class TermsActivity extends AppCompatActivity{
    private static final String TAG = TermsActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_terms);

        DataManager.getInstance().context = getApplicationContext();
        DataManager.getInstance().init();
        DataManager.getInstance().currActivity = this;

        WebView webView = (WebView) findViewById(R.id.webviewTerms);
        webView.loadUrl("https://docs.analytics.alpha.jisc.ac.uk/docs/learning-analytics/App-service-terms-and-conditions");

        Button decline = (Button) findViewById(R.id.button_decline);
        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TermsActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        Button accept = (Button) findViewById(R.id.button_accept);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TermsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
