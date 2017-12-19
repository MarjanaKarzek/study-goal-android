package com.studygoal.jisc.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.studygoal.jisc.Fragments.Target.TargetDetailsFragment;
import com.studygoal.jisc.Fragments.Target.TargetItemFragment;
import com.studygoal.jisc.Models.Targets;

import java.util.List;

/**
 * Recurring Target Pager Adapter
 * <p>
 * Handles recurring target slider.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class RecurringTargetPagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = RecurringTargetPagerAdapter.class.getSimpleName();

    public List<Targets> list;
    public TargetDetailsFragment reference;

    public RecurringTargetPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getItemPosition(Object object) {
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