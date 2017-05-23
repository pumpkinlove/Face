package com.miaxis.face.bean;

/**
 * ajax请求返回数据
 * 
 * @author raindy
 * 
 */
public class AjaxResponse<T> {

	public static final int SUCCESS = 200; // 请求成功

	public static final int FAILURE = 400; // 请求失败

	private int code; // 状态码

	private String message; // 提示信息

	private Object data; // 返回数据
	private Object listData; // 返回数据
	private Object pageData;
	private Object mapData;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Object getListData() {
		return listData;
	}

	public void setListData(Object listData) {
		this.listData = listData;
	}

	public Object getPageData() {
		return pageData;
	}

	public void setPageData(Object pageData) {
		this.pageData = pageData;
	}

	public Object getMapData() {
		return mapData;
	}

	public void setMapData(Object mapData) {
		this.mapData = mapData;
	}


}