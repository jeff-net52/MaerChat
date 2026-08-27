package eu.siacs.conversations.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.VisibleForTesting;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/** Persists only hashes of approved pairing sessions to reject accidental local replay. */
public final class PairingReplayGuard {

    private static final String PREFERENCES = "maer_pairing_replay_guard_v1";
    private static final long MINIMUM_RETENTION_SECONDS = 600;

    private PairingReplayGuard() {}

    public static boolean isConsumed(
            final Context context, final String sessionId, final Instant now) {
        final SharedPreferences preferences = preferences(context);
        prune(preferences, now);
        return preferences.getLong(key(sessionId), 0L) > now.getEpochSecond();
    }

    public static void markConsumed(
            final Context context,
            final String sessionId,
            final Instant sessionExpiresAt,
            final Instant now) {
        final long retentionFloor =
                now.plus(MINIMUM_RETENTION_SECONDS, ChronoUnit.SECONDS).getEpochSecond();
        final long expiresAt = Math.max(retentionFloor, sessionExpiresAt.getEpochSecond());
        preferences(context).edit().putLong(key(sessionId), expiresAt).apply();
    }

    private static SharedPreferences preferences(final Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    private static void prune(final SharedPreferences preferences, final Instant now) {
        final SharedPreferences.Editor editor = preferences.edit();
        boolean changed = false;
        for (final Map.Entry<String, ?> entry : preferences.getAll().entrySet()) {
            final Object value = entry.getValue();
            if (!(value instanceof Long) || ((Long) value) <= now.getEpochSecond()) {
                editor.remove(entry.getKey());
                changed = true;
            }
        }
        if (changed) {
            editor.apply();
        }
    }

    @VisibleForTesting
    static String key(final String sessionId) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] bytes = digest.digest(sessionId.getBytes(StandardCharsets.US_ASCII));
            final StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (final byte value : bytes) {
                hex.append(Character.forDigit((value >>> 4) & 0x0f, 16));
                hex.append(Character.forDigit(value & 0x0f, 16));
            }
            return hex.toString();
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
