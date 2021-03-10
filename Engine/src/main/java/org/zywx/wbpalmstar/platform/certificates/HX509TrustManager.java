package org.zywx.wbpalmstar.platform.certificates;

import org.zywx.wbpalmstar.base.BDebug;

import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class HX509TrustManager implements X509TrustManager {

    private static final String TAG = "HX509TrustManager";

    private volatile static X509TrustManager mDefaultTrustManager;
    private final X509TrustManager mTrustManager;

    public HX509TrustManager(KeyStore ksP12) throws Exception {
        TrustManagerFactory tFactory = TrustManagerFactory
                .getInstance(Http.algorithm);
        tFactory.init(ksP12);
        if (ksP12 != null){
            BDebug.i(TAG, "ksP12 size is " + ksP12.size());
        }
        TrustManager[] trustMgr = tFactory.getTrustManagers();
        if (trustMgr.length == 0) {
            throw new NoSuchAlgorithmException("no trust manager found");
        }
        mTrustManager = (X509TrustManager) trustMgr[0];
        if (mDefaultTrustManager == null) {
            synchronized (HX509TrustManager.class) {
                if (mDefaultTrustManager == null) {
                    // 初始化系统默认证书库的trustManager
                    TrustManagerFactory defaultTFactory = TrustManagerFactory
                            .getInstance(Http.algorithm);
                    defaultTFactory.init((KeyStore) null);
                    TrustManager[] defaultTrustMgr = defaultTFactory.getTrustManagers();
                    if (defaultTrustMgr.length == 0) {
                        throw new NoSuchAlgorithmException(
                                "no default trust manager found");
                    }
                    mDefaultTrustManager = (X509TrustManager) defaultTrustMgr[0];
                }
            }
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        BDebug.e(TAG, "checkClientTrusted: this method should not be invoked.");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        if (Http.isCheckTrustCert()) {
            try {
                // 验证当前时间是否在证书链中证书的有效期内
                if(chain != null){
                    BDebug.i(TAG, "checkServerTrusted:  ServerCertificateChain Size is " + chain.length);
                    for (X509Certificate cert : chain){
                        cert.checkValidity();
                        if (BDebug.isDebugMode()) {
                            String certStr = "\nSubjectDN:" + cert.getSubjectDN().getName()
                                    + "\nIssuerDN:" + cert.getIssuerDN().getName()
                                    + "\nNotBefore:" + cert.getNotBefore()
                                    + "\nNotAfter:" + cert.getNotAfter();
                            BDebug.i(TAG, "checkServerTrusted:  " + certStr);
                        }
                    }
                }
                // 验证证书是否可信
                mTrustManager.checkServerTrusted(chain, authType);
            } catch (Exception e) {
                try {
                    mDefaultTrustManager.checkServerTrusted(chain, authType);
                } catch (CertificateException certificateException) {
                    // 系统信任区也失败的话，就抛出异常。抛出之前先打印一下上一个错误。
                    if (BDebug.isDebugMode()){
                        e.printStackTrace();
                    }
                    throw certificateException;
                }
            }
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        X509Certificate[] certs = mTrustManager.getAcceptedIssuers();
        if (BDebug.isDebugMode()) {
            if (certs != null){
                BDebug.i(TAG, "getAcceptedIssuers certs size is " + certs.length);
                for (X509Certificate cert : certs) {
                    String certStr = "S:" + cert.getSubjectDN().getName() + "\nI:" + cert.getIssuerDN().getName();
                    BDebug.i(TAG, "getAcceptedIssuers:  " + certStr);
                }
            }
        }
        return certs;
    }

}
