package com.zhy.viewpager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnPullEventListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.State;
import com.zhy.util.GsonUtil;
import com.zhy.util.ImageDownloader;
import com.zhy.util.L;
import com.zhy.util.NetManager;
import com.zhy.util.ImageDownloader.OnImageDownload;
import com.zhy.util.NewImageDownloader;
import com.zhy.util.NewImageDownloader.OnImageLoader;
import com.zhy.vo.ImageVO;
import com.zhy.zhy_slidemenu_demo04.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ImageFragment extends MyBaseFragment {

	private static final String TAG = "ImageTab";
	// listview的数据
	private List<ImageVO> data;
	// 控件
	private PullToRefreshListView listView;
	// 下载图片(old)
	private ImageDownloader downloader;
	// 新版
	private NewImageDownloader imageDownLoader;
	// 滚动当前状态
	private int currentScrollState = 0;
	// 滚动当前页面的第一条数据
	private int firstPosition = -1;
	// 滚动当前页面的最后一条数据
	private int lastPosition = -1;
	// 是否最后一页
	private boolean isLastPage = false;
	// 每页显示的数量
	private int pageRows = 20;
	// 当前页面的页面
	private int currentPage = 0;
	// 判断是否第一次下载数据
	private boolean isFirstDownload = true;
	// false表示滚动中不加载，
	private boolean a = true;
	// false表示用新版下载类
	private boolean b = false;
	private Myadapter adapter;
	private RequestQueue queue;
	private Gson gson;
	private ProgressBar imageProgress;
	private View rootView;

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		if (imageDownLoader != null && imageDownLoader.mDiskLruCache != null) {
			try {
				imageDownLoader.mDiskLruCache.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		super.onPause();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.image_tab, container, false);
		}
		// 缓存的rootView需要判断是否已经被加过parent，
		// 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if (parent != null) {
			parent.removeView(rootView);
		}

		listView = (PullToRefreshListView) rootView.findViewById(R.id.image_tab_listview);
		imageProgress = (ProgressBar) rootView.findViewById(R.id.image_progress);
		if (!isFirstDownload) {
			imageProgress.setVisibility(View.GONE);
		}
		adapter = new Myadapter(getActivity());
		listView.setAdapter(adapter);
		if (imageDownLoader == null) {
			imageDownLoader = NewImageDownloader.getInstance(getActivity(), NetManager.DISK_LRUCACHE_PATH);
		}
		listView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				currentScrollState = scrollState;
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

				firstPosition = firstVisibleItem;
				lastPosition = view.getLastVisiblePosition();
			}
		});
		initListView();

		return rootView;
	}

	public void initListView() {
		listView.setMode(Mode.BOTH);
		listView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				getData(currentPage, pageRows);
			}
		});
	}

	private void getData(int offset, int rows) {
		if (isLastPage) {
			offset = 0;
			rows = pageRows;
			isLastPage = false;
			currentPage = 0;
		}
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("offset", offset);
		map.put("rows", rows);
		JSONObject params = new JSONObject(map);
		if (NetManager.images != null) {
			data = NetManager.images;
			adapter.notifyDataSetChanged();
		}
		if (!NetManager.startAppImage) {
			imageProgress.setVisibility(View.GONE);
			NetManager.startAppImage = false;
		}
		JsonRequest<JSONObject> objRequest = new JsonObjectRequest(Method.POST, NetManager.FIND_IMAGE_PAGE, params,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						// TODO Auto-generated method stub
						try {
							Type type = new TypeToken<List<ImageVO>>() {
							}.getType();
							List<ImageVO> tempData = gson.fromJson(response.getString("jsonString"), type);
							if (tempData.size() < pageRows) {
								isLastPage = true;
							}
							data = tempData;
							currentPage = currentPage + tempData.size();
							imageProgress.setVisibility(View.GONE);
							listView.onRefreshComplete();
							adapter.notifyDataSetChanged();
							listView.getRefreshableView().setSelection(0);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}, null);

		queue.add(objRequest);
		queue.start();
	}

	public ImageFragment() {
		// TODO Auto-generated constructor stub
		queue = NetManager.getQueue();
		gson = GsonUtil.getGson();
		data = new ArrayList<ImageVO>();
	}

	private class Myadapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public Myadapter(Context context) {
			// TODO Auto-generated constructor stub
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.image_tab_listitem, null);
				holder = new ViewHolder();
				holder.title = (TextView) convertView.findViewById(R.id.image_title);
				holder.content = (ImageView) convertView.findViewById(R.id.image_content);
				holder.id = (TextView) convertView.findViewById(R.id.image_id);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.title.setText(data.get(position).getTitle());
			holder.id.setText(data.get(position).getId().toString());
			holder.content.setImageResource(R.drawable.ic_launcher);
			holder.content.setTag(data.get(position).getImageHttp());
			if (a) {
				if (b) {
					if (downloader == null) {
						downloader = ImageDownloader.getInstance();
					}
					downloader.imageDownload(data.get(position).getImageHttp(), holder.content, "/kuaishou",
							getActivity(), new OnImageDownload() {

								@Override
								public void onSuccess(Bitmap bitmap, String url, ImageView imageView) {
									// TODO Auto-generated method stub
									imageView.setImageBitmap(bitmap);
								}
							});
				} else {
					if (imageDownLoader == null) {
						imageDownLoader = NewImageDownloader.getInstance(getActivity(), NetManager.DISK_LRUCACHE_PATH);
					}
					imageDownLoader.imageDownload(data.get(position).getImageHttp(), getActivity(), holder.content,
							new OnImageLoader() {
								@Override
								public void onSuccess(String url, ImageView imageView, Bitmap bitmap) {
									// TODO Auto-generated method stub
									imageView.setImageBitmap(bitmap);
								}
							});

				}
			}

			return convertView;
		}

	}

	private class ViewHolder {
		private TextView title;
		private ImageView content;
		private TextView id;
	}

	@Override
	public void lazyLoad() {
		if (isFirstDownload) {
			getData(currentPage, pageRows);
			isFirstDownload = false;
		}

	}
}
