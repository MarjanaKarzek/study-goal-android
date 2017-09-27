package com.studygoal.jisc.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.studygoal.jisc.Fragments.Target.TargetDetailsFragment;
import com.studygoal.jisc.Fragments.Target.TargetItemFragment;
import com.studygoal.jisc.Models.Targets;

import java.util.List;

public class TargetPagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = TargetPagerAdapter.class.getSimpleName();

    public List<Targets> list;
    public TargetDetailsFragment reference;

    public TargetPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getItemPosition(Object object){
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Fragment getItem(int position) {
        position = list.size() - 1 - position;
        TargetItemFragment fragment = new TargetItemFragment();
        fragment.target = list.get(position);
        fragment.position = position;
        fragment.reference = reference;
        return fragment;
    }
}