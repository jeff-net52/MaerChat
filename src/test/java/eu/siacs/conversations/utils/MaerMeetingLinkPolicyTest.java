package eu.siacs.conversations.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MaerMeetingLinkPolicyTest {
    @Test
    public void suppressesCanonicalExpiredTamperedAndRawMaerMeetingLinks() {
        final String base = "https://meet.jit.si/MAER-1234567890ABCDEF";
        assertTrue(MaerMeetingLinkPolicy.mustSuppressRawLink(base));
        assertTrue(
                MaerMeetingLinkPolicy.mustSuppressRawLink(
                        base + "#config.startWithVideoMuted=true"));
        assertTrue(MaerMeetingLinkPolicy.mustSuppressRawLink(base + "?tampered=true"));
    }

    @Test
    public void preservesOrdinaryLinks() {
        assertFalse(MaerMeetingLinkPolicy.mustSuppressRawLink("https://example.org/help"));
        assertFalse(MaerMeetingLinkPolicy.mustSuppressRawLink("https://meet.jit.si/team-room"));
        assertFalse(
                MaerMeetingLinkPolicy.mustSuppressRawLink(
                        "https://evil.meet.jit.si/MAER-1234567890ABCDEF"));
    }
}
