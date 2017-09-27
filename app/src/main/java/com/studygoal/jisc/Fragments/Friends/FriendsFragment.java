package com.studygoal.jisc.Fragments.Friends;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.studygoal.jisc.Managers.DataManager;
import com.studygoal.jisc.Models.ReceivedRequest;
import com.studygoal.jisc.R;

public class FriendsFragment extends Fragment {
    private static final String TAG = FriendsFragment.class.getSimpleName();

    private FragmentTabHost fragmentTabHost;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.layout_friends, container, false);

        fragmentTabHost = (FragmentTabHost) mainView.findViewById(R.id.tabhost);

        if(!DataManager.getInstance().isLandscape)
            fragmentTabHost.setup(DataManager.getInstance().mainActivity, getChildFragmentManager(), R.id.realtabcontent);
        else
            fragmentTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);

        fragmentTabHost.addTab(fragmentTabHost.newTabSpec("search").setIndicator(DataManager.getInstance().mainActivity.getString(R.string.search)),
                FriendsSearchFragment.class, null);
        fragmentTabHost.addTab(fragmentTabHost.newTabSpec("newrequests").setIndicator(DataManager.getInstance().mainActivity.getString(R.string.requests)),
                FriendsRequestsFragment.class, null);
        fragmentTabHost.addTab(fragmentTabHost.newTabSpec("myfriends").setIndicator(DataManager.getInstance().mainActivity.getString(R.string.friends)),
                FriendsListFragment.class, null);

        for(int i = 0; i < 3; i++) {
            View v = fragmentTabHost.getTabWidget().getChildTabViewAt(i);
            ((TextView)v.findViewById(android.R.id.title)).setTextColor(ContextCompat.getColor(DataManager.getInstance().mainActivity, R.color.colorPrimary));
            ((TextView)v.findViewById(android.R.id.title)).setTypeface(DataManager.getInstance().myriadpro_regular);
            ((TextView)v.findViewById(android.R.id.title)).setTextSize(16f);
            ((TextView)v.findViewById(android.R.id.title)).setAllCaps(false);
            fragmentTabHost.getTabWidget().getChildAt(i).getLayoutParams().height = (int) (50 * this.getResources().getDisplayMetrics().density);
        }

        if(new Select().from(ReceivedRequest.class).count() > 0)
            fragmentTabHost.setCurrentTab(1);

        return mainView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentTabHost = null;
    }
}
