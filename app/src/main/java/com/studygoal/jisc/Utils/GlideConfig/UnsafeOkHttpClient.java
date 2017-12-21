package com.studygoal.jisc.Utils.GlideConfig;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;

/**
 * Glide unsafe Ok Http Client Class
 * <p>
 * Provides an unsafe http client if the connection is ok.
 *
 * @author Therapy Box
 * @version 1.5
 * @date unknown
 */
public class UnsafeOkHttpClient {

    /**
     * Gets the unsafe http client.
     *
     * @param appContext application context
     * @return client object
     */
    public static OkHttpClient getUnsafeOkHttpClient(final Context appContext) {
        try {
            final SSLContext sslContext = setCertificate(appContext);
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory)
                    .protocols(Arrays.asList(Protocol.HTTP_1_1))
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    })
                    .build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the certificate for the client.
     *
     * @param appContext application context
     * @return SSL context object
     */
    private static SSLContext setCertificate(Context appContext) {
        SSLContext result = null;

        try {
            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // From https://www.washington.edu/itconnect/security/ca/load-der.crt
            InputStream caInput = new BufferedInputStream(appContext.getAssets().open("cert/certificate.crt"));
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            result = SSLContext.getInstance("TLS");
            result.init(null, tmf.getTrustManagers(), null);
            SSLContext.setDefault(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}