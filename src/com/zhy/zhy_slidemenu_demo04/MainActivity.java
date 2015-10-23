package com.zhy.zhy_slidemenu_demo04;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenedListener;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.zhy.component.PagerSlidingTabStrip;
import com.zhy.slidingmenu.MenuLeftFragment;
import com.zhy.slidingmenu.MenuRightFragment;
import com.zhy.util.L;
import com.zhy.util.NetManager;
import com.zhy.viewpager.MainViewPagerAdapter;

public class MainActivity extends SlidingFragmentActivity {

	private static final String TAG = "MainActivity";
	MainViewPagerAdapter adapter;
	private ViewPager mViewPager;
	// 切换页面显示当前的页面
	private int currentPage = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		isFirstLunacher();
		// 初始化SlideMenu
		initRightMenu();
		// 初始化ViewPager
		initViewPager();
		// 实例化整个程序的NET管理,管理所有请求的生命周期
		NetManager.getInstance(this);
	}

	/**
	 * 判断程序是否第一次运行
	 */
	private void isFirstLunacher() {
		SharedPreferences sharedPreferences = this.getSharedPreferences("share", MODE_PRIVATE);
		boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
		Editor editor = sharedPreferences.edit();
		if (isFirstRun) {
			editor.putBoolean("isFirstRun", false);
			editor.commit();
		} else {
			Log.e(TAG, "不是第一次运行");
		}
	}

	// 初始化中间的三个TAB
	private void initViewPager() {
		adapter = new MainViewPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.id_viewpager);
		mViewPager.setAdapter(adapter);
		// 关闭fragmentpageradapter的预加载
		mViewPager.setOffscreenPageLimit(0);
		PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.viewpager_tabs);
		tabs.setViewPager(mViewPager);
		/**
		 * TAB切换监听，当TAB切换可以对TAB做一些开始和收尾的工作，本来我是打算直接在TAB的生命周期里做就好了
		 * TAB是fragment,fragment的生命周期是跟 activity挂钩的。也就是说这个fragment的生命周期在主线程
		 * 坑爹啊。除非退出程序。不然fragment都不会执行完整的生命周期。
		 * 
		 * 我也不知道对不对。反正TAB的生命周期是乱的。更不无法正常使用
		 */
		tabs.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				currentPage = arg0;
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				if (arg0 == 0) {
					switch (currentPage) {
					case 0:
						adapter.getMessageTab().lazyLoad();
						break;
					case 1:
						adapter.getImageTab().lazyLoad();
						break;
					case 2:
						adapter.getVideoTab().lazyLoad();
						break;
					default:
						break;
					}
				}
				// 如果切换tab,停止播放视频。
				if (adapter.getVideoTab() != null) {
					if (adapter.getVideoTab().getmVideoView() != null) {
						adapter.getVideoTab().getmVideoView().pause();
						adapter.getVideoTab().getmMediaCtrl().hide();
					}
				}
			}
		});
	}

	private void initRightMenu() {

		Fragment leftMenuFragment = new MenuLeftFragment();
		setBehindContentView(R.layout.left_menu_frame);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction().replace(R.id.id_left_menu_frame,
				leftMenuFragment, MenuLeftFragment.class.getName());
		ft.commit();
		SlidingMenu menu = getSlidingMenu();
		menu.setMode(SlidingMenu.LEFT_RIGHT);
		// 设置触摸屏幕的模式
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		menu.setShadowDrawable(R.drawable.shadow);
		// 设置滑动菜单视图的宽度
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		// menu.setBehindWidth()
		// 设置渐入渐出效果的值
		menu.setFadeDegree(0.35f);
		// menu.setBehindScrollScale(1.0f);
		menu.setSecondaryShadowDrawable(R.drawable.shadow);
		menu.setFadeEnabled(true);
		// 设置右边（二级）侧滑菜单
		menu.setSecondaryMenu(R.layout.right_menu_frame);
		Fragment rightMenuFragment = new MenuRightFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.id_right_menu_frame, rightMenuFragment).commit();
		menu.setOnOpenedListener(new OnOpenedListener() {

			@Override
			public void onOpened() {
				if (adapter.getVideoTab().getmVideoView() != null) {
					adapter.getVideoTab().getmVideoView().pause();
					adapter.getVideoTab().getmMediaCtrl().hide();
				}
			}
		});

	}

	/**
	 * 切换做菜单，这个方法是直接放在layout里面调用的。大家进去看看就能找到了
	 * 
	 * @param view
	 */
	public void showLeftMenu(View view) {
		if (adapter.getVideoTab().getmVideoView() != null) {
			adapter.getVideoTab().getmVideoView().pause();
			adapter.getVideoTab().getmMediaCtrl().hide();
		}
		getSlidingMenu().showMenu();
	}

	/**
	 * 切换右侧菜单
	 * 
	 * @param view
	 */
	public void showRightMenu(View view) {
		if (adapter.getVideoTab().getmVideoView() != null) {
			adapter.getVideoTab().getmVideoView().pause();
			adapter.getVideoTab().getmMediaCtrl().hide();
		}
		getSlidingMenu().showSecondaryMenu();
	}
}
