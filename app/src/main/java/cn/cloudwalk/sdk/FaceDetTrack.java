package cn.cloudwalk.sdk;



public class FaceDetTrack {

	static FaceDetTrack sCJNI = null;
	
	public FaceDetTrack() {
		loadLibrarys();
	}

	public static FaceDetTrack getInstance() {

		if (null == sCJNI) {
			sCJNI = new FaceDetTrack();

		}
		return sCJNI;
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

	/**
	 *
	 * @param pFaceDetectFile
	 * @param pFaceKeyPtFile
	 * @param pFaceQualityFile
	 * @param pFaceKeyPtTrackFile
	 * @return pDetector
	 */
	static public native int cwCreateDetHandleFromFile(String pFaceDetectFile, 
													   String pFaceKeyPtDetFile, 
													   String pFaceKeyPtTrackFile,
													   String pFaceQualityFile, 
													   String pFaceLivenessFile,
													   String pLicence);

	static public native int cwCreateDetHandleFromMem(String pLicence);

	static public native int cwReleaseDetHandle(int pDetector);

	static public native int cwGetFaceParam(int pDetector, FaceParam param);
	static public native int cwSetFaceParam(int pDetector, FaceParam param);

	static public native int cwFaceDetection(int pDetector, byte[] pFrameImg, 
											 int iWidth, 
											 int iHeight, 
											 int iFormat, 
											 int iAngle, 
											 int iMirror,
											 int iOp,
											 FaceInfo[] pFaceBuffer);

}
