package cn.cloudwalk.sdk;



public class FaceNationality {

	static FaceNationality nati = null;
	
	public FaceNationality() {
		loadLibrarys();
	}

	public static FaceNationality getInstance() {

		if (null == nati) {
			nati = new FaceNationality();
		}
		return nati;
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


	static public native int cwCreateNationalityAgeGroupHandle(String pConfigurePath, String pLicence, int iGpuId);

	static public native int cwReleaseNationalityAgeGroupHandle(int pHandle);
	
	static public native int cwGetNationalityAgeGroupEval(int pHandle, byte[] dataAlign, int iWidth, int iHeight, int iChannels, 
			                                              FaceAttrRet attr);

}
