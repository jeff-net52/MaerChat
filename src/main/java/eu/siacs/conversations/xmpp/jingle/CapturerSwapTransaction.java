package eu.siacs.conversations.xmpp.jingle;

/** Performs a capturer replacement without destroying the current capturer before commit. */
final class CapturerSwapTransaction {

    interface Participant {
        void start() throws InterruptedException;

        void stop() throws InterruptedException;

        void dispose();
    }

    private CapturerSwapTransaction() {}

    static void execute(
            final Participant current, final Participant replacement, final Runnable commit)
            throws InterruptedException {
        boolean replacementStarted = false;
        boolean currentStopAttempted = false;
        try {
            replacement.start();
            replacementStarted = true;
            currentStopAttempted = true;
            current.stop();
            commit.run();
        } catch (final InterruptedException | RuntimeException e) {
            if (replacementStarted) {
                safelyStop(replacement);
            }
            safelyDispose(replacement);
            if (currentStopAttempted) {
                safelyStart(current);
            }
            throw e;
        }
        safelyDispose(current);
    }

    private static void safelyStart(final Participant participant) {
        try {
            participant.start();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (final RuntimeException ignored) {
            // The original failure remains the actionable one.
        }
    }

    private static void safelyStop(final Participant participant) {
        try {
            participant.stop();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (final RuntimeException ignored) {
            // Cleanup is best effort; dispose follows.
        }
    }

    private static void safelyDispose(final Participant participant) {
        try {
            participant.dispose();
        } catch (final RuntimeException ignored) {
            // A committed swap must not be reported as failed because old cleanup failed.
        }
    }
}
