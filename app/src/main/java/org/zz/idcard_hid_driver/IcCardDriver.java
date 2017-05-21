package org.zz.idcard_hid_driver;

import java.util.Calendar;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

/**
 * @author  chen.gs
 * @version V1.0.1.20170122 M1卡驱动库：实现激活、认证、读写块等四个接口。
 * @see    
 *     
 * */
public class IcCardDriver{
	
	private UsbBase m_usbBase;
	private Handler m_fHandler        = null;
	public static byte  CMD_ICCARD_COMMAND   = (byte)0xB0; //接触式IC卡控制指令
	public static byte  CMD_IDCARD_COMMAND	 = (byte)0xB1; //ID卡控制指令
	public static byte  CMD_ID64CARD_COMMAND = (byte)0xB2;  //ID卡控制指令
	public static byte  CONTACT_CARD		 = (byte)0;
	public static byte  CONTACT_LESS_CARD	 = (byte)1;
	public static byte  CONTACT_64CARD		 = (byte)2;
	
	public static short  CMD_U_IC_CARD_AVTIVE	 = (short)0x3241;
	public static short  CMD_U_IC_CARD_S_VERIFY	 = (short)0x3242;	
	public static short  CMD_U_IC_CARD_S_READ	 = (short)0x3243;
	public static short  CMD_U_IC_CARD_S_WRITE	 = (short)0x3244;
	
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
	public IcCardDriver(Context context){
		m_usbBase = new UsbBase(context);

	}
	
	public IcCardDriver(Context context, Handler bioHandler){
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
		String strVersion = "MIAXIS IcCard Driver V1.0.1.20170122";
		return strVersion;
	}
		
	/**
	 * @author   chen.gs
	 * @category 激活非接触式存储卡
	 * @param    delaytime - 超时时间，单位：毫秒
	 * 			 ATR       - 卡类型+卡UID
	 * 			 ATRLen    - 长度
	 * @return   0 - 成功，其他 - 失败
	 * */
	public int ContactLessStorageCardActive(short delaytime,byte[] ATR,short[] ATRLen)
	{
		int lRV = ConStant.ERRCODE_SUCCESS;
		byte[] oPackDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN];
		short[] oPackLen = new short[1];
		oPackLen[0] = (short)oPackDataBuffer.length;
		byte[] oRecvDataBuffer = new byte[ConStant.DATA_BUFFER_SIZE_MIN];
		short[] oRecvLen = new short[1];
		oRecvLen[0] = (short)oRecvDataBuffer.length;
		byte[] SendBufferData = new byte[ConStant.DATA_BUFFER_SIZE_MIN-7];
		short SendLen =0;
		short[] Status = new short[1];
		int offsize =0 ;
		int flag = 1;
		//组合输入的数据
		byte[] dtemp = new byte[2];
		//组合输入的数据
		dtemp[0] = (byte) ((byte)(delaytime/0x100)&0xFF);
		dtemp[1] = (byte) ((byte)delaytime&0xFF);
		SendBufferData[0]=dtemp[0];
		SendBufferData[1]=dtemp[1];

		offsize = offsize + 2;
		SendLen = (short) offsize;
		
		SendMsg("=============================");
		SendMsg("SendBufferData:"+zzStringTrans.hex2str(SendBufferData));
		SendMsg("SendLen:"+SendLen);
		
		lRV = SendICCardPack(CMD_U_IC_CARD_AVTIVE,SendBufferData,SendLen,oPackDataBuffer,oPackLen,flag);
		if (lRV != ConStant.ERRCODE_SUCCESS)
		{
			SendMsg("SendICCardPack failed,lRV="+lRV);
			return lRV;
		}
		
		SendMsg("=============================");
		SendMsg("oPackDataBuffer:"+zzStringTrans.hex2str(oPackDataBuffer));
		SendMsg("oPackLen:"+oPackLen[0]);
		
		lRV =zzICCardAPDU(CONTACT_LESS_CARD,oPackDataBuffer,oPackLen[0],100,oRecvDataBuffer,oRecvLen,100);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			SendMsg("zzICCardAPDU failed,lRV="+lRV);
			return lRV;
		}
		
		SendMsg("=============================");
		SendMsg("oRecvDataBuffer:"+zzStringTrans.hex2str(oRecvDataBuffer));
		SendMsg("oRecvLen:"+oRecvLen[0]);
		
		lRV = RecvICCardPack(oRecvDataBuffer,oRecvLen[0],Status,oPackDataBuffer,oPackLen,flag);
		if (lRV != ConStant.ERRCODE_SUCCESS)
		{
			SendMsg("RecvICCardPack failed,lRV="+lRV);
			return lRV;
		}
		
		SendMsg("=============================");	
		SendMsg("oPackDataBuffer:"+zzStringTrans.hex2str(oPackDataBuffer));
		SendMsg("oPackLen:"+oPackLen[0]);
		
		if (Status[0] !=0)
		{
			SendMsg("Status[0]="+Status[0]);
			return Status[0];
		}
		
		byte[] tmp = new byte[oPackLen[0]];
		for (int j = 0; j < tmp.length; j++) {
			tmp[j]=oPackDataBuffer[j];
		}
		SendMsg("oPackDataBuffer:"+zzStringTrans.hex2str(tmp));
		
		SendMsg("ATRLen[0]="+ATRLen[0]);
		SendMsg("oPackLen[0]="+oPackLen[0]);
		if (ATR!=null && ATRLen[0] >= oPackLen[0])
		{
			
			for(int i=0;i<oPackLen[0];i++)
			{
				ATR[i]=oPackDataBuffer[i];
			}
			ATRLen[0] = oPackLen[0];
			SendMsg("ATR:"+zzStringTrans.hex2str(ATR));
		}
		else
		{
			ATRLen[0] = 0;
			return ConStant.ERRCODE_MEMORY_OVER;
		}
		return ConStant.ERRCODE_SUCCESS;
	}

	/**
	 * @author   chen.gs
	 * @category 非接触式存储卡认证扇区
	 * @param    sectorNum - 扇区号： 50卡 0-15, 70卡 0-39
	 * 			 pintype   - 密钥类型
	 * 			 pin       - 密钥，6字节
	 * @return   0 - 成功，其他 - 失败
	 * */
	public int ContactLessCardVerify(byte sectorNum,byte pintype,byte[] pin)
	{
		int i = 0;
		int lRV = ConStant.ERRCODE_SUCCESS;
		byte[] oPackDataBuffer =  new byte[ConStant.DATA_BUFFER_SIZE_MIN];
		short[] oPackLen = new short[1];
		oPackLen[0] = (short)oPackDataBuffer.length;
		byte[] oRecvDataBuffer =  new byte[ConStant.DATA_BUFFER_SIZE_MIN];
		short[] oRecvLen = new short[1];
		oRecvLen[0] = (short)oRecvDataBuffer.length;
		byte[] SendBufferData =  new byte[ConStant.DATA_BUFFER_SIZE_MIN-7];
		short SendLen =0;
		short[] Status = new short[1];
		int offsize =0 ;
		int flag = 1;
		//组合输入的数据
		SendBufferData[offsize] = sectorNum;
		offsize = offsize + 1;
		SendBufferData[offsize] = pintype;
		offsize = offsize + 1;
		for (i = 0; i < 6; i++) {
			SendBufferData[offsize+i] = pin[i];
		}
		offsize = offsize + 6;
		SendLen = (short) offsize; 
		
		SendMsg("=============================");
		SendMsg("SendBufferData:"+zzStringTrans.hex2str(SendBufferData));
		SendMsg("SendLen:"+SendLen);
		
		lRV = SendICCardPack(CMD_U_IC_CARD_S_VERIFY,SendBufferData,SendLen,oPackDataBuffer,oPackLen,flag);
		if (lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		
		SendMsg("=============================");
		SendMsg("oPackDataBuffer:"+zzStringTrans.hex2str(oPackDataBuffer));
		SendMsg("oPackLen:"+oPackLen[0]);
		
		lRV =zzICCardAPDU(CONTACT_LESS_CARD,oPackDataBuffer,oPackLen[0],100,oRecvDataBuffer,oRecvLen,100);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		
		SendMsg("=============================");
		SendMsg("oRecvDataBuffer:"+zzStringTrans.hex2str(oRecvDataBuffer));
		SendMsg("oRecvLen:"+oRecvLen[0]);
		
		lRV =RecvICCardPack(oRecvDataBuffer,oRecvLen[0],Status,oPackDataBuffer,oPackLen,flag);
		if (lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		
		SendMsg("=============================");	
		SendMsg("oPackDataBuffer:"+zzStringTrans.hex2str(oPackDataBuffer));
		SendMsg("oPackLen:"+oPackLen[0]);
		
		if (Status[0] !=0)
		{
			return Status[0];
		}
		return ConStant.ERRCODE_SUCCESS;
	}
	
	/**
	 * @author   chen.gs
	 * @category 非接触式存储卡读块
	 * @param    blockNum  - 块号
	 * 			 block     - 块数据，16字节
	 * @return   0 - 成功，其他 - 失败
	 * */
	public int ContactLessCardReadBlock(byte blockNum,byte[] block)
	{
		int i = 0;
		int lRV = ConStant.ERRCODE_SUCCESS;
		byte[] oPackDataBuffer =  new byte[ConStant.DATA_BUFFER_SIZE_MIN];
		short[] oPackLen = new short[1];
		oPackLen[0] = (short)oPackDataBuffer.length;
		byte[] oRecvDataBuffer =  new byte[ConStant.DATA_BUFFER_SIZE_MIN];
		short[] oRecvLen = new short[1];
		oRecvLen[0] = (short)oRecvDataBuffer.length;
		byte[] SendBufferData =  new byte[ConStant.DATA_BUFFER_SIZE_MIN-7];
		short SendLen =0;
		short[] Status = new short[1];
		int offsize =0 ;
		int flag = 1;

		//组合输入的数据
		SendBufferData[offsize] = blockNum;
		offsize = offsize + 1;
		SendLen = (short) offsize; 
		
		SendMsg("=============================");
		SendMsg("SendBufferData:"+zzStringTrans.hex2str(SendBufferData));
		SendMsg("SendLen:"+SendLen);
		
		lRV = SendICCardPack(CMD_U_IC_CARD_S_READ,SendBufferData,SendLen,oPackDataBuffer,oPackLen,flag);
		if (lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		
		SendMsg("=============================");
		SendMsg("oPackDataBuffer:"+zzStringTrans.hex2str(oPackDataBuffer));
		SendMsg("oPackLen:"+oPackLen[0]);
		
		lRV = zzICCardAPDU(CONTACT_LESS_CARD,oPackDataBuffer,oPackLen[0],100,oRecvDataBuffer,oRecvLen,100);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		
		SendMsg("=============================");
		SendMsg("oRecvDataBuffer:"+zzStringTrans.hex2str(oRecvDataBuffer));
		SendMsg("oRecvLen:"+oRecvLen[0]);
		
		lRV = RecvICCardPack(oRecvDataBuffer,oRecvLen[0],Status,oPackDataBuffer,oPackLen,flag);
		if (lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		
		SendMsg("=============================");	
		SendMsg("oPackDataBuffer:"+zzStringTrans.hex2str(oPackDataBuffer));
		SendMsg("oPackLen:"+oPackLen[0]);
		
		if (Status[0] !=0)
		{
			return Status[0];
		}
		
		if (block != null && oPackLen[0]<=16)
		{
			for (i = 0; i < oPackLen[0]; i ++) {
				block[i]=oPackDataBuffer[i];
			}
		}
		
		return ConStant.ERRCODE_SUCCESS;
	}

	/**
	 * @author   chen.gs
	 * @category 非接触式存储卡写块
	 * @param    blockNum  - 块号
	 * 			 block     - 块数据，16字节
	 * @return   0 - 成功，其他 - 失败
	 * */
	public int ContactLessCardWriteBlock(byte blockNum,byte[] block)
	{
		int i   = 0;
		int lRV = ConStant.ERRCODE_SUCCESS;
		byte[] oPackDataBuffer =  new byte[ConStant.DATA_BUFFER_SIZE_MIN];
		short[] oPackLen = new short[1];
		oPackLen[0] = (short)oPackDataBuffer.length;
		byte[] oRecvDataBuffer =  new byte[ConStant.DATA_BUFFER_SIZE_MIN];
		short[] oRecvLen = new short[1];
		oRecvLen[0] = (short)oRecvDataBuffer.length;
		byte[] SendBufferData =  new byte[ConStant.DATA_BUFFER_SIZE_MIN-7];
		short SendLen =0;
		short[] Status = new short[1];
		int offsize =0 ;
		int flag = 1;
		
		//组合输入的数据
		SendBufferData[offsize] = blockNum;
		offsize = offsize + 1;
		for (i = 0; i < 16;i ++) {
			SendBufferData[offsize+i] = block[i];
		}
		offsize = offsize + 16;
		SendLen = (short) offsize; 
		
		SendMsg("=============================");
		SendMsg("SendBufferData:"+zzStringTrans.hex2str(SendBufferData));
		SendMsg("SendLen:"+SendLen);
		
		lRV = SendICCardPack(CMD_U_IC_CARD_S_WRITE,SendBufferData,SendLen,oPackDataBuffer,oPackLen,flag);
		if (lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		
		SendMsg("=============================");
		SendMsg("oPackDataBuffer:"+zzStringTrans.hex2str(oPackDataBuffer));
		SendMsg("oPackLen:"+oPackLen[0]);
		
		lRV =zzICCardAPDU(CONTACT_LESS_CARD,oPackDataBuffer,oPackLen[0],100,oRecvDataBuffer,oRecvLen,100);
		if(lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		
		SendMsg("=============================");
		SendMsg("oRecvDataBuffer:"+zzStringTrans.hex2str(oRecvDataBuffer));
		SendMsg("oRecvLen:"+oRecvLen[0]);
		
		lRV =RecvICCardPack(oRecvDataBuffer,oRecvLen[0],Status,oPackDataBuffer,oPackLen,flag);
		if (lRV != ConStant.ERRCODE_SUCCESS)
		{
			return lRV;
		}
		
		SendMsg("=============================");	
		SendMsg("oPackDataBuffer:"+zzStringTrans.hex2str(oPackDataBuffer));
		SendMsg("oPackLen:"+oPackLen[0]);
		
		if (Status[0] !=0)
		{
			return Status[0];
		}
		return ConStant.ERRCODE_SUCCESS;
	}

	/* 数据编码 */
	void EncData(byte[] lpRawData, int nRawLen, byte[] lpEncData)
	{
		int i = 0;
		for (i=0; i<nRawLen; i++)
		{
			int aaa = JUnsigned(lpRawData[i]);
			lpEncData[2*i]   = (byte) ((aaa>>4) + 0x30); 
			lpEncData[2*i+1] = (byte) ((aaa&0xF) + 0x30); 
		}
		lpEncData[2*nRawLen] = 0;
	}
	
	/* 数据解码 */
	void DecData(byte[] lpEncData, int nRawLen, byte[] lpRawData)
	{
		int i = 0;
		for (i=0; i<nRawLen; i++)
		{
			lpRawData[i] = (byte) (((lpEncData[2*i]-0x30)<<4) + (lpEncData[2*i+1]-0x30));
		}
	}

	int SendICCardPack(short CommandID ,byte[] SendDataBuffer,short SendLen,byte[] oPackDataBuffer,short[] oPackLen,int flag)
	{
		int i;
		byte[] bodyBufferData = new byte[ConStant.CMD_BUFSIZE*2];//实际上此处不能到64字节应该减去外层包装后的字节数 56字节
		byte[] tempBufferData = new byte[ConStant.CMD_BUFSIZE*2];
		byte[] DataEncode     = new byte[ConStant.CMD_BUFSIZE*2];
		byte packstx  = 0x02;  //头
		byte packetx  = 0x03;  //尾
		byte AddCheck = 0x00;  //异或和
		byte[] dtemp  = new byte[2];
		int offsize = 0;
		
		//数据包头 STX
		tempBufferData[offsize] = packstx;
		offsize = offsize + 1;
		
		//数据单元长度 2byte
		dtemp[0] = 0x00;
		dtemp[1] = 0x00;
		SendLen  = (short) (SendLen +2);
		dtemp[0] = (byte) ((byte)(SendLen/0x100)&0xFF);
		dtemp[1] = (byte) ((byte)SendLen&0xFF);
		for(i=0;i<dtemp.length;i++)
		{
			tempBufferData[offsize+i] = dtemp[i];
		}
		offsize = offsize +dtemp.length;
				
		//数据单元格式（类别+代码+不定长参数）
		dtemp[0] = 0x00;
		dtemp[1] = 0x00;
		dtemp[0] = (byte) ((byte)(CommandID/0x100)&0xFF);
		dtemp[1] = (byte) ((byte)CommandID&0xFF);
		for(i=0;i<dtemp.length;i++)
		{
			tempBufferData[offsize+i] = dtemp[i];
		}
		offsize = offsize +dtemp.length;
				
		//拷贝数据
		for(i=0;i<(SendLen-2);i++)
		{
			tempBufferData[offsize+i] = SendDataBuffer[i];
		}
		offsize = offsize + SendLen-2;
		
		//异或和 冗余校验LRC
		for (i=0; i<SendLen; i++)
		{
			AddCheck ^= tempBufferData[i+3];
		}
		tempBufferData[offsize] = AddCheck;
		offsize = offsize + 1;

		//数据包尾 ETX
		tempBufferData[offsize] = packetx;
		offsize = offsize + 1;
		
//		byte[] tmp = new byte[offsize];
//		for (int j = 0; j < tmp.length; j++) {
//			tmp[j]=tempBufferData[j];
//		}
//		SendMsg("==tempBufferData:"+zzStringTrans.hex2str(tmp));
		//////////////////////////////////////////////////////////////////////////
		//是否拆分
		if (flag==1)
		{
			int bodylen = offsize-2;
			for (i=0; i<bodylen; i++)
			{
				bodyBufferData[i]=tempBufferData[i+1];
			}
			EncData(bodyBufferData, bodylen,DataEncode);
			//重新组包
			offsize =0;
			//数据包头 STX
			tempBufferData[offsize] = packstx;
			offsize = offsize + 1;
			//编码后数据长度为2倍
			for (i=0; i<bodylen*2; i++)
			{
				tempBufferData[offsize+i]=DataEncode[i];
			}
			offsize = offsize + bodylen*2;
			//数据包尾 ETX
			tempBufferData[offsize] = packetx;
			offsize = offsize + 1;
		}
		
		if (oPackDataBuffer != null && oPackLen[0]>=offsize ){
			for (i=0; i<offsize; i++)
			{
				oPackDataBuffer[i]=tempBufferData[i];
			}
			oPackLen[0]  = (short) offsize;
		}else{
			return ConStant.ERRCODE_MEMORY_OVER;
		}	
		return ConStant.ERRCODE_SUCCESS;
	} 
	
	//////////////////////////////////////////////////////////////////////////
	int RecvICCardPack(byte[] RecvDataBuffer,short RecvLen,short[] Status,byte[] oPackDataBuffer,short[] oPackLen,int flag)
	{
		int i = 0;
		int a,b;
		byte[] bodyBufferData = new byte[ConStant.CMD_BUFSIZE];
		byte[] tempBufferData = new byte[ConStant.CMD_BUFSIZE];
		byte[] DecDataBuffer = new byte[ConStant.CMD_BUFSIZE];
		
		byte packstx  = 0x00; //空头
		byte packetx  = 0x00; //空尾
		byte recvCheck =0x00;    //收到的数据校验和
		byte currentCheck =0x00; //根据收到的数据计算的当前校验和
		byte[] dtemp = new byte[2];
		int offsize  = 0;
		short len    = 0;
		int bodylen  = RecvLen-2; //收到的包体的大小
		//////////////////////////////////////////////////////////////////////////
		//数据包头 STX
		packstx = RecvDataBuffer[0];
		if (packstx !=0x02)
		{
			return ConStant.ERRCODE_CRC; //校验头错误
		}
		offsize = offsize + 1;
		
		packetx = RecvDataBuffer[RecvLen-1];
		if (packetx !=0x03)
		{
			return ConStant.ERRCODE_CRC; //校验尾错误
		}
		
		for (i=0; i<bodylen; i++)
		{
			bodyBufferData[i]=RecvDataBuffer[i+offsize];
		}

		if (flag==1)
		{	
			byte[] tmp = new byte[bodylen];
			for (int j = 0; j < tmp.length; j++) {
				tmp[j]=bodyBufferData[j];
			}
			SendMsg("bodyBufferData:"+zzStringTrans.hex2str(tmp));

			DecData(bodyBufferData,bodylen,DecDataBuffer);
			for (i=0; i<bodylen; i++)
			{
				bodyBufferData[i]=0x00;
			}
			
			for (i=0; i<bodylen/2; i++)
			{
				bodyBufferData[i]=DecDataBuffer[i];
			}
			
			for (int j = 0; j < tmp.length; j++) {
				tmp[j]=bodyBufferData[j];
			}
			SendMsg("DecDataBuffer:"+zzStringTrans.hex2str(tmp));

		}
	
		//数据单元长度 2byte
		offsize =0 ;
		dtemp[0] = bodyBufferData[1];
		dtemp[1] = bodyBufferData[0];
		offsize = offsize + 2;
	
		a = (int)dtemp[0];
		if(a<0)
		{
			a = a+256;
		}
		b = (int)dtemp[1];
		if(b<0)
		{
			b = b+256;
		}
		len = (short) (b*256+a); 
		SendMsg("len:"+len);
		//应答单元格式（状态码高 +状态码低 + 不定长数据）	
		for(i=0;i<len;i++)
		{
			tempBufferData[i]=bodyBufferData[i+offsize];
		}
		offsize = offsize + len;
	
		//冗余校验LRC
		recvCheck=bodyBufferData[offsize];
		offsize = offsize +1;
	
		//异或和 冗余校验LRC
		for (i=0; i<len; i++)
		{
			currentCheck ^= tempBufferData[i];
		}
	
		if (currentCheck !=recvCheck)
		{
			return ConStant.ERRCODE_CRC; //校验和验证错误
		}

		//dtemp[0] = tempBufferData[1];
		//dtemp[1] = tempBufferData[0];
		//memcpy(Status,dtemp,2);
		dtemp[0] = tempBufferData[1];
		dtemp[1] = tempBufferData[0];
		a = (int)dtemp[0];
		if(a<0)
		{
			a = a+256;
		}
		b = (int)dtemp[1];
		if(b<0)
		{
			b = b+256;
		}
		Status[0] = (short) (b*256+a); 
		SendMsg("Status[0]:"+Status[0]);
		for (i=0; i<len-2; i++)
		{
			oPackDataBuffer[i] = tempBufferData[i+2];
		}
		oPackLen[0] = (short) (len-2);
		return ConStant.ERRCODE_SUCCESS;
	}
	
	//////////////////////////////////////////////////////////////////////////
	int zzICCardAPDU(byte cardtype,byte[] lpSendData,short wSendLength,int iSendTime,byte[] lpRecvData,short[] io_wRecvLength,int iRecvTime)
	{
		//调用ExeCommand将组好包的数据发送到设备
		int ret = ConStant.ERRCODE_SUCCESS;
		byte nCommandID =CMD_ICCARD_COMMAND;	
		if (cardtype == CONTACT_LESS_CARD)
		{
			nCommandID = CMD_IDCARD_COMMAND; //非接触式卡
		}
		else if (cardtype ==CONTACT_CARD )
		{
			nCommandID = CMD_ICCARD_COMMAND; //接触式卡
		}
		else if (cardtype == CONTACT_64CARD)
		{	
			nCommandID = CMD_ID64CARD_COMMAND; //接触64式卡
		}
		else
		{
			return ConStant.ERRCODE_CRC;
		}	
		ret = ExeCommand(nCommandID,lpSendData,wSendLength,iSendTime,lpRecvData,io_wRecvLength,iRecvTime);
		return ret;
	}	
	

	int ExeCommand(byte nCommandID, 
			byte[] lpSendData,short wSendLength,int iSendTime,
			byte[] lpRecvData,short[] io_wRecvLength,int iRecvTime)
	{
		byte[] outBuffer = new byte[ConStant.CMD_BUFSIZE];
		byte[] buf= new byte[ConStant.DATA_BUFFER_SIZE+1];
		byte[] nRetCode = new byte[1];
		short[] wRecvLen = new short[1];
		wRecvLen[0] = (short) buf.length;
		int realsize = 0;
		int packsize = ConStant.DATA_BUFFER_SIZE_MIN;
		//打开设备
		int iRet = ConStant.ERRCODE_SUCCESS;
		//打开设备
		iRet = m_usbBase.openDev(ConStant.VID, ConStant.PID);
		if(iRet != 0){
			SendMsg("openDev failed,iRet="+iRet);
			return ConStant.ERRCODE_NODEVICE;
		}

		//组包发送数据
		int packnum      = wSendLength/packsize;
		int lastpacksize = wSendLength % packsize;
		if (lastpacksize !=0)
		{
			packnum = packnum+1;
		}

		for (int i =0 ; i < packnum ;i++)
		{
			for (int j = 0; j < buf.length; j++) {
				buf[j]=0x00;
			}
			if (i == (packnum -1)) //最后一包
			{
				for (int j = 0; j < lastpacksize; j++) {
					buf[j]=lpSendData[j+i*packsize];
				}
				//发送数据包
				iRet = sendPacket(nCommandID,buf,lastpacksize);
			}
			else
			{	
				for (int j = 0; j < packsize; j++) {
					buf[j]=lpSendData[j+i*packsize];
				}
				//发送数据包
				iRet = sendPacket(nCommandID,buf,packsize);
			}
			if (iRet != 0) {
				SendMsg("sendPacket failed,iRet="+iRet);
				//关闭设备
				m_usbBase.closeDev();
				return ConStant.ERRCODE_IOSEND;
			}	
		}

		//收包解析数据
		long  duration = -1;
		int timeout = 5000;//防止USB假死程序不退出的超时
		//一直收到数据超时
		Calendar time1 = Calendar.getInstance();
		if (iRecvTime < 2000)
		{
			iRecvTime = 2000;//如果外部传递的超时时间过短则修改为2000
		}
		timeout = iRecvTime; //以外部超时时间为准	
		while (true)
		{		
			if (duration>=timeout)
			{
				//关闭设备
				m_usbBase.closeDev();
				return ConStant.ERRCODE_TIMEOUT;
			}
			//接受数据包，一直等到收包超时标识数据收完 
			iRet = recvPacket(nRetCode,buf,wRecvLen,iRecvTime);
			if (iRet == ConStant.ERRCODE_SUCCESS)
			{
				iRecvTime = 50;//当收到第一个包成功的时候 将第2包的接收超时设置为100毫秒
			}
			if (iRet == ConStant.ERRCODE_TIMEOUT )
			{		
				break;
			}
			SendMsg("===wRecvLen:"+wRecvLen[0]);
			if(wRecvLen[0]>0)
			{
				byte[] tmp = new byte[wRecvLen[0]];
				for (int j = 0; j < tmp.length; j++) {
					tmp[j]=buf[j];
				}
				SendMsg("tmp:"+zzStringTrans.hex2str(tmp));	
			}
			SendMsg("buf:"+zzStringTrans.hex2str(buf));			
			if (iRet != ConStant.ERRCODE_SUCCESS)
			{
				if(wRecvLen[0]>0)
					break;
				m_usbBase.closeDev();
				return ConStant.ERRCODE_CRC;
			}
			if (wRecvLen[0]>=2)
			{
				for (int k = 0; k < wRecvLen[0]-2; k++) {
					outBuffer[k+realsize]=buf[k+2];
				}
				realsize = realsize + wRecvLen[0]-2;//实际收到的数据
			}else
			{
				realsize = realsize;
			}
			Calendar time2 = Calendar.getInstance();
			duration = time2.getTimeInMillis() - time1.getTimeInMillis();
		}
		SendMsg("io_wRecvLength[0]:"+io_wRecvLength[0]);	
		SendMsg("realsize:"+realsize);	
		if (io_wRecvLength[0] > 0 && realsize <= io_wRecvLength[0])
		{
			if (realsize > 0)
			{
				for (int k = 0; k < realsize; k++) {
					lpRecvData[k]=outBuffer[k];
				}				
			}
			io_wRecvLength[0] = (short) realsize;
		}
		else
		{
			m_usbBase.closeDev();
			return ConStant.ERRCODE_MEMORY_OVER;
		}

		//关闭设备
		m_usbBase.closeDev();
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
	private int recvPacket(byte[] bResult,byte[] bRecvBuf,short[] iRecvLen,int iTimeOut){
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
		iRecvLen[0] =(short) (iDataLen-1);
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
	
	  public static int JUnsigned(int x)
	  {
	    if (x>=0)
	      return x;
	    else
	      return (x+256);
	  }
}
