package com.zhy.viewpager;

import android.support.v4.app.Fragment;

public abstract class MyBaseFragment extends Fragment {
	/**
	 * 在这个方法体内加载数据，实现延时加载数据
	 */
	public abstract void lazyLoad();
}
