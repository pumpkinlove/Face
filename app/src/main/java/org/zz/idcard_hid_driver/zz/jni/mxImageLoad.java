package org.zz.idcard_hid_driver.zz.jni;

public class mxImageLoad {

    static {
        System.loadLibrary("mxImageLoad");
    }

    /*******************************************************************************************
     功	能：	图像文件加载到内存
     参	数：	szLoadFilePath		- 输入	图像路径
     pRGBBuffer          - 输出	外部图像缓冲区,用于接收RGB图像数据，如果为NULL，则不接收
     pRawBuffer			- 输出	外部图像缓冲区,用于接收灰度图像数据，如果为NULL，则不接收
     oX					- 输出	图像宽度
     oY					- 输出	图像高度
     返	回：	1-成功，其他-失败
     *******************************************************************************************/
    public native  int LoadFaceImage(String szLoadFilePath,
                                     byte[] pRGBBuffer,
                                     byte[] pGrayBuffer,
                                     int[] oX,
                                     int[] oY);

    /*******************************************************************************************
     功	能：	保存图像数据
     参	数：	szSaveFilePath		- 输入	保存图像路径
     pRawBuffer			- 输入	图像缓冲区
     iX					- 输入	图像宽度
     iY					- 输入	图像高度
     iChannels           - 输入  图像通道
     返	回：	1-成功，其他-失败
     *******************************************************************************************/
    public native  int SaveFaceImage(String szSaveFilePath, byte[] pImgBuf, int iX, int iY, int iChannels);


    /*******************************************************************************************
     功	能：	在输入的图像上根据输入的Rect绘制矩形框
     参	数：	szFilePath		- 输入	原始图像路径
     iRect			- 输入	Rect[0]	=x;
     Rect[1]	=y;
     Rect[2]	=width;
     Rect[3]	=height;
     szOutFilePath	- 输入	绘制矩形框之后的图像路径
     返	回：	1-成功，其他-失败
     *******************************************************************************************/
    public native  int  FaceDrawRect(String szFilePath, int[] iRect, String szOutFilePath);

    /*******************************************************************************************
     功	能：	在输入的图像上根据输入的点坐标绘制点
     参	数：	szFilePath		- 输入	原始图像路径
     iPointPos		- 输入	点坐标序列（x1,y1,x2,y2,...）
     iPointNum       - 输入  点个数
     szOutFilePath	- 输入	绘制点之后的图像路径
     返	回：	1-成功，其他-失败
     *******************************************************************************************/
    public native  int FaceDrawPoint(String szFilePath, int[]  iPointPos, int iPointNum, String szOutFilePath);

    /*******************************************************************************************
     功	能：	在输入的图像上根据输入的点坐标绘制点序号
     参	数：	szFilePath		- 输入	原始图像路径
     iPointPos		- 输入	点坐标序列（x1,y1,x2,y2,...）
     iPointNum       - 输入  点个数
     szOutFilePath	- 输入	绘制点序号之后的图像路径
     返	回：	1-成功，其他-失败
     *******************************************************************************************/
    public native  int FaceDrawText(String szFilePath, int[] iPointPos, int iPointNum, String szOutFilePath);

}
