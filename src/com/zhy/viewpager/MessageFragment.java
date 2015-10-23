package com.zhy.viewpager;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnPullEventListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.State;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.zhy.util.GsonUtil;
import com.zhy.util.L;
import com.zhy.util.NetManager;
import com.zhy.vo.MessageVO;
import com.zhy.zhy_slidemenu_demo04.R;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MessageFragment extends MyBaseFragment {
	private static final String TAG = "MessageTab";
	private Gson gson;
	private RequestQueue queue;
	private List<MessageVO> data;
	private PullToRefreshListView listView;
	private Myadapter adapter;
	private ProgressBar messageProgress;
	// 每页加载数量
	private int pageRows = 20;
	// 已经加载的数量
	private boolean isLastPage = false;
	// 当前 已经加载的数据
	private int currentPage = 0;
	// 判断是否第一次下载数据
	private boolean isFirstDownload = true;
	private View rootView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.message_tab, container, false);
		}
		// 缓存的rootView需要判断是否已经被加过parent，
		// 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if (parent != null) {
			parent.removeView(rootView);
		}
		listView = (PullToRefreshListView) rootView.findViewById(R.id.message_tab_listview);
		messageProgress = (ProgressBar) rootView.findViewById(R.id.message_progress);
		adapter = new Myadapter(getActivity());
		listView.setAdapter(adapter);
		initListView();
		if(!isFirstDownload){
			messageProgress.setVisibility(View.GONE);
		}
		if (isFirstDownload) {
			getData(currentPage,pageRows);
			isFirstDownload=false;
		}
		
		return rootView;
	}

	private void initListView() {
		String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
				DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
		listView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
		listView.setMode(Mode.BOTH);
		listView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
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
		JSONObject params = null;
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("offset", offset);
		map.put("rows", rows);
		params = new JSONObject(map);

		// 判断安装后第一次打开程序。
		if (NetManager.messages != null) {
			data = NetManager.messages;
			adapter.notifyDataSetChanged();
		}
		if (!NetManager.startAppMessage) {
			messageProgress.setVisibility(View.GONE);
			NetManager.startAppMessage = true;
		}
		JsonRequest<JSONObject> objRequest = new JsonObjectRequest(Method.POST, NetManager.FIND_MESSAGE_PAGE, params,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
							String jsonString = response.getString("jsonString");
							Type type = new TypeToken<List<MessageVO>>() {
							}.getType();
							List<MessageVO> tempData = gson.fromJson(jsonString, type);
							// 判断是否到达最后一条数据
							if (tempData.size() < pageRows) {
								isLastPage = true;
							}
							data = tempData;
							currentPage = currentPage + tempData.size();
							messageProgress.setVisibility(View.GONE);
							listView.onRefreshComplete();
							adapter.notifyDataSetChanged();
							listView.getRefreshableView().setSelection(0);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
					}
				});
		queue.add(objRequest);
		queue.start();
	}

	public MessageFragment() {
		queue = NetManager.getQueue();
		gson = GsonUtil.getGson();
		data = new ArrayList<MessageVO>();
	}

	private class Myadapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public Myadapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.message_tab_listitem, null);
				holder = new ViewHolder();
				holder.username = (TextView) convertView.findViewById(R.id.message_username);
				holder.content = (TextView) convertView.findViewById(R.id.message_content);
				holder.id = (TextView) convertView.findViewById(R.id.message_id);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.username.setText(data.get(position).getTitle());
			holder.content.setText(data.get(position).getContent());
			holder.id.setText(data.get(position).getId().toString());
			return convertView;
		}

	}

	private class ViewHolder {
		private TextView username;
		private TextView content;
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
