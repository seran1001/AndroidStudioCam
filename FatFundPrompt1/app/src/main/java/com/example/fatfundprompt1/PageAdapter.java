package com.example.fatfundprompt1;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class PageAdapter extends FragmentPagerAdapter {

    private int numOfTabs;

    public PageAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.numOfTabs =numOfTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new PhotoReview();
            case 2:
                return new MyPageFragment();
            case 3:
                return new PTFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 0;
    }
}
