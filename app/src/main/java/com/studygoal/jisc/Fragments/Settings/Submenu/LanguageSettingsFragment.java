package com.studygoal.jisc.Fragments.Settings.Submenu;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.studygoal.jisc.Adapters.GenericAdapter;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Managers.NetworkManager;
import com.studygoal.jisc.R;
import com.studygoal.jisc.Utils.Connection.ConnectionHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Language Settings Fragment
 * <p>
 * Provides the possibility to select the language settings for the following languages:
 * <p>
 * English
 * Welsh
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class LanguageSettingsFragment extends Fragment {

    private static final String TAG = LanguageSettingsFragment.class.getSimpleName();

    public String selectedValue;

    @Override
    public void onResume() {
        super.onResume();
        DataManager.getInstance().mainActivity.setTitle(DataManager.getInstance().mainActivity.getString(R.string.language_title));
        DataManager.getInstance().mainActivity.hideAllButtons();
        DataManager.getInstance().mainActivity.showCertainButtons(7);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.layout_generic_screen, container, false);

        selectedValue = DataManager.getInstance().language == null
                || DataManager.getInstance().language.toLowerCase().equals("english") ?
                getString(R.string.english) : getString(R.string.welsh);

        ((TextView) mainView.findViewById(R.id.title)).setText(DataManager.getInstance().mainActivity.getString(R.string.setup_language));

        final ArrayList<String> list = new ArrayList<>();
        list.add(DataManager.getInstance().mainActivity.getString(R.string.english));
        list.add(DataManager.getInstance().mainActivity.getString(R.string.welsh));

        final ListView listView = (ListView) mainView.findViewById(R.id.list);
        listView.setAdapter(new GenericAdapter(DataManager.getInstance().mainActivity, selectedValue, list));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (DataManager.getInstance().user.email.equals("demouser@jisc.ac.uk")) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LanguageSettingsFragment.this.getActivity());
                    alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3791ee'>" + getString(R.string.demo_mode_changeappsettings) + "</font>"));
                    alertDialogBuilder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                    return;
                } else if (ConnectionHandler.isConnected(getContext())) {
                    if (!((TextView) view.findViewById(R.id.dialog_item_name)).getText().toString().equals(selectedValue)) {

                        String lang = ((TextView) view.findViewById(R.id.dialog_item_name)).getText().toString().toLowerCase().equals(getActivity().getString(R.string.english).toLowerCase()) ? "english" : "welsh";
                        HashMap<String, String> map = new HashMap<>();
                        map.put("student_id", DataManager.getInstance().user.id);
                        map.put("setting_type", "language");
                        map.put("setting_value", lang);
                        if (NetworkManager.getInstance().changeAppSettings(map)) {
                            DataManager.getInstance().language = lang;
                            SharedPreferences preferences = DataManager.getInstance().mainActivity.getSharedPreferences("jisc", Context.MODE_PRIVATE);
                            preferences.edit().putString("language", DataManager.getInstance().language).apply();

                            if (DataManager.getInstance().language.equals("english")) {
                                Locale locale = new Locale("en");
                                Locale.setDefault(locale);
                                Configuration config = new Configuration();
                                config.locale = locale;
                                getActivity().getBaseContext().getResources().updateConfiguration(config,
                                        getActivity().getBaseContext().getResources().getDisplayMetrics());
                            } else if (DataManager.getInstance().language.equals("welsh")) {
                                Locale locale = new Locale("cy");
                                Locale.setDefault(locale);
                                Configuration config = new Configuration();
                                config.locale = locale;
                                getActivity().getBaseContext().getResources().updateConfiguration(config,
                                        getActivity().getBaseContext().getResources().getDisplayMetrics());
                            }
                            DataManager.getInstance().reload();
                            DataManager.getInstance().languageChanged = true;
                            DataManager.getInstance().mainActivity.recreate();
                        }
                    }
                    if (!DataManager.getInstance().mainActivity.isLandscape)
                        DataManager.getInstance().mainActivity.onBackPressed();
                    else {
                        String selected = DataManager.getInstance().language.toLowerCase().equals("english") ? getString(R.string.english) : getString(R.string.welsh);
                        list.clear();
                        list.add(getActivity().getString(R.string.english));
                        list.add(getActivity().getString(R.string.welsh));
                        listView.setAdapter(new GenericAdapter(getActivity(), selected.toUpperCase(), list));
                        DataManager.getInstance().mainActivity.onBackPressed();
                    }
                } else {
                    ConnectionHandler.showNoInternetConnectionSnackbar();
                }
            }
        });

        return mainView;
    }
}