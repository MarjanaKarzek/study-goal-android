package com.studygoal.jisc.Managers;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.studygoal.jisc.R;

public class SocialManager {
    private static final String TAG = SocialManager.class.getSimpleName();

    private static SocialManager ourInstance = new SocialManager();

    private SocialManager() {
    }

    public static SocialManager getInstance() {
        return ourInstance;
    }

    public void shareOnIntent(String text, String subject) {

        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        if(subject != null) {
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        } else {
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, text);
        }

        shareIntent.putExtra(Intent.EXTRA_TEXT, text);

        DataManager.getInstance().mainActivity.startActivity(shareIntent);
    }
}
