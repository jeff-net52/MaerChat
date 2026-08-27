package eu.siacs.conversations.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import java.time.Instant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.ConscryptMode;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35)
@ConscryptMode(ConscryptMode.Mode.OFF)
public class PairingReplayGuardTest {

    @Test
    public void approvedSessionIsHashedBlockedAndEventuallyPruned() {
        final Context context = RuntimeEnvironment.getApplication();
        final String session = "S1M4g7D8u2kL9pQ3xY6w";
        final Instant now = Instant.parse("2026-08-27T12:00:00Z");

        assertNotEquals(session, PairingReplayGuard.key(session));
        assertFalse(PairingReplayGuard.isConsumed(context, session, now));
        PairingReplayGuard.markConsumed(context, session, now.plusSeconds(300), now);
        assertTrue(PairingReplayGuard.isConsumed(context, session, now.plusSeconds(599)));
        assertFalse(PairingReplayGuard.isConsumed(context, session, now.plusSeconds(601)));
    }
}
