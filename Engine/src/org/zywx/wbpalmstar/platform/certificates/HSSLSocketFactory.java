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

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.params.HttpParams;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.SecureRandom;

public class HSSLSocketFactory extends SSLSocketFactory {

    private SSLContext mSSLContext;

    public HSSLSocketFactory(KeyStore ksP12, String keyPass) throws Exception {
        super(ksP12);
        mSSLContext = SSLContext.getInstance(SSLSocketFactory.TLS);
        KeyManagerFactory kMgrFact = null;
        TrustManager[] tMgrs = null;
        KeyManager[] kMgrs = null;
        TrustManager tMgr = null;
        tMgr = new HX509TrustManager(ksP12);
        kMgrFact = KeyManagerFactory.getInstance(Http.algorithm);
        if (null != keyPass) {
            kMgrFact.init(ksP12, keyPass.toCharArray());
        } else {
            kMgrFact.init(ksP12, null);
        }
        kMgrs = kMgrFact.getKeyManagers();
        tMgrs = new TrustManager[]{tMgr};
        SecureRandom secureRandom = new java.security.SecureRandom();
        mSSLContext.init(kMgrs, tMgrs, secureRandom);
        if (!Http.isCheckTrustCert()) {
            setHostnameVerifier(new HX509HostnameVerifier());
        } else {
            setHostnameVerifier(STRICT_HOSTNAME_VERIFIER);
        }
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
            throws IOException, UnknownHostException {
        javax.net.ssl.SSLSocketFactory socketfact = mSSLContext.getSocketFactory();
        Socket result = socketfact.createSocket(socket, host, port, autoClose);
        return result;
    }

    @Override
    public Socket connectSocket(Socket sock, String host, int port,
                                InetAddress localAddress, int localPort, HttpParams params)
            throws IOException {

        return super.connectSocket(sock, host, port, localAddress, localPort, params);
    }

    @Override
    public Socket createSocket() throws IOException {
        javax.net.ssl.SSLSocketFactory socketfact = mSSLContext.getSocketFactory();
        Socket result = socketfact.createSocket();
        return result;
    }
}
