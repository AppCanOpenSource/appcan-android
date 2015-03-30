package org.zywx.wbpalmstar.base;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import org.zywx.wbpalmstar.acedes.ACEDes;

import java.io.*;

public class ACEContentProvider extends ContentProvider {
   
   @Override  
   public AssetFileDescriptor openAssetFile(Uri uri, String mode)  
           throws FileNotFoundException {  
       // TODO Auto-generated method stub  
       AssetManager am = getContext().getAssets();  
       String path = uri.getPath().substring(1);  
       try {  
    	   
    	   InputStream is = am.open(path);
    	   InputStream localCopy;
    	   
    	   ByteArrayOutputStream baos = new ByteArrayOutputStream();

			byte[] buffer = new byte[1024];
			int len;
			while ((len = is.read(buffer)) > -1 ) {
			    baos.write(buffer, 0, len);
			}
			baos.flush();

			
			InputStream is1 = new ByteArrayInputStream(baos.toByteArray()); 
			InputStream is2 = new ByteArrayInputStream(baos.toByteArray()); 
			
			boolean isV = ACEDes.isEncrypted(is1);
			
			if (isV) {
				
	 	    	InputStream resStream = null;
	 	    	byte[] data = null;
	 	    	String fileName;
	 	    	String result = null;

	    		data = BUtility.transStreamToBytes(is2, is2.available());
	    		fileName = BUtility.getFileNameWithNoSuffix(path);
	    		result = ACEDes.htmlDecode(data, fileName);
	    		resStream = new ByteArrayInputStream(result.getBytes());
	    		localCopy = resStream;
			} else {
				localCopy = is2;
			}
    	   
    	   ParcelFileDescriptor parcel = ACEParcelFileDescriptorUtil.pipeFrom(localCopy);
    	   
    	   AssetFileDescriptor localAssetFileDescriptor = new AssetFileDescriptor(parcel, 0L, -1L);
           return localAssetFileDescriptor;
//           AssetFileDescriptor afd = am.openFd(path);  
//           return afd;  
       } catch (IOException e) {  
    	   e.printStackTrace();
    	   Log.e("LocalFileContentProvider", "uri " + uri.toString(), e);
       }  
       return super.openAssetFile(uri, mode);  
   }  

   @Override
   public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
    		ParcelFileDescriptor parcel = null;

    		try {
    			
    			String string = uri.getPath();
    			
//    			string = "file://" + string;
    			
//    			File file = new File(string);
//    			
//    			if (file.isDirectory())
//    				return null;
//    				
//    			parcel = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    			
//    			AssetManager am = getContext().getAssets();  
//    			String path = string.substring(1); 
//    			InputStream localCopy = am.open(string);
//    	    	   
//    			parcel = ParcelFileDescriptorUtil.pipeFrom(localCopy);
    			
    			
    			return parcel;
    		} catch (Exception e) {
    			return null;
    		}    	
   }

   @Override
   public boolean onCreate() {
       return true;
   }

   @Override
   public int delete(Uri uri, String s, String[] as) {
       throw new UnsupportedOperationException("Not supported by this provider");
   }

   @Override
   public String getType(Uri uri) {
       throw new UnsupportedOperationException("Not supported by this provider");
   }

   @Override
   public Uri insert(Uri uri, ContentValues contentvalues) {
       throw new UnsupportedOperationException("Not supported by this provider");
   }

   @Override
   public Cursor query(Uri uri, String[] as, String s, String[] as1, String s1) {
       throw new UnsupportedOperationException("Not supported by this provider");
   }

   @Override
   public int update(Uri uri, ContentValues contentvalues, String s, String[] as) {
       throw new UnsupportedOperationException("Not supported by this provider");
   }

}
