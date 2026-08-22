package de.gultsch.common;

import android.content.Context;
import android.os.Build;
import androidx.annotation.Nullable;
import com.google.common.collect.Iterables;
import eu.siacs.conversations.R;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Enumeration;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public final class TrustManagers {

    private static final char[] BUNDLED_KEYSTORE_PASSWORD = "letsencrypt".toCharArray();

    private TrustManagers() {
        throw new IllegalStateException("Do not instantiate me");
    }

    public static X509TrustManager createTrustManager(@Nullable final KeyStore keyStore)
            throws NoSuchAlgorithmException, KeyStoreException {
        final TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        return Iterables.getOnlyElement(
                Iterables.filter(
                        Arrays.asList(trustManagerFactory.getTrustManagers()),
                        X509TrustManager.class));
    }

    public static X509TrustManager createForAndroidVersion(final Context context)
            throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            return TrustManagers.createDefaultWithBundledLetsEncrypt(context);
        } else {
            return TrustManagers.createDefaultTrustManager();
        }
    }

    public static X509TrustManager createDefaultTrustManager()
            throws NoSuchAlgorithmException, KeyStoreException {
        return createTrustManager(null);
    }

    /**
     * Builds a trust manager from Android's system CA partition only.
     *
     * <p>The platform default trust manager can include user-installed certificate authorities on
     * Android 6. Maer Chat uses this method for unattended XMPP and HTTP connections so a local
     * user CA cannot silently intercept them.
     */
    public static X509TrustManager createSystemTrustManager()
            throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        final KeyStore androidCaStore = KeyStore.getInstance("AndroidCAStore");
        androidCaStore.load(null);

        final KeyStore systemCaStore = KeyStore.getInstance(KeyStore.getDefaultType());
        systemCaStore.load(null, null);
        final int certificateCount = copySystemCertificates(androidCaStore, systemCaStore);
        if (certificateCount == 0) {
            throw new KeyStoreException("Android system CA store is empty");
        }
        return createTrustManager(systemCaStore);
    }

    static int copySystemCertificates(final KeyStore source, final KeyStore destination)
            throws KeyStoreException {
        int certificateCount = 0;
        final Enumeration<String> aliases = source.aliases();
        while (aliases.hasMoreElements()) {
            final String alias = aliases.nextElement();
            if (!alias.startsWith("system:")) {
                continue;
            }
            final Certificate certificate = source.getCertificate(alias);
            if (certificate != null) {
                destination.setCertificateEntry("system-" + certificateCount++, certificate);
            }
        }
        return certificateCount;
    }

    private static X509TrustManager createDefaultWithBundledLetsEncrypt(final Context context)
            throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        final var bundleTrustManager =
                createWithKeyStore(context.getResources().openRawResource(R.raw.letsencrypt));
        return CombiningTrustManager.combineWithDefault(bundleTrustManager);
    }

    private static X509TrustManager createWithKeyStore(final InputStream inputStream)
            throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException {
        final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(inputStream, BUNDLED_KEYSTORE_PASSWORD);
        return TrustManagers.createTrustManager(keyStore);
    }
}
