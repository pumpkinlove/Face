package cn.cloudwalk.sdk;


public class FaceParam {
	/**
	 * roi区域设置, 默认整帧图像
	 **/
	public int roiX; 
	public int roiY;
	public int roiWidth;
	public int roiHeight;
	/**
	 * 人脸尺寸范围-最小尺寸，默认100
	 **/
	public int minSize;
	/**
	 * 人脸尺寸范围-最大尺寸，默认400
	 **/
	public int maxSize;
	/**
	 * 每帧最大人脸数，默认20
	 **/
	public int maxFaceNumPerImg;
	/**
	 * 一般1-10，越大检出率越低，但误检越小，默认3
	 **/
	public int nMinNeighbors; 
	/**
	 * 全局检测频率， 默认10
	 **/
	public int globleDetFreq;
	/**
	 * 是否开启单目标跟踪， 默认开启. 0关闭，非0开启
	 **/
	public int b_track;
	/**
	 * 预跟踪帧数，默认3
	 **/
	public int det_frame_for_new;
	/**
	 * 跟丢到退出跟踪的帧数，默认150
	 **/
	public int max_frame_since_lost; 
	
}