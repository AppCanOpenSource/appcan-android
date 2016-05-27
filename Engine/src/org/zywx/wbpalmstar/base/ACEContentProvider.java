package org.zywx.wbpalmstar.base;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.input.BOMInputStream;
import org.zywx.wbpalmstar.acedes.ACEDes;
import org.zywx.wbpalmstar.widgetone.dataservice.WDataManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ACEContentProvider extends ContentProvider {

    private static final String TAG = "ACEContentProvider";

    @Override
    public AssetFileDescriptor openAssetFile(Uri uri, String mode)
            throws FileNotFoundException {
        AssetManager am = getContext().getAssets();
        String path = uri.getPath().substring(1);
        try {
            InputStream is = null;
            InputStream tempInputStream = null;
            if (path.startsWith("android_asset/")) {
                path = path.substring("android_asset/".length());
            }

            String sdCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String sboxPath = getContext().getFilesDir().getAbsolutePath();
            boolean isLocalPath = false;
            //开启增量或者是内置sd卡路径
            if ((WDataManager.isUpdateWidget && WDataManager.isCopyAssetsFinish) ||
                    (!TextUtils.isEmpty(sdCardPath) && path.startsWith(sdCardPath.substring(1)))
                    || (!TextUtils.isEmpty(sboxPath) && path.startsWith(sboxPath.substring(1)))) {
                isLocalPath = true;
            } else {
                //外置sd卡路径
                List<String> sdCardList = BUtility.getAllExtraSdcardPath();
                Log.i(TAG, "size = " + sdCardList.size());
                for (int i = 0; i < sdCardList.size(); i++) {
                    String sdExtraPath = sdCardList.get(i);
                    Log.i(TAG, "sdExtraPath = " + sdExtraPath);
                    if (!TextUtils.isEmpty(sdExtraPath)
                            && path.startsWith(sdExtraPath.substring(1))) {
                        isLocalPath = true;
                    }
                }
            }

            if (isLocalPath) {
                File file = new File(path);
                is = new FileInputStream(file);
                tempInputStream = new FileInputStream(file);
            } else {
                is = am.open(path);
                tempInputStream = am.open(path);
            }
            BOMInputStream bomInputStream = new BOMInputStream(tempInputStream);
            if (bomInputStream.hasBOM()) {
                is = bomInputStream;
            } else {
                bomInputStream.close();
                tempInputStream.close();
            }

            if (!path.endsWith(".html")
                    && !path.endsWith(".css")
                    && !path.endsWith(".js")
                    && !path.endsWith(".htm")
                    && !path.endsWith(".xml")) {
                ParcelFileDescriptor parcel = ACEParcelFileDescriptorUtil.pipeFrom(is);
                AssetFileDescriptor localAssetFileDescriptor = new AssetFileDescriptor(parcel, 0L, -1L);
                return localAssetFileDescriptor;
            }

            InputStream localCopy;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();

            InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
            InputStream is2 = new ByteArrayInputStream(baos.toByteArray());

            boolean isV = ACEDes.isEncrypted(is1);

            if (isV) {
                byte[] data = null;
                String fileName;
                String result = null;

                data = BUtility.transStreamToBytes(is2, is2.available());
                fileName = BUtility.getFileNameWithNoSuffix(path);
                result = ACEDes.htmlDecode(data, fileName);
                localCopy = new ByteArrayInputStream(result.getBytes());
            } else {
                localCopy = is2;
            }

            ParcelFileDescriptor parcel = ACEParcelFileDescriptorUtil.pipeFrom(localCopy);

            AssetFileDescriptor localAssetFileDescriptor = new AssetFileDescriptor(parcel, 0L, -1L);
            return localAssetFileDescriptor;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.openAssetFile(uri, mode);
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        ParcelFileDescriptor parcel = null;

        try {

            String string = uri.getPath();

//                string = "file://" + string;

//                File file = new File(string);
//                
//                if (file.isDirectory())
//                    return null;
//                    
//                parcel = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);

//                AssetManager am = getContext().getAssets();  
//                String path = string.substring(1); 
//                InputStream localCopy = am.open(string);
//                   
//                parcel = ParcelFileDescriptorUtil.pipeFrom(localCopy);


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
