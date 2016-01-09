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

import javax.net.ssl.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.SecureRandom;

public class HNetSSLSocketFactory extends SSLSocketFactory {

    private SSLContext mSSLContext;

    public HNetSSLSocketFactory(KeyStore ksP12, String keyPass) throws Exception {
        super();
        mSSLContext = SSLContext.getInstance("TLS");
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
    }

    @Override
    public String[] getDefaultCipherSuites() {
        javax.net.ssl.SSLSocketFactory socketfact = mSSLContext.getSocketFactory();
        return socketfact.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        javax.net.ssl.SSLSocketFactory socketfact = mSSLContext.getSocketFactory();
        return socketfact.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        javax.net.ssl.SSLSocketFactory socketfact = mSSLContext.getSocketFactory();
        Socket result = socketfact.createSocket(socket, host, port, autoClose);
        return result;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException,
            UnknownHostException {
        javax.net.ssl.SSLSocketFactory socketfact = mSSLContext.getSocketFactory();
        return socketfact.createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost,
                               int localPort) throws IOException, UnknownHostException {
        javax.net.ssl.SSLSocketFactory socketfact = mSSLContext.getSocketFactory();
        return socketfact.createSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        javax.net.ssl.SSLSocketFactory socketfact = mSSLContext.getSocketFactory();
        return socketfact.createSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port,
                               InetAddress localAddress, int localPort) throws IOException {
        javax.net.ssl.SSLSocketFactory socketfact = mSSLContext.getSocketFactory();
        return socketfact.createSocket(address, port, localAddress, localPort);
    }

}
