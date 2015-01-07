/*
 *  Copyright (C) 2014 The AppCan Open Source Project.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.zywx.wbpalmstar.base;

import android.content.Context;
import org.zywx.wbpalmstar.engine.EUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class BHtmlDecrypt {
	
	private static String contentSuffix = "3G2WIN Safe Guard";
	
	public static String decrypt(String inUrl, Context context,
			boolean isSdcardWidget, String strData) {
		if (inUrl == null || inUrl.length() == 0) {
			return null;
		}
		byte[] data = null;
		if ((inUrl.startsWith(BUtility.F_RES_PATH) || inUrl
				.startsWith(BUtility.F_ASSET_PATH)) && context != null) {
			InputStream inputStream = null;
			int index = 0;
			if (inUrl.startsWith(BUtility.F_RES_PATH)) {
				index = BUtility.F_RES_PATH.length();
			} else {
				index = BUtility.F_ASSET_PATH.length();
			}
			try {
				inputStream = context.getAssets().open(inUrl.substring(index));
				data = BUtility.transStreamToBytes(inputStream,
						inputStream.available());

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					inputStream = null;
				}
			}
		} else if (inUrl.startsWith(BUtility.F_SDCARD_PATH)) {

			String widgetPath = inUrl
					.substring(BUtility.F_SDCARD_PATH.length());

			String sdcardRoot = BUtility.getSdCardRootPath();
			if (sdcardRoot != null) {
				File file = new File(sdcardRoot + "/" + widgetPath);
				if (!file.exists()) {
					return inUrl;
				}
				data = getByteFromFile(file);
			}
		} else if (inUrl.startsWith("/")) {
			File file = new File(inUrl);
			if (!file.exists()) {
				return inUrl;
			}
			data = getByteFromFile(file);

		} else if (inUrl.startsWith(BUtility.F_DATA_PATH) && !isSdcardWidget
				&& context != null) {
			File file = new File(inUrl.substring("file://".length()));
			if (!file.exists()) {
				return null;
			}
			data = getByteFromFile(file);
		} else if(inUrl.startsWith(BUtility.F_HTTP_PATH)){
			data = getByteFromNetWork(inUrl);
		}
		if(data == null){
			return inUrl;
		}
		String fileName = getFileNameWithNoSuffix(inUrl);
		String result = EUtil.htmlDecode(data, fileName);
		StringBuffer reStr = new StringBuffer(result);
		if (strData != null && strData.length() > 0) {
			reStr.append(strData);
		}
		return reStr.toString();
	}
	
	public static String getFileNameWithNoSuffix(String path){
    	String name = null;
    	int index = path.lastIndexOf('/');
		if(index > 0){
			name = path.substring(index + 1, path.length());
		}
		int index1 = name.lastIndexOf('.');
		if(index1 > 0){
			name = name.substring(0, index1);
		}
		return name;
    }

	private static byte[] getByteFromFile(File file) {
		InputStream inputStream = null;
		byte[] data = null;
		try {
			inputStream = new FileInputStream(file);
			data = BUtility.transStreamToBytes(inputStream,
					inputStream.available());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				inputStream = null;
			}
		}
		return data;
	}
	
	private static byte[] getByteFromNetWork(String inUrl){
		byte[] result = null;
		HttpURLConnection connection = null;  
        InputStream inSream = null;  
        try {  
            URL url = new URL(inUrl);  
            connection = (HttpURLConnection)url.openConnection(); 
            connection.setConnectTimeout(5 * 1000);
            int code = connection.getResponseCode();  
            if (HttpURLConnection.HTTP_OK == code) {  
                connection.connect();  
                inSream = connection.getInputStream();  
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024 * 4];
                int len = -1;  
                while((len = inSream.read(buffer)) != -1){  
                	outStream.write(buffer, 0, len);  
                }  
                result = outStream.toByteArray();
                inSream.close();  
                outStream.close();  
            }  
        } catch (MalformedURLException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {  
            if (connection != null) {  
                connection.disconnect();  
            }  
        } 
		return result;
	}
	
	public static boolean isEncrypted(InputStream inStream) {
		
		boolean isV = false;
		
		if (inStream == null) {
			return isV;
		}
		
		try {
			
			String text = InputStreamTOString(inStream, "UTF-8");
			
			String lastStr = text.substring(text.length() - 17, text.length());
			
			if (lastStr.equals(contentSuffix)) {
				isV = true;
			}
			
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return isV;
	}
	
	public static String InputStreamTOString(InputStream in,String encoding) throws Exception{
    	
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	       byte[] data = new byte[4096];
	       int count = -1;
	       while((count = in.read(data,0,4096)) != -1)
	           outStream.write(data, 0, count);

	       data = null;
	       return new String(outStream.toByteArray(),encoding);
       
   }
}
