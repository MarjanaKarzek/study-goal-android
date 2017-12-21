package com.studygoal.jisc.Managers;

import android.content.Intent;

/**
 * Social Manager class
 * <p>
 * Provides the share intent to share objects via social channels.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class SocialManager {

    private static final String TAG = SocialManager.class.getSimpleName();

    private static SocialManager ourInstance = new SocialManager();

    private SocialManager() {

    }

    public static SocialManager getInstance() {
        return ourInstance;
    }

    /**
     * Creates the share intent and handles it result.
     *
     * @param text    text to share
     * @param subject subject of the message
     */
    public void shareOnIntent(String text, String subject) {

        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        if (subject != null) {
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        } else {
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, text);
        }

        shareIntent.putExtra(Intent.EXTRA_TEXT, text);

        DataManager.getInstance().mainActivity.startActivity(shareIntent);
    }

}
