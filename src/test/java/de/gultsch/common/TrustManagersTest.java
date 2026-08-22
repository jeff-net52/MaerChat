package de.gultsch.common;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import eu.siacs.conversations.TestCertificates;
import java.security.KeyStore;
import java.security.cert.Certificate;
import org.junit.Test;

public class TrustManagersTest {

    @Test
    public void strictStoreCopiesSystemAuthoritiesAndRejectsUserAuthorities() throws Exception {
        final KeyStore source = KeyStore.getInstance(KeyStore.getDefaultType());
        source.load(null, null);
        final Certificate systemCertificate = TestCertificates.selfSignedX509();
        final Certificate userCertificate = TestCertificates.selfSignedX509();
        source.setCertificateEntry("system:root", systemCertificate);
        source.setCertificateEntry("user:local-proxy", userCertificate);

        final KeyStore destination = KeyStore.getInstance(KeyStore.getDefaultType());
        destination.load(null, null);

        assertEquals(1, TrustManagers.copySystemCertificates(source, destination));
        assertEquals(1, destination.size());
        assertArrayEquals(
                systemCertificate.getEncoded(),
                destination.getCertificate("system-0").getEncoded());
    }
}
