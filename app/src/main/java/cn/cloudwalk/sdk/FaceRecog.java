package cn.cloudwalk.sdk;



public class FaceRecog {

	static FaceRecog recog = null;
	
	public FaceRecog() {
		loadLibrarys();
	}

	public static FaceRecog getInstance() {

		if (null == recog) {
			recog = new FaceRecog();
		}
		return recog;
	}

	private static void loadLibrary(String libraryName) {
		System.loadLibrary(libraryName);
	}

	private static void loadLibrarys() {
		loadLibrary("CWFaceIDCardDet");
		loadLibrary("CloudWalkRecog");
		loadLibrary("CWFaceSDK");
		loadLibrary("CWFaceSDKJni");
	}


	static public native int cwCreateRecogHandle(String pConfigurePath, 
			                                     String pLicence,
											     int emRecogPattern, 
											     int iGpuId);

	static public native int cwReleaseRecogHandle(int pRecogHandle);

	static public native int cwGetFeatureLength(int pRecogHandle);
	
	static public native int cwGetFiledFeature(int pRecogHandle, byte[] dataAlign, int iWidth, int iHeight, int iChannels, int iAlignedFaceNum, 
			                                   byte[] pFeatureData, int nFeatureLength);
	
	static public native int cwGetProbeFeature(int pRecogHandle, byte[] dataAlign, int iWidth, int iHheight, int iChannels, int iAlignedFaceNum, 
                                               byte[] pFeatureData, int nFeatureLength);

	static public native int cwConvertFiledFeatureToProbeFeature(int pRecogHandle, byte[] pFeaFiled, int iFeaFiledDim, int iFeaFiledNum, byte[] pFeaProbe);
	
	static public native int cwComputeMatchScore(int pRecogHandle, byte[] pFeaProbe, int iFeaProbeDim, int iFeaProbeNum, 
			                                     byte[] pFeaFiled, int iFeaFiledDim, int iFeaFiledNum, float[] pScores);

}
