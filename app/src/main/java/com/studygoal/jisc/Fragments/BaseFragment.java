package com.studygoal.jisc.Fragments;

import android.support.v4.app.Fragment;

/**
 * Base Fragment
 *
 * Provides a basic ui Thread method for several classes.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public abstract class BaseFragment extends Fragment {
    private static final String TAG = BaseFragment.class.getSimpleName();

    protected void runOnUiThread(Runnable action) {
        if (action != null && getActivity() != null) {
            getActivity().runOnUiThread(action);
        }
    }
}
