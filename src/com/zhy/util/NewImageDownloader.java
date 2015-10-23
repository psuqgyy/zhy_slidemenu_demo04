package com.zhy.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;

import com.zhy.io.DiskLruCache;
import com.zhy.io.DiskLruCache.Snapshot;
import com.zhy.zhy_slidemenu_demo04.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

/**
 * 我们经常会使用一种非常流行的内存缓存技术的实现，即软引用或弱引用 (SoftReference or
 * WeakReference)。但是现在已经不再推荐使用这种方式了，因为从 Android 2.3 (API Level
 * 9)开始，垃圾回收器会更倾向于回收持有软引用或弱引用的对象，这让软引用和弱引用变得不再可靠。另外，Android 3.0 (API Level
 * 11)中，图片的数据会存储在本地的内存当中，因而无法用一种可预见的方式将其释放，这就有潜在的风险造成应用程序的内存溢出并崩溃。
 * 
 * @author Administrator LurCache它的主要算法原理是把最近使用的对象用强引用存储在 LinkedHashMap
 *         中，并且把最近最少使用的对象在缓存值达到预设定值之前从内存中移除
 */
public class NewImageDownloader {

	private static NewImageDownloader n;
	// 调试使用
	private static final String TAG = "NewImageDownloader";
	// 内存缓存类
	private LruCache<String, Bitmap> mLruCache;
	// 磁盘缓存类
	public DiskLruCache mDiskLruCache;
	// 磁盘缓存地址
	private File diskCacheFile;
	// 应用程序版本号
	private int appVersion;
	// 防止同一张图片开启多次线程
	private Map<String,AsyncTask> tasks = new HashMap<String, AsyncTask>();

	public static NewImageDownloader getInstance(Activity activity, String uniqueName) {
		if (n == null) {
			n = new NewImageDownloader(activity, uniqueName);
		}
		return n;
	}

	public NewImageDownloader(Activity activity, String uniqueName) {
		// TODO Auto-generated constructor stub
		int cacheSize = AppUtil.getInstance().getAppMaxMemory() / 8;
		mLruCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				// TODO Auto-generated method stub
				return value.getRowBytes() * value.getHeight();
			}
		};

		// 获取当前app应用程序版本
		appVersion = AppUtil.getInstance().getAppVersion(activity);
		try {

			// 获取磁盘缓存地址
			diskCacheFile = AppUtil.getInstance().getDiskCacheDir(activity, uniqueName);
			if (!diskCacheFile.exists()) {
				diskCacheFile.mkdirs();
			}
			mDiskLruCache = DiskLruCache.open(diskCacheFile, appVersion, 1, cacheSize);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
	}

	/**
	 * 初始化操作
	 * 
	 * @param url
	 *            图片网络地址
	 * @param activity
	 *            活动上下文
	 * @param uniqueName
	 *            磁盘缓存文件夹名字
	 * @param imageView
	 *            图片显示空间
	 * @param imageloader
	 *            回调函数，主线程更新UI
	 */
	public void imageDownload(String url, Activity activity, ImageView imageView, OnImageLoader imageloader) {
		// TODO Auto-generated constructor stub

		// 开始获取图片
		Bitmap mermoryBitmap = getBitmapFromMermoryCache(url);
		Bitmap diskBitmap = getBitmapFromDiskMermory(url);
		if (mermoryBitmap != null) {
			imageView.setImageBitmap(mermoryBitmap);
			//L.e(TAG, "从内存中取出图片,当前已经使用内存缓存---->"+mLruCache.size()/1024/1024);
		//	L.e(TAG, url);
		} else if (diskBitmap != null) {
			imageView.setImageBitmap(diskBitmap);
			addBitmapToMermoryCache(url, diskBitmap);
		} else if (needNewTask(url)) {
			ImageAsyncTask task = new ImageAsyncTask(imageView, url, imageloader);
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			tasks.put(url, task);
		}

	}
	/**
	 * 第一次启动程序预加载数据开启多线程
	 * @param url
	 */
	public void imagePreDownload(String url) {
		// TODO Auto-generated constructor stub
		ImagePreAsyncTask task = new ImagePreAsyncTask(url);
		task.execute();
		tasks.put(url, task);
	}

	/**
	 * 是否需要开启新的线程
	 * 
	 * @param url
	 * @return
	 */
	public boolean needNewTask(String url) {

		boolean b = false;
		if (tasks.get(url) == null) {
			b = true;
		}
		return b;
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
	 * 根据图片的地址转换成数据流
	 * 
	 * @param urlString
	 * @param outputStream
	 * @return
	 */
	private boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
		HttpURLConnection urlConnection = null;
		BufferedOutputStream out = null;
		BufferedInputStream in = null;
		try {
			final URL url = new URL(urlString);
			urlConnection = (HttpURLConnection) url.openConnection();
			in = new BufferedInputStream(urlConnection.getInputStream(), 8 * 1024);
			out = new BufferedOutputStream(outputStream, 8 * 1024);
			int b;
			while ((b = in.read()) != -1) {
				out.write(b);
			}
			return true;
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 从磁盘中获取数据
	 * 
	 * @param url
	 * @return
	 */
	private Bitmap getBitmapFromDiskMermory(String url) {
		Bitmap bitmap = null;
		String key = AppUtil.getInstance().string2MD5(url);
		try {
			DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
			if (snapshot != null) {
				// 配置加载到内存中图片大小
				BitmapFactory.Options opt = new BitmapFactory.Options();
				opt.inPreferredConfig = Bitmap.Config.RGB_565;
				opt.inPurgeable = true;
				opt.inInputShareable = true;
				InputStream is = snapshot.getInputStream(0);
				bitmap = BitmapFactory.decodeStream(is, null, opt);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		return bitmap;
	}

	private class ImageAsyncTask extends AsyncTask<String, Void, Bitmap> {

		private ImageView imageView;
		private String urlString;
		private OnImageLoader imageLoader;

		public ImageAsyncTask(ImageView imageView, String urlString, OnImageLoader imageLoader) {
			this.imageView = imageView;
			this.urlString = urlString;
			this.imageLoader = imageLoader;
		}

		/**
		 * 先将图片内容放到磁盘上，然后在放入内存
		 */

		@Override
		protected Bitmap doInBackground(String... params) {
			// TODO Auto-generated method stub
			Bitmap bitmap = null;
			String key = AppUtil.getInstance().string2MD5(urlString);
			// 将图片写入磁盘缓存区
			try {
				DiskLruCache.Editor editor = mDiskLruCache.edit(key);
				if (editor != null) {
					OutputStream outputStream = editor.newOutputStream(0);
					if (downloadUrlToStream(urlString, outputStream)) {
						editor.commit();
					} else {
						editor.abort();
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

			bitmap = getBitmapFromDiskMermory(urlString);
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (imageLoader != null) {
				imageLoader.onSuccess(urlString, imageView, result);
			}
			tasks.remove(urlString);
		}

	}

	public interface OnImageLoader {
		void onSuccess(String url, ImageView imageView, Bitmap bitmap);
	}

	/**
	 * 预加载线程。将图片放入内存和磁盘
	 * 
	 * @author Administrator
	 *
	 */
	private class ImagePreAsyncTask extends AsyncTask<String, Void, Void> {

		private String urlString;

		public ImagePreAsyncTask(String url) {
			// TODO Auto-generated constructor stub
			urlString = url;
		}

		@Override
		protected Void doInBackground(String... params) {
			String key = AppUtil.getInstance().convertMD5(urlString);
			try {
				DiskLruCache.Editor editor = mDiskLruCache.edit(key);
				if (editor != null) {
					OutputStream outputStream = editor.newOutputStream(0);
					if (downloadUrlToStream(urlString, outputStream)) {
						editor.commit();
					} else {
						editor.abort();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

	}
}
