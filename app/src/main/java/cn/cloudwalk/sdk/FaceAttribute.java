package cn.cloudwalk.sdk;



public class FaceAttribute {

	static FaceAttribute attri = null;
	
	public FaceAttribute() {
		loadLibrarys();
	}

	public static FaceAttribute getInstance() {

		if (null == attri) {
			attri = new FaceAttribute();
		}
		return attri;
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


	static public native int cwCreateAttributeHandle(String pConfigurePath, String pLicence, int iGpuId);

	static public native int cwReleaseAttributeHandle(int pAttributeHandle);
	
	static public native int cwGetAgeGenderEval(int pAttributeHandle, byte[] dataAlign, int iWidth, int iHeight, int iChannels, 
			                                    FaceAttrRet attr);

}
