package org.zz.idcard_hid_driver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BMP {
	
	public static int JUnsigned(int x)
	{
		if (x>=0)
			return x;
		else
			return (x+256);
	}
	
	/**
	 * @author   chen.gs
	 * @category ISO格式指纹图像数据->Bitmap
	 * @param    imgIsoBuf - ISO格式指纹图像数据
	 * @return   Bitmap    - 成功
	 *           null      - 失败  	
	 * */
	public static Bitmap Iso2Bimap(byte[] imgIsoBuf){
		if ((imgIsoBuf[0]!='F')||(imgIsoBuf[1]!='I')||(imgIsoBuf[2]!='R'))
		{
			return null;
		}	
		int iImgX = JUnsigned(imgIsoBuf[32+9]<<8)+JUnsigned(imgIsoBuf[32+10]);
		int iImgY = JUnsigned(imgIsoBuf[32+11]<<8)+JUnsigned(imgIsoBuf[32+12]);
		if (iImgX < 0 || iImgX>1000) {
			return null;
		}
		if (iImgY < 0 || iImgY>1000) {
			return null;
		}
		byte[] imgBuf = new byte[iImgX*iImgY];
		System.arraycopy(imgIsoBuf,46,imgBuf,0,iImgX*iImgY);
        return Raw2Bimap(imgBuf,iImgX,iImgY);
	}
	
	/**
	 * @author   chen.gs
	 * @category 指纹图像原始数据->Bitmap
	 * @param    imgBuf - 指纹图像原始数据
	 * 			 iImgX  - 指纹图像宽度
	 * 			 iImgY	- 指纹图像高度
	 * @return   Bitmap - 成功
	 *           null   - 失败  	
	 * */
	public static Bitmap Raw2Bimap(byte[] imgBuf, int iImgX, int iImgY){
		byte[] bmpBuf = new byte[iImgX*iImgY+1078];
		Raw2Bmp(bmpBuf,imgBuf,iImgX,iImgY);
        return BitmapFactory.decodeByteArray(bmpBuf, 0, bmpBuf.length);
	}
	
	/**
	 * @author   chen.gs
	 * @category 指纹图像原始数据-> BMP格式指纹图像数据
	 * @param    pBmp   - BMP格式指纹图像数据
	 * 			 pRaw   - 指纹图像原始数据
	 * 			 iImgX  - 指纹图像宽度
	 * 			 iImgY	- 指纹图像高度
	 * @return     0    - 成功
	 *            其他            - 失败  	
	 * */
	public static int Raw2Bmp(byte[] pBmp, byte[] pRaw, int X, int Y) {
		int num;
		int i, j;
		byte[] head = new byte[1078];

		byte[] temp = { 0x42, 0x4d, // file header
				0x0, 0x00, 0x0, 0x00, // file size***
				0x00, 0x00, // reserved
				0x00, 0x00,// reserved
				0x36, 0x4, 0x00, 0x00,// head byte***
				0x28, 0x00, 0x00, 0x00,// struct size
				0x00, 0x00, 0x00, 0x00,// map width***
				0x00, 0x00, 0x00, 0x00,// map height***
				0x01, 0x00,// must be 1
				0x08, 0x00,// color count***
				0x00, 0x00, 0x00, 0x00, // compression
				0x00, 0x00, 0x00, 0x00,// data size***
				0x00, 0x00, 0x00, 0x00, // dpix
				0x00, 0x00, 0x00, 0x00, // dpiy
				0x00, 0x00, 0x00, 0x00,// color used
				0x00, 0x00, 0x00, 0x00,// color important
		};
		System.arraycopy(temp, 0, head, 0, temp.length);
		// 确定图象宽度数值
		num = X;
		head[18] = (byte) (num & 0xFF);
		num = num >> 8;
		head[19] = (byte) (num & 0xFF);
		num = num >> 8;
		head[20] = (byte) (num & 0xFF);
		num = num >> 8;
		head[21] = (byte) (num & 0xFF);
		// 确定图象高度数值
		num = Y;
		head[22] = (byte) (num & 0xFF);
		num = num >> 8;
		head[23] = (byte) (num & 0xFF);
		num = num >> 8;
		head[24] = (byte) (num & 0xFF);
		num = num >> 8;
		head[25] = (byte) (num & 0xFF);
		// 确定调色板数值
		j = 0;
		for (i = 54; i < 1078; i = i + 4) {
			head[i] = head[i + 1] = head[i + 2] = (byte) j;
			head[i + 3] = 0;
			j++;
		}
		// 写入文件头
		System.arraycopy(head, 0, pBmp, 0, 1078);
		// 写入图象数据
		for (i = 0; i < Y; i++) {
			System.arraycopy(pRaw, i * X, pBmp, 1078 + (Y - 1 - i) * X, X);
		}
		return 0;
	}
	
	/**
	 * @author   chen.gs
	 * @category 保存数据为文件
	 * @param    filepath - 文件路径
	 * 			 buffer   - 数据缓存
	 * 			 size     - 数据长度
	 * @return     0    - 成功
	 *            其他            - 失败  	
	 * */
	public static int SaveData(String filepath, byte[] buffer, int size) {
		File f = new File(filepath);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		try {
			fos.write(buffer, 0, size);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -2;
		}
		return 0;
	}
	
	/**
	 * @author   chen.gs
	 * @category 保存BMP图像
	 * @param    strFileName - BMP文件路径
	 *           ucImgBuf    - 指向指纹图象缓冲的指针
	 *           iWidth      - 图像宽度
	 *           iHeight     - 图像高度 
	 * @return    0 - 成功
	 *           其他  - 失败  
	 * */
	public int SaveBMP(String strFileName, byte[] ucImgBuf, int iWidth, int iHeight)
	{
		byte[] bmpimagebuff = new byte[iWidth*iHeight + 1078];
		Raw2Bmp(bmpimagebuff, ucImgBuf, iWidth, iHeight);
		return SaveData(strFileName, bmpimagebuff, bmpimagebuff.length);
	}
	
	/**
	 * @author   chen.gs
	 * @category 保存ISO图像
	 * @param    strFileName     - 保存图像路径
	 *           bIsoFingerImage - 图像数据缓存
	 * @return   0-成功，其他-失败
	 * */
	public int SaveIsoImg(String strFileName, byte[] bIsoFingerImage) {
		if ((bIsoFingerImage[0]!='F')||(bIsoFingerImage[1]!='I')||(bIsoFingerImage[2]!='R'))
		{
			return -1;
		}	
		int iWidth  = JUnsigned(bIsoFingerImage[32+9]<<8)+JUnsigned(bIsoFingerImage[32+10]);
		int iHeight = JUnsigned(bIsoFingerImage[32+11]<<8)+JUnsigned(bIsoFingerImage[32+12]);
		if (iWidth < 0 || iWidth>1000) {
			return -2;
		}
		if (iHeight < 0 || iHeight>1000) {
			return -3;
		}
		byte[] imageBuf = new byte[iWidth*iHeight];
		System.arraycopy(bIsoFingerImage,46,imageBuf,0,iWidth*iHeight);
		return SaveBMP(strFileName,imageBuf,iWidth,iHeight);
	}
}
