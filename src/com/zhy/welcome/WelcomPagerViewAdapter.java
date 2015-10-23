package com.zhy.welcome;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class WelcomPagerViewAdapter extends FragmentPagerAdapter {

	List<Fragment> tabs;
	public WelcomPagerViewAdapter(FragmentManager fm,List<Fragment> fragments) {
		super(fm);
		tabs=fragments;
	}

	@Override
	public Fragment getItem(int arg0) {
		return tabs.get(arg0);
	}

	@Override
	public int getCount() {
		return tabs.size();
	}

}
