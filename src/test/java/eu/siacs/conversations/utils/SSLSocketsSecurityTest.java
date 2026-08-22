package eu.siacs.conversations.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import org.junit.Assume;
import org.junit.Test;

public class SSLSocketsSecurityTest {

    @Test
    public void standardPolicyEnablesOnlyTls12AndTls13() throws Exception {
        try (final SSLSocket socket = socket()) {
            final Set<String> supported = Set.of(socket.getSupportedProtocols());
            final Set<String> expected =
                    Set.of("TLSv1.2", "TLSv1.3").stream()
                            .filter(supported::contains)
                            .collect(Collectors.toSet());
            assertFalse("Runtime must support a secure TLS protocol", expected.isEmpty());

            SSLSockets.setSecurity(socket, false);

            assertEquals(expected, Set.of(socket.getEnabledProtocols()));
        }
    }

    @Test
    public void strictPolicyEnablesOnlyTls13() throws Exception {
        try (final SSLSocket socket = socket()) {
            Assume.assumeTrue(Arrays.asList(socket.getSupportedProtocols()).contains("TLSv1.3"));

            SSLSockets.setSecurity(socket, true);

            assertEquals(Set.of("TLSv1.3"), Set.of(socket.getEnabledProtocols()));
        }
    }

    private static SSLSocket socket() throws Exception {
        return (SSLSocket) SSLContext.getDefault().getSocketFactory().createSocket();
    }
}
