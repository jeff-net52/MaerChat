package eu.siacs.conversations.utils;

import static org.junit.Assert.*;

import java.time.Instant;
import org.junit.Test;

public class MaerCallInviteTest {
    private static final Instant NOW = Instant.parse("2026-08-27T12:00:00.000Z");
    private static final String ROOM = "MAER-1234567890ABCDEF";

    @Test
    public void parsesCanonicalWindowsVectors() {
        assertVector("audio", "Appel audio", "#config.startWithVideoMuted=true");
        assertVector("video", "Appel vidéo", "");
        assertVector("screen", "Partage d’écran", "#config.startWithVideoMuted=true");
    }

    @Test
    public void rejectsContractAndUrlTampering() {
        final String valid = vector("video", "Appel vidéo", "");
        final String[] invalid = {
            valid.replace("mode=video issued=", "issued=")
                    .replace(" expires=", " expires=2026-08-27T14:00:00.000Z mode=video "),
            valid.replace("14:00:00.000Z", "14:00:00Z"),
            valid.replace("14:00:00.000Z", "14:00:00.001Z"),
            valid.replace(ROOM + "\n", ROOM + "X\n"),
            valid.replace("Appel vidéo", "Appel audio"),
            valid.replace("https://", "http://"),
            valid.replace("meet.jit.si", "evil.meet.jit.si"),
            valid.replace("meet.jit.si/", "user@meet.jit.si/"),
            valid.replace("meet.jit.si/", "meet.jit.si:443/"),
            valid + "#config.startWithVideoMuted=true",
            valid + "?x=1",
            valid + "\n",
            valid.replace("\n", "\r\n"),
            vector("audio", "Appel audio", "")
        };
        for (final String body : invalid)
            assertTrue(body, MaerCallInvite.parse(body, NOW).isEmpty());
    }

    @Test
    public void enforcesExpiryLifetimeAndWindowsFutureSkew() {
        assertTrue(MaerCallInvite.parse(vector("video", "Appel vidéo", ""), NOW).isPresent());
        assertTrue(
                MaerCallInvite.parse(
                                vectorAt("2026-08-27T12:05:00.000Z", "2026-08-27T14:05:00.000Z"),
                                NOW)
                        .isPresent());
        assertTrue(
                MaerCallInvite.parse(
                                vectorAt("2026-08-27T12:05:00.001Z", "2026-08-27T14:05:00.001Z"),
                                NOW)
                        .isEmpty());
        assertTrue(
                MaerCallInvite.parse(
                                vectorAt("2026-08-27T12:00:00.000Z", "2026-08-27T14:00:00.001Z"),
                                NOW)
                        .isEmpty());
        assertTrue(
                MaerCallInvite.parse(
                                vector("video", "Appel vidéo", ""),
                                Instant.parse("2026-08-27T14:00:00.000Z"))
                        .isEmpty());
    }

    @Test
    public void revalidatesExpirationWhenTheUserClicks() {
        final MaerCallInvite invite =
                MaerCallInvite.parse(vector("video", "Appel vidéo", ""), NOW).orElseThrow();
        assertTrue(invite.isJoinableAt(Instant.parse("2026-08-27T13:59:59.999Z")));
        assertFalse(invite.isJoinableAt(Instant.parse("2026-08-27T14:00:00.000Z")));
        assertFalse(invite.isJoinableAt(null));
    }

    @Test
    public void emitsCanonicalMessagesAcceptedByTheStrictWindowsContract() {
        for (final MaerCallInvite.Mode mode : MaerCallInvite.Mode.values()) {
            final MaerCallInvite emitted = MaerCallInvite.create(mode, NOW, ROOM);
            final MaerCallInvite reparsed =
                    MaerCallInvite.parse(emitted.getMessageBody(), NOW).orElseThrow();
            assertEquals(mode, reparsed.getMode());
            assertEquals(emitted.getJoinUri(), reparsed.getJoinUri());
            assertEquals(Instant.parse("2026-08-27T14:00:00.000Z"), reparsed.getExpiresAt());
        }
        assertTrue(
                MaerCallInvite.create(MaerCallInvite.Mode.AUDIO, NOW, ROOM)
                        .getJoinUri()
                        .toString()
                        .endsWith("#config.startWithVideoMuted=true"));
        assertFalse(
                MaerCallInvite.create(MaerCallInvite.Mode.VIDEO, NOW, ROOM)
                        .getJoinUri()
                        .toString()
                        .contains("#"));
    }

    @Test
    public void normalizesIssuedTimeAndRejectsUnsafeRooms() {
        final MaerCallInvite invite =
                MaerCallInvite.create(
                        MaerCallInvite.Mode.VIDEO,
                        Instant.parse("2026-08-27T12:00:00.987654321Z"),
                        ROOM);
        assertTrue(invite.getMessageBody().contains("issued=2026-08-27T12:00:00.987Z"));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        MaerCallInvite.create(
                                MaerCallInvite.Mode.VIDEO, NOW, "MAER-../../evil-room-123456789"));
    }

    private static void assertVector(final String mode, final String label, final String fragment) {
        final MaerCallInvite invite =
                MaerCallInvite.parse(vector(mode, label, fragment), NOW).orElseThrow();
        assertEquals(mode.toUpperCase(), invite.getMode().name());
        assertEquals("https://meet.jit.si/" + ROOM + fragment, invite.getJoinUri().toString());
    }

    private static String vector(final String mode, final String label, final String fragment) {
        return label
                + " MAER — Invitation envoyée via la conversation XMPP.\n"
                + "MAER-CALL/1 mode="
                + mode
                + " issued=2026-08-27T12:00:00.000Z expires=2026-08-27T14:00:00.000Z room="
                + ROOM
                + "\n"
                + "https://meet.jit.si/"
                + ROOM
                + fragment;
    }

    private static String vectorAt(final String issued, final String expires) {
        return "Appel vidéo MAER — Invitation envoyée via la conversation XMPP.\n"
                + "MAER-CALL/1 mode=video issued="
                + issued
                + " expires="
                + expires
                + " room="
                + ROOM
                + "\n"
                + "https://meet.jit.si/"
                + ROOM;
    }
}
