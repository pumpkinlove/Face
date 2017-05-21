package org.zz.faceapi;

public class DataDecode {
	/** 
	*：YUV420SP解码的方式 
	*yuv420sp[]为原始的数据 
	*width为图片位图长，height为图片位图高 
	*rgbBuf[]是用于存贮原始数据经过解码后的r g b三元色数据 
	*/  
	 public static void decodeYUV420SP(byte[] rawBuf,byte[] yuv420sp,int width,int height) {     
	              //定义单通道数据长度  
	        final int frameSize = width * height;     
	             //如果传进来的rgbBuf 为空，则抛出空指针异常  
	    if (rawBuf == null)     
	        throw new NullPointerException("buffer 'rgbBuf' is null");      
	       //如果传进来的rgbBuf 为比三通道数据长度小，则抛出异常,并打出相应信息  
	    if (rawBuf.length < frameSize )     
	        throw new IllegalArgumentException("buffer 'rgbBuf' size "    
	                 + rawBuf.length + " < minimum " + frameSize);     
	       //如果传进来的yuv420sp 为空，则抛出空指针异常  
	    if (yuv420sp == null)     
	        throw new NullPointerException("buffer 'yuv420sp' is null");     
	      //如果传进来的rgbBuf 为比三通道数据长度的一半小，则抛出异常,并打出相应信息  
	    if (yuv420sp.length < frameSize * 3 / 2)     
	        throw new IllegalArgumentException("buffer 'yuv420sp' size " 
	                    + yuv420sp.length+ " < minimum " + frameSize * 3 / 2);     
	           //经过上面的叛断，我们正式进行解码了  
	        int i = 0, y = 0;     
	        int uvp = 0, u = 0, v = 0;    
	               //r g b 三元色初始化   
	        int y1192 = 0, r = 0, g = 0, b = 0; 
	        int Gray;
	             //下面的两个for循环都只是为了把第一个像素点的的R G B读取出来，就是一行一行循环读取.  
	        for (int j = 0, yp = 0; j < height; j++) {     
	             uvp = frameSize + (j >> 1) * width;     
	             u = 0;     
	             v = 0;     
	            for (i = 0; i < width; i++, yp++) {     
	                 y = (0xff & ((int) yuv420sp[yp])) - 16;     
	                if (y < 0) y = 0;     
	                if ((i & 1) == 0) {     
	                     v = (0xff & yuv420sp[uvp++]) - 128;     
	                     u = (0xff & yuv420sp[uvp++]) - 128;     
	                 }     
	                     
	                 y1192 = 1192 * y;     
	                 r = (y1192 + 1634 * v);     
	                 g = (y1192 - 833 * v - 400 * u);     
	                 b = (y1192 + 2066 * u);     
	                   //始终持 r g b在0 - 262143  
	                if (r < 0) r = 0; else if (r > 262143) r = 262143;     
	                if (g < 0) g = 0; else if (g > 262143) g = 262143;     
	                if (b < 0) b = 0; else if (b > 262143) b = 262143;     
	                   //安位运算，分别将一个像素点中的r g b 存贮在rgbBuf中  
	                 //rgbBuf[yp * 3]    = (byte)(r >> 10);     
	                 //rgbBuf[yp * 3 + 1] = (byte)(g >> 10);     
	                 //rgbBuf[yp * 3 + 2] = (byte)(b >> 10); 
	                Gray = ((r >> 10)*19595 + (g >> 10)*38469 + (b >> 10)*7472)>>16;
	                rawBuf[yp] = (byte) Gray;
	             }     
	         }     
	       } 
	
	/*
	================================================================================
	函数:	ImrotateLevel
	功能:	水平镜像
	================================================================================
	*/
	 static void ImrotateLevel_raw(byte[] rawBuf, int imgX, int imgY)
	{
	    int i,j,k;
		byte tmp;
	    for (i=0; i<imgY; i++)
	    {
			for (j=0; j<(imgX>>1); j++)
			{
		
					tmp = rawBuf[i*imgX+j];
					rawBuf[i*imgX+j] = rawBuf[i*imgX+(imgX-1-j)];
					rawBuf[i*imgX+(imgX-1-j)] = tmp;			
			}
	    }
	}

}
