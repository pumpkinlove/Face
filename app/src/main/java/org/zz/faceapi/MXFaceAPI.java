package org.zz.faceapi;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import cn.cloudwalk.sdk.ConStant;
import cn.cloudwalk.sdk.FaceDetTrack;
import cn.cloudwalk.sdk.FaceInterface;
import cn.cloudwalk.sdk.FaceParam;
import cn.cloudwalk.sdk.FaceRecog;

public class MXFaceAPI {
	
    private FaceDetTrack cwDet   = null;
    private FaceRecog cwRecog = null;
    int iDetHandle   = -1;    // 比对句柄
    int iRecogHandle = -1;    // 识别句柄
    int iFeaLen = 0;          // 特征长度，不会变
    
	/**
     * @author   chen.gs
     * @category 获取算法版本
     * @param    filepath - 文件路径
     * @return   算法版本
     * */
	public String mxAlgVersion()
	{
		return MXConStant.strVersion;
	}
	
	private boolean IsValidHandle() {
		if (iDetHandle >= FaceInterface.cw_errcode_t.CW_EMPTY_FRAME_ERR) {
			return false;
		}
		if (iRecogHandle >= FaceInterface.cw_errcode_t.CW_EMPTY_FRAME_ERR) {
			return false;
		}
		return true;
	}
	 
	String CopyConfigFile(String strDstPath, String strConfigFileName, Context context)
	{
		String szDstFile = new StringBuilder(strDstPath).append(File.separator).append(strConfigFileName).toString();
		Log.e("MIAXIS", "szDstFile="+szDstFile);
//		InputStream is = this.getClass().getResourceAsStream(strConfigFileName);
		try {
			InputStream is = context.getAssets().open(strConfigFileName);
			File temp = new File(szDstFile);
			ToolUnit.inputstreamtofile(is, temp);
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(ToolUnit.getFileSizes(szDstFile)<10)
		{
			return null;
		}	
		return szDstFile;
	}
	
	int HashCheck() {
		String strSDPath = ToolUnit.getSDCardPath();
		if (strSDPath == null)
			return -101;
		String sModelPath = new StringBuilder(strSDPath).append(File.separator)
				.append(MXConStant.strModelDir).toString();
		String strConfigXML = new StringBuilder(sModelPath).append(File.separator).append(MXConStant.strConfigXML).toString();
		if(ToolUnit.isExist(strConfigXML)==false)
        {
			return -101;
        }
		try {
			String strTmp = getHash(strConfigXML,"MD5");
			if(strTmp.equals(MXConStant.HashConfigXML) == false)
			{
				return -101;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -101;
		}
		
		String strFeatureBin = new StringBuilder(sModelPath).append(File.separator).append(MXConStant.strFeatureBin).toString();
		if(ToolUnit.isExist(strFeatureBin)==false)
        {
			return -101;
        }
		try {
			String strTmp = getHash(strFeatureBin,"MD5");
			if(strTmp.equals(MXConStant.HashFeatureBin) == false)
			{
				return -101;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -101;
		}
		
		String strFeatureWeightBin = new StringBuilder(sModelPath).append(File.separator).append(MXConStant.strFeatureWeightBin).toString();
		if(ToolUnit.isExist(strFeatureWeightBin)==false)
        {
			return -101;
        }
		try {
			String strTmp = getHash(strFeatureWeightBin,"MD5");
			if(strTmp.equals(MXConStant.HashFeatureWeightBin) == false)
			{
				return -101;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -101;
		}
		
		String strMdlBin = new StringBuilder(sModelPath).append(File.separator).append(MXConStant.strMdlBin).toString();
		if(ToolUnit.isExist(strMdlBin)==false)
        {
			return -101;
        }
		try {
			String strTmp = getHash(strMdlBin,"MD5");
			if(strTmp.equals(MXConStant.HashMdlBin) == false)
			{
				return -101;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -101;
		}
		return 0;
	}
	
	/**
     * @author   chen.gs
     * @category 初始化算法
     * @param    context       - 输入，上下文句柄
     * 			 szConfigFile  - 输入，配置文件(预留)	
     * 	         bForce        - 输入，true自解压模型文件，否则不自解压模型文件
     * @see      需要读写SDCard权限，模型文件自解压到SDCard根目录ZZFaceModels文件夹下。	 
     * @return   0-成功，其他-失败
     * */
	public int mxInitAlg(Context context, String szConfigFile, boolean bForce)
	{
		Log.e("MIAXIS", "szConfigFile="+szConfigFile);
		int nRet = 0;
		String sModelPath = null;
		cwDet   = new FaceDetTrack();
        cwRecog = new FaceRecog();
        if(bForce == true)
        {
        	String strSDPath = ToolUnit.getSDCardPath();
	        if(strSDPath == null)
	        	return -1;
	        sModelPath = new StringBuilder(strSDPath).append(File.separator).append(MXConStant.strModelDir).append(File.separator).append(MXConStant.strConfigXML).toString();
	        String strDstPath = null;
	        Log.e("MIAXIS", "sModelPath="+sModelPath);
	       	strDstPath = new StringBuilder(strSDPath).append(File.separator).append(MXConStant.strModelDir).toString();
	       	ToolUnit.AddDirectory(strDstPath);
	        Log.e("MIAXIS", "strDstPath="+strDstPath);
	        CopyConfigFile(strDstPath, MXConStant.strConfigXML, context);
	        CopyConfigFile(strDstPath, MXConStant.strFeatureBin, context);
	        CopyConfigFile(strDstPath, MXConStant.strFeatureWeightBin, context);
	        CopyConfigFile(strDstPath, MXConStant.strMdlBin, context);
	        if(ToolUnit.isExist(sModelPath)==false)
	        {
	        	return -2;
	        }
        } else {
			if (szConfigFile!=null) {
	        	sModelPath = new StringBuilder(szConfigFile).append(File.separator).append(MXConStant.strConfigXML).toString();
	        	Log.e("MIAXIS", "sModelPath="+sModelPath);
	        }
			String strSDPath = ToolUnit.getSDCardPath();
			if (strSDPath == null)
				return -1;
			sModelPath = new StringBuilder(strSDPath).append(File.separator).append(MXConStant.strModelDir).append(File.separator).append(MXConStant.strConfigXML).toString();
		}
        //Hash校验
        nRet = HashCheck();
        if(nRet != 0)
        {
        	return nRet;
        }
        
        // 程序启动创建句柄，退出再销毁句柄
        // 需要注意的是，创建句柄操作比较耗时间，使用时可以将该操作放到线程里面
        iDetHandle   = cwDet.cwCreateDetHandleFromMem(ConStant.sLicence);
        iRecogHandle = cwRecog.cwCreateRecogHandle(sModelPath, ConStant.sLicence, 0, -1);
        if (IsValidHandle())
        {  	
        	// 人脸检测参数，最小人脸默认100，最大默认400
        	FaceParam param = new FaceParam();
        	if (FaceInterface.cw_errcode_t.CW_OK == cwDet.cwGetFaceParam(iDetHandle, param))
        	{
        		param.minSize = MXConStant.iFaceDetectMinSize;
        		param.maxSize = MXConStant.iFaceDetectMaxSize;
        		cwDet.cwSetFaceParam(iDetHandle, param);
        	}
        	iFeaLen = cwRecog.cwGetFeatureLength(iRecogHandle);
        	Log.e("MIAXIS", "iFeaLen="+iFeaLen);
        }
        else
        {
        	return -100;
        }
		return nRet;
	}

	/**
     * @author   chen.gs
     * @category 释放算法
     * @param    
     * @return   0-成功，其他-失败
     * */
	public int mxFreeAlg() {
		if (IsValidHandle()) {
			// 销毁句柄
			cwDet.cwReleaseDetHandle(iDetHandle);
			cwRecog.cwReleaseRecogHandle(iRecogHandle);
		}
		return 0;
	}
	
	/**
     * @author   chen.gs
     * @category 获取人脸特征长度
     * @param    
     * @return   人脸特征长度
     * */
	public int mxGetFeatureSize()
	{
		return iFeaLen;
	}
	
	/**
     * @author   chen.gs
     * @category 人脸检测
     * @param    pGrayImage  - 输入，灰度图像数据
     * 			 nImgWidth   - 输入，图像宽度
     * 			 nImgHeight  - 输入，图像高度
     * 			 pFaceNum    - 输入/输出，人脸数
     * 			 pFaceInfo   - 输出，人脸信息
     * @return   0-成功，其他-失败
     * */
	public int mxDetectFace(byte[] pGrayImage,int nImgWidth,int nImgHeight,int[] pFaceNum,MXFaceInfo[] pFaceInfo)
	{
    	int iRet = cwDet.cwFaceDetection(iDetHandle, pGrayImage, nImgWidth, nImgHeight,
				FaceInterface.cw_img_form_t.CW_IMAGE_GRAY8, 0, 0,
				FaceInterface.cw_op_t.CW_OP_DET
						| FaceInterface.cw_op_t.CW_OP_ALIGN, pFaceInfo);
    	if (iRet < 1)
    	{
    		pFaceNum[0] = 0;
    		return -1;
    	}
    	if (iRet >= FaceInterface.cw_errcode_t.CW_EMPTY_FRAME_ERR)
    	{
    		pFaceNum[0] = 0;
    		return -2;
    	}
    	pFaceNum[0] = iRet;   	
    	return 0;
	}
	

	/**
     * @author   chen.gs
     * @category 人脸检测
     * @param    pYUVImgBuf  - 输入，YUV格式视频流图像数据 
     * 			 nImgWidth   - 输入，图像宽度
     * 			 nImgHeight  - 输入，图像高度
     * 			 pFaceNum    - 输入/输出，人脸数
     * 			 pFaceInfo   - 输出，人脸信息
     * @return   0-成功，其他-失败
     * */
	public int mxDetectFaceYUV(byte[] pYUVImgBuf,int nImgWidth,int nImgHeight,int[] pFaceNum,MXFaceInfo[] pFaceInfo)
	{
		byte[] pGrayImage = new byte[nImgWidth*nImgHeight];
		DataDecode.decodeYUV420SP(pGrayImage,pYUVImgBuf,nImgWidth,nImgHeight);
		DataDecode.ImrotateLevel_raw(pGrayImage,nImgWidth,nImgHeight);
		return mxDetectFace(pGrayImage,nImgWidth,nImgHeight,pFaceNum,pFaceInfo);
	}

	/**
     * @author   chen.gs
     * @category 人脸特征提取
     * @param    pGrayImage  - 输入，灰度图像数据
     * 			 nImgWidth   - 输入，图像宽度
     * 			 nImgHeight  - 输入，图像高度
     * 			 faceInfo    - 输入，人脸信息
     * 			 pFeatureData - 输出，人脸特征，特征长度	
     * @return   0-成功，其他-失败
     * */
	public int mxFeatureExtract(byte[] pGrayImage, int nImgWidth, int nImgHeight, MXFaceInfo faceInfo, byte[] pFeatureData)
	{
		int iRet = 0;
		iRet = cwRecog.cwGetFiledFeature(iRecogHandle, faceInfo.alignedData, faceInfo.alignedW,  
				faceInfo.alignedH, faceInfo.nChannels, 1, pFeatureData, iFeaLen);
		return iRet;
	}
	
	/**
     * @author   chen.gs
     * @category 人脸特征提取
     * @param    pYUVImgBuf  - 输入，YUV格式视频流图像数据 
     * 			 nImgWidth   - 输入，图像宽度
     * 			 nImgHeight  - 输入，图像高度
     * 			 pFaceNum    - 输入/输出，人脸数
     * 			 pFaceInfo   - 输出，人脸信息
     * @return   0-成功，其他-失败
     * */
	public int mxFeatureExtractYUV(byte[] pYUVImgBuf, int nImgWidth, int nImgHeight, MXFaceInfo faceInfo, byte[] pFeatureData)
	{
		return mxFeatureExtract(null,nImgWidth,nImgHeight,faceInfo,pFeatureData);
	}
	
	/**
     * @author   chen.gs
     * @category 人脸特征比对
     * @param    pFaceFeaA - 输入，人脸特征A
     * 			 pFaceFeaB - 输入，人脸特征B
     * 			 fScore    - 输出，相似性度量值，0~1.0 ，越大越相似。
     * @return   0-成功，其他-失败
     * */
	public int mxFeatureMatch(byte[] pFaceFeaA,byte[] pFaceFeaB,float[] fScore)
	{
		byte[] btFeaProbe = new byte[iFeaLen];
		cwRecog.cwConvertFiledFeatureToProbeFeature(iRecogHandle,pFaceFeaB,iFeaLen,1,btFeaProbe);
		int iRet = cwRecog.cwComputeMatchScore(iRecogHandle, btFeaProbe, iFeaLen, 1, pFaceFeaA, iFeaLen, 1, fScore);
		return iRet;
	}
	
	
	//要计算文件本身的hash值，用MessageDigest类，需要读入文件的byte。一篇例文：
	//http://hi.baidu.com/black_zhu/item/04472b099819e8e1f55ba6da
	//hashType的值："MD5"，"SHA1"，"SHA-256"，"SHA-384"，"SHA-512"
	private static String getHash(String fileName, String hashType)
	        throws Exception
	    {
	        InputStream fis = new FileInputStream(fileName);
	        byte buffer[] = new byte[1024];
	        MessageDigest md5 = MessageDigest.getInstance(hashType);
	        for(int numRead = 0; (numRead = fis.read(buffer)) > 0;)
	        {
	            md5.update(buffer, 0, numRead);
	        }
	        fis.close();
	        return toHexString(md5.digest());
	    }
	
	public static String toHexString(byte[] hex) {
		StringBuilder sb = new StringBuilder();
		for (byte b : hex) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}
}
