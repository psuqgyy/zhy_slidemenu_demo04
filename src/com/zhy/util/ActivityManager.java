package com.zhy.util;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

public class ActivityManager {
	List<Activity> as = new ArrayList<Activity>();
	private static ActivityManager manager;

	public static ActivityManager getInstance() {
		if (manager == null) {
			manager = new ActivityManager();
		}
		return manager;
	}

	public void addActivity(Activity activity) {
		as.add(activity);
	}

	public void delActivity(Activity activity) {
		as.remove(activity);
	}

	public void delAllActivity() {
		for (Activity a : as) {
			as.remove(a);
		}
	}
}
