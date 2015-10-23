package com.zhy.viewpager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MainViewPagerAdapter extends FragmentPagerAdapter {

	private MessageFragment messageTab;
	private VideoFragment videoTab;
	private ImageFragment imageTab;

	public MessageFragment getMessageTab() {
		return messageTab;
	}

	public void setMessageTab(MessageFragment messageTab) {
		this.messageTab = messageTab;
	}

	public VideoFragment getVideoTab() {
		return videoTab;
	}

	public void setVideoTab(VideoFragment videoTab) {
		this.videoTab = videoTab;
	}

	public ImageFragment getImageTab() {
		return imageTab;
	}

	public void setImageTab(ImageFragment imageTab) {
		this.imageTab = imageTab;
	}

	public String[] getTitle() {
		return title;
	}

	public void setTitle(String[] title) {
		this.title = title;
	}

	private String[] title = { "文字", "图片", "视频" };

	public MainViewPagerAdapter(FragmentManager fm) {

		super(fm);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Fragment getItem(int position) {
		// TODO Auto-generated method stub
		switch (position) {
		case 0:
			if(messageTab==null){
				messageTab= new MessageFragment();
			}
			return messageTab;
		case 1:
			if(imageTab==null){
				imageTab=new ImageFragment();
			}
			return imageTab;
		case 2:
			if(videoTab==null){
				videoTab= new VideoFragment();
			}
			return videoTab;
		default:
			return null;
		}

	}

	@Override
	public CharSequence getPageTitle(int position) {
		// TODO Auto-generated method stub
		return title[position];
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return title.length;
	}

}
