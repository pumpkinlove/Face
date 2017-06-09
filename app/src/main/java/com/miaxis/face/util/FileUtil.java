package com.miaxis.face.util;

import android.app.smdt.SmdtManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.miaxis.face.constant.Constants;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Created by Administrator on 2017/5/21 0021.
 */

public class FileUtil {

    public static final String FACE_MAIN_PATH = Environment.getExternalStorageDirectory() + File.separator + "miaxis" + File.separator + "FaceId_CW";
    public static final String LICENCE_NAME = "cw_lic.txt";
    public static final String IMG_PATH_NAME = "zzFaces";

    public static String readLicence() {
        File lic = new File(FACE_MAIN_PATH, LICENCE_NAME);
        return readFile(lic);
    }

    public static String readFile(File file) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String readLine;
            while ((readLine = br.readLine()) != null) {
                sb.append(readLine);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static void writeFile(File file, String content, boolean isAdd) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file, isAdd));
            bw.write(content);
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(bw != null) {
                    bw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeFile(InputStream is, File file) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            byte[] b = new byte[1024];
            while((is.read(b)) != -1){
                fos.write(b);
            }
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void delDirectory(File file) {
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()){
                File photoFile = new File(files[i].getPath());
                if (photoFile.isDirectory()) {
                    delDirectory(photoFile);
                } else {
                    photoFile.delete();
                }
            }
        }
        file.delete();
    }

    public static String getAvailablePath(Context context) {
        File saveDir = new File(new SmdtManager(context).smdtGetSDcardPath(context));
        if (!saveDir.exists() || !saveDir.canWrite()) {
            return FACE_MAIN_PATH;
        } else {
            return saveDir.getPath();
        }
    }

    public static int getAvailablePathType(Context context) {
        File saveDir = new File(new SmdtManager(context).smdtGetSDcardPath(context));
        if (!saveDir.exists() || !saveDir.canWrite()) {
            return Constants.PATH_LOCAL;
        } else {
            return Constants.PATH_TFCARD;
        }
    }

    public static String getAvailableImgPath(Context context) {
        return getAvailablePath(context) + File.separator + IMG_PATH_NAME;
    }

    public static void saveBitmap(Bitmap bitmap, String path, String name) throws Exception {
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdirs();
        }
        File file = new File(path, name);
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.flush();
        out.close();
    }

    public static void writeBytesToFile(byte[] bfile, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if(!dir.exists() || !dir.isDirectory()){//判断文件目录是否存在
                dir.mkdirs();
            }
            file = new File(filePath, fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static long getSDFreeSize(String path) {
        File file = new File(path);
        //取得SD卡文件路径
        StatFs sf = new StatFs(file.getPath());
        //获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        //空闲的数据块的数量
        long freeBlocks = sf.getAvailableBlocks();
        //返回SD卡空闲大小
        //return freeBlocks * blockSize;  //单位Byte
        //return (freeBlocks * blockSize)/1024;   //单位KB
        return (freeBlocks * blockSize)/1024 /1024; //单位MB
    }

    public static long getSDAllSize(String path) {
        File file = new File(path);
        StatFs sf = new StatFs(file.getPath());
        //获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        //获取所有数据块数
        long allBlocks = sf.getBlockCount();
        //返回SD卡大小
        //return allBlocks * blockSize; //单位Byte
        //return (allBlocks * blockSize)/1024; //单位KB
        return (allBlocks * blockSize)/1024/1024; //单位MB
    }
}
