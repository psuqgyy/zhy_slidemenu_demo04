package com.zhy.vo;


public class VideoVO{

	/**
	 * 
	 */
	private Integer id;
	/**�����ַ*/
	private String coverHttp;
	/**��Ƶ��ַ*/
	private String videoHttp;
	/**����*/
	private String title;
	/**����*/
	private String info;
	/**������Ʒ���û�*/
	private Integer user_id;
	
	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getCoverHttp() {
		return coverHttp;
	}
	public void setCoverHttp(String coverHttp) {
		this.coverHttp = coverHttp;
	}
	public String getVideoHttp() {
		return videoHttp;
	}
	public void setVideoHttp(String videoHttp) {
		this.videoHttp = videoHttp;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public Integer getUser_id() {
		return user_id;
	}
	public void setUser_id(Integer user_id) {
		this.user_id = user_id;
	}
	
}
