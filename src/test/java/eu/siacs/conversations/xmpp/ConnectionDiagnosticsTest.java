package eu.siacs.conversations.xmpp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import eu.siacs.conversations.TestCertificates;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.junit.Test;

public class ConnectionDiagnosticsTest {

    @Test
    public void computesColonSeparatedUppercaseSha256Fingerprint() {
        assertEquals(
                "BA:78:16:BF:8F:01:CF:EA:41:41:40:DE:5D:AE:22:23:B0:03:61:A3:96:17:7A:9C:B4:10:FF:61:F2:00:15:AD",
                ConnectionDiagnostics.sha256Fingerprint("abc".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void certificateSummaryContainsOnlyDisplayFieldsAndFingerprint() {
        final var summary =
                ConnectionDiagnostics.summarize(
                        "CN=contacts.chaumont.me\n",
                        "CN=Issuer\tCA",
                        new Date(1_000L),
                        new Date(2_000L),
                        "abc".getBytes(StandardCharsets.UTF_8));

        assertEquals("CN=contacts.chaumont.me", summary.subject());
        assertEquals("CN=Issuer CA", summary.issuer());
        assertEquals(1_000L, summary.validFrom());
        assertEquals(2_000L, summary.validUntil());
        assertEquals(
                ConnectionDiagnostics.sha256Fingerprint("abc".getBytes(StandardCharsets.UTF_8)),
                summary.sha256Fingerprint());
    }

    @Test
    public void summarizesX509CertificateWithoutRetainingCertificateOrKey() throws Exception {
        final X509Certificate certificate = TestCertificates.selfSignedX509();

        final var summary = ConnectionDiagnostics.summarize(certificate);

        assertTrue(summary.subject().contains("CN=diagnostics.test"));
        assertTrue(summary.issuer().contains("CN=diagnostics.test"));
        assertEquals(certificate.getNotBefore().getTime(), summary.validFrom());
        assertEquals(certificate.getNotAfter().getTime(), summary.validUntil());
        assertEquals(
                ConnectionDiagnostics.sha256Fingerprint(certificate.getEncoded()),
                summary.sha256Fingerprint());
    }

    @Test
    public void sanitizesTransportValuesAndDoesNotExposeNulls() {
        final var diagnostics =
                new ConnectionDiagnostics(
                        " contacts.chaumont.me\r\n",
                        null,
                        -1,
                        new ConnectionDiagnostics.TlsSummary(true, "TLSv1.3", " cipher\n", null));

        assertEquals("contacts.chaumont.me", diagnostics.resolvedServer());
        assertEquals("", diagnostics.remoteAddress());
        assertEquals(0, diagnostics.port());
        assertEquals("cipher", diagnostics.tls().cipherSuite());
        assertEquals("", diagnostics.tls().certificate().subject());
        assertFalse(diagnostics.tls().certificate().sha256Fingerprint().contains("null"));
    }
}
