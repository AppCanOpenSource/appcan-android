package org.zywx.wbpalmstar.platform.certificates;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class HX509HostnameVerifierTrustAll implements HostnameVerifier {

    @Override
    public boolean verify(String host, SSLSession session) {
        return true;
    }

}
