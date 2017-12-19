package com.studygoal.jisc.Fragments.Settings.Submenu;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.R;

/**
 * Terms Settings Fragment
 * <p>
 * Displays the terms and conditions of Study Goal
 *
 * @author Therapy Box - Marjana Karzek
 * @version 1.5
 * @date 07/09/17
 */
public class TermsSettingsFragment extends Fragment {

    private static final String TAG = TermsSettingsFragment.class.getSimpleName();

    @Override
    public void onResume() {
        super.onResume();
        DataManager.getInstance().mainActivity.setTitle(DataManager.getInstance().mainActivity.getString(R.string.terms_title));
        DataManager.getInstance().mainActivity.hideAllButtons();
        DataManager.getInstance().mainActivity.showCertainButtons(7);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.layout_settings_terms_screen, container, false);

        WebView webView = (WebView) mainView.findViewById(R.id.webviewTerms);
        webView.loadUrl("https://docs.analytics.alpha.jisc.ac.uk/docs/learning-analytics/App-service-terms-and-conditions");

        return mainView;
    }
}
