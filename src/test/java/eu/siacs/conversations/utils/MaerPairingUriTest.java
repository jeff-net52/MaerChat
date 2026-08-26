package eu.siacs.conversations.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.Test;

public class MaerPairingUriTest {

    private static final String SESSION = "S1M4g7D8u2kL9pQ3xY6w";

    @Test
    public void parsesValidUriIndependentOfQueryOrder() {
        final var pairingUri =
                MaerPairingUri.parse(
                        "maerchat://pair?code=482913&host=xmpp.maer.fr&sid=" + SESSION + "&v=1");

        assertEquals("xmpp.maer.fr", pairingUri.getHost());
        assertEquals(SESSION, pairingUri.getSessionId());
        assertEquals("482913", pairingUri.getVerificationCode());
        assertFalse(pairingUri.toString().contains(SESSION));
        assertFalse(pairingUri.toString().contains("482913"));
    }

    @Test
    public void rejectsNonCanonicalUris() {
        assertInvalid("https://pair?host=xmpp.maer.fr&sid=" + SESSION + "&code=482913&v=1");
        assertInvalid("maerchat://other?host=xmpp.maer.fr&sid=" + SESSION + "&code=482913&v=1");
        assertInvalid("MAERCHAT://pair?host=xmpp.maer.fr&sid=" + SESSION + "&code=482913&v=1");
        assertInvalid("maerchat://user@pair?host=xmpp.maer.fr&sid=" + SESSION + "&code=482913&v=1");
        assertInvalid("maerchat://pair:443?host=xmpp.maer.fr&sid=" + SESSION + "&code=482913&v=1");
        assertInvalid("maerchat://pair/?host=xmpp.maer.fr&sid=" + SESSION + "&code=482913&v=1");
        assertInvalid(
                "maerchat://pair?host=xmpp.maer.fr&sid=" + SESSION + "&code=482913&v=1#fragment");
    }

    @Test
    public void rejectsInvalidContractValues() {
        assertInvalid("maerchat://pair?host=legacy.example&sid=" + SESSION + "&code=482913&v=1");
        assertInvalid("maerchat://pair?host=xmpp.maer.fr&sid=" + SESSION + "&code=482913&v=2");
        assertInvalid("maerchat://pair?host=xmpp.maer.fr&sid=too-short&code=482913&v=1");
        assertInvalid("maerchat://pair?host=xmpp.maer.fr&sid=" + SESSION + "&code=12345&v=1");
        assertInvalid("maerchat://pair?host=xmpp.maer.fr&sid=" + SESSION + "&code=１２３４５６&v=1");
    }

    @Test
    public void rejectsAmbiguousQueries() {
        assertInvalid(
                "maerchat://pair?host=xmpp.maer.fr&sid="
                        + SESSION
                        + "&code=482913&code=482913&v=1");
        assertInvalid(
                "maerchat://pair?host=xmpp.maer.fr&sid=" + SESSION + "&code=482913&v=1&extra=x");
        assertInvalid("maerchat://pair?host=xmpp.maer.fr&sid=" + SESSION + "&code=482913&v=1&");
        assertInvalid("maerchat://pair?host=xmpp.maer.fr&sid=" + SESSION + "&code=482913&v=%31");
        assertInvalid("maerchat://pair?host=xmpp.maer.fr&sid=" + SESSION + "&code=482913&v=1+0");
    }

    @Test
    public void rejectsOversizedUri() {
        assertInvalid("maerchat://pair?" + "x".repeat(2049));
    }

    private static void assertInvalid(final String value) {
        try {
            MaerPairingUri.parse(value);
            fail("Expected URI to be rejected");
        } catch (final IllegalArgumentException expected) {
            // Expected.
        }
    }
}
