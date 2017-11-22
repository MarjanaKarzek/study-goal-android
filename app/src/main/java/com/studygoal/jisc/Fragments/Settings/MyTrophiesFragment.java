package com.studygoal.jisc.Fragments.Settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.activeandroid.query.Select;
import com.studygoal.jisc.Activities.MainActivity;
import com.studygoal.jisc.Activities.SettingsActivity;
import com.studygoal.jisc.Activities.TrophyDetailsActivity;
import com.studygoal.jisc.Adapters.MyTrophiesAdapter;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Models.Trophy;
import com.studygoal.jisc.Models.TrophyMy;
import com.studygoal.jisc.R;

/**
 * Created by MarcelC on 1/14/16.
 */
public class MyTrophiesFragment extends Fragment {
    private static final String TAG = MyTrophiesFragment.class.getSimpleName();

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
        final MyTrophiesAdapter adapter = new MyTrophiesAdapter(getActivity(), this);
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

    public void showTrophy(TrophyMy trophyMy) {
        Intent intent = new Intent(DataManager.getInstance().mainActivity, TrophyDetailsActivity.class);
        intent.putExtra("days", trophyMy.days);
        intent.putExtra("type", trophyMy.trophy_type.substring(0, 1).toUpperCase() + trophyMy.trophy_type.substring(1, trophyMy.trophy_type.length()));
        intent.putExtra("activity_name", trophyMy.activity_name);
        intent.putExtra("image", trophyMy.getImageName());
        intent.putExtra("title", trophyMy.trophy_name);
        intent.putExtra("details", trophyMy.count);

        Trophy trophy = new Select().from(Trophy.class).where("trophy_id = ?", trophyMy.trophy_id).executeSingle();
        intent.putExtra("statement", trophy.statement);

        startActivity(intent);
    }
}
