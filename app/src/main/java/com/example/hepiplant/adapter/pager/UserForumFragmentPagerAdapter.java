package com.example.hepiplant.adapter.pager;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.hepiplant.fragments.PostsListFragment;
import com.example.hepiplant.fragments.SalesOffersListFragment;

public class UserForumFragmentPagerAdapter extends FragmentPagerAdapter {

    private final int PAGE_COUNT = 2;
    private final String[] tabTitles = new String[] { "Forum", "Bazarek" };
    private Context context;

    public UserForumFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        bundle.putString("view","user");
        switch (position) {
            case 0:
                PostsListFragment postsListFragment = new PostsListFragment();
                postsListFragment.setArguments(bundle);
                return postsListFragment;
            case 1:
                SalesOffersListFragment salesOffersListFragment = new SalesOffersListFragment();
                salesOffersListFragment.setArguments(bundle);
                return salesOffersListFragment;
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
