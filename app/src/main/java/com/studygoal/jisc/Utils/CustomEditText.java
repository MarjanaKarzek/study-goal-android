package com.studygoal.jisc.Utils;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.studygoal.jisc.Fragments.ActivityFeed.FeedFragment;
import com.studygoal.jisc.R;

/**
 * Custom Edit Text
 *
 * Used for feed push notification.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class CustomEditText extends AppCompatEditText {

    public FeedFragment fragment;

    public CustomEditText(Context context) {
        super(context);
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Listens on the back key to press to close the push message view.
     *
     * @param keyCode code of the key
     * @param event   event fired
     * @return handling
     */
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            fragment.mainView.findViewById(R.id.overlay).callOnClick();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
