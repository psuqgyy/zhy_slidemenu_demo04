package com.zhy.fragments;

import com.zhy.slidingmenu.MenuLeftFragment;
import com.zhy.util.L;
import com.zhy.zhy_slidemenu_demo04.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class UserInformationFragment extends Fragment {
	private static final String TAG = "UserInformationFragment";
	private TextView backTextView;
	private View rootView;// 缓存Fragment view

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (rootView == null) {
			rootView = inflater.inflate(R.layout.left_menu_user,container,false);
		}
		// 缓存的rootView需要判断是否已经被加过parent，
		// 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if (parent != null) {
			parent.removeView(rootView);
		}

		backTextView=(TextView) rootView.findViewById(R.id.user_back_textView);
		backTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentManager fm = getActivity().getSupportFragmentManager();
				Fragment f = fm.findFragmentByTag(MenuLeftFragment.class.getName());
				if (f == null) {
					f = new MenuLeftFragment();
				}
				FragmentTransaction ft = fm.beginTransaction();
				ft.setCustomAnimations(R.anim.left_enter_enter,
						R.anim.left_enter_exist,R.anim.left_back_enter, R.anim.left_back_exist).replace(R.id.id_left_menu_frame, f);
				ft.commit();
			}
		});
		return rootView;
	}
}
