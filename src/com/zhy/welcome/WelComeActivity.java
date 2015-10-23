package com.zhy.welcome;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhy.util.GsonUtil;
import com.zhy.util.L;
import com.zhy.util.NetManager;
import com.zhy.util.NewImageDownloader;
import com.zhy.vo.ImageVO;
import com.zhy.vo.MessageVO;
import com.zhy.vo.VideoVO;
import com.zhy.zhy_slidemenu_demo04.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Window;

public class WelComeActivity extends FragmentActivity {
	private static final String TAG = "WelComeActivity";
	private Gson gson;
	private NewImageDownloader imagedownloader;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.welcome);
		List<Fragment> fragments = new ArrayList<Fragment>();
		fragments.add(new WelcomeFragment1());
		fragments.add(new WelcomeFragment2());
		fragments.add(new WelcomeFragment3());
		WelcomPagerViewAdapter adapter = new WelcomPagerViewAdapter(getSupportFragmentManager(), fragments);
		ViewPager viewPager = (ViewPager) findViewById(R.id.welcome_viewpager);
		viewPager.setAdapter(adapter);
		NetManager.getInstance(this);
		loadData();

	}
	/**
	 * 第一次启动程序预加载数据。文字就放在内存。图片就放在内存和磁盘
	 */
	public void loadData() {
		L.e(TAG, "当前的ACTIVITY"+toString());
		gson = GsonUtil.getGson();
		if(imagedownloader==null){
			imagedownloader=NewImageDownloader.getInstance( this, NetManager.DISK_LRUCACHE_PATH);
		}
		RequestQueue queue = NetManager.getQueue();
		JsonRequest<JSONObject> messageRequest = new JsonObjectRequest(Method.POST, NetManager.FIND_MESSAGE_PAGE, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						// TODO Auto-generated method stub
						try {
							String jsonString = response.getString("jsonString");
							Type type = new TypeToken<List<MessageVO>>() {
							}.getType();
							List<MessageVO> data = gson.fromJson(jsonString, type);
							NetManager.messages = data;
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}, null);
		JsonRequest<JSONObject> imageRequest = new JsonObjectRequest(Method.POST, NetManager.FIND_IMAGE_PAGE, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						// TODO Auto-generated method stub
						try {
							Type type = new TypeToken<List<ImageVO>>() {
							}.getType();
							List<ImageVO> data = gson.fromJson(response.getString("jsonString"), type);
							NetManager.images = data;
							for(int i=0;i<5;i++){
								imagedownloader.imagePreDownload(data.get(i).getImageHttp());
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}, null);
		JsonRequest<JSONObject> vidoeRequest = new JsonObjectRequest(Method.POST, NetManager.FIND_VIDEO_PAGE, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {

							Type type = new TypeToken<List<VideoVO>>() {
							}.getType();
							List<VideoVO> data = gson.fromJson(response.getString("jsonString"), type);
							NetManager.videos = data;
							for(int i=0;i<5;i++){
								imagedownloader.imagePreDownload(data.get(i).getCoverHttp());
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}, null);

		queue.add(vidoeRequest);
		queue.add(messageRequest);
		queue.add(imageRequest);
		queue.start();
	}
}
