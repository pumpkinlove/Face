package cn.cloudwalk.sdk;

import android.os.Parcel;
import android.os.Parcelable;

public class FaceInfo implements Parcelable {
	public FaceInfo()
	{

	}

	public int detected; // 1: 检测到的人脸,.0：跟踪到的人脸.
	                     // 注： 跟踪到的仅ID和人脸框数据有效

	public int trackId;  // 人脸ID（ID<0表示没有进入跟踪）
	
	// face rect人脸框
	public int x;        // 左上角x坐标
	public int y;        // 坐上角y坐标
	public int width;    // 人脸宽
	public int height;   // 人脸高

	
	// face_point关键点，最多68个关键点，目前使用9点关键点模型
	public float[] keypt_x;      // 关键点x坐标
	public float[] keypt_y;      // 关键点y坐标
	public float keyptScore;    // 关键点得分
	
	// face_aligned人脸对齐数据，用于提特征
	public byte[] alignedData;  // 图像数据，空间分配128*128
	public int alignedW;        // 宽
	public int alignedH;        // 高
	public int nChannels;       // 图像通道
	
	// 活体检测
	int livenessErrcode;		            // 活体检测错误码
	int headPitch;							// 低抬头动作（1：抬头，0:头未动,-1：低头）
	int headYaw;							// 头转动作（1：往图像左侧转头，0：头未动，-1往图像右侧转头）
	int mouthAct;							// 嘴部动作（1：张嘴，0：未张嘴）
	int eyeAct;								// 眼睛动作（1：眨眼，0：未眨眼）
	int attack;								// 攻击类型（0:正常图像 -1:图像抖动 -2:嘴被扣取 -3:右眼被扣取
		                                    //          -4:图片攻击 -5:人脸不稳定 -6:方框(如纸片、pad)攻击 -7:视频攻击）
	
	// face_quality人脸质量分
	public int   errcode;		// 质量分析错误码
	public float faceScore;     // 总分，0~1.0之间，越大则人脸质量越好.
	public float brightness;    // 亮度
	public float clearness;     // 清晰度
	public float symmetry;      // 对称性
	public float glassness;     // 眼镜: 返回值0~1，值越大，越有可能没戴眼镜.
	public float skiness;       // 肤色：返回值0~1, 肤色面积占人脸面积的比例
	public float mouthness;     // 嘴部: 自行设定阈值Thres，约在(0,1)区间，大于Thres为闭嘴.
	public float eyeLeft;       // 左眼
	public float eyeRight;      // 右眼
	public float occlusion;     // 眼睛被遮挡的置信度（用于活体），推荐阈值为0.5

	// head_pose头部姿态
	public float pitch;         // 抬头、低头,范围-90到90，越大表示越抬头
	public float yaw;           // 左右转头
	public float roll;          // 平面内偏头

	protected FaceInfo(Parcel in) {
		detected = in.readInt();
		trackId = in.readInt();
		x = in.readInt();
		y = in.readInt();
		width = in.readInt();
		height = in.readInt();
		keypt_x = in.createFloatArray();
		keypt_y = in.createFloatArray();
		keyptScore = in.readFloat();
		alignedData = in.createByteArray();
		alignedW = in.readInt();
		alignedH = in.readInt();
		nChannels = in.readInt();
		livenessErrcode = in.readInt();
		headPitch = in.readInt();
		headYaw = in.readInt();
		mouthAct = in.readInt();
		eyeAct = in.readInt();
		attack = in.readInt();
		errcode = in.readInt();
		faceScore = in.readFloat();
		brightness = in.readFloat();
		clearness = in.readFloat();
		symmetry = in.readFloat();
		glassness = in.readFloat();
		skiness = in.readFloat();
		mouthness = in.readFloat();
		eyeLeft = in.readFloat();
		eyeRight = in.readFloat();
		occlusion = in.readFloat();
		pitch = in.readFloat();
		yaw = in.readFloat();
		roll = in.readFloat();
	}

	public static final Creator<FaceInfo> CREATOR = new Creator<FaceInfo>() {
		@Override
		public FaceInfo createFromParcel(Parcel in) {
			return new FaceInfo(in);
		}

		@Override
		public FaceInfo[] newArray(int size) {
			return new FaceInfo[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(detected);
		dest.writeInt(trackId);
		dest.writeInt(x);
		dest.writeInt(y);
		dest.writeInt(width);
		dest.writeInt(height);
		dest.writeFloatArray(keypt_x);
		dest.writeFloatArray(keypt_y);
		dest.writeFloat(keyptScore);
		dest.writeByteArray(alignedData);

		dest.writeInt(alignedW);
		dest.writeInt(alignedH);
		dest.writeInt(nChannels);
		dest.writeInt(livenessErrcode);
		dest.writeInt(headPitch);
		dest.writeInt(headYaw);
		dest.writeInt(mouthAct);
		dest.writeInt(eyeAct);
		dest.writeInt(attack);
		dest.writeInt(errcode);

		dest.writeFloat(faceScore);
		dest.writeFloat(brightness);
		dest.writeFloat(clearness);
		dest.writeFloat(symmetry);
		dest.writeFloat(glassness);
		dest.writeFloat(skiness);
		dest.writeFloat(mouthness);
		dest.writeFloat(eyeLeft);
		dest.writeFloat(eyeRight);
		dest.writeFloat(occlusion);
		dest.writeFloat(pitch);
		dest.writeFloat(yaw);
		dest.writeFloat(roll);

	}
}
