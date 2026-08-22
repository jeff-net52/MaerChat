package eu.siacs.conversations.xmpp;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;

/** Immutable, non-secret details about the active XMPP transport. */
public record ConnectionDiagnostics(
        String resolvedServer, String remoteAddress, int port, TlsSummary tls) {

    private static final int MAX_TEXT_LENGTH = 512;

    public ConnectionDiagnostics {
        resolvedServer = sanitize(resolvedServer);
        remoteAddress = sanitize(remoteAddress);
        port = Math.max(port, 0);
        tls = tls == null ? TlsSummary.unavailable() : tls;
    }

    public record TlsSummary(
            boolean established,
            String protocol,
            String cipherSuite,
            CertificateSummary certificate) {

        public TlsSummary {
            protocol = sanitize(protocol);
            cipherSuite = sanitize(cipherSuite);
            certificate = certificate == null ? CertificateSummary.unavailable() : certificate;
        }

        public static TlsSummary unavailable() {
            return new TlsSummary(false, "", "", CertificateSummary.unavailable());
        }
    }

    public record CertificateSummary(
            String subject,
            String issuer,
            long validFrom,
            long validUntil,
            String sha256Fingerprint) {

        public CertificateSummary {
            subject = sanitize(subject);
            issuer = sanitize(issuer);
            validFrom = Math.max(validFrom, 0);
            validUntil = Math.max(validUntil, 0);
            sha256Fingerprint = sanitize(sha256Fingerprint);
        }

        public static CertificateSummary unavailable() {
            return new CertificateSummary("", "", 0, 0, "");
        }
    }

    public static CertificateSummary summarize(final X509Certificate certificate) {
        if (certificate == null) {
            return CertificateSummary.unavailable();
        }
        final byte[] encoded;
        try {
            encoded = certificate.getEncoded();
        } catch (final CertificateEncodingException e) {
            return summarize(
                    certificate.getSubjectX500Principal().getName(),
                    certificate.getIssuerX500Principal().getName(),
                    certificate.getNotBefore(),
                    certificate.getNotAfter(),
                    null);
        }
        return summarize(
                certificate.getSubjectX500Principal().getName(),
                certificate.getIssuerX500Principal().getName(),
                certificate.getNotBefore(),
                certificate.getNotAfter(),
                encoded);
    }

    static CertificateSummary summarize(
            final String subject,
            final String issuer,
            final Date validFrom,
            final Date validUntil,
            final byte[] encoded) {
        return new CertificateSummary(
                subject,
                issuer,
                validFrom == null ? 0 : validFrom.getTime(),
                validUntil == null ? 0 : validUntil.getTime(),
                encoded == null ? "" : sha256Fingerprint(encoded));
    }

    static String sha256Fingerprint(final byte[] encoded) {
        if (encoded == null || encoded.length == 0) {
            return "";
        }
        final byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256").digest(encoded);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
        final char[] alphabet = "0123456789ABCDEF".toCharArray();
        final StringBuilder fingerprint = new StringBuilder(digest.length * 3 - 1);
        for (int i = 0; i < digest.length; i++) {
            if (i > 0) {
                fingerprint.append(':');
            }
            final int value = digest[i] & 0xff;
            fingerprint.append(alphabet[value >>> 4]).append(alphabet[value & 0x0f]);
        }
        return fingerprint.toString();
    }

    static String sanitize(final String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        final StringBuilder sanitized =
                new StringBuilder(Math.min(value.length(), MAX_TEXT_LENGTH));
        boolean previousWasSpace = false;
        for (int offset = 0; offset < value.length() && sanitized.length() < MAX_TEXT_LENGTH; ) {
            final int codePoint = value.codePointAt(offset);
            offset += Character.charCount(codePoint);
            final boolean replaceWithSpace = Character.isISOControl(codePoint);
            final boolean isSpace = replaceWithSpace || Character.isWhitespace(codePoint);
            if (isSpace) {
                if (sanitized.length() > 0 && !previousWasSpace) {
                    sanitized.append(' ');
                    previousWasSpace = true;
                }
            } else {
                sanitized.appendCodePoint(codePoint);
                previousWasSpace = false;
            }
        }
        final int length = sanitized.length();
        if (length > 0 && sanitized.charAt(length - 1) == ' ') {
            sanitized.setLength(length - 1);
        }
        return sanitized.toString();
    }
}
