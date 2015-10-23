package com.zhy.vo;

public class ImageVO {

	private Integer id;
	private String title;
	private String imageHttp;
	private Integer userId;

	public Integer getId() {
		return id;
	}

	
	public ImageVO(Integer id, String title, String imageHttp, Integer userId) {
		super();
		this.id = id;
		this.title = title;
		this.imageHttp = imageHttp;
		this.userId = userId;
	}


	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getImageHttp() {
		return imageHttp;
	}

	public void setImageHttp(String imageHttp) {
		this.imageHttp = imageHttp;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

}
