package com.zhy.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

public class ImageUtil {
	private static ImageUtil util;
	public static int flag = 0;

	public ImageUtil() {
		// TODO Auto-generated constructor stub
	}

	public static ImageUtil getInstance() {
		if (util == null) {
			util = new ImageUtil();
		}
		return util;
	}

	/**
	 * 判断是否有SDCard
	 * 
	 * @return
	 */
	public boolean hasSDCard() {
		boolean b = false;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			b = true;
		}
		return b;
	}

	/**
	 * 获取SDCard的路径
	 * 
	 * @return
	 */
	public String getExtSDCardPath() {
		if (hasSDCard()) {
			return Environment.getExternalStorageDirectory().getPath();
		} else {
			return "";
		}
	}

	/**
	 * 系统本身的目录/data/data/下面
	 * 
	 * @return
	 */
	public String getPackagePath(Activity activity) {
		return activity.getFilesDir().toString();
	}

	/**
	 * 根据URL查找图片的名字
	 * 
	 * @param url
	 * @return 图片的名字
	 */
	public String getImageName(String url) {
		if (url != null) {
			return url.substring(url.lastIndexOf("/") + 1);
		} else {
			return "";
		}
	}

	/**
	 * 从文件中去图片
	 * 
	 * @param activity
	 * @param imageName
	 * @param path
	 * @return
	 */
	public Bitmap getBitmapFromFile(Activity activity, String imageName, String path) {
		Bitmap bitmap = null;
		if (imageName != null) {
			File file = null;
			String real_path;
			try {
				if (hasSDCard() && path != null) {
					// 判断path是不是以斜杠开头的。
					if (path.startsWith("/")) {
						real_path = getExtSDCardPath() + path;
					} else {
						real_path = getExtSDCardPath() + "/" + path;
					}
				} else {
					if (path.startsWith("/")) {
						real_path = getPackagePath(activity) + path;
					} else {
						real_path = getPackagePath(activity) + "/" + path;
					}
				}
				file = new File(real_path, imageName);
				if (file.exists()) {
					BitmapFactory.Options opt=new BitmapFactory.Options(); 
					opt.inPreferredConfig=Bitmap.Config.RGB_565; 
					opt.inPurgeable=true; 
					opt.inInputShareable=true; 
					bitmap = BitmapFactory.decodeStream(new FileInputStream(file),null,opt);
				}
			} catch (Exception e) {
				e.printStackTrace();
				// TODO: handle exception
			}
		}
		return bitmap;
	}

	/**
	 * 保存图片到文件
	 * 
	 * @param path
	 *            保存的路径
	 * @param activity
	 *            activity上下文
	 * @param imageName
	 *            图片名字
	 * @param bitmap
	 *            图片
	 */
	public boolean setBitmapToFile(String path, Activity activity, String imageName, Bitmap bitmap) {
		File file = null;
		String realPath = null;
		try {
			if (hasSDCard()) {
				realPath = getExtSDCardPath() + (path != null && path.startsWith("/") ? path : "/" + path);
			} else {
				realPath = getPackagePath(activity) + (path != null && path.startsWith("/") ? path : "/" + path);
			}
			file = new File(realPath, imageName);
			if (!file.exists()) {
				new File(realPath + "/").mkdirs();
			}
			file.createNewFile();
			FileOutputStream os = null;
			if (hasSDCard()) {
				os = new FileOutputStream(file);
			} else {
				os = activity.openFileOutput(imageName, Context.MODE_PRIVATE);
			}
			if (imageName != null && (imageName.contains(".png") || imageName.contains(".PNG"))) {
				bitmap.compress(Bitmap.CompressFormat.PNG, 90, os);
			} else {
				bitmap.compress(Bitmap.CompressFormat.JPEG, 90, os);
			}
			os.flush();
			if (os != null) {
				os.close();
			}
			return true;
		} catch (Exception e) {
			// TODO: hanredle exception
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 从磁盘上删除保存的图片
	 * 
	 * @param path
	 *            图片路径
	 * @param activity
	 *            活动
	 * @param imageName
	 *            图片名字
	 */
	public void removeBitmapFromFile(String path, Activity activity, String imageName) {
		File file = null;
		String realPath;
		try {
			if (hasSDCard()) {
				realPath = getExtSDCardPath() + (path != null && path.startsWith("/") ? path : "/" + path);
			} else {
				realPath = getPackagePath(activity) + (path != null && path.startsWith("/") ? path : "/" + path);
			}
			file = new File(realPath, imageName);
			if (file != null) {
				file.delete();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	
}
