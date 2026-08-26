package eu.siacs.conversations.entities;

import java.time.Instant;
import java.util.Objects;

public final class LinkedDevice {

    private final String id;
    private final String label;
    private final String platform;
    private final Instant createdAt;
    private final Instant lastSeenAt;
    private final Instant expiresAt;

    public LinkedDevice(
            final String id,
            final String label,
            final String platform,
            final Instant createdAt,
            final Instant lastSeenAt,
            final Instant expiresAt) {
        this.id = Objects.requireNonNull(id);
        this.label = Objects.requireNonNull(label);
        this.platform = Objects.requireNonNull(platform);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.lastSeenAt = lastSeenAt;
        this.expiresAt = Objects.requireNonNull(expiresAt);
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getPlatform() {
        return platform;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof LinkedDevice that)) {
            return false;
        }
        return id.equals(that.id)
                && label.equals(that.label)
                && platform.equals(that.platform)
                && createdAt.equals(that.createdAt)
                && Objects.equals(lastSeenAt, that.lastSeenAt)
                && expiresAt.equals(that.expiresAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, platform, createdAt, lastSeenAt, expiresAt);
    }
}
