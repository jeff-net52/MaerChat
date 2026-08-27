package eu.siacs.conversations.xmpp.jingle;

final class ScreenSharingLifecycle {

    enum State {
        IDLE,
        STARTING,
        ACTIVE,
        STOPPING,
        CLOSED
    }

    private State state = State.IDLE;

    synchronized boolean beginStart() {
        if (state != State.IDLE) {
            return false;
        }
        state = State.STARTING;
        return true;
    }

    synchronized void started() {
        require(State.STARTING);
        state = State.ACTIVE;
    }

    synchronized void startFailed() {
        if (state == State.STARTING) {
            state = State.IDLE;
        }
    }

    synchronized boolean beginStop() {
        if (state != State.ACTIVE && state != State.STARTING) {
            return false;
        }
        state = State.STOPPING;
        return true;
    }

    synchronized void stopFailed() {
        if (state == State.STOPPING) {
            state = State.ACTIVE;
        }
    }

    synchronized void stopped() {
        if (state != State.CLOSED) {
            state = State.IDLE;
        }
    }

    synchronized boolean close() {
        final boolean wasSharing = isSharing();
        state = State.CLOSED;
        return wasSharing;
    }

    synchronized boolean isSharing() {
        return state == State.STARTING || state == State.ACTIVE || state == State.STOPPING;
    }

    synchronized State state() {
        return state;
    }

    private void require(final State expected) {
        if (state != expected) {
            throw new IllegalStateException("Expected " + expected + " but was " + state);
        }
    }
}
