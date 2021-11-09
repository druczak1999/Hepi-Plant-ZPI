package com.example.hepiplant.adapter.pager;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.hepiplant.fragments.EventsListFragment;
import com.example.hepiplant.fragments.PlantsListFragment;
import com.example.hepiplant.fragments.SalesOffersListFragment;

public class MainFragmentPagerAdapter extends FragmentPagerAdapter {

    private final int PAGE_COUNT = 2;
    private final String[] tabTitles = new String[] { "Twoje rośliny", "Kalendarz" };
    private Context context;

    public MainFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new PlantsListFragment();
            case 1:
                return new EventsListFragment();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
