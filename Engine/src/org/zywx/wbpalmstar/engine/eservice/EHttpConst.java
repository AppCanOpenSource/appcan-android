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

import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class EHttpConst {
    protected static final String HTTP_OK = "200 OK";
    protected static final String HTTP_PARTIALCONTENT = "206 Partial Content";
    protected static final String HTTP_RANGE_NOT_SATISFIABLE = "416 Requested Range Not Satisfiable";
    protected static final String HTTP_REDIRECT = "301 Moved Permanently";
    protected static final String HTTP_NOTMODIFIED = "304 Not Modified";
    protected static final String HTTP_FORBIDDEN = "403 Forbidden";
    protected static final String HTTP_NOTFOUND = "404 Not Found";
    protected static final String HTTP_BADREQUEST = "400 Bad Request";
    protected static final String HTTP_INTERNALERROR = "500 Internal Server Error";
    protected static final String HTTP_NOTIMPLEMENTED = "501 Not Implemented";

    protected static final String MIME_PLAINTEXT = "text/plain";
    protected static final String MIME_HTML = "text/html";
    protected static final String MIME_DEFAULT_BINARY = "application/octet-stream";
    protected static final String MIME_XML = "text/xml";

    protected static final String CRLF = "\r\n";

    protected static final int outBufferSize = 4 * 1024;

    protected static SimpleDateFormat DateFormt;
    protected static Hashtable<String, String> MimeTypes;

    static {
        MimeTypes = new Hashtable<String, String>();
        StringTokenizer st = new StringTokenizer(
                "css		text/css "
                        + "htm		text/html "
                        + "html		text/html "
                        + "xml		text/xml "
                        + "txt		text/plain "
                        + "asc		text/plain "
                        + "gif		image/gif "
                        + "jpg		image/jpeg "
                        + "jpeg		image/jpeg "
                        + "png		image/png "
                        + "mp3		audio/mpeg "
                        + "m3u		audio/mpeg-url "
                        + "mp4		video/mp4 "
                        + "qdv		video/mp4 "
                        + "ogv		video/ogg "
                        + "flv		video/x-flv "
                        + "mov		video/quicktime "
                        + "swf		application/x-shockwave-flash "
                        + "js		application/javascript "
                        + "pdf		application/pdf "
                        + "doc		application/msword "
                        + "ogg		application/x-ogg "
                        + "zip		application/octet-stream "
                        + "exe		application/octet-stream "
                        + "class	application/octet-stream ");
        while (st.hasMoreTokens()) {
            MimeTypes.put(st.nextToken(), st.nextToken());
        }
        DateFormt = new java.text.SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        DateFormt.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
}
