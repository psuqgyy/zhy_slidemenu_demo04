package com.zhy.util;

import java.util.List;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.zhy.vo.ImageVO;
import com.zhy.vo.MessageVO;
import com.zhy.vo.VideoVO;

import android.content.Context;

/**
 * 主要是管理所有请求的生命周期。在程序退出或者切换出去的时候要取消请求
 * 
 * @author Administrator
 *
 */
public class NetManager {
	public static final String DISK_LRUCACHE_PATH="kuaishou";
	public static final String FIND_VIDEO_PAGE = "http://192.168.0.100:8080/kuaishou/Gson_UserAction_findVideosByPage";
	// 分页查找IMAGE
	public static final String FIND_IMAGE_PAGE = "http://192.168.0.100:8080/kuaishou/Gson_UserAction_findImagesByPage";
	// 分页查找MESSAGE
	public static final String FIND_MESSAGE_PAGE = "http://192.168.0.100:8080/kuaishou/Gson_UserAction_findMessagesByPage";
	// 实例化一个就够了
	private static NetManager manager;
	// 管理所有生命请求的生命周期。实例化的时候传入程序的第一个上下文
	private static RequestQueue queue;
	/** 第一次启动程序预加载数据 */
	public static List<MessageVO> messages;
	/** 第一次启动程序预加载数据 */
	public static List<ImageVO> images;
	/** 第一次启动程序预加载数据 */
	public static List<VideoVO> videos;
	//启动程序加载数据判断
	public static boolean startAppMessage =true;
	//启动程序加载数据判断
	public static boolean startAppImage=true;
	//启动程序加载数据判断
	public static boolean startAppVideo=true;
	public static NetManager getInstance(Context context) {
		if (manager == null) {
			manager = new NetManager(context);
		}
		return manager;
	}

	public NetManager(Context context) {
		// TODO Auto-generated constructor stub
		queue = Volley.newRequestQueue(context);
	}

	public static RequestQueue getQueue() {
		return queue;
	}


}
