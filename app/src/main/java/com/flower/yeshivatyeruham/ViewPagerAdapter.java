package com.flower.yeshivatyeruham;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

/**
 *
 */
public class ViewPagerAdapter extends SmartFragmentStatePagerAdapter {
    int mNumOfTabs;
    RecordsFragment tab1;
    SksFragment tab2;
    ContactsFragment tab3;

    public ViewPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        tab1 = new RecordsFragment();
        tab2 = new SksFragment();
        tab3 = new ContactsFragment();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
//                    RecordsFragment tab1 = new RecordsFragment();
                return tab1;
            case 1:
//                    SksFragment tab2 = new SksFragment();
                return tab2;
            case 2:
//                    ContactsFragment tab3 = new ContactsFragment();
                return tab3;
            default:
                return null;
        }
    }
    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}

abstract class SmartFragmentStatePagerAdapter extends FragmentStatePagerAdapter {
    // Sparse array to keep track of registered fragments in memory
    private SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

    public SmartFragmentStatePagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    // Register the fragment when the item is instantiated
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    // Unregister when the item is inactive
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    // Returns the fragment for the position (if instantiated)
    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
}
