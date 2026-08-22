package eu.siacs.conversations;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public final class TestCertificates {

    private static final String SELF_SIGNED_X509 =
            "MIICMDCCAZmgAwIBAgIUI/yejypW9pSSNRJC3bU2U3Csm24wDQYJKoZIhvcNAQELBQAwKjEZMBcGA1UEAwwQZGlhZ25vc3RpY3Mu"
                + "dGVzdDENMAsGA1UECgwETUFFUjAeFw0yNjA4MjExNzM1MjJaFw0yNjA4MjIxNzM1MjJaMCoxGTAXBgNVBAMMEGRpYWdub3N0aWNz"
                + "LnRlc3QxDTALBgNVBAoMBE1BRVIwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAMDbwtX02OYvpNozY+rLsivBRk5N3y+rW5Ya"
                + "kjZnk4sIb0F58W8a1DpC/h8yOpF+SBXMviJiAHXBaRxtxRhfpuzVZXd7SuJm4IbSH9faiTe8nSl8lTXMBwrDseKcYTaSxtMcK3AQ"
                + "s69Go4sSgW735sWzILQv1qJemthbo6DJd+dfAgMBAAGjUzBRMB0GA1UdDgQWBBRnBeZbtRAXm5rIz01+XVsuwChNiDAfBgNVHSME"
                + "GDAWgBRnBeZbtRAXm5rIz01+XVsuwChNiDAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4GBAJvP93UMk43RG9tlkHeU"
                + "1J45xi7uEuVXTf4IpKBO7laQyyA1/R9E9O0ZfAu6cYOr38sqeWlGQSfOQjJJcCf3CahNybw0jf3/UXZkQL5VQLNFHoWXTrvL9/rc"
                + "QhgTyuhZRYazTc0HRuLblRfVF5iUeAwxq+u3VMvQKrJGTZQ5Hal7";

    private TestCertificates() {}

    public static X509Certificate selfSignedX509() throws Exception {
        return (X509Certificate)
                CertificateFactory.getInstance("X.509")
                        .generateCertificate(
                                new ByteArrayInputStream(
                                        Base64.getDecoder().decode(SELF_SIGNED_X509)));
    }
}
