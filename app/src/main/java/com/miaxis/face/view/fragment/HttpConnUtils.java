package com.miaxis.face.view.fragment;

import android.support.v4.widget.ContentLoadingProgressBar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpConnUtils {
	
	public static HttpURLConnection getConnection(String curl) {
		try {
			URL url = new URL(curl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");		
			conn.setRequestProperty("Content-type", "text/html");	
			conn.setRequestProperty("Accept-Charset", "UTF-8");	
			conn.setRequestProperty("contentType", "UTF-8");				
			conn.setConnectTimeout(30000);
			
			return conn;
		} catch (Exception ex) {
			return null;
		}
	}

	public static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;

		try {

			while ((line = reader.readLine()) != null) {

				sb.append(line);

			}

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				is.close();

			} catch (IOException e) {

				e.printStackTrace();

			}

		}

		return sb.toString();

	}
	public  static File downFile(String filepath,
                                 ContentLoadingProgressBar pd, String curl) {
		try {
			URL url = new URL(curl);
			File file = new File(filepath);
			FileOutputStream fos = new FileOutputStream(file);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			//下载的请求是GET方式，conn的默认方式也是GET请求
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept-Encoding", "identity"); 
			//服务端的响应的时间
			conn.setConnectTimeout(5000);
			//获取到服务端的文件的总长度
			int max = conn.getContentLength();
			if(max==0){
				fos.close();
				return null;
			}
			//将进度条的最大值设置为要下载的文件的总长度
			pd.setMax(max/1024);
			//获取到要下载的apk的文件的输入流
			InputStream is = conn.getInputStream();
			//设置一个缓存区
			byte[] buffer = new byte[1024];
			int len=0;
			int process = 0;
			while ((len = is.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
				//每读取一次输入流，就刷新一次下载进度
				process+=(len/1024);
				pd.setProgress(process);
				//设置睡眠时间，便于我们观察下载进度
			}
			//刷新缓存数据到文件中
			fos.flush();
			//关流
			fos.close();
			is.close();
			return file;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 获取一个路径中的文件名。例如：mobilesafe.apk
	 * 
	 * @param urlpath
	 * @return
	 */
	public String getFilename(String urlpath) {
		return urlpath
				.substring(urlpath.lastIndexOf("/") + 1, urlpath.length());
	}
}
