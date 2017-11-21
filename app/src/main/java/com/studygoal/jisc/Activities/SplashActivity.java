package com.studygoal.jisc.Activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.R;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = SplashActivity.class.getSimpleName();

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NetworkManager.getInstance().init(getApplicationContext());
        DataManager.getInstance().currActivity = this;

        if(getResources().getBoolean(R.bool.landscape_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            DataManager.getInstance().isLandscape = true;
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            DataManager.getInstance().isLandscape = false;
        }

        setContentView(R.layout.activity_layout_splash);

        Handler handler = (Handler) new Handler();

        handler.postDelayed(
                new Runnable() {
                    public void run() {
                        jump();
                    }
                },
                1500);
    }



    public void jump() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        this.finish();
    }

}
