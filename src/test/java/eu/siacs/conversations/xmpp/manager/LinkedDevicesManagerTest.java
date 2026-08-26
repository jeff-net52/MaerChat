package eu.siacs.conversations.xmpp.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import eu.siacs.conversations.entities.LinkedDevice;
import eu.siacs.conversations.utils.MaerPairingUri;
import eu.siacs.conversations.xmpp.Jid;
import im.conversations.android.xmpp.model.maerpairing.Approve;
import im.conversations.android.xmpp.model.maerpairing.Approved;
import im.conversations.android.xmpp.model.maerpairing.Device;
import im.conversations.android.xmpp.model.maerpairing.Devices;
import im.conversations.android.xmpp.model.maerpairing.Inspect;
import im.conversations.android.xmpp.model.maerpairing.Revoke;
import im.conversations.android.xmpp.model.maerpairing.Revoked;
import im.conversations.android.xmpp.model.maerpairing.Session;
import im.conversations.android.xmpp.model.stanza.Iq;
import java.time.Instant;
import java.util.List;
import org.junit.Test;

public class LinkedDevicesManagerTest {

    private static final Jid DOMAIN = Jid.ofDomain("xmpp.maer.fr");
    private static final String SESSION = "S1M4g7D8u2kL9pQ3xY6w";
    private static final String DEVICE_ID = "device_1234567890abcdef";
    private static final MaerPairingUri PAIRING_URI =
            MaerPairingUri.parse(
                    "maerchat://pair?v=1&host=xmpp.maer.fr&sid=" + SESSION + "&code=482913");

    @Test
    public void buildsExactInspectIq() {
        final var iq = LinkedDevicesManager.buildInspect(DOMAIN, PAIRING_URI);
        final var inspect = iq.getOnlyExtension(Inspect.class);

        assertEquals(Iq.Type.GET, iq.getType());
        assertEquals(DOMAIN, iq.getTo());
        assertEquals("urn:maer:pairing:1", inspect.getNamespace());
        assertEquals(SESSION, inspect.getAttribute("session"));
        assertEquals("482913", inspect.getAttribute("code"));
    }

    @Test
    public void buildsExactApproveDevicesAndRevokeIq() {
        final var approveIq = LinkedDevicesManager.buildApprove(DOMAIN, PAIRING_URI);
        final var approve = approveIq.getOnlyExtension(Approve.class);
        assertEquals(Iq.Type.SET, approveIq.getType());
        assertEquals(DOMAIN, approveIq.getTo());
        assertEquals(SESSION, approve.getAttribute("session"));
        assertEquals("482913", approve.getAttribute("code"));

        final var devicesIq = LinkedDevicesManager.buildDevices(DOMAIN);
        assertEquals(Iq.Type.GET, devicesIq.getType());
        assertEquals(DOMAIN, devicesIq.getTo());
        assertEquals(
                "urn:maer:pairing:1", devicesIq.getOnlyExtension(Devices.class).getNamespace());

        final var revokeIq = LinkedDevicesManager.buildRevoke(DOMAIN, DEVICE_ID);
        final var revoke = revokeIq.getOnlyExtension(Revoke.class);
        assertEquals(Iq.Type.SET, revokeIq.getType());
        assertEquals(DOMAIN, revokeIq.getTo());
        assertEquals(DEVICE_ID, revoke.getAttribute("device-id"));
    }

    @Test
    public void rejectsRequestsForAnotherDomain() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        LinkedDevicesManager.buildInspect(
                                Jid.ofDomain("legacy.example"), PAIRING_URI));
    }

    @Test
    public void parsesInspectedSessionResponse() {
        final var session = new Session();
        session.setAttribute("id", SESSION);
        session.setAttribute("label", "PC Atelier");
        session.setAttribute("platform", "windows");
        session.setAttribute("expires", "2026-08-26T13:00:00Z");

        final var result =
                LinkedDevicesManager.parseSession(new Iq(Iq.Type.RESULT, session), SESSION);

        assertEquals("PC Atelier", result.getLabel());
        assertEquals("windows", result.getPlatform());
        assertEquals(Instant.parse("2026-08-26T13:00:00Z"), result.getExpiresAt());
    }

    @Test
    public void parsesApprovedAndRevokedResponses() {
        final var approved = new Approved();
        approved.setAttribute("device-id", DEVICE_ID);
        assertEquals(
                DEVICE_ID, LinkedDevicesManager.parseApproved(new Iq(Iq.Type.RESULT, approved)));

        final var revoked = new Revoked();
        revoked.setAttribute("device-id", DEVICE_ID);
        LinkedDevicesManager.parseRevoked(new Iq(Iq.Type.RESULT, revoked), DEVICE_ID);
    }

    @Test
    public void parsesDeviceListWithOptionalLastSeen() {
        final var devices = new Devices();
        final var active = devices.addExtension(device(DEVICE_ID));
        active.setAttribute("last-seen", "2026-08-26T12:00:00Z");
        devices.addExtension(device("device_abcdef1234567890"));

        final List<LinkedDevice> result =
                LinkedDevicesManager.parseDevices(new Iq(Iq.Type.RESULT, devices));

        assertEquals(2, result.size());
        assertEquals(Instant.parse("2026-08-26T12:00:00Z"), result.get(0).getLastSeenAt());
        assertNull(result.get(1).getLastSeenAt());
    }

    @Test
    public void rejectsMalformedResponses() {
        assertThrows(
                LinkedDevicesManager.MalformedResponseException.class,
                () -> LinkedDevicesManager.parseSession(new Iq(Iq.Type.RESULT), SESSION));
        assertThrows(
                LinkedDevicesManager.MalformedResponseException.class,
                () -> LinkedDevicesManager.parseApproved(new Iq(Iq.Type.GET, new Approved())));

        final var invalidTimestamp = device(DEVICE_ID);
        invalidTimestamp.setAttribute("created", "not-an-instant");
        final var invalidTimestampDevices = new Devices();
        invalidTimestampDevices.addExtension(invalidTimestamp);
        assertThrows(
                LinkedDevicesManager.MalformedResponseException.class,
                () ->
                        LinkedDevicesManager.parseDevices(
                                new Iq(Iq.Type.RESULT, invalidTimestampDevices)));

        final var duplicateDevices = new Devices();
        duplicateDevices.addExtension(device(DEVICE_ID));
        duplicateDevices.addExtension(device(DEVICE_ID));
        assertThrows(
                LinkedDevicesManager.MalformedResponseException.class,
                () -> LinkedDevicesManager.parseDevices(new Iq(Iq.Type.RESULT, duplicateDevices)));

        final var wrongRevoked = new Revoked();
        wrongRevoked.setAttribute("device-id", "device_abcdef1234567890");
        assertThrows(
                LinkedDevicesManager.MalformedResponseException.class,
                () ->
                        LinkedDevicesManager.parseRevoked(
                                new Iq(Iq.Type.RESULT, wrongRevoked), DEVICE_ID));

        final var wrongSession = new Session();
        wrongSession.setAttribute("id", "different_session_1234");
        wrongSession.setAttribute("label", "PC Atelier");
        wrongSession.setAttribute("platform", "windows");
        wrongSession.setAttribute("expires", "2026-08-26T13:00:00Z");
        assertThrows(
                LinkedDevicesManager.MalformedResponseException.class,
                () ->
                        LinkedDevicesManager.parseSession(
                                new Iq(Iq.Type.RESULT, wrongSession), SESSION));
    }

    private static Device device(final String id) {
        final var device = new Device();
        device.setAttribute("id", id);
        device.setAttribute("label", "PC Atelier");
        device.setAttribute("platform", "windows");
        device.setAttribute("created", "2026-08-25T12:00:00Z");
        device.setAttribute("expires", "2026-09-25T12:00:00Z");
        return device;
    }
}
