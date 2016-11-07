package com.grudus.nativeexamshelper.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.grudus.nativeexamshelper.activities.fragments.AddingExamFragment;
import com.grudus.nativeexamshelper.activities.fragments.OldExamsFragment;


public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private String[] tabsTitles;
    private Fragment[] fragments;

    public ViewPagerAdapter(FragmentManager fm, String[] tabsTitles) {
        super(fm);
        this.tabsTitles = tabsTitles;
        fragments = new Fragment[tabsTitles.length];
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        switch (position) {
            case 0: fragment = new AddingExamFragment(); break;
            case 1: fragment = new OldExamsFragment(); break;
            default: fragment = null;
        }
        return fragment;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object object =  super.instantiateItem(container, position);
        if (object instanceof Fragment) {
            fragments[position] = (Fragment) object;
        }
        return object;
    }

    public Fragment getFragment(int position) {
        if (position > fragments.length || position < 0) return null;
        return fragments[position];
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        fragments[position] = null;
        super.destroyItem(container, position, object);
    }

    @Override
    public int getCount() {
        return tabsTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabsTitles[position];
    }
}
