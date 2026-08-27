package eu.siacs.conversations.xmpp.jingle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ScreenSharingLifecycleTest {

    @Test
    public void startFailureReturnsToIdle() {
        final ScreenSharingLifecycle lifecycle = new ScreenSharingLifecycle();
        assertTrue(lifecycle.beginStart());
        lifecycle.startFailed();
        assertEquals(ScreenSharingLifecycle.State.IDLE, lifecycle.state());
        assertFalse(lifecycle.isSharing());
    }

    @Test
    public void failedExplicitStopKeepsSharingActive() {
        final ScreenSharingLifecycle lifecycle = activeLifecycle();
        assertTrue(lifecycle.beginStop());
        lifecycle.stopFailed();
        assertEquals(ScreenSharingLifecycle.State.ACTIVE, lifecycle.state());
    }

    @Test
    public void systemStopCanCompleteAfterExplicitStopFailure() {
        final ScreenSharingLifecycle lifecycle = activeLifecycle();
        assertTrue(lifecycle.beginStop());
        lifecycle.stopFailed();
        assertTrue(lifecycle.beginStop());
        lifecycle.stopped();
        assertEquals(ScreenSharingLifecycle.State.IDLE, lifecycle.state());
        assertFalse(lifecycle.isSharing());
    }

    @Test
    public void systemStopAndCloseAreIdempotent() {
        final ScreenSharingLifecycle lifecycle = activeLifecycle();
        assertTrue(lifecycle.beginStop());
        lifecycle.stopped();
        assertFalse(lifecycle.isSharing());
        assertFalse(lifecycle.close());
        assertEquals(ScreenSharingLifecycle.State.CLOSED, lifecycle.state());
    }

    @Test
    public void closingAnActiveCallRequiresForegroundCleanup() {
        final ScreenSharingLifecycle lifecycle = activeLifecycle();
        assertTrue(lifecycle.close());
        assertFalse(lifecycle.isSharing());
        assertEquals(ScreenSharingLifecycle.State.CLOSED, lifecycle.state());
    }

    private static ScreenSharingLifecycle activeLifecycle() {
        final ScreenSharingLifecycle lifecycle = new ScreenSharingLifecycle();
        assertTrue(lifecycle.beginStart());
        lifecycle.started();
        return lifecycle;
    }
}
