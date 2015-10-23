package com.zhy.slidingmenu;

import java.util.Arrays;
import java.util.List;

import com.zhy.fragments.UserInformationFragment;
import com.zhy.util.L;
import com.zhy.zhy_slidemenu_demo04.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class MenuLeftFragment extends Fragment {
	private static final String TAG = "MenuLeftFragment";
	private View rootView;
	private ListView mCategories;
	private List<String> mDatas = Arrays.asList("用户", "发现", "通讯录", "朋友圈", "订阅号");
	private ListAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.left_menu,container,false);
		}
		// 缓存的rootView需要判断是否已经被加过parent，
		// 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if (parent != null) {
			parent.removeView(rootView);
		}
		initView();
		return rootView;
	}

	@Override
	public void setMenuVisibility(boolean menuVisible) {
		super.setMenuVisibility(menuVisible);
		if (this.getView() != null)
			this.getView().setVisibility(menuVisible ? View.VISIBLE : View.GONE);
	}

	private void initView() {
		mCategories = (ListView) rootView.findViewById(R.id.id_listview_categories);
		mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mDatas);
		mCategories.setAdapter(mAdapter);
		mCategories.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				switch (position) {
				case 0:
					FragmentManager fm = getActivity().getSupportFragmentManager();
					Fragment userFragment=fm.findFragmentByTag(UserInformationFragment.class.getName());
					if(userFragment==null){
						userFragment=new UserInformationFragment();
					}
					FragmentTransaction ft = fm.beginTransaction();
					ft.setCustomAnimations(R.anim.right_enter_new, R.anim.right_enter_exist, R.anim.right_back_new,
							R.anim.right_back_exits).replace(R.id.id_left_menu_frame, userFragment,UserInformationFragment.class.getName());
					ft.commit();
					break;
				case 1:
					break;
				case 2:

					break;
				case 3:

					break;
				default:
					break;
				}
			}
		});
	}

}
