package eu.siacs.conversations.xmpp.jingle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CapturerSwapTransactionTest {

    @Test
    public void commitsOnlyAfterReplacementStartsAndCurrentStops() throws Exception {
        final FakeParticipant current = new FakeParticipant();
        current.started = true;
        final FakeParticipant replacement = new FakeParticipant();
        final boolean[] committed = {false};

        CapturerSwapTransaction.execute(current, replacement, () -> committed[0] = true);

        assertTrue(committed[0]);
        assertTrue(current.disposed);
        assertFalse(current.started);
        assertTrue(replacement.started);
        assertFalse(replacement.disposed);
    }

    @Test
    public void replacementStartFailureLeavesCurrentUntouched() {
        final FakeParticipant current = new FakeParticipant();
        current.started = true;
        final FakeParticipant replacement = new FakeParticipant();
        replacement.failStart = true;

        assertThrows(
                SecurityException.class,
                () -> CapturerSwapTransaction.execute(current, replacement, () -> {}));

        assertTrue(current.started);
        assertFalse(current.disposed);
        assertTrue(replacement.disposed);
    }

    @Test
    public void currentStopFailureRollsBackReplacementAndRestartsCurrent() {
        final FakeParticipant current = new FakeParticipant();
        current.started = true;
        current.startCount = 1;
        current.failStop = true;
        final FakeParticipant replacement = new FakeParticipant();

        assertThrows(
                IllegalStateException.class,
                () -> CapturerSwapTransaction.execute(current, replacement, () -> {}));

        assertEquals(2, current.startCount);
        assertTrue(current.started);
        assertFalse(current.disposed);
        assertTrue(replacement.disposed);
        assertFalse(replacement.started);
    }

    private static final class FakeParticipant implements CapturerSwapTransaction.Participant {
        boolean started;
        boolean disposed;
        boolean failStart;
        boolean failStop;
        int startCount;

        @Override
        public void start() {
            startCount++;
            if (failStart) {
                throw new SecurityException("capture denied");
            }
            started = true;
        }

        @Override
        public void stop() {
            started = false;
            if (failStop) {
                failStop = false;
                throw new IllegalStateException("stop failed");
            }
        }

        @Override
        public void dispose() {
            disposed = true;
        }
    }
}
