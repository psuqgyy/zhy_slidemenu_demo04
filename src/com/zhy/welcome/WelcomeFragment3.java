package com.zhy.welcome;

import com.zhy.util.ActivityManager;
import com.zhy.zhy_slidemenu_demo04.MainActivity;
import com.zhy.zhy_slidemenu_demo04.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class WelcomeFragment3 extends Fragment{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view=inflater.inflate(R.layout.welcome_fragment3, container,false);
		Button button=(Button) view.findViewById(R.id.welcome_button);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(getActivity(),MainActivity.class);
				startActivity(intent);
				getActivity().finish();
			}
		});
		return view;
	}
}
