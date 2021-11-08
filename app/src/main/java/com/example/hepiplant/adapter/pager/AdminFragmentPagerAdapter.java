package com.example.hepiplant.adapter.pager;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.hepiplant.fragments.CategoryListFragment;
import com.example.hepiplant.fragments.PostsListFragment;
import com.example.hepiplant.fragments.SalesOffersListFragment;
import com.example.hepiplant.fragments.SpeciesListFragment;
import com.example.hepiplant.fragments.TagListFragment;

public class AdminFragmentPagerAdapter extends FragmentPagerAdapter {

    private final int PAGE_COUNT = 5;
    private final String[] tabTitles = new String[] { "Kategorie", "Gatunki", "Tagi", "Forum", "Bazarek" };
    private Context context;

    public AdminFragmentPagerAdapter(FragmentManager fm, Context context) {
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
                return new CategoryListFragment();
            case 1:
                return new SpeciesListFragment();
            case 2:
                return new TagListFragment();
            case 3:
                return new PostsListFragment();
            case 4:
                return new SalesOffersListFragment();
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
