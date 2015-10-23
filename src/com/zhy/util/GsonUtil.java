package com.zhy.util;

import com.google.gson.Gson;

/**
 *  整个应用程序一个Gson就够了
 * @author Administrator
 *
 */
public class GsonUtil {
	private static Gson gson;

	public static Gson getGson() {
		if (gson == null) {
			gson = new Gson();
		}
		return gson;
	}
}
