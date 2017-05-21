package cn.cloudwalk.sdk;


public interface FaceInterface {

	// 图像格式
	interface cw_img_form_t extends FaceInterface {
		int CW_IMAGE_GRAY8 = 0;
		int CW_IMAGE_BGR888 = 1;
		int CW_IMAGE_BGRA8888 = 2;
		int CW_IMAGE_YUV420P = 3;
		int CW_IMAGE_YV12 = 4;
		int CW_IMAGE_NV12 = 5;
		int CW_IMAGE_NV21 = 6;
		int CW_IMAGE_BINARY = 7;
	}

	// 图像旋转角度（逆时针）
	interface cw_img_angle_t extends FaceInterface {
		int CW_IMAGE_ANGLE_0 = 0;
		int CW_IMAGE_ANGLE_90 = 1;
		int CW_IMAGE_ANGLE_180 = 2;
		int CW_IMAGE_ANGLE_270 = 3;
	}
	
	// 图像镜像
	interface cw_img_mirror_t extends FaceInterface {
		int CW_IMAGE_MIRROR_NONE = 0;        // 不镜像  
		int CW_IMAGE_MIRROR_HOR = 1;         // 水平镜像
		int CW_IMAGE_MIRROR_VER = 2;         // 垂直镜像
		int CW_IMAGE_MIRROR_HV = 3;          // 垂直和水平镜像
	}

	// 检测开关选项
	interface cw_op_t extends FaceInterface {
		int CW_OP_DET     = 0;                  // (1 << 0) 进行人脸检测，并返回人脸矩形位置
		int CW_OP_TRACK   = 2;                  // (1 << 1)进行人脸跟踪，并返回人脸跟踪的ID
		int CW_OP_KEYPT   = 4;                  // (1 << 2)进行人脸关键点检测，并返回人脸上的关键点坐标信息
		int CW_OP_ALIGN   = 8;                  // (1 << 3)进行人脸图像对齐，并返回对齐后的人脸图像（可直接送入特征提取接口）、关键点信息
		int CW_OP_QUALITY_BASE = 16;            // (1 << 4)人脸质量分基础部分
		// 检测活体开关选项（实际应用中，检测什么活体就用对应的动作开关+攻击开关）
		int CW_OP_LIVENESS_HEAD_UP = 64;		// (1<<6) 活体检测: 抬头
		int CW_OP_LIVENESS_HEAD_DOWN = 128;		// (1<<7) 活体检测: 点头
		int CW_OP_LIVENESS_HEAD_LEFT = 256;	    // (1<<8) 活体检测: 向左转头
		int CW_OP_LIVENESS_HEAD_RIGHT = 512;	// (1<<9) 活体检测: 向右转头
		int CW_OP_LIVENESS_MOUTH = 1024;        // (1<<10)活体检测: 嘴部
		int CW_OP_LIVENESS_EYE = 2048;	        // (1<<11)活体检测: 眼睛
		int CW_OP_LIVENESS_ATTACK = 4096;	    // (1<<12)活体检测: 攻击类型
		int CW_OP_LIVENESS = 8128;	            // 活体检测：上面七种开关综合 
	}
	
	// 特征句柄功能
	interface cw_recog_pattern_t extends FaceInterface {
		int CW_Feature_Extract = 0;
		int CW_Recognition = 1;
	}

	// 通用错误码
	interface cw_errcode_t extends FaceInterface {
		int CW_OK = 0;                               // 成功

		int CW_EMPTY_FRAME_ERR = 20000;              // 空图像
		int CW_UNSUPPORT_FORMAT_ERR = 20001;         // 图像格式不支持
		int CW_ROI_ERR = 20002;                      // ROI设置失败
		int CW_MINMAX_ERR = 20003;                   // 最小最大人脸设置失败
		int CW_OUTOF_RANGE_ERR = 20004;              // 数据范围错误
		
		int CW_UNAUTHORIZED_ERR = 20005;             // 未授权
		int CW_UNINITIALIZED_ERR = 20006;            // 尚未初始化
		int CW_METHOD_UNAVAILABLE = 20007;	         // 方法无效
		int CW_PARAM_INVALID = 20008;                // 参数无效
		int CW_DETECT_MODEL_ERR = 20009;             // 加载检测模型失败
		int CW_KEYPT_MODEL_ERR = 20010;              // 加载关键点模型失败
		int CW_QUALITY_MODEL_ERR = 20011;            // 加载质量评估模型失败
		int CW_LIVENESS_MODEL_ERR = 20012;		     // 加载活体检测模型失败
		
		int CW_DET_ERR = 20013;                      // 检测失败
		int CW_TRACK_ERR = 20014;                    // 跟踪失败
		int CW_KEYPT_ERR = 20015;                    // 提取关键点失败
		int CW_ALIGN_ERR = 20016;                    // 对齐人脸失败
		int CW_QUALITY_ERR = 20017;                  // 质量评估失败
		int CW_EXCEEDMAXHANDLE_ERR = 20018;          // 超过授权最大句柄数
		
		int CW_RECOG_FEATURE_MODEL_ERR = 20019;      // 加载特征识别模型失败
		int CW_RECOG_ALIGNEDFACE_ERR = 20020;        // 对齐图片数据错误
		int CW_RECOG_MALLOCMEMORY_ERR = 20021;       // 预分配特征空间不足
		int CW_RECOG_FILEDDATA_ERR = 20022;          // 用于注册的特征数据错误
		int CW_RECOG_PROBEDATA_ERR = 20023;          // 用于检索的特征数据错误
		int CW_RECOG_EXCEEDMAXFEASPEED = 20024;      // 超过授权最大提特征速度
		int CW_RECOG_EXCEEDMAXCOMSPEED = 20025;      // 超过授权最大比对速度
		
		int CW_ATTRI_AGEGENDER_MODEL_ERR = 20026;    // 加载年龄性别模型失败 
		int CW_ATTRI_EVAL_AGEGENDER_ERR = 20027;     // 年龄性别识别失败
		int CW_ATTRI_NATIONALITY_MODEL_ERR = 20028;  // 加载国籍年龄段模型失败 
		int CW_ATTRI_EVAL_NATIONALITY_ERR = 20029;   // 国籍年龄段识别失败

		int CW_LICENCE_ACCOUNT_TIMEOUT = 20030;      // 安卓网络授权账号过期
		int CW_LICENCE_HTTP_ERROR = 20031;           // 安卓网络授权http错误
		int CW_LICENCE_MALLOCMEMORY_ERR = 20032;     // 安卓网络授权内存分配不足

	}
	
	// 质量分检测错误码
	interface cw_quality_errcode_t extends FaceInterface {
		int CW_QUALITY_OK            = 0;				// 质量分数据有效
		int	CW_QUALITY_NO_DATA       = 20150;		    // 质量分数据无效，原因：尚未检测
		int	CW_QUALITY_ERROR_UNKNOWN = 20151;           // 未知错误
	}
	
	interface cw_liveness_errcode_t extends FaceInterface {
		int CW_LIVENESS_OK = 0;				            // 活体数据有效
		int CW_LIVENESS_BUFFERING = 20100;		        // 活体数据无效，原因：正在检测
		int CW_LIVENESS_NOT_TARGET = 20101;			    // 活体数据无效，原因：不是被检目标（非最大人脸或尚未进入跟踪）
		int CW_LIVENESS_CHANGED = 20102;				// 活体数据无效，原因：换人 
		int CW_LIVENESS_POOR_QAULITY = 20103;			// 活体数据无效，原因：人脸质量差
	}
	
}
