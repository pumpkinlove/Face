package cn.cloudwalk.sdk;


public class FaceAttrRet {

	/**
	 * 年龄或者年龄段
	 * 如果是年龄段，则：0为小孩，1为成年人，2为老人
	 **/
	public int m_iAge;
	
	/**
	 * 性别或者国籍
	 * 如果是性别，则：1为男士，2为女士
	 * 如果是国籍，则：0为中国人，1为外国人
	 **/
	public int m_iGender;
}