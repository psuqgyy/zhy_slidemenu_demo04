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
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.zhy.util.GsonUtil;
import com.zhy.util.L;
import com.zhy.util.NetManager;
import com.zhy.util.NewImageDownloader;
import com.zhy.util.NewImageDownloader.OnImageLoader;
import com.zhy.vo.ImageVO;
import com.zhy.vo.VideoVO;
import com.zhy.zhy_slidemenu_demo04.R;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

public class VideoFragment extends MyBaseFragment {
	private static final String TAG = "VieoTab";
	private PullToRefreshListView mListView;
	private List<VideoVO> data;
	private myAdapter adapter;
	private int currentIndex = -1;
	private VideoView mVideoView;
	private MediaController mMediaCtrl;
	private RequestQueue queue;
	private Gson gson;
	private NewImageDownloader imageDownloader;
	private int playPosition = -1;
	private boolean isPaused = false;
	private boolean isPlaying = false;
	private View rootView;
	// 当前item地址
	private int currentPosition = -1;
	// 当前滚动状态
	private int currentScrollState = -1;
	// 当前频幕第一条数据
	private int currentFirstPosition = -1;
	// 当前频幕最后一条数据
	private int currentLastPosition = -1;
	// 加载数据显示圆圈
	private ProgressBar videoProgress;
	// 当前页面
	private int currentPage = 0;
	// 每页显示的数量
	private int pageRows = 20;
	// 是否最后一条数据
	private boolean isLastPage = false;
	// 判断是否第一次下载数据
	private boolean isFirstDownload = true;

	public MediaController getmMediaCtrl() {
		return mMediaCtrl;
	}

	public VideoView getmVideoView() {
		return mVideoView;
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		if (imageDownloader != null && imageDownloader.mDiskLruCache != null) {
			try {
				imageDownloader.mDiskLruCache.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		super.onPause();
	}

	/**
	 * 初始化数据
	 */
	private void getData(int offset, int rows) {
		if (isLastPage) {
			offset = 0;
			isLastPage = false;
			currentPage = 0;
		}
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("offset", offset);
		map.put("rows", rows);
		JSONObject params = new JSONObject(map);
		if (NetManager.videos != null) {
			data = NetManager.videos;
			adapter.notifyDataSetChanged();
		}
		if (!NetManager.startAppVideo) {
			NetManager.startAppVideo = false;
			videoProgress.setVisibility(View.GONE);
		}
		JsonRequest<JSONObject> objRequest = new JsonObjectRequest(Method.POST, NetManager.FIND_VIDEO_PAGE, params,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
							Type type = new TypeToken<List<VideoVO>>() {
							}.getType();
							List<VideoVO> tempData = gson.fromJson(response.getString("jsonString"), type);
							if (tempData.size() < pageRows) {
								isLastPage = true;
							}
							data = tempData;
							currentPage = currentPage + tempData.size();
							adapter.notifyDataSetChanged();
							videoProgress.setVisibility(View.GONE);
							mListView.setVisibility(View.VISIBLE);
							mListView.getRefreshableView().setSelection(0);
							mListView.onRefreshComplete();
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}, null);
		queue.add(objRequest);
		queue.start();
	}

	public VideoFragment() {
		data = new ArrayList<VideoVO>();
		queue = NetManager.getQueue();
		gson = GsonUtil.getGson();
	}

	/**
	 * 初始化listView
	 */
	public void initListView() {
		mListView.setMode(Mode.BOTH);
		mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				getData(currentPage,pageRows);
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.video_tab, container, false);
		}
		// 缓存的rootView需要判断是否已经被加过parent，
		// 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if (parent != null) {
			parent.removeView(rootView);
		}
		mMediaCtrl = new MediaController(getActivity(), false);
		mListView = (PullToRefreshListView) rootView.findViewById(R.id.video_tab_listview);
		videoProgress = (ProgressBar) rootView.findViewById(R.id.video_progress);
		if (!isFirstDownload) {
			videoProgress.setVisibility(View.GONE);
		}
		adapter = new myAdapter();
		mListView.setAdapter(adapter);
		if (imageDownloader == null) {
			imageDownloader = NewImageDownloader.getInstance(getActivity(), NetManager.DISK_LRUCACHE_PATH);
		}
		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			/**
			 * 控制视频滚出界面暂停，滚进来就继续播放。
			 */
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});
		initListView();
		return rootView;
	}

	class myAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stubs
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
			currentPosition = position;
			final ViewHolder holder;
			final int mPosition = position;
			if (convertView == null) {
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.video_tab_listitem, null);
				holder = new ViewHolder();
				holder.videoImage = (ImageView) convertView.findViewById(R.id.video_image);
				holder.videoNameText = (TextView) convertView.findViewById(R.id.video_name_text);
				holder.videoPlayBtn = (ImageButton) convertView.findViewById(R.id.video_play_btn);
				holder.mProgressBar = (ProgressBar) convertView.findViewById(R.id.progressbar);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.videoImage.setTag(data.get(position).getCoverHttp());
			holder.videoNameText
					.setText("id=" + data.get(position).getId().toString() + ":title=" + data.get(position).getTitle());
			holder.videoPlayBtn.setVisibility(View.VISIBLE);
			holder.videoImage.setVisibility(View.VISIBLE);
			holder.videoNameText.setVisibility(View.VISIBLE);
			if (currentIndex == position) {
				holder.videoPlayBtn.setVisibility(View.INVISIBLE);
				holder.videoImage.setVisibility(View.INVISIBLE);
				holder.videoNameText.setVisibility(View.INVISIBLE);

				if (isPlaying || playPosition == -1) {
					if (mVideoView != null) {
						mVideoView.setVisibility(View.GONE);
						mVideoView.stopPlayback();
						holder.mProgressBar.setVisibility(View.GONE);
					}
				}
				mVideoView = (VideoView) convertView.findViewById(R.id.videoview);
				mVideoView.setVisibility(View.VISIBLE);
				mMediaCtrl.setAnchorView(mVideoView);
				mMediaCtrl.setMediaPlayer(mVideoView);
				mVideoView.setMediaController(mMediaCtrl);
				mVideoView.requestFocus();
				holder.mProgressBar.setVisibility(View.VISIBLE);
				if (playPosition > 0 && isPaused) {
					mVideoView.start();
					isPaused = false;
					isPlaying = true;
					holder.mProgressBar.setVisibility(View.GONE);
				} else {
					mVideoView.setVideoPath(data.get(mPosition).getVideoHttp());
					isPaused = false;
					isPlaying = true;
					System.out.println("播放新的视频");
				}
				mVideoView.setOnCompletionListener(new OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						if (mVideoView != null) {
							mVideoView.seekTo(0);
							// mVideoView.stopPlayback(); 停止回放视频
							currentIndex = -1;
							isPaused = false;
							isPlaying = false;
							holder.mProgressBar.setVisibility(View.GONE);
							adapter.notifyDataSetChanged();
						}
					}
				});
				mVideoView.setOnPreparedListener(new OnPreparedListener() {

					@Override
					public void onPrepared(MediaPlayer mp) {
						holder.mProgressBar.setVisibility(View.GONE);
						mVideoView.start();
					}
				});

			} else {
				holder.videoPlayBtn.setVisibility(View.VISIBLE);
				holder.videoImage.setVisibility(View.VISIBLE);
				holder.videoNameText.setVisibility(View.VISIBLE);
				holder.mProgressBar.setVisibility(View.GONE);
			}

			holder.videoPlayBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					currentIndex = mPosition;
					playPosition = -1;
					adapter.notifyDataSetChanged();
				}
			});
			String url = data.get(position).getCoverHttp();
			imageDownloader.imageDownload(url, getActivity(), (ImageView) convertView.findViewWithTag(url),
					new OnImageLoader() {
						@Override
						public void onSuccess(String url, ImageView imageView, Bitmap bitmap) {
							// TODO Auto-generated method stub
							imageView.setImageBitmap(bitmap);
						}
					});
			return convertView;
		};
	}

	// 防止重复绘制控件
	private class ViewHolder {
		ImageView videoImage;
		TextView videoNameText;
		ImageButton videoPlayBtn;
		ProgressBar mProgressBar;
	}

	@Override
	public void lazyLoad() {
		if (isFirstDownload) {
			getData(currentPage, pageRows);
			isFirstDownload = false;
		}
	}

}
