package com.zhy.util;

import android.util.Log;
/**
 * 开发日志。装逼模式
 * @author Administrator
 *
 */
public class L {
	public static final boolean DEBUG = true;

	public static void e(String tag, String msg) {
		if (DEBUG)
			Log.e(tag, msg);
	}
	public static void e(String tag,int msg){
		if (DEBUG)
			Log.e(tag, msg+"");
	}

}
