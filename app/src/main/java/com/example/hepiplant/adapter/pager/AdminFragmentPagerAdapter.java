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
import com.example.hepiplant.fragments.UsersListFragment;

public class AdminFragmentPagerAdapter extends FragmentPagerAdapter {

    private final String[] tabTitles = new String[] { "Kategorie", "Gatunki", "Tagi", "Forum", "Bazarek", "Użytkownicy" };

    public AdminFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
    }

    @Override
    public int getCount() {
        int PAGE_COUNT = 6;
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
            case 5:
                return new UsersListFragment();
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
