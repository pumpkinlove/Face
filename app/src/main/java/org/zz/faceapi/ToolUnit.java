package org.zz.faceapi;

import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.List;

public class ToolUnit {
	

	/**
	 * @author chen.gs
	 * @category 获取sd卡路径
	 * */
	public static String getSDCardPath(){
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); //判断sd卡是否存在
		if (sdCardExist)
		{
			sdDir = Environment.getExternalStorageDirectory();  //获取跟目录
			return sdDir.toString();
		}
		else
		{
			return null;
		}
	}

	/**
	 * @author chen.gs
	 * @category 判读文件或文件夹是否存在
	 * @param  path 文件夹路径
	 * @return true 存在  false 不存在
	*/
	public static Boolean isExist(String path) {
		if(path==null)
			return false;
		File file = new File(path);
		if (!file.exists()) {
			return false;
		}
		else{
			return true;
		}
	}

	/**
	 * @author chen.gs
	 *
	 * @category 判断文件夹是否存在,如果不存在则创建文件夹
	 * @param  path 文件夹路径
	 * @return true 存在  false 不存在
	*/
	public static Boolean AddDirectory(String path) {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}
		return true;
	}

	/**
	 * @author   chen.gs
	 * @category 遍历文件夹下子目录
	 * @param    strMainFolder 文件夹路径
	 * @param    strSubFolders 子目录路径列表
	 * @return
	 * */
	public static void GetSubFolders(String strMainFolder,List<String> strSubFolders) {
	    File[] files =new File(strMainFolder).listFiles();
	    for (int i =0; i < files.length; i++){
	        File f = files[i];
	        if (f.isDirectory()){
	        	strSubFolders.add(f.getPath());
	        }
	    }
	}

	/**
	 * @author   chen.gs
	 * @category 搜索目录，扩展名，
	 * @param    Path        文件夹路径
	 * @param    Extension   扩展名
	 * @param    strSubFiles 子文件路径列表
	 * @return
	 * */
	public static void GetSubFiles(String Path, String Extension,List<String> strSubFiles)
	{
	    File[] files =new File(Path).listFiles();

	    for (int i =0; i < files.length; i++)
	    {
	        File f = files[i];
	        if (f.isFile())
	        {
	        	//判断扩展名
	            if (f.getPath().substring(f.getPath().length() - Extension.length()).equals(Extension))
	            	strSubFiles.add(f.getPath());
	        }
	    }
	}

	public static void GetSubFiles(String Path,List<String> strSubFiles)
	{
	    File[] files =new File(Path).listFiles();

	    for (int i =0; i < files.length; i++)
	    {
	        File f = files[i];
	        if (f.isFile())
	        {
	            strSubFiles.add(f.getPath());
	        }
	    }
	}

	/**
     * @author   chen.gs
     * @category 保存数据为文件
     * @param    filepath - 文件路径
     *              buffer   - 数据缓存
     *              size     - 数据长度
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
     * @category 获取文件大小
     * @param    filepath - 文件路径
     * @return   文件大小
     * */
	public static long getFileSizes(String filepath) {
		File f = new File(filepath);
		long s = 0;
		if (f.exists()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(f);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				s = fis.available();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			return -1;
		}
		return s;
	}

	/**
     * @author   chen.gs
     * @category 读取文件数据到byte数组
     * @param    filepath - 文件路径
     * @return   byte数组
     * */
    public static byte[] ReadData(String filepath){
        File f = new File(filepath);
        if(!f.exists()){
        	return null;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream((int)f.length());
        BufferedInputStream in = null;
        try{
            in = new BufferedInputStream(new FileInputStream(f));
            int buf_size = 1024;
            byte[] buffer = new byte[buf_size];
            int len = 0;
            while(-1 != (len = in.read(buffer,0,buf_size))){
                bos.write(buffer,0,len);
            }
            return bos.toByteArray();
        }catch (IOException e) {
            e.printStackTrace();
        }finally{
            try{
                in.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
            try {
				bos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		return null;
    }

    /**
     * 删除单个文件
     * @param   sPath    被删除文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String sPath) {
    	boolean flag = false;
    	File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }

    /**
     * 追加文件：使用RandomAccessFile
     *
     * @param fileName 文件名
     * @param content  追加的内容
     * @param iPos     0-开始位置，<0 结束位置，>0-指定位置
     */
    public static void AppendFile(String fileName, String content,int iPos) {
        RandomAccessFile randomFile = null;
        try {
            // 打开一个随机访问文件流，按读写方式
            randomFile = new RandomAccessFile(fileName, "rw");
            // 文件长度，字节数
            long fileLength = randomFile.length();
            if(iPos==0)
            {
            	fileLength = 0;
            }
            else if(iPos>0)
            {
            	fileLength = iPos;
            }
            // 将写文件指针移到文件尾。
            randomFile.seek(fileLength);
            randomFile.writeBytes(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(randomFile != null){
                try {
                    randomFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 追加文件：使用RandomAccessFile
     *
     * @param fileName 文件名
     * @param content  追加的内容
     * @param iPos     0-开始位置，<0 结束位置，>0-指定位置
     */
    public static void AppendFile_bk(String fileName, String content,int iPos) {
        RandomAccessFile randomFile = null;
        try {
            // 打开一个随机访问文件流，按读写方式
            randomFile = new RandomAccessFile(fileName, "rw");
            // 文件长度，字节数
            long fileLength = randomFile.length();
            if(iPos==0)
            {
             fileLength = 0;
            }
            else if(iPos>0)
            {
             fileLength = iPos;
            }
            // 将写文件指针移到文件尾。
            randomFile.seek(fileLength);

            int szlen = content.length();
            byte  buffer[]  =  new   byte [szlen ];
            buffer  = content.getBytes();

            randomFile.write(buffer);

        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(randomFile != null){
                try {
                    randomFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
	 * Java文件操作 获取文件扩展名
	 */
	public static String getExtensionName(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int dot = filename.lastIndexOf('.');
			if ((dot > -1) && (dot < (filename.length() - 1))) {
				return filename.substring(dot + 1);
			}
		}
		return filename;
	}

	/*
	 * Java文件操作 获取不带扩展名的文件名
	 */
	public static String getFileNameNoEx(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int dot = filename.lastIndexOf('.');
			if ((dot > -1) && (dot < (filename.length()))) {
				return filename.substring(0, dot);
			}
		}
		return filename;
	}
	
	public static void inputstreamtofile(InputStream ins, File file)
			throws IOException {
		OutputStream os = new FileOutputStream(file);
		int bytesRead = 0;
		byte[] buffer = new byte[8192];
		while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
			os.write(buffer, 0, bytesRead);
		}
		os.close();
		ins.close();
	}
		

}
