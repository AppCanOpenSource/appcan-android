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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Properties;
import java.util.StringTokenizer;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import org.zywx.wbpalmstar.engine.EUtil;


public class EHttpService {

	protected int mTcpPort;
	protected Thread mThread;
	protected File mRootDir;
	protected ServerSocket mServerSocket;

	public EHttpService(int port, File root) throws IOException {
		mTcpPort = port;
		mRootDir = root;
		mServerSocket = new ServerSocket(mTcpPort);
		mThread = new Thread(new Runnable() {
			public void run() {
				try {
					while (true){
						new HTTPSession(mServerSocket.accept());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
//		mThread.setDaemon(true);
	}
	
	public void start(){
		mThread.start();
	}
	
	public HttpResponse serve(String uri, String method, Properties header, Properties parms, Properties files) {
		EUtil.logi("Response method: " + method + " '" + uri + "' ");
		Enumeration<?> enm = header.propertyNames();
		while (enm.hasMoreElements()) {
			String value = (String)enm.nextElement();
			EUtil.logi("Response header: '" + value + "' = '" + header.getProperty(value) + "'");
		}
		enm = parms.propertyNames();
		while (enm.hasMoreElements()) {
			String value = (String) enm.nextElement();
			EUtil.logi("Response parms: '" + value + "' = '" + parms.getProperty(value) + "'");
		}
		enm = files.propertyNames();
		while (enm.hasMoreElements()) {
			String value = (String) enm.nextElement();
			EUtil.logi("Response upload files: '" + value + "' = '" + files.getProperty(value) + "'");
		}
		return fileServe(uri, header, mRootDir, true);
	}


	public class HttpResponse {
		
		public String mStatus;
		public String mMimeType;
		public InputStream mData;
		public long mRemain = -1;
		public Properties mHeader = new Properties();
		
		public HttpResponse() {
			mStatus = EHttpConst.HTTP_OK;
		}

		public HttpResponse(String status, String mimeType, InputStream data) {
			mStatus = status;
			mMimeType = mimeType;
			mData = data;
		}

		public HttpResponse(String status, String mimeType, String txt) {
			mStatus = status;
			mMimeType = mimeType;
			try {
				mData = new ByteArrayInputStream(txt.getBytes("UTF-8"));
			} catch (java.io.UnsupportedEncodingException uee) {
				uee.printStackTrace();
			}
		}

		public void addHeader(String name, String value) {
			mHeader.put(name, value);
		}
	}

	public void stop() {
		try {
			mServerSocket.close();
			mThread.interrupt();
			EUtil.logi("server stop!!");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private class HTTPSession extends Thread {
		
		private Socket mSocket;
		
		public HTTPSession(Socket sok) {
			mSocket = sok;
			start();
		}

		public void run() {
			try {
				InputStream is = mSocket.getInputStream();
				if (is == null){
					return;
				}
				// Apache's default header limit is 8KB.
				int bufsize = 1024 * 8;
				byte[] buf = new byte[bufsize];
				int rlen = is.read(buf, 0, bufsize);
				if (rlen <= 0){
					return;
				}
				ByteArrayInputStream hbis = new ByteArrayInputStream(buf, 0, rlen);
				BufferedReader hin = new BufferedReader(new InputStreamReader(hbis));
				Properties pre = new Properties();
				Properties parms = new Properties();
				Properties header = new Properties();
				Properties files = new Properties();
				parseHeader(hin, pre, parms, header);
				String method = pre.getProperty("method");
				String uri = pre.getProperty("uri");
				long size = 0x7FFFFFFFFFFFFFFFl;
				String contentLength = header.getProperty("content-length");
				if (contentLength != null) {
					try {
						size = Integer.parseInt(contentLength);
					} catch (NumberFormatException ex) {
						ex.printStackTrace();
					}
				}
				int splitbyte = 0;
				boolean sbfound = false;
				while (splitbyte < rlen) {
					if (buf[splitbyte] == '\r' && buf[++splitbyte] == '\n'
							&& buf[++splitbyte] == '\r'
							&& buf[++splitbyte] == '\n') {
						sbfound = true;
						break;
					}
					splitbyte++;
				}
				splitbyte++;
				ByteArrayOutputStream bOut = new ByteArrayOutputStream();
				if (splitbyte < rlen){
					bOut.write(buf, splitbyte, rlen - splitbyte);
				}
				if (splitbyte < rlen){
					size -= rlen - splitbyte + 1;
				}else if (!sbfound || size == 0x7FFFFFFFFFFFFFFFl){
					size = 0;
				}
				buf = new byte[512];
				while (rlen >= 0 && size > 0) {
					rlen = is.read(buf, 0, 512);
					size -= rlen;
					if (rlen > 0){
						bOut.write(buf, 0, rlen);
					}
				}
				byte[] fbuf = bOut.toByteArray();
				ByteArrayInputStream bin = new ByteArrayInputStream(fbuf);
				BufferedReader in = new BufferedReader(new InputStreamReader(bin));
				if (method.equalsIgnoreCase("POST")) {
					String contentType = "";
					String contentTypeHeader = header.getProperty("content-type");
					StringTokenizer st = new StringTokenizer(contentTypeHeader, "; ");
					if (st.hasMoreTokens()) {
						contentType = st.nextToken();
					}
					if (contentType.equalsIgnoreCase("multipart/form-data")) {
						// Handle multipart/form-data
						if (!st.hasMoreTokens()){
							sendError(EHttpConst.HTTP_BADREQUEST,
									"BAD REQUEST: Content type is multipart/form-data but boundary missing.");
						}
						String boundaryExp = st.nextToken();
						st = new StringTokenizer(boundaryExp, "=");
						if (st.countTokens() != 2){
							sendError(EHttpConst.HTTP_BADREQUEST,
									"BAD REQUEST: Content type is multipart/form-data but boundary syntax error.");
						}
						st.nextToken();
						String boundary = st.nextToken();
						parseMultipartData(boundary, fbuf, in, parms, files);
					} else {
						// Handle application/x-www-form-urlencoded
						String postLine = "";
						char pbuf[] = new char[512];
						int read = in.read(pbuf);
						while (read >= 0 && !postLine.endsWith(EHttpConst.CRLF)) {
							postLine += String.valueOf(pbuf, 0, read);
							read = in.read(pbuf);
						}
						postLine = postLine.trim();
						parseParms(postLine, parms);
					}
				}
				if (method.equalsIgnoreCase("PUT")){
					files.put("content", saveTmpFile(fbuf, 0, bOut.size()));
				}
				HttpResponse rPonse = serve(uri, method, header, parms, files);
				if (rPonse == null){
					sendError(EHttpConst.HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: Serve() returned a null response.");
				}else{
					sendResponse(rPonse.mStatus, rPonse.mMimeType, rPonse.mHeader, rPonse.mData, rPonse);
				}
				in.close();
				is.close();
			} catch (IOException ioe) {
				try {
					sendError(EHttpConst.HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
				} catch (Throwable t) {
					t.printStackTrace();
				}
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}

		private void parseHeader(BufferedReader in, Properties pre, Properties parms, Properties header)throws InterruptedException {
			try {
				String inLine = in.readLine();
				if (inLine == null){
					return;
				}
				StringTokenizer st = new StringTokenizer(inLine);
				if (!st.hasMoreTokens()){
					sendError(EHttpConst.HTTP_BADREQUEST, "BAD REQUEST: Syntax error.");
				}
				String method = st.nextToken();
				pre.put("method", method);
				if (!st.hasMoreTokens()){
					sendError(EHttpConst.HTTP_BADREQUEST, "BAD REQUEST: Missing URI.");
				}
				String uri = st.nextToken();
				int qmi = uri.indexOf('?');
				if (qmi >= 0) {
					parseParms(uri.substring(qmi + 1), parms);
					uri = parsePercent(uri.substring(0, qmi));
				} else{
					uri = parsePercent(uri);
				}
				if (st.hasMoreTokens()) {
					String line = in.readLine();
					while (line != null && line.trim().length() > 0) {
						int p = line.indexOf(':');
						if (p >= 0){
							String key = line.substring(0, p).trim().toLowerCase();
							String value = line.substring(p + 1).trim();
							header.put(key, value);
						}
						line = in.readLine();
					}
				}
				pre.put("uri", uri);
			} catch (IOException ioe) {
				sendError(EHttpConst.HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
			}
		}

		private void parseMultipartData(String boundary, byte[] fbuf, BufferedReader in, Properties parms, Properties files)
				throws InterruptedException {
			try {
				int[] bpositions = getBoundaryPositions(fbuf, boundary.getBytes());
				int boundarycount = 1;
				String mpline = in.readLine();
				while (mpline != null) {
					if (mpline.indexOf(boundary) == -1)
						sendError(EHttpConst.HTTP_BADREQUEST,
								"BAD REQUEST: Content type is multipart/form-data but next chunk does not start with boundary");
					boundarycount++;
					Properties item = new Properties();
					mpline = in.readLine();
					while (mpline != null && mpline.trim().length() > 0) {
						int p = mpline.indexOf(':');
						if (p != -1){
							String key = mpline.substring(0, p).trim().toLowerCase();
							String value = mpline.substring(p + 1).trim();
							item.put(key, value);
						}
						mpline = in.readLine();
					}
					if (mpline != null) {
						String contentDisposition = item.getProperty("content-disposition");
						if (contentDisposition == null) {
							sendError(EHttpConst.HTTP_BADREQUEST,
									"BAD REQUEST: Content type is multipart/form-data but no content-disposition info found.");
						}
						StringTokenizer st = new StringTokenizer(contentDisposition, "; ");
						Properties disposition = new Properties();
						while (st.hasMoreTokens()) {
							String token = st.nextToken();
							int p = token.indexOf('=');
							if (p != -1){
								String key = token.substring(0, p).trim().toLowerCase();
								String value = token.substring(p + 1).trim();
								disposition.put(key, value);
							}
						}
						String pname = disposition.getProperty("name");
						pname = pname.substring(1, pname.length() - 1);
						String value = "";
						if (item.getProperty("content-type") == null) {
							while (mpline != null && mpline.indexOf(boundary) == -1) {
								mpline = in.readLine();
								if (mpline != null) {
									int d = mpline.indexOf(boundary);
									if (d == -1){
										value += mpline;
									}else{
										value += mpline.substring(0, d - 2);
									}
								}
							}
						} else {
							if (boundarycount > bpositions.length){
								sendError(EHttpConst.HTTP_INTERNALERROR, "Error processing request");
							}
							int offset = stripMultipartHeaders(fbuf, bpositions[boundarycount - 2]);
							String path = saveTmpFile(fbuf, offset, bpositions[boundarycount - 1] - offset - 4);
							files.put(pname, path);
							value = disposition.getProperty("filename");
							value = value.substring(1, value.length() - 1);
							do {
								mpline = in.readLine();
							} while (mpline != null && mpline.indexOf(boundary) == -1);
						}
						parms.put(pname, value);
					}
				}
			} catch (IOException ioe) {
				sendError(EHttpConst.HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
			}
		}

		public int[] getBoundaryPositions(byte[] b, byte[] boundary) {
			int matchcount = 0;
			int matchbyte = -1;
			Vector<Integer> matchbytes = new Vector<Integer>();
			for (int i = 0; i < b.length; i++) {
				if (b[i] == boundary[matchcount]) {
					if (matchcount == 0){
						matchbyte = i;
					}
					matchcount++;
					if (matchcount == boundary.length) {
						matchbytes.addElement(new Integer(matchbyte));
						matchcount = 0;
						matchbyte = -1;
					}
				} else {
					i -= matchcount;
					matchcount = 0;
					matchbyte = -1;
				}
			}
			int[] ret = new int[matchbytes.size()];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = ((Integer) matchbytes.elementAt(i)).intValue();
			}
			return ret;
		}

		private String saveTmpFile(byte[] b, int offset, int len) {
			String path = "";
			if (len > 0) {
				String tmpdir = System.getProperty("java.io.tmpdir");
				try {
					File temp = File.createTempFile("QDHTTPD", "", new File(tmpdir));
					OutputStream fstream = new FileOutputStream(temp);
					fstream.write(b, offset, len);
					fstream.close();
					path = temp.getAbsolutePath();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return path;
		}

		private int stripMultipartHeaders(byte[] b, int offset) {
			int i = 0;
			for (i = offset; i < b.length; i++) {
				if (b[i] == '\r' && b[++i] == '\n' && b[++i] == '\r'
						&& b[++i] == '\n')
					break;
			}
			return i + 1;
		}

		private String parsePercent(String str) throws InterruptedException {
			try {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < str.length(); i++) {
					char c = str.charAt(i);
					switch (c) {
					case '+':
						sb.append(' ');
						break;
					case '%':
						sb.append((char) Integer.parseInt(str.substring(i + 1, i + 3), 16));
						i += 2;
						break;
					default:
						sb.append(c);
						break;
					}
				}
				return sb.toString();
			} catch (Exception e) {
				sendError(EHttpConst.HTTP_BADREQUEST, "BAD REQUEST: Bad percent-encoding.");
				return null;
			}
		}

		private void parseParms(String parms, Properties p)throws InterruptedException {
			if (parms == null){
				return;
			}
			StringTokenizer st = new StringTokenizer(parms, "&");
			while (st.hasMoreTokens()) {
				String e = st.nextToken();
				int sep = e.indexOf('=');
				if (sep >= 0){
					p.put(parsePercent(e.substring(0, sep)).trim(),
							parsePercent(e.substring(sep + 1)));
				}
			}
		}

		private void sendError(String status, String msg) throws InterruptedException {
			sendResponse(status, EHttpConst.MIME_PLAINTEXT, null, new ByteArrayInputStream(msg.getBytes()), null);
			EUtil.logi(msg);
			throw new InterruptedException();
		}

		@SuppressWarnings("unused")
		private void sendResponse(String status, String mime, Properties header, InputStream data, HttpResponse repon) {
			try {
				if (status == null){
					throw new Error("sendResponse(): Status can't be null.");
				}
				OutputStream out = mSocket.getOutputStream();
				PrintWriter pw = new PrintWriter(out);
				pw.print("HTTP/1.0 " + status + " " + EHttpConst.CRLF);
				if (mime != null){
					pw.print("Content-Type: " + mime + EHttpConst.CRLF);
				}
				if (header == null || header.getProperty("Date") == null){
					pw.print("Date: " + EHttpConst.DateFormt.format(new Date()) + EHttpConst.CRLF);
				}
				if (header != null) {
					Enumeration<Object> e = header.keys();
					while (e.hasMoreElements()) {
						String key = (String) e.nextElement();
						String value = header.getProperty(key);
						pw.print(key + ": " + value + EHttpConst.CRLF);
					}
				}
				pw.print(EHttpConst.CRLF);
				pw.flush();
				int remain = (int)repon.mRemain;
				if (data != null) {
					int pending = data.available(); // order to support partial sends
					int bs = EHttpConst.outBufferSize;
					byte[] buff = new byte[bs];
					while (pending > 0) {
						int length = pending > bs ? bs : pending;
						int read = data.read(buff, 0, length);
						if (read <= 0){
							break;
						}
//						byte[] tb = EUtil.mediaDecode(buff, 0, read);
//						if(null != tb){
//							if(remain > 0){
//								int len = read - remain;
//								out.write(tb, remain, len);
//								remain = -1;
//							}else{
//								out.write(tb, 0, read);
//							}
//						}else{
//							;
//						}
//						pending -= read;
					}
				}
				out.flush();
				out.close();
				if (data != null){
					data.close();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
				try {
					mSocket.close();
				} catch (Throwable t) {
					;
				}
			}
		}
	}

	public HttpResponse fileServe(String uri, Properties header, File homeDir, boolean allowDirectoryListing) {
		HttpResponse res = null;
		if (!homeDir.isDirectory()){
			res = new HttpResponse(EHttpConst.HTTP_INTERNALERROR, EHttpConst.MIME_PLAINTEXT, "INTERNAL ERRROR: fileServe(): given homeDir is not a directory.");
		}
		if (res == null) {
			uri = uri.trim().replace(File.separatorChar, '/');
			if (uri.indexOf('?') >= 0){
				uri = uri.substring(0, uri.indexOf('?'));
			}
			if (uri.startsWith("..") || uri.endsWith("..") || uri.indexOf("../") >= 0){
				res = new HttpResponse(EHttpConst.HTTP_FORBIDDEN, EHttpConst.MIME_PLAINTEXT,
						"FORBIDDEN: Won't serve ../ for security reasons.");
			}
		}
		File file = new File(homeDir, uri);
		if (res == null && !file.exists()){
			res = new HttpResponse(EHttpConst.HTTP_NOTFOUND, EHttpConst.MIME_PLAINTEXT, "Error 404, file not found.");
		}
		try {
			if (res == null) {
				String mime = null;
				int dot = file.getCanonicalPath().lastIndexOf('.');
				if (dot >= 0){
					mime = (String) EHttpConst.MimeTypes.get(file.getCanonicalPath().substring(dot + 1).toLowerCase());
				}
				if (mime == null){
					mime = EHttpConst.MIME_DEFAULT_BINARY;
				}
				long fileLen = file.length();
				String etag = Integer.toHexString((file.getAbsolutePath() + file.lastModified() + fileLen).hashCode());
				long startFrom = 0;
				long endAt = -1;
				String range = header.getProperty("range");
				if (range != null) {
					if (range.startsWith("bytes=")) {
						range = range.substring("bytes=".length());
						int minus = range.indexOf('-');
						try {
							if (minus > 0) {
								startFrom = Long.parseLong(range.substring(0, minus));
								String rang = range.substring(minus + 1);
								if(null != rang && 0 != rang.length()){
									endAt = Long.parseLong(rang);
								}
							}
						} catch (NumberFormatException nfe) {
							nfe.printStackTrace();
						}
					}
				}
				final long reallyfileLength = fileLen - 256;
//				int headerByte = mQDPlayer.mIgnoreByte;
				if (range != null && startFrom >= 0) {
					if (startFrom >= reallyfileLength) {
						res = new HttpResponse(EHttpConst.HTTP_RANGE_NOT_SATISFIABLE, EHttpConst.MIME_PLAINTEXT, "");
						res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
						res.addHeader("ETag", etag);
					} else {
						if (endAt < 0){
							endAt = reallyfileLength - 1;
						}
						long newLen = endAt - startFrom + 1;
						if (newLen < 0){
							newLen = 0;
						}
						long remain = 0;
						long skip = startFrom;
						remain = startFrom % (EHttpConst.outBufferSize);
						if(remain > 0){
							skip = skip - remain;
						}
//						skip += headerByte;
						EUtil.logi("remain: " + remain);
						
						final long dataLen = newLen + remain;
						FileInputStream fis = new FileInputStream(file) {
							public int available() throws IOException {

								return (int) dataLen;
							}
						};
						fis.skip(skip);
						res = new HttpResponse(EHttpConst.HTTP_PARTIALCONTENT, mime, fis);
						res.addHeader("Content-Length", "" + newLen);
						res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + reallyfileLength);
						res.addHeader("ETag", etag);
						res.mRemain = remain;
						
						EUtil.logi("Range skip = " + skip);
						EUtil.logi("Range Content-Length = " + newLen);
						EUtil.logi("Range Content-Range = " + "bytes " + startFrom + "-" + endAt + "/" + reallyfileLength);
					}
				} else {
					if (etag.equals(header.getProperty("if-none-match"))){
						res = new HttpResponse(EHttpConst.HTTP_NOTMODIFIED, mime, "");
					}else {
						FileInputStream finput = new FileInputStream(file){
							public int available() throws IOException {
								return (int) reallyfileLength;
							}
						};
//						finput.skip(headerByte);
						res = new HttpResponse(EHttpConst.HTTP_OK, mime, finput);
						res.addHeader("Content-Length", "" + reallyfileLength);
						res.addHeader("ETag", etag);
					}
				}
			}
		} catch (IOException ioe) {
			res = new HttpResponse(EHttpConst.HTTP_FORBIDDEN, EHttpConst.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
		}
		res.addHeader("Accept-Ranges", "bytes"); 
		return res;
	}
}
