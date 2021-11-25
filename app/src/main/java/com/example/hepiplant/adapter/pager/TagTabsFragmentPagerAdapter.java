package com.example.hepiplant.adapter.pager;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.hepiplant.fragments.PostsListSimpleFragment;
import com.example.hepiplant.fragments.SalesOffersListSimpleFragment;

public class TagTabsFragmentPagerAdapter extends FragmentPagerAdapter {

    private final int PAGE_COUNT = 2;
    private final String[] tabTitles = new String[] { "Posty", "Oferty sprzedaży" };
    private final String tag;

    public TagTabsFragmentPagerAdapter(FragmentManager fm, Context context, String tag) {
        super(fm);
        this.tag = tag;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new PostsListSimpleFragment(tag);
            case 1:
                return new SalesOffersListSimpleFragment(tag);
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
