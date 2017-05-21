package org.zz.idcard_hid_driver;
import java.util.Calendar;

import com.guoguang.jni.JniCall;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

/**
 * @author  chen.gs
 * @version V1.0.1.20160714 二代证读卡接口库（基于HID端点协议），采用MR-300测试验证。
 * @see     V1.0.2.20161202 修改VID和PID为0x10c4和0x0007，增加图片解码接口和base64编码接口
 *          V1.0.3.20170122 代码进行整理，部分与IcCard共用。      
 *          V1.0.4.20170213 (1)ExeCommand中收包增加5秒超时退出(防止USB假死程序不退出的超时)
 *                          (2)增加是否开启日志接口 mxSetTraceLevel
 *                          (3)ExeCommand中收包增加防止通信脏数据导致内存溢出判断       
 *          V1.0.5.20170224 (1)屏蔽开天线AntControl(1)接口
 *          				(2)修改收包操作：收第1包解析数据长度，根据数据长度计算还需要接收多少包
 *                          (3)读取身份证信息(包含指纹信息)mxReadCardFullInfo,0成功修改为0和1均成功， 0-成功(有指纹信息)，1-成功(无指纹信息)             
 *          V1.0.6.20170306 增加发送数据前，增加清USB脏数据 
 * */
public class IdCardDriver {

	public static byte   CMD_IDCARD_COMMAND	     = (byte) 0xB1; //ID卡控制指令
	public static short  CMD_ANTCTL_CONTROL		 = (short)0xFA11;
	public static short  CMD_READIDVER_CONTROL   = (short)0xFAF0;
	public static short  CMD_READIDMSG_CONTROL   = (short)0xFA92;
	public static short  CMD_GETSAMID_CONTROL	 = (short)0x12FF;
	public static short  CMD_FindCARD_CONTROL	 = (short)0x2001;
	public static short  CMD_SELECTCARD_CONTROL	 = (short)0x2002;
	public static short  CMD_READMSG_CONTROL	 = (short)0x3001;
	public static short  CMD_READFULLMSG_CONTROL = (short)0x3010;
	
	private static int IMAGE_X             = 256;
	private static int IMAGE_Y             = 360;
	private static int IMAGE_SIZE          = IMAGE_X*IMAGE_Y;
	
	
	private static byte  CMD_GET_IMAGE	      	= (byte) 0x0A ;  //上传图像
	private static byte  CMD_READ_VERSION  		= (byte) 0x0D;   //读取版本
	private static byte  CMD_GET_HALF_IMG    	= (byte) 0x14;   //上传高低位压缩的图像
		
	private static final int mPhotoWidth      = 102;
	private static final int mPhotoWidthBytes = (((mPhotoWidth * 3 + 3) / 4) * 4);
	private static final int mPhotoHeight     = 126;
	private static final int mPhotoSize       = (14 + 40 + mPhotoWidthBytes * mPhotoHeight);
	
	private UsbBase m_usbBase;
	private Handler m_fHandler        = null;

	/******************************************************************************************
	功	能：是否开启日志
	参	数：iTraceLevel - 0不开启，非0开启
	返	回：
	 ******************************************************************************************/
	public void mxSetTraceLevel(int iTraceLevel)
	{
		if(iTraceLevel!=0){
			ConStant.DEBUG = true;
		}else{
			ConStant.DEBUG = false;
		}
	}

	/**
	 * 功	能：调试
	 * 参	数：obj - 调试打印信息
	 * 返	回：
	 * */
	public void SendMsg(String obj) {
		if(ConStant.DEBUG)
		{
			Message message = new Message();
			message.what  = ConStant.SHOW_MSG;
			message.obj   = obj;
			message.arg1  = 0;
			if (m_fHandler!=null) {
				m_fHandler.sendMessage(message);	
			}	
		}
	}
	
	/**
	 * 功	能：构造函数
	 * 参	数：context - 应用上下文
	 * 返	回：
	 * */
	public IdCardDriver(Context context){
		m_usbBase = new UsbBase(context);

	}
	
	public IdCardDriver(Context context, Handler bioHandler){
		m_fHandler = bioHandler;
		m_usbBase = new UsbBase(context,bioHandler);
	}
	
	/**
	 * @author   chen.gs
	 * @category 获取驱动库Jar包版本
	 * @param    
	 * @return   Jar包版本
	 * */
	public String mxGetJarVersion() {
		// 命令
		String strVersion = "MIAXIS IdCard Driver V1.0.6.20170306";
		return strVersion;
	}
	
	/**
	 * 功	能：	获取连接设备个数
	 * 参	数：	
	 * 返	回：  >=0	-	设备个数，<0	-	失败
	 * */
	public int mxGetDevNum()
	{
		return m_usbBase.getDevNum(ConStant.VID, ConStant.PID);
	}
	
	/**
	 * 功	能：	获取设备版本信息
	 * 参	数：	bVersion - 版本信息（64字节）
	 * 返	回：  0	-	成功，其他	-	失败
	 * */
	public int mxGetDevVersion(byte[] bVersion) {
		int nRet = ConStant.ERRCODE_SUCCESS;
		int[] wRecvLength = new int[1];
		wRecvLength[0] = 56;
		nRet = ExeCommand(CMD_READ_VERSION,null,0,100,bVersion,wRecvLength, ConStant.CMD_TIMEOUT);
		return nRet;
	}
	
	/**
	 * 功	能：	读取身份证模块版本
	 * 参	数：	bVersion - 版本信息（64字节）
	 * 返	回：  0	-	成功，其他	-	失败
	 * */
	public int mxGetIdCardModuleVersion(byte[] bVersion) {
		int iRet = ConStant.ERRCODE_SUCCESS;
		iRet = GetIdCardModuleVersion(bVersion);
		if (iRet !=0x90)
		{
			return iRet;//ERRCODE_ANTENNA_ON;
		}
		return ConStant.ERRCODE_SUCCESS;
	}
	
	/**
	 * 功	能：	读取身份证ID
	 * 参	数：	bCardId - 身份证ID（64字节）
	 * 返	回：  0	-	成功，其他	-	失败
	 * */
	public int mxReadCardId(byte[] bCardId) {	
		int iRet = ConStant.ERRCODE_SUCCESS;

		iRet = GetIdCardNo(bCardId);
		if (iRet !=0x90)
		{
			return iRet;//ERRCODE_ANTENNA_ON;
		}
		iRet = AntControl(0);
		if (iRet !=0x90)
		{
			return iRet;//ERRCODE_ANTENNA_ON;
		}
		//SendMsg("bCardId: " + zzStringTrans.hex2str(bCardId));
		return ConStant.ERRCODE_SUCCESS;
	}
	
/*
	//按照二代证标准定义
	typedef struct st_id_Card
	{
		unsigned char id_Name[30];		    //姓名
		unsigned char id_Sex[2];		    //性别 1为男 其他为女
		unsigned char id_Rev[4];		    //民族
		unsigned char id_Born[16];		    //出生日期
		unsigned char id_Home[70];			//住址
		unsigned char id_Code[36];			//身份证号
		unsigned char id_RegOrg[30];		//签发机关
		unsigned char id_ValidPeriod[32];	//有效日期  起始日期16byte 截止日期16byte
		unsigned char id_NewAddr[36];		//预留区域
		unsigned char id_pImage[1024];		//图片区域
	}CARD_INFO;
*/
	/**
	 * 功	能：	读取身份证信息
	 * 参	数：	bCardInfo - 身份证信息（256+1024字节）
	 * 返	回：  0	-	成功，其他	-	失败
	 * */
	public int mxReadCardInfo(byte[] bCardInfo) {
		SendMsg("========================");
		SendMsg("mxReadCardInfo");
		if(bCardInfo.length<(256+1024)){
			return ConStant.ERRCODE_MEMORY_OVER;
		}
		int iRet = ConStant.ERRCODE_SUCCESS;
		byte[] ucCHMsg     = new  byte[256];
		byte[] ucPHMsg     = new  byte[1024];
		byte[] pucManaInfo = new  byte[256];
		int[] uiCHMsgLen   = new int[1];
		int[] uiPHMsgLen   = new int[1];
		byte[] bmp = new byte[mPhotoSize];
		SendMsg("GetSAMID");
		iRet = GetSAMID(pucManaInfo);
		if (iRet !=0x90)
		{
			AntControl(0);
			return iRet;//ERRCODE_ID_CARD_FIND;
		}
		//寻卡
		SendMsg("StartFindIDCard");
		iRet = StartFindIDCard(pucManaInfo);
		if (iRet !=0x9f)
		{
			iRet = StartFindIDCard(pucManaInfo);
			if (iRet !=0x9f)
			{
				//return ERRCODE_ID_CARD_FIND; //如果需要连续读 则不需要判断返回值
			}
		}
		//选卡
		SendMsg("SelectIDCard");
		iRet = SelectIDCard(pucManaInfo);
		if (iRet !=0x90)
		{
			return iRet;//ERRCODE_ID_CARD_FIND; //如果需要连续读 则不需要判断返回值
		}
		//读卡
		SendMsg("ReadBaseMsgUnicode");
		iRet =  ReadBaseMsgUnicode(ucCHMsg,uiCHMsgLen,ucPHMsg,uiPHMsgLen); //发送
		if (iRet !=0x90)
		{
			//SendMsg("ReadBaseMsgUnicode,iRet=" +iRet);
			AntControl(0);
			return iRet;//ERRCODE_ID_CARD_READ;
		}
		for (int i = 0; i < uiCHMsgLen[0]; i++) {
			bCardInfo[i] = ucCHMsg[i];
		}
		for (int i = 0; i < uiPHMsgLen[0]; i++) {
			bCardInfo[i+256] = ucPHMsg[i];
		}
		//BMP.SaveData("/mnt/sdcard/DCIM/tttttt1111.dat", ucCHMsg, uiCHMsgLen[0]);
		//BMP.SaveData("/mnt/sdcard/DCIM/tttttt2222.dat", ucPHMsg, uiPHMsgLen[0]);
		
		//SendMsg("ucCHMsg: " +zzStringTrans.hex2str(ucCHMsg));
		//SendMsg("uiCHMsgLen: " +uiCHMsgLen[0]);
		//SendMsg("ucPHMsg: " +zzStringTrans.hex2str(ucPHMsg));
		//SendMsg("uiPHMsgLen: " +uiPHMsgLen[0]);
		SendMsg("AntControl(0)");
		AntControl(0);
		SendMsg("========================");
		return ConStant.ERRCODE_SUCCESS;
	}
	
	/*
	  //重新定义二代证标准，包含指纹信息
		typedef struct st_id_Card_full
		{
			unsigned char id_Name[30];			//姓名
			unsigned char id_Sex[2];	        //性别 1为男 其他为女
			unsigned char id_Rev[4];		    //民族
			unsigned char id_Born[16];			//出生日期
			unsigned char id_Home[70];			//住址
			unsigned char id_Code[36];		    //身份证号
			unsigned char id_RegOrg[30];	    //签发机关
			unsigned char id_ValidPeriod[32];	//有效日期  起始日期16byte 截止日期16byte
			unsigned char id_NewAddr[36];		//预留区域
			unsigned char id_pImage[1024];		//图片区域
			unsigned char id_finger[1024];      //指纹区域
			unsigned char id_pBMP[38862];       //解码后图片数据
		}CARD_INFO_FULL;
	 */
	/**
	 * 功	能：	读取身份证信息(包含指纹信息)
	 * 参	数：	bCardFullInfo - 身份证信息（256+1024+1024字节）
	 * 返	回：  0-成功(有指纹信息)，1-成功(无指纹信息)，其他-失败
	 * */
	public int mxReadCardFullInfo(byte[] bCardFullInfo) {
		SendMsg("========================");
		SendMsg("mxReadCardFullInfo");
		if(bCardFullInfo.length<(256+1024+1024)){
			return ConStant.ERRCODE_MEMORY_OVER;
		}
		int iRet = ConStant.ERRCODE_SUCCESS;
		byte[] ucCHMsg = new  byte[256];
		byte[] ucPHMsg = new  byte[1024];
		byte[] ucFPMsg  = new  byte[1024];
		byte[] pucManaInfo = new  byte[256];
		int[] uiCHMsgLen = new int[1];
		int[] uiPHMsgLen = new int[1];
		int[] uiFPMsgLen  = new int[1];
		byte[] bmp = new byte[mPhotoSize];

		SendMsg("GetSAMID");
		iRet = GetSAMID(pucManaInfo);
		if (iRet !=0x90)
		{
			AntControl(0);
			return iRet;//ERRCODE_ID_CARD_FIND;
		}
		//寻卡
		SendMsg("StartFindIDCard");
		iRet = StartFindIDCard(pucManaInfo);
		if (iRet !=0x9f)
		{
			iRet = StartFindIDCard(pucManaInfo);
			if (iRet !=0x9f)
			{
				return iRet; 
			}
		}
		//选卡
		SendMsg("SelectIDCard");
		iRet = SelectIDCard(pucManaInfo);
		if (iRet !=0x90)
		{
			SendMsg("SelectIDCard iRet="+iRet);
			return iRet; 
		}
		//读卡
		SendMsg("ReadFullMsgUnicode");
		iRet =  ReadFullMsgUnicode(ucCHMsg,uiCHMsgLen,ucPHMsg,uiPHMsgLen,ucFPMsg,uiFPMsgLen); //发送	
		if (iRet !=0x90)
		{
			SendMsg("ReadBaseMsgUnicode,iRet=" +iRet);
			AntControl(0);
			return ConStant.ERRCODE_ID_CARD_READ;
		}
		for (int i = 0; i < uiCHMsgLen[0]; i++) {
			bCardFullInfo[i] = ucCHMsg[i];
		}
		for (int i = 0; i < uiPHMsgLen[0]; i++) {
			bCardFullInfo[i+256] = ucPHMsg[i];
		}
		for (int i = 0; i < uiFPMsgLen[0]; i++) {
			bCardFullInfo[i+256+1024] = ucFPMsg[i];
		}
		
		SendMsg("AntControl(0)");
		AntControl(0);
		SendMsg("========================");
		if(uiFPMsgLen[0]==0)
			return 1;
		return ConStant.ERRCODE_SUCCESS;
	}
	
	//////////////////////////////////////////////////////////////////////////
	int GetIdCardModuleVersion(byte[] bVersion)
	{
		int lRV = ConStant.ERRCODE_SUCCESS;
		byte[] oPackDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN]; //实际上此处不能到64字节应该减去外层包装后的字节数 56字节
		int[] oPackLen = new int[1];
		oPackLen[0] = oPackDataBuffer.length;
		byte[] oRecvDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN]; //实际上此处不能到64字节应该减去外层包装后的字节数 56字节
		int[] oRecvLen = new int[1];
		oRecvLen[0] = oRecvDataBuffer.length;
		int[] result = new int[1];
		byte[] bSendBuf= new byte[1];

		lRV = SendIDCardPack(CMD_READIDVER_CONTROL,null,0,oPackDataBuffer,oPackLen);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		lRV =IDCardAPDU(oPackDataBuffer,oPackLen[0],100,oRecvDataBuffer,oRecvLen,500);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
	
		for (int i = 0; i < oPackDataBuffer.length; i++) {
			oPackDataBuffer[i] = 0x00;
		}
		oPackLen[0] = oPackDataBuffer.length;
		lRV =RecvIDCardPack(oRecvDataBuffer,oRecvLen[0],oPackDataBuffer,oPackLen,result);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		if ( result[0]!=0x90)
		{
			return  result[0];
		}
		for (int i = 0; i <oPackLen[0]; i++) {
			bVersion[i] = oPackDataBuffer[i];
		}
		return result[0];
	}
	
	//////////////////////////////////////////////////////////////////////////
	int GetIdCardNo(byte[] bVersion)
	{
		int lRV = ConStant.ERRCODE_SUCCESS;
		byte[] oPackDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN]; //实际上此处不能到64字节应该减去外层包装后的字节数 56字节
		int[] oPackLen = new int[1];
		oPackLen[0] = oPackDataBuffer.length;
		byte[] oRecvDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN]; //实际上此处不能到64字节应该减去外层包装后的字节数 56字节
		int[] oRecvLen = new int[1];
		oRecvLen[0] = oRecvDataBuffer.length;
		int[] result = new int[1];
		byte[] bSendBuf= new byte[1];
		
		lRV = SendIDCardPack(CMD_READIDMSG_CONTROL,null,0,oPackDataBuffer,oPackLen);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		lRV =IDCardAPDU(oPackDataBuffer,oPackLen[0],100,oRecvDataBuffer,oRecvLen,500);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		
		for (int i = 0; i < oPackDataBuffer.length; i++) {
			oPackDataBuffer[i] = 0x00;
		}
		oPackLen[0] = oPackDataBuffer.length;
		lRV =RecvIDCardPack(oRecvDataBuffer,oRecvLen[0],oPackDataBuffer,oPackLen,result);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		if ( result[0]!=0x90)
		{
			return  result[0];
		}
		for (int i = 0; i <oPackLen[0]; i++) {
			bVersion[i] = oPackDataBuffer[i];
		}
		return result[0];
	}
	
	//////////////////////////////////////////////////////////////////////////
	int GetSAMID(byte[] bVersion)
	{
		int lRV = ConStant.ERRCODE_SUCCESS;
		byte[] oPackDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN]; //实际上此处不能到64字节应该减去外层包装后的字节数 56字节
		int[] oPackLen = new int[1];
		oPackLen[0] = oPackDataBuffer.length;
		byte[] oRecvDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN]; //实际上此处不能到64字节应该减去外层包装后的字节数 56字节
		int[] oRecvLen = new int[1];
		oRecvLen[0] = oRecvDataBuffer.length;
		int[] result = new int[1];
		byte[] bSendBuf= new byte[1];
		
		lRV = SendIDCardPack(CMD_GETSAMID_CONTROL,null,0,oPackDataBuffer,oPackLen);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		lRV =IDCardAPDU(oPackDataBuffer,oPackLen[0],100,oRecvDataBuffer,oRecvLen,500);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		
		for (int i = 0; i < oPackDataBuffer.length; i++) {
			oPackDataBuffer[i] = 0x00;
		}
		oPackLen[0] = oPackDataBuffer.length;
		lRV =RecvIDCardPack(oRecvDataBuffer,oRecvLen[0],oPackDataBuffer,oPackLen,result);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		if ( result[0]!=0x90)
		{
			return  result[0];
		}
		for (int i = 0; i <oPackLen[0]; i++) {
			bVersion[i] = oPackDataBuffer[i];
		}
		return result[0];
	}
	//////////////////////////////////////////////////////////////////////////
	int StartFindIDCard(byte[] bVersion)
	{
		int lRV = ConStant.ERRCODE_SUCCESS;
		byte[] oPackDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN]; //实际上此处不能到64字节应该减去外层包装后的字节数 56字节
		int[] oPackLen = new int[1];
		oPackLen[0] = oPackDataBuffer.length;
		byte[] oRecvDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN]; //实际上此处不能到64字节应该减去外层包装后的字节数 56字节
		int[] oRecvLen = new int[1];
		oRecvLen[0] = oRecvDataBuffer.length;
		int[] result = new int[1];
		byte[] bSendBuf= new byte[1];
	
		lRV = SendIDCardPack(CMD_FindCARD_CONTROL,null,0,oPackDataBuffer,oPackLen);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		lRV =IDCardAPDU(oPackDataBuffer,oPackLen[0],100,oRecvDataBuffer,oRecvLen,500);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
	
		for (int i = 0; i < oPackDataBuffer.length; i++) {
			oPackDataBuffer[i] = 0x00;
		}
		oPackLen[0] = oPackDataBuffer.length;
		lRV =RecvIDCardPack(oRecvDataBuffer,oRecvLen[0],oPackDataBuffer,oPackLen,result);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		if ( result[0]!=0x90)
		{
			return  result[0];
		}
		for (int i = 0; i <oPackLen[0]; i++) {
			bVersion[i] = oPackDataBuffer[i];
		}
		return result[0];
	}
	
	//////////////////////////////////////////////////////////////////////////
	int SelectIDCard(byte[] bVersion)
	{
		int lRV = ConStant.ERRCODE_SUCCESS;
		byte[] oPackDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN]; //实际上此处不能到64字节应该减去外层包装后的字节数 56字节
		int[] oPackLen = new int[1];
		oPackLen[0] = oPackDataBuffer.length;
		byte[] oRecvDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN]; //实际上此处不能到64字节应该减去外层包装后的字节数 56字节
		int[] oRecvLen = new int[1];
		oRecvLen[0] = oRecvDataBuffer.length;
		int[] result = new int[1];
		byte[] bSendBuf= new byte[1];
		
		SendMsg("SendIDCardPack");
		lRV = SendIDCardPack(CMD_SELECTCARD_CONTROL,null,0,oPackDataBuffer,oPackLen);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			SendMsg("SendIDCardPack lRV="+lRV);
			return lRV;
		}
		SendMsg("IDCardAPDU");
		lRV =IDCardAPDU(oPackDataBuffer,oPackLen[0],100,oRecvDataBuffer,oRecvLen,500);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			SendMsg("IDCardAPDU lRV="+lRV);
			return lRV;
		}
		
		for (int i = 0; i < oPackDataBuffer.length; i++) {
			oPackDataBuffer[i] = 0x00;
		}
		oPackLen[0] = oPackDataBuffer.length;
		SendMsg("RecvIDCardPack");
		lRV =RecvIDCardPack(oRecvDataBuffer,oRecvLen[0],oPackDataBuffer,oPackLen,result);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			SendMsg("RecvIDCardPack lRV="+lRV);
			return lRV;
		}
		if ( result[0]!=0x90)
		{
			SendMsg("RecvIDCardPack result[0]="+result[0]);
			return  result[0];
		}
		for (int i = 0; i <oPackLen[0]; i++) {
			bVersion[i] = oPackDataBuffer[i];
		}
		return result[0];
	}
	//////////////////////////////////////////////////////////////////////////
	int ReadBaseMsgUnicode(byte[] pucCHMsg, int[] puiCHMsgLen,byte[] PucPHMsg,int[] puiPHMsgLen)
	{
		int lRV = ConStant.ERRCODE_SUCCESS;
		byte[] oPackDataBuffer = new byte[ConStant.CMD_BUFSIZE]; //实际上此处不能到64字节应该减去外层包装后的字节数 56字节
		int[] oPackLen = new int[1];
		oPackLen[0] = oPackDataBuffer.length;
		byte[] oRecvDataBuffer = new byte[ConStant.CMD_BUFSIZE]; //实际上此处不能到64字节应该减去外层包装后的字节数 56字节
		int[] oRecvLen = new int[1];
		oRecvLen[0] = oRecvDataBuffer.length;
		int[] result = new int[1];
		byte[] bSendBuf= new byte[1];
		
		lRV = SendIDCardPack(CMD_READMSG_CONTROL,null,0,oPackDataBuffer,oPackLen);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		lRV =IDCardAPDU(oPackDataBuffer,oPackLen[0],100,oRecvDataBuffer,oRecvLen,500);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		
		for (int i = 0; i < oPackDataBuffer.length; i++) {
			oPackDataBuffer[i] = 0x00;
		}
		oPackLen[0] = oPackDataBuffer.length;
		lRV =RecvIDCardPack(oRecvDataBuffer,oRecvLen[0],oPackDataBuffer,oPackLen,result);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		if ( result[0]!=0x90)
		{
			return  result[0];
		}
		if (oPackLen[0] != 1295)
		{
			return ConStant.ERRCODE_CRC;
		}
		for (int i = 0; i < 256; i++) {
			pucCHMsg[i] = oPackDataBuffer[i+4];
		}
		puiCHMsgLen[0] = 256;
		for (int i = 0; i < 1024; i++) {
			PucPHMsg[i] = oPackDataBuffer[i+4+256];
		}
		puiPHMsgLen[0] = 1024;
		return result[0];
	}
	
	//////////////////////////////////////////////////////////////////////////
	int ReadFullMsgUnicode(byte[] pucCHMsg, int[] puiCHMsgLen,
			byte[] PucPHMsg,int[] puiPHMsgLen,
			byte[] PucFPMsg,int[] puiFPMsgLen)
	{
		int lRV = ConStant.ERRCODE_SUCCESS;
		byte[] oPackDataBuffer = new byte[ConStant.CMD_BUFSIZE]; //实际上此处不能到64字节应该减去外层包装后的字节数 56字节
		int[] oPackLen = new int[1];
		oPackLen[0] = oPackDataBuffer.length;
		byte[] oRecvDataBuffer = new byte[ConStant.CMD_BUFSIZE]; //实际上此处不能到64字节应该减去外层包装后的字节数 56字节
		int[] oRecvLen = new int[1];
		oRecvLen[0] = oRecvDataBuffer.length;
		int[] result = new int[1];
		byte[] bSendBuf= new byte[1];

		lRV = SendIDCardPack(CMD_READFULLMSG_CONTROL,null,0,oPackDataBuffer,oPackLen);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}

		lRV =IDCardAPDU(oPackDataBuffer,oPackLen[0],100,oRecvDataBuffer,oRecvLen,500);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		
		//指纹数据长度
		puiFPMsgLen[0] = oRecvDataBuffer[14]*256+oRecvDataBuffer[13];
		SendMsg("puiFPMsgLen[0]="+puiFPMsgLen[0]);
		for (int i = 0; i < oPackDataBuffer.length; i++) {
			oPackDataBuffer[i] = 0x00;
		}
		oPackLen[0] = oPackDataBuffer.length;
		SendMsg("RecvIDCardPack");
		lRV =RecvIDCardPack(oRecvDataBuffer,oRecvLen[0],oPackDataBuffer,oPackLen,result);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			SendMsg("RecvIDCardPack lRV="+lRV);
			return lRV;
		}
		if ( result[0]!=0x90)
		{
			SendMsg("RecvIDCardPack result[0]="+result[0]);
			return  result[0];
		}
//		if (oPackLen[0] != 2321)
//		{
//			return ConStant.ERRCODE_CRC;
//		}
		for (int i = 0; i < 256; i++) {
			pucCHMsg[i] = oPackDataBuffer[i+4+2];
		}
		puiCHMsgLen[0] = 256;
		for (int i = 0; i < 1024; i++) {
			PucPHMsg[i] = oPackDataBuffer[i+4+2+256];
		}
		puiPHMsgLen[0] = 1024;
		for (int i = 0; i < puiFPMsgLen[0]; i++) {
			PucFPMsg[i] = oPackDataBuffer[i+4+2+256+1024];
		}
		//puiFPMsgLen[0] = 1024;
		return result[0];
	}

	//////////////////////////////////////////////////////////////////////////
	int AntControl(int dAntState)
	{
		int lRV = ConStant.ERRCODE_SUCCESS;
		byte[] oPackDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN]; //实际上此处不能到64字节应该减去外层包装后的字节数 56字节
		int[] oPackLen = new int[1];
		oPackLen[0] = oPackDataBuffer.length;
		byte[] oRecvDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN]; //实际上此处不能到64字节应该减去外层包装后的字节数 56字节
		int[] oRecvLen = new int[1];
		oRecvLen[0] = oRecvDataBuffer.length;
		int[] result = new int[1];
		byte[] bSendBuf= new byte[1];
		bSendBuf[0] = (byte) dAntState;
		lRV = SendIDCardPack(CMD_ANTCTL_CONTROL,bSendBuf,1,oPackDataBuffer,oPackLen);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		lRV =IDCardAPDU(oPackDataBuffer,oPackLen[0],100,oRecvDataBuffer,oRecvLen,500);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		
		for (int i = 0; i < oPackDataBuffer.length; i++) {
			oPackDataBuffer[i] = 0x00;
		}
		oPackLen[0] = oPackDataBuffer.length;
		lRV =RecvIDCardPack(oRecvDataBuffer,oRecvLen[0],oPackDataBuffer,oPackLen,result);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		return result[0];
	}

	//////////////////////////////////////////////////////////////////////////
	//身份证数据组包
	int SendIDCardPack(short IDCardCommandIDAndIDCardparam,byte[] SendDataBuffer,
			int SendLen,byte[] oPackDataBuffer,int[] oPackLen)
	{
		byte[] tempBufferData = new byte[ConStant.DATA_BUFFER_SIZE_MIN];//实际上此处不能到64字节应该减去外层包装后的字节数 56字节
		int i=0;
		int offsize =0;
		byte AddCheck=0;
		short len =0;
		byte[] FlagStart = new byte[5];
		byte[] dtemp    = new byte[2];
		FlagStart[0] = (byte) 0xAA;
		FlagStart[1] = (byte) 0xAA;
		FlagStart[2] = (byte) 0xAA;
		FlagStart[3] = (byte) 0x96;
		FlagStart[4] = (byte) 0x69;
		dtemp[0]    = 0x00;
		dtemp[1]    = 0x00;
		//SendLen 超过46除去包头则大于了56 所以直接不允许发送
		if (SendLen > ConStant.DATA_BUFFER_SIZE_MIN-10 || SendLen < 0)
		{
			return ConStant.ERRCODE_MEMORY_OVER;
		}
	
		//标记头 AA AA AA 96 69
		for(i=0;i<FlagStart.length;i++)
		{
			tempBufferData[offsize+i] = FlagStart[i];
		}
		offsize = offsize +FlagStart.length;
	
		//2字节的包长度
		len = (short) (1 + 1+ SendLen + 1); //1命令+1参数 + 数据长+ 1校验和
	
		dtemp[0] = (byte) ((byte)(len/0x100)&0xFF);
		dtemp[1] = (byte) ((byte)len&0xFF);
		for(i=0;i<dtemp.length;i++)
		{
			tempBufferData[offsize+i] = dtemp[i];
		}
		offsize = offsize +dtemp.length;
		
		//1字节命令 +1字节参数
		for(i=0;i<dtemp.length;i++)
		{
			dtemp[i] = 0x00;
		}

		dtemp[0] = (byte) ((byte)(IDCardCommandIDAndIDCardparam>>8)&0xFF);
		dtemp[1] = (byte) ((byte)IDCardCommandIDAndIDCardparam&0xFF);
		for(i=0;i<dtemp.length;i++)
		{
			tempBufferData[offsize+i] = dtemp[i];
		}
		offsize = offsize +dtemp.length;
		//数据区
		if (SendLen >0 && SendLen < (ConStant.DATA_BUFFER_SIZE_MIN-10))
		{
			for(i=0;i<SendLen;i++)
			{
				tempBufferData[offsize+i] = SendDataBuffer[i];
			}
			offsize = offsize +SendLen;
		}
		//异或和
		for (i=0; i<(len+2); i++)
		{
			AddCheck ^= tempBufferData[i+5];
		}
		tempBufferData[offsize] = (byte) AddCheck;
		offsize = offsize +1;
	
		if (oPackLen[0] < offsize)
		{
			return ConStant.ERRCODE_MEMORY_OVER;
		}
		else
		{
			oPackLen[0]= (short) offsize;
			for(i=0;i<offsize;i++)
			{
				oPackDataBuffer[i] = tempBufferData[i];
			}
		}
		return ConStant.ERRCODE_SUCCESS;
	}

	//////////////////////////////////////////////////////////////////////////
	//身份证数据解包
	//对输入的数据进行解包并校验获得应答数据
	int RecvIDCardPack(byte[] RecvDataBuffer,int RecvLen,
			byte[] oPackDataBuffer, int[] oPackLen,int[] oResult)
	{
		byte[] tempBufferData = new byte[ConStant.CMD_BUFSIZE];
		int offsize =0;
		short len  =0;
		byte dresult          = (byte) 0xff;
		byte recvCheck     = 0x00;    //收到的数据校验和
		byte currentCheck = 0x00;    //根据收到的数据计算的当前校验和
		byte[] FlagStart = new byte[5];
		byte[] dtemp    = new byte[2];
		byte[]  Reser    = new byte[2]; //应答包保留位
		FlagStart[0] = (byte) 0xAA;
		FlagStart[1] = (byte) 0xAA;
		FlagStart[2] = (byte) 0xAA;
		FlagStart[3] = (byte) 0x96;
		FlagStart[4] = (byte) 0x69;
		dtemp[0]    = 0x00;
		dtemp[1]    = 0x00;
		Reser[0]     = 0x00;
		Reser[1]     = 0x00;
		//SendMsg("RecvIDCardPack RecvDataBuffer: " + zzStringTrans.hex2str(RecvDataBuffer));
		//SendMsg("RecvLen: " +RecvLen);
		for (int i = 0; i < FlagStart.length; i++) {
			if(RecvDataBuffer[i]!=FlagStart[i])
			{
				//SendMsg("RecvDataBuffer["+i+"]="+RecvDataBuffer[i]+"  vs  "+"FlagStart["+i+"]="+FlagStart[i]);
				return ConStant.ERRCODE_CRC; //校验包头错误
			}
		}
		offsize = offsize + 5;
		//长度
		len = (short) (256*RecvDataBuffer[offsize]+RecvDataBuffer[offsize+1]);
		offsize= offsize +2;
		
//		dtemp[0] = (byte) ((byte)(len/0x100)&0xFF);
//		dtemp[1] = (byte) ((byte)len&0xFF);
		//memcpy(&len,dtemp,sizeof(dtemp));
	
		//应答保留位
		Reser[0] = RecvDataBuffer[offsize];
		Reser[1] = RecvDataBuffer[offsize+1];
		for (int i = 0; i < Reser.length; i++) {
			if(Reser[i]!=0x00)
			{
				//SendMsg("Reser["+i+"]="+Reser[i]);
				return ConStant.ERRCODE_CRC; //校验包头错误
			}
		}
		
		offsize = offsize +2;
		//操作结果
		dresult = RecvDataBuffer[offsize];
		offsize = offsize +1;
		//返回数据
		if (len >4 )
		{
			for(int i=0;i<len-4;i++)
			{
				tempBufferData[i]=RecvDataBuffer[offsize+i];
			}
			offsize = offsize + len-4;
		}
	
		//异或和
		recvCheck = RecvDataBuffer[offsize];
	
		//计算 
		for (int i=0; i<(len+2-1); i++) //长度包含校验和 所以-1
		{
			currentCheck ^= RecvDataBuffer[i+5];
		}
		offsize = offsize+1;
	
		if (currentCheck != recvCheck)
		{
			return ConStant.ERRCODE_CRC;
		}

		if (oPackDataBuffer!=null && oPackLen[0]>(len-4))
		{
			oPackLen[0] = (short) offsize;
			for(int i=0;i<len-4;i++)
			{
				oPackDataBuffer[i] = tempBufferData[i];
			}
		}
		else
		{
			return ConStant.ERRCODE_MEMORY_OVER;
		}
		oResult[0] = dresult;
		if(dresult<0)
		{
			oResult[0] = dresult+256;
		}
		return ConStant.ERRCODE_SUCCESS;
	}
	
	//////////////////////////////////////////////////////////////////////////
	int IDCardAPDU(byte[] lpSendData,int wSendLength,int iSendTime,byte[] lpRecvData,int[] io_wRecvLength,int iRecvTime)
	{
		//调用ExeCommand将组好包的数据发送到设备
		int lRV = ConStant.ERRCODE_SUCCESS;
		lRV = ExeCommand(CMD_IDCARD_COMMAND,lpSendData,wSendLength,iSendTime,lpRecvData,io_wRecvLength,iRecvTime);
		return lRV;
	}

	int ExeCommand(byte nCommandID, byte[] lpSendData, int wSendLength,
			int iSendTime, byte[] lpRecvData, int[] io_wRecvLength,
			int iRecvTime) {
		int iMaxRecvLen = io_wRecvLength[0];
		SendMsg("nCommandID:" + nCommandID);
		int iRet = ConStant.ERRCODE_SUCCESS;
		// 打开设备
		iRet = m_usbBase.openDev(ConStant.VID, ConStant.PID);
		if (iRet != 0) {
			return iRet;
		}
		
		//清USB脏数据
		byte[] DataBuffer = new byte[ConStant.CMD_DATA_BUF_SIZE];
		while(true)
		{
			iRet = m_usbBase.recvData(DataBuffer,DataBuffer.length,5);
			if(iRet < 0)
				break;
		}

		// 发送数据包
		iRet = sendPacket(nCommandID, lpSendData, wSendLength);
		if (iRet != 0) {
			// 关闭设备
			m_usbBase.closeDev();
			return iRet;
		}
		// 接收数据包
		byte[] bResult = new byte[1];
		byte[] bRecvBuf = new byte[ConStant.CMD_DATA_BUF_SIZE];

		// 接收第一包
		iRet = recvPacket(bResult, bRecvBuf, io_wRecvLength,
				ConStant.CMD_TIMEOUT);
		if (iRet != 0) {
			// 关闭设备
			m_usbBase.closeDev();
			return iRet;
		}
		// 从第一包解析需要接收的数据长度
		int len = 0;
		len = bRecvBuf[7] * 256 + bRecvBuf[8]+7;
		SendMsg("len=" + len);
		int packsize = len / ConStant.REVC_BUFFER_SIZE_MIN;
		if (len % ConStant.REVC_BUFFER_SIZE_MIN != 0) {
			packsize++;
		}
		SendMsg("packsize=" + packsize);
		byte[] outBuffer = new byte[ConStant.CMD_BUFSIZE];
		int realsize = 0;
		SendMsg("io_wRecvLength[0]=" + io_wRecvLength[0]);
		if (io_wRecvLength[0] >= 2) {
			for (int i = 2; i < io_wRecvLength[0]; i++) {
				outBuffer[i - 2 + realsize] = bRecvBuf[i];
			}
			realsize = realsize + io_wRecvLength[0] - 2;// 实际收到的数据
		} else {
			realsize = realsize;
		}
		SendMsg("realsize=" + realsize);
		// 从第二包开始收数据
		for (int k = 1; k < packsize; k++) {
			iRet = recvPacket(bResult, bRecvBuf, io_wRecvLength,
					ConStant.CMD_TIMEOUT);
			if (iRet != 0) {
				// 关闭设备
				m_usbBase.closeDev();
				return iRet;
			}
			if (io_wRecvLength[0] >= 2) {
				for (int i = 2; i < io_wRecvLength[0]; i++) {
					outBuffer[i - 2 + realsize] = bRecvBuf[i];
				}
				realsize = realsize + io_wRecvLength[0] - 2;// 实际收到的数据
			} else {
				realsize = realsize;
			}
		}

		// 防止通信脏数据导致内存溢出
		SendMsg("====realsize=" + realsize);
		SendMsg("====iMaxRecvLen=" + iMaxRecvLen);
		if (realsize > iMaxRecvLen) {
			// 关闭设备
			m_usbBase.closeDev();
			return ConStant.ERRCODE_MEMORY_OVER;
		}
		if (realsize >= 2) {
			for (int i = 0; i < realsize; i++) {
				lpRecvData[i] = outBuffer[i];
			}
			io_wRecvLength[0] = realsize;
		}	
//		if(packsize>10)
//		{
//			recvPacket(bResult, bRecvBuf, io_wRecvLength,5);
//			SendMsg("recvPacket: " + zzStringTrans.hex2str(bRecvBuf));
//			SendMsg("recvPacket: " +io_wRecvLength[0]);
//		}
		// 关闭设备
		m_usbBase.closeDev();
		// SendMsg("recvPacket: " + zzStringTrans.hex2str(lpRecvData));
		// SendMsg("recvPacket: " +io_wRecvLength[0]);

		return ConStant.ERRCODE_SUCCESS;
	}
	
	/**
	 * 功	能：	发送数据包
	 * 参	数：	bCmd		- 	指令ID
	 *         	  	bSendBuf	- 	待发送数据缓存，缓存大小：64字节
	 *          	iDataLen   - 	数据长度
	 * 返回值：	0	-	成功，其他	-	失败
	 * */
	private int sendPacket(byte bCmd,byte[] bSendBuf,int iDataLen){
		int iRet = -1;
		int   offsize     = 0;
		short iCheckSum   = 0;
		byte[] DataBuffer = new byte[ConStant.CMD_DATA_BUF_SIZE];
		//1字节开始标志	0x88
		DataBuffer[offsize++] = ConStant.CMD_REQ_FLAG;
		//2字节SRN包序号递增
		DataBuffer[offsize++] = 0x00;
		DataBuffer[offsize++] = 0x00;
		//2字节Length(Length short直接拷贝)
		DataBuffer[offsize++] = (byte) ((iDataLen+1) & 0xFF);
		DataBuffer[offsize++] = (byte) ((iDataLen+1) >> 8);
		//1字节命令
		DataBuffer[offsize++] = bCmd;
		//数据
		if (iDataLen>1)
		{
			for (int i = 0; i < iDataLen; i++) {
				DataBuffer[offsize++] = bSendBuf[i];
			}
		}	
		//2字节AddCheck
		short tmp;
		for (int i=3; i<offsize; i++)
		{
			tmp = DataBuffer[i];
			if(tmp<0)
			{
				tmp += 256;	
			}
			iCheckSum = (short) (iCheckSum+tmp);
		}
		if(iCheckSum<0)
		{
			iCheckSum += 256;	
		}
		// 2字节校验和
		DataBuffer[offsize++] = (byte) (iCheckSum & 0xFF); 
		DataBuffer[offsize++] = (byte) (((byte) (iCheckSum >> 8))& 0xFF);
		//发送数据
		//SendMsg("sendData: " + zzStringTrans.hex2str(DataBuffer));
		//SendMsg("sendDataLen: " +offsize);
		iRet = m_usbBase.sendData(DataBuffer,DataBuffer.length, ConStant.CMD_TIMEOUT);
		if(iRet < 0)
		{
			return -1;
		}
		return 0;
	}
	
	/**
	 * 功	能：	接收数据包
	 * 参	数：	bResult     - 返回码
	 * 				bRecvBuf 	- 	待接收数据缓存，缓存大小：64字节
	 * 				iTimeOut	-	超时时间，单位：毫秒
	 * 返	回：  0	-	成功，其他	-	失败
	 * */
	private int recvPacket(byte[] bResult,byte[] bRecvBuf,int[] iRecvLen,int iTimeOut){
		int iRet    = -1;
		int offsize = 0;
		int iDataLen = 0;
		int a=0,b=0;
		byte[] DataBuffer = new byte[ConStant.CMD_DATA_BUF_SIZE];
		byte[] SRN = new byte[2];
		short	recvCheckSum    = 0;  //收到的校验和
		short	currentCheckSum = 0;  //当前数据计算出的校验和
		
		iRet = m_usbBase.recvData(DataBuffer,DataBuffer.length,iTimeOut);
		
		//SendMsg("recvPacket recvData: " + zzStringTrans.hex2str(DataBuffer));
		//SendMsg("recvPacket recvLen: " +iRet);
		if (iRet < 0) {
			return iRet;
		}
		//1字节开始标识 0xAA
		if (DataBuffer[offsize++] != ConStant.CMD_RET_FLAG)
		{
			return ConStant.ERRCODE_CRC;
		}
		//2字节SRN
		SRN[0] = DataBuffer[offsize++];
		SRN[1] = DataBuffer[offsize++];
		//2字节长度
		//原因：byte的取值范围-128~127
		a = (int)DataBuffer[offsize++];
		if(a<0)
		{
			a = a+256;
		}
		b = (int)DataBuffer[offsize++];
		if(b<0)
		{
			b = b+256;
		}
		iDataLen = b*256+a; 
		if (iDataLen> ConStant.CMD_DATA_BUF_SIZE-5) {
			return ConStant.ERRCODE_CRC;
		}
		//1字节包执行结果
		bResult[0] = DataBuffer[offsize];
		//数据(根据长度-1得到实际长度)
		
		//SendMsg("offsize: " + offsize);
		//SendMsg("iDataLen: " +iDataLen);
		if ((iDataLen-1)>0)
		{
			for (int i = 1; i < iDataLen; i++) {
				bRecvBuf[i-1] = DataBuffer[offsize + i];
			}	
		}
		iRecvLen[0] =iDataLen-1;
		offsize = offsize  + iDataLen;
		//计算数据包的校验和
		for (int i=3; i<offsize; i++)
		{
			a = (int)DataBuffer[i];
			if(a<0)
			{
				a = a+256;
			}
			currentCheckSum = (short) (currentCheckSum+a);
		}
		
		//2字节校验和
		a = (int)DataBuffer[offsize++];
		if(a<0)
		{
			a = a+256;
		}
		b = (int)DataBuffer[offsize++];
		if(b<0)
		{
			b = b+256;
		}
		recvCheckSum = (short) (b*256+a); 
		//SendMsg(SHOW_MSG,"："+a+",b:"+b);
		//SendMsg(SHOW_MSG,"currentCheckSum："+currentCheckSum+",recvCheckSum:"+recvCheckSum);
		if (currentCheckSum != recvCheckSum)
		{
			return ConStant.ERRCODE_CRC;
		}
		return ConStant.ERRCODE_SUCCESS;
	}
	
	/**
	 * 功	能：	证件照解码
	 * 参	数：wlt     - 输入，解码前数据，1024字节
	 * 			bmp		- 输入，解码后数据，38862字节
	 * 返	回：  0	-	成功，其他	-	失败
	 * */
	public int Wlt2Bmp(byte[] wlt, byte[] bmp) {
		if(bmp.length<mPhotoSize)
			return ConStant.ERRCODE_MEMORY_OVER;
		JniCall.Huaxu_Wlt2Bmp(wlt, bmp, 0);
		return 0;
	}
	
	/**
	* 功  能: 对数据块进行base64编码
	* 输  入: 
	*  pInput - 输入，编码前数据块
	*  inputLen -  输入，输入数据块（pInput）长度
	*  pOutput - 输出，base64编码后数据块，大小为输入数据的4/3倍，
	*      输出数据块pInput 和输入数据块pOutput 起始地址可以相同
	*  outputbufsize- 输入,存放编码后数据（pOutput）的缓冲区大小
	* 返  回: 
	*     0:用于存放编码后数据的缓冲区不够，编码失败。
	*     大于0：编码后数据长度，值为(inputLen+2)/3*4
	*/
	public int Base64Encode(byte[] pInput, int inputLen, byte[] pOutput,int outputbufsize){
		return zzJavaBase64.JavaBase64Encode(pInput,inputLen,pOutput,outputbufsize);
	}
	
}
