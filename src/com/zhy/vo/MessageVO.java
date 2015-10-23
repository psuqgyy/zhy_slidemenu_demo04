package com.zhy.vo;

import java.io.Serializable;


public class MessageVO implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**��ʶ��**/
	private Integer id;
	/**����**/
	private String title;
	/**����**/
	private String content;
	/**���ߵ�id**/
	private int userId;
	
	public MessageVO(Integer id, String title, String content, int userId) {
		super();
		this.id = id;
		this.title = title;
		this.content = content;
		this.userId = userId;
	}
	public Integer getId() {
		return id;
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
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	
	
}
