package eu.siacs.conversations.utils;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Strict parser for the interoperable MAER-CALL/1 message contract emitted by Windows. */
public final class MaerCallInvite {
    public static final String ALLOWED_MEETING_HOST = "meet.jit.si";
    private static final Duration LIFETIME = Duration.ofHours(2);
    private static final Duration MAX_FUTURE_SKEW = Duration.ofMinutes(5);
    private static final String MUTED_FRAGMENT = "config.startWithVideoMuted=true";
    private static final Pattern TIMESTAMP =
            Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z");
    private static final Pattern HEADER =
            Pattern.compile(
                    "MAER-CALL/1 mode=(audio|video|screen) issued=([^ ]+) expires=([^ ]+)"
                            + " room=(MAER-[A-Za-z0-9]{16,128})");

    public enum Mode {
        AUDIO("Appel audio MAER — Invitation envoyée via la conversation XMPP."),
        VIDEO("Appel vidéo MAER — Invitation envoyée via la conversation XMPP."),
        SCREEN("Partage d’écran MAER — Invitation envoyée via la conversation XMPP.");

        private final String label;

        Mode(final String label) {
            this.label = label;
        }
    }

    private final Mode mode;
    private final Instant expiresAt;
    private final URI joinUri;

    private MaerCallInvite(final Mode mode, final Instant expiresAt, final URI joinUri) {
        this.mode = mode;
        this.expiresAt = expiresAt;
        this.joinUri = joinUri;
    }

    public static Optional<MaerCallInvite> parse(final String body, final Instant now) {
        if (body == null || now == null || body.indexOf('\r') >= 0) return Optional.empty();
        final String[] lines = body.split("\\n", -1);
        if (lines.length != 3) return Optional.empty();
        final Matcher matcher = HEADER.matcher(lines[1]);
        if (!matcher.matches()) return Optional.empty();
        final Mode mode = Mode.valueOf(matcher.group(1).toUpperCase(Locale.ROOT));
        if (!mode.label.equals(lines[0])
                || !TIMESTAMP.matcher(matcher.group(2)).matches()
                || !TIMESTAMP.matcher(matcher.group(3)).matches()) return Optional.empty();
        final Instant issuedAt;
        final Instant expiresAt;
        try {
            issuedAt = Instant.parse(matcher.group(2));
            expiresAt = Instant.parse(matcher.group(3));
        } catch (final RuntimeException e) {
            return Optional.empty();
        }
        if (!Duration.between(issuedAt, expiresAt).equals(LIFETIME)
                || issuedAt.isAfter(now.plus(MAX_FUTURE_SKEW))
                || !expiresAt.isAfter(now)) return Optional.empty();
        final String room = matcher.group(4);
        final URI uri;
        try {
            uri = URI.create(lines[2]);
        } catch (final RuntimeException e) {
            return Optional.empty();
        }
        final String fragment = mode == Mode.VIDEO ? null : MUTED_FRAGMENT;
        if (!"https".equals(uri.getScheme())
                || !ALLOWED_MEETING_HOST.equals(uri.getHost())
                || uri.getPort() != -1
                || uri.getRawUserInfo() != null
                || uri.getRawQuery() != null
                || !('/' + room).equals(uri.getRawPath())
                || !Objects.equals(fragment, uri.getRawFragment())) return Optional.empty();
        return Optional.of(new MaerCallInvite(mode, expiresAt, uri));
    }

    public Mode getMode() {
        return mode;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public URI getJoinUri() {
        return joinUri;
    }

    public boolean isJoinableAt(final Instant now) {
        return now != null && expiresAt.isAfter(now);
    }
}
