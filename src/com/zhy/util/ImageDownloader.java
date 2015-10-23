package com.zhy.util;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

public class ImageDownloader {
	// 打印标识，调试使用
	private static final String TAG = "ImageDownloader";
	// 装载线程，主要功能是防止同一个图片加载使用多次线程加载。
	private Map<String, MyAsyncTask> tasks = new HashMap<String, MyAsyncTask>();
	// 内存缓存类
	private LruCache<String, Bitmap> mLruCache;

	private static ImageDownloader i;

	public static ImageDownloader getInstance() {
		if (i == null) {
			i = new ImageDownloader();
		}
		return i;
	}

	public ImageDownloader() {
		// TODO Auto-generated constructor stub
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int mCacheSize = maxMemory / 8;
		mLruCache = new LruCache<String, Bitmap>(mCacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				// TODO Auto-generated method stub
				return value.getRowBytes() * value.getHeight();
			}
		};
	}

	/**
	 * 外部调用接口
	 * 
	 * @param url
	 *            图片的地址
	 * @param imageView
	 *            控件
	 * @param path
	 *            储存磁盘的地址
	 * @param activity
	 *            活动上下文
	 * @param download
	 *            回调函数，在onPostExecute()中调用
	 */
	public void imageDownload(String url, ImageView imageView, String path, Activity activity,
			OnImageDownload download) {
		// 从软引用中取数据

		String imageName = null;
		if (url != null) {
			imageName = ImageUtil.getInstance().getImageName(url);
		}
		// 从磁盘中取数据
		Bitmap diskBitMap = ImageUtil.getInstance().getBitmapFromFile(activity, imageName, path);
		Bitmap bitmap = getBitmapFromMermoryCache(url);
		if (bitmap != null) {
			L.e("~~~~~~~~~~~~", "已经使用多少内存了------->"+mLruCache.size()/1024/1024);
			imageView.setImageBitmap(bitmap);
			Log.e("从内存缓存中取图片", imageName + "");
		} else if (diskBitMap != null) {
			imageView.setImageBitmap(diskBitMap);
			addBitmapToMermoryCache(url, diskBitMap);
			Log.e("从磁盘用中取图片", imageName + "");
		} else if (needCreateNewTask(imageView)) {
			Log.e("开启新线程下载图片", imageName + "");
			MyAsyncTask task = new MyAsyncTask(imageView, url, download, path, activity);
			Log.e(TAG, "执行MyAsyncTask --> " + ImageUtil.flag);
			ImageUtil.flag++;
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			tasks.put(url, task);
		}

	}

	/**
	 * 添加图片到内存缓存
	 * 
	 * @param key
	 * @param bitmap
	 */
	public void addBitmapToMermoryCache(String url, Bitmap bitmap) {
		if (getBitmapFromMermoryCache(url) == null) {
			mLruCache.put(url, bitmap);
			L.e("显示已经使用多少空间了------->", (float) mLruCache.size() / 1024 / 1024 + "");
		}
	}

	/**
	 * 从缓存中获取内存图片
	 * 
	 * @param key
	 * @return
	 */
	public Bitmap getBitmapFromMermoryCache(String url) {
		return mLruCache.get(url);
	}

	/**
	 * 线程池中是否已经包含次线程
	 * 
	 * @param url
	 *            线程的标识
	 * @return
	 */
	private boolean isTasksContains(String url) {
		boolean b = false;
		if (url != null && tasks.get(url) == null) {
			b = true;
		}
		return b;
	}

	/**
	 * 判断是否需要开启新的线程
	 * 
	 * @param imageView
	 * @return
	 */
	private boolean needCreateNewTask(ImageView imageView) {
		boolean b = false;
		if (imageView != null) {
			String url = imageView.getTag().toString();
			if (isTasksContains(url)) {
				b = true;
			}
		}
		return b;
	}

	/**
	 * 当线程完成后，删除线程的引用
	 * 
	 * @param url
	 */
	private void removeTask(String url) {
		if (url != null && tasks != null && tasks.get(url) != null) {
			tasks.remove(url);
		}
	}

	// 线程类
	private class MyAsyncTask extends AsyncTask<String, Void, Bitmap> {

		// 显示图拍控件
		private ImageView imageView;
		// 图片地址
		private String url;
		// 主线程回调函数，返回主线程的函数
		private OnImageDownload download;
		// 磁盘保存地址
		private String path;
		// 上下文
		private Activity activity;

		public MyAsyncTask(ImageView imageView, String url, OnImageDownload download, String path, Activity activity) {
			super();
			this.imageView = imageView;
			this.url = url;
			this.download = download;
			this.path = path;
			this.activity = activity;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			// TODO Auto-generated method stub
			Bitmap bitmap = null;
			if (url != null) {
				try {
					URL curl = new URL(url);
					InputStream is = curl.openStream();
					bitmap = BitmapFactory.decodeStream(is);
					String imageName = ImageUtil.getInstance().getImageName(url);
					if (!ImageUtil.getInstance().setBitmapToFile(path, activity, imageName, bitmap)) {
						ImageUtil.getInstance().removeBitmapFromFile(path, activity, imageName);
					}
					addBitmapToMermoryCache(url, bitmap);

				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub

			if (download != null) {
				download.onSuccess(result, url, imageView);
				removeTask(url);
				Log.e("当前线程池数量", tasks.size() + "");
			}

			super.onPostExecute(result);
		}

	}

	// 回调函数接口。当完成下载后调用
	public interface OnImageDownload {
		void onSuccess(Bitmap bitmap, String url, ImageView imageView);
	}
}
