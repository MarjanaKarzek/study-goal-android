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
 * Privacy Settings Fragment
 * <p>
 * Displayes privacy disclaimer of Study Goal.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class PrivacySettingsFragment extends Fragment {

    private static final String TAG = PrivacySettingsFragment.class.getSimpleName();

    @Override
    public void onResume() {
        super.onResume();
        DataManager.getInstance().mainActivity.setTitle(DataManager.getInstance().mainActivity.getString(R.string.privacy_statement));
        DataManager.getInstance().mainActivity.hideAllButtons();
        DataManager.getInstance().mainActivity.showCertainButtons(7);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.layout_privacy_webview, container, false);
        WebView webView = (WebView) mainView.findViewById(R.id.privacy_web_view);
        webView.loadUrl("https://github.com/jiscdev/learning-analytics/wiki/Privacy-Statement");
        return mainView;
    }
}
