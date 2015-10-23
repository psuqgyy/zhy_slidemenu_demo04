package com.zhy.vo;

import java.io.Serializable;
import java.util.List;

public class UserVO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */

	private Integer id;
	/** �˺� **/
	private String username;
	/** ���� **/
	private String password;
	/** �Ա� **/
	private String sex;
	/** ��� **/
	private String uid;
	/** ���ҽ��� **/
	private String info;
	/** �û�����Ʒ */
	private List<Integer> work_ids;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public List<Integer> getWork_ids() {
		return work_ids;
	}

	public void setWork_ids(List<Integer> work_ids) {
		this.work_ids = work_ids;
	}

}
