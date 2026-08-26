package eu.siacs.conversations.entities;

import java.time.Instant;
import java.util.Objects;

public final class PairingRequestInfo {

    private final String label;
    private final String platform;
    private final Instant expiresAt;

    public PairingRequestInfo(
            final String label, final String platform, final Instant expiresAt) {
        this.label = Objects.requireNonNull(label);
        this.platform = Objects.requireNonNull(platform);
        this.expiresAt = Objects.requireNonNull(expiresAt);
    }

    public String getLabel() {
        return label;
    }

    public String getPlatform() {
        return platform;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
