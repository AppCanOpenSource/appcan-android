/*
 *  Copyright (C) 2014 The AppCan Open Source Project.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.zywx.wbpalmstar.engine.eservice;

import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.ByteArrayBuffer;

import java.io.EOFException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class EServiceTest {

    public static void test() {
        String realyPath = "http://localhost:8000/other.QDV";
        HttpRequestBase mHhttpRequest = new HttpGet(realyPath);
        mHhttpRequest.addHeader("range", "bytes=34199-");
        BasicHttpParams bparams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(bparams, 20000);
        HttpConnectionParams.setSoTimeout(bparams, 20000);
        HttpConnectionParams.setSocketBufferSize(bparams, 8 * 1024);
        HttpClientParams.setRedirecting(bparams, true);
        DefaultHttpClient mDefaultHttpClient = new DefaultHttpClient(bparams);
        HttpResponse response = null;
        try {
            response = mDefaultHttpClient.execute(mHhttpRequest);

            int responseCode = response.getStatusLine().getStatusCode();
            byte[] arrayOfByte = null;
            HttpEntity httpEntity = response.getEntity();
            if (responseCode == 200 || responseCode == 206) {
                arrayOfByte = toByteArray(httpEntity);
                String m = new String(arrayOfByte, "UTF-8");
                Log.i("ldx", "" + m.length());
                Log.i("ldx", m);
                return;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] toByteArray(HttpEntity entity) throws Exception {
        if (entity == null) {
            throw new Exception("HTTP entity may not be null");
        }
        InputStream instream = entity.getContent();
        if (instream == null) {
            return new byte[]{};
        }
        long len = entity.getContentLength();
        if (len > Integer.MAX_VALUE) {
            throw new Exception(
                    "HTTP entity too large to be buffered in memory");
        }
        Header contentEncoding = entity.getContentEncoding();
        boolean gzip = false;
        if (null != contentEncoding) {
            if ("gzip".equalsIgnoreCase(contentEncoding.getValue())) {
                instream = new GZIPInputStream(instream, 2048);
                gzip = true;
            }
        }
        ByteArrayBuffer buffer = new ByteArrayBuffer(1024 * 8);
        // \&:38, \n:10, \r:13, \':39, \":34, \\:92
        try {
            if (gzip) {
                int lenth = 0;
                while (lenth != -1) {
                    byte[] buf = new byte[2048];
                    try {
                        lenth = instream.read(buf, 0, buf.length);
                        if (lenth != -1) {
                            buffer.append(buf, 0, lenth);
                        }
                    } catch (EOFException e) {
                        int tl = buf.length;
                        int surpl;
                        for (int k = 0; k < tl; ++k) {
                            surpl = buf[k];
                            if (surpl != 0) {
                                buffer.append(surpl);
                            }
                        }
                        lenth = -1;
                    }
                }
                int bl = buffer.length();
                ByteArrayBuffer temBuffer = new ByteArrayBuffer(
                        (int) (bl * 1.4));
                for (int j = 0; j < bl; ++j) {
                    int cc = buffer.byteAt(j);
//					if (cc == 34 || cc == 39 || cc == 92 || cc == 10
//							|| cc == 13 || cc == 38) {
//						temBuffer.append('\\');
//					}
                    temBuffer.append(cc);
                }
                buffer = temBuffer;
            } else {
                int c;
                while ((c = instream.read()) != -1) {
//					if (c == 34 || c == 39 || c == 92 || c == 10 || c == 13
//							|| c == 38) {
//						buffer.append('\\');
//					}
                    buffer.append(c);
                }
            }
        } catch (Exception e) {
            instream.close();
        } finally {
            instream.close();
        }
        return buffer.toByteArray();
    }
}
