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

package org.zywx.wbpalmstar.platform.certificates;

import org.apache.http.conn.ssl.X509HostnameVerifier;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import java.io.IOException;
import java.security.cert.X509Certificate;


public class HX509HostnameVerifier implements X509HostnameVerifier {

    @Override
    public boolean verify(String host, SSLSession session) {

        return true;
    }

    @Override
    public void verify(String host, SSLSocket ssl) throws IOException {
        ;
    }

    @Override
    public void verify(String host, X509Certificate cert) throws SSLException {
        ;
    }

    @Override
    public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
        ;
    }

}
