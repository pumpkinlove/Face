package org.zz.faceapi;

public class MXConStant {
	/**
	 * @author  chen.gs
	 * @version V1.0.1.20170208 基于云从科技人脸SDK3.1.0.20170105-Android封装
	 * 							需要将识别模型拷贝到手机外部存储  
	 * 			V1.0.1.20170209  对代码进行整理，并生成算法jar
	 * 			V1.0.2.20170209 人脸检测接口增加对YUV格式视频流图像数据支持
	 * 			V1.0.3.20170213  规范 对外接口
	 * 			V1.0.4.20170417 mxInitAlg增加参数，是否每次生成模型文件
	 * 			V1.0.5.20170421 mxInitAlg增加（1）模型文件hash校验失败，返回-101
	 * 									           （2）创建句柄失败，返回-100
	 * 			V1.0.6.20170424 mxInitAlg参数bForce为true自解压模型文件，否则不自解压模型文件
	 * */
	// 人脸算法版本
	public static String strVersion  = "CW Face SDK V1.0.6.20170424";
	// 人脸检测最大数
	public static int iMaxFaceNum        = 10;
	// 人脸检测参数，最小人脸默认100，最大默认400
	public static int iFaceDetectMinSize = 20;
	public static int iFaceDetectMaxSize = 400;
	// 模型文件参数
	public static String strModelDir         = "ZZFaceModels";
	public static String strConfigXML        = "CWR_Config_1_1.xml";
	public static String strFeatureBin       = "feature.bin";
	public static String strFeatureWeightBin = "featureWeight.bin";
	public static String strMdlBin           = "mdl.bin";
	

	public static String HashConfigXML        = "d5b0583018112d3e29c85836eab50dfd";
	public static String HashFeatureBin       = "4cc03518062e2ab7dfc99fcbfed0c2ae";
	public static String HashFeatureWeightBin = "7825220f2b5fafe5951322f9a059b48a";
	public static String HashMdlBin           = "cbbb5d9f86634e3f1b1a74b1661d185d";
}
