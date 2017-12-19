package com.studygoal.jisc.Fragments.Settings.Submenu.Trophies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.studygoal.jisc.Activities.MainActivity;
import com.studygoal.jisc.Activities.TrophyDetailsActivity;
import com.studygoal.jisc.Adapters.TrophiesAdapter;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Models.Trophy;
import com.studygoal.jisc.R;

/**
 * All Trophies Fragment
 * <p>
 * Displays all trophies.
 *
 * @author Therapy Box - MarcelC
 * @version 1.5
 * @date 14/01/16
 */
public class AllTrophiesFragment extends Fragment {
    private static final String TAG = AllTrophiesFragment.class.getSimpleName();

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setTitle(DataManager.getInstance().mainActivity.getString(R.string.trophies_title));
        ((MainActivity) getActivity()).hideAllButtons();
        DataManager.getInstance().mainActivity.showCertainButtons(7);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.layout_trophies_all, container, false);

        final GridView list = (GridView) mainView.findViewById(R.id.gridlist);
        final View noData = mainView.findViewById(R.id.no_data);
        final TrophiesAdapter adapter = new TrophiesAdapter(getActivity(), this);
        list.setAdapter(adapter);

        if (adapter.getCount() > 0) {
            list.setVisibility(View.VISIBLE);
            noData.setVisibility(View.GONE);
        } else {
            list.setVisibility(View.GONE);
            noData.setVisibility(View.VISIBLE);
        }

        return mainView;
    }

    /**
     * Navigates to the details page for the chosen trophy.
     *
     * @param trophy trophy to be displayed
     */
    public void showTrophy(Trophy trophy) {
        Intent intent = new Intent(DataManager.getInstance().mainActivity, TrophyDetailsActivity.class);

        intent.putExtra("type", trophy.trophy_type.substring(0, 1).toUpperCase() + trophy.trophy_type.substring(1, trophy.trophy_type.length()));
        intent.putExtra("image", trophy.getImageName());
        intent.putExtra("title", trophy.trophy_name);
        intent.putExtra("statement", trophy.statement);

        startActivity(intent);
    }
}
