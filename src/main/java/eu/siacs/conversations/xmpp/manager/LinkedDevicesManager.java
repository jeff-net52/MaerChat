package eu.siacs.conversations.xmpp.manager;

import android.content.Context;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import eu.siacs.conversations.entities.LinkedDevice;
import eu.siacs.conversations.entities.PairingRequestInfo;
import eu.siacs.conversations.utils.MaerPairingUri;
import eu.siacs.conversations.xmpp.Jid;
import eu.siacs.conversations.xmpp.XmppConnection;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class LinkedDevicesManager extends AbstractManager {

    private static final int MAX_DEVICES = 100;
    private static final Pattern OPAQUE_ID = Pattern.compile("[A-Za-z0-9_-]{16,128}");
    private static final Pattern PLATFORM = Pattern.compile("[A-Za-z0-9._-]{1,32}");

    public LinkedDevicesManager(final Context context, final XmppConnection connection) {
        super(context, connection);
    }

    public boolean isAvailable() {
        return isCanonicalDomain(getAccount().getDomain()) && getAccount().isOnlineAndConnected();
    }

    public ListenableFuture<PairingRequestInfo> inspect(final MaerPairingUri pairingUri) {
        final ListenableFuture<PairingRequestInfo> unavailable = unavailableFuture();
        if (unavailable != null) {
            return unavailable;
        }
        return Futures.transform(
                connection.sendIqPacket(buildInspect(getAccount().getDomain(), pairingUri)),
                response -> {
                    ensureAuthenticatedResponse(response);
                    return parseSession(response, pairingUri.getSessionId());
                },
                MoreExecutors.directExecutor());
    }

    public ListenableFuture<String> approve(final MaerPairingUri pairingUri) {
        final ListenableFuture<String> unavailable = unavailableFuture();
        if (unavailable != null) {
            return unavailable;
        }
        return Futures.transform(
                connection.sendIqPacket(buildApprove(getAccount().getDomain(), pairingUri)),
                response -> {
                    ensureAuthenticatedResponse(response);
                    return parseApproved(response);
                },
                MoreExecutors.directExecutor());
    }

    public ListenableFuture<List<LinkedDevice>> getDevices() {
        final ListenableFuture<List<LinkedDevice>> unavailable = unavailableFuture();
        if (unavailable != null) {
            return unavailable;
        }
        return Futures.transform(
                connection.sendIqPacket(buildDevices(getAccount().getDomain())),
                response -> {
                    ensureAuthenticatedResponse(response);
                    return parseDevices(response);
                },
                MoreExecutors.directExecutor());
    }

    public ListenableFuture<Void> revoke(final String deviceId) {
        final ListenableFuture<Void> unavailable = unavailableFuture();
        if (unavailable != null) {
            return unavailable;
        }
        if (!isOpaqueId(deviceId)) {
            return Futures.immediateFailedFuture(
                    new IllegalArgumentException("Invalid linked device identifier"));
        }
        return Futures.transform(
                connection.sendIqPacket(buildRevoke(getAccount().getDomain(), deviceId)),
                response -> {
                    ensureAuthenticatedResponse(response);
                    parseRevoked(response, deviceId);
                    return null;
                },
                MoreExecutors.directExecutor());
    }

    private <T> ListenableFuture<T> unavailableFuture() {
        if (isAvailable()) {
            return null;
        }
        return Futures.immediateFailedFuture(
                new UnavailableException("Linked devices require an online MAER account"));
    }

    private void ensureAuthenticatedResponse(final Iq response) {
        if (!response.fromServer(getAccount())) {
            throw new MalformedResponseException("Unexpected pairing response sender");
        }
    }

    static Iq buildInspect(final Jid domain, final MaerPairingUri pairingUri) {
        requireCanonicalDomain(domain);
        final var inspect = new Inspect();
        inspect.setSession(pairingUri.getSessionId());
        inspect.setCode(pairingUri.getVerificationCode());
        return new Iq(Iq.Type.GET, domain, inspect);
    }

    static Iq buildApprove(final Jid domain, final MaerPairingUri pairingUri) {
        requireCanonicalDomain(domain);
        final var approve = new Approve();
        approve.setSession(pairingUri.getSessionId());
        approve.setCode(pairingUri.getVerificationCode());
        return new Iq(Iq.Type.SET, domain, approve);
    }

    static Iq buildDevices(final Jid domain) {
        requireCanonicalDomain(domain);
        return new Iq(Iq.Type.GET, domain, new Devices());
    }

    static Iq buildRevoke(final Jid domain, final String deviceId) {
        requireCanonicalDomain(domain);
        if (!isOpaqueId(deviceId)) {
            throw new IllegalArgumentException("Invalid linked device identifier");
        }
        final var revoke = new Revoke();
        revoke.setDeviceId(deviceId);
        return new Iq(Iq.Type.SET, domain, revoke);
    }

    static PairingRequestInfo parseSession(final Iq response, final String expectedSessionId) {
        requireResult(response);
        final var session = response.getOnlyExtension(Session.class);
        if (session == null
                || !isOpaqueId(session.getId())
                || !session.getId().equals(expectedSessionId)) {
            throw malformed();
        }
        final String label = requireDisplayText(session.getLabel(), 128);
        final String platform = requirePlatform(session.getPlatform());
        final Instant expiresAt = session.getExpiresAt();
        if (expiresAt == null) {
            throw malformed();
        }
        return new PairingRequestInfo(label, platform, expiresAt);
    }

    static String parseApproved(final Iq response) {
        requireResult(response);
        final var approved = response.getOnlyExtension(Approved.class);
        if (approved == null || !isOpaqueId(approved.getDeviceId())) {
            throw malformed();
        }
        return approved.getDeviceId();
    }

    static List<LinkedDevice> parseDevices(final Iq response) {
        requireResult(response);
        final var devices = response.getOnlyExtension(Devices.class);
        if (devices == null || devices.getDevices().size() > MAX_DEVICES) {
            throw malformed();
        }
        final var result = new ImmutableList.Builder<LinkedDevice>();
        final Set<String> seenIds = new HashSet<>();
        for (final Device device : devices.getDevices()) {
            final String id = device.getId();
            final String label = requireDisplayText(device.getLabel(), 128);
            final String platform = requirePlatform(device.getPlatform());
            final Instant createdAt = device.getCreatedAt();
            final Instant expiresAt = device.getExpiresAt();
            final String lastSeenValue = device.getLastSeenValue();
            final Instant lastSeenAt = device.getLastSeenAt();
            if (!isOpaqueId(id)
                    || !seenIds.add(id)
                    || createdAt == null
                    || expiresAt == null
                    || (lastSeenValue != null && lastSeenAt == null)) {
                throw malformed();
            }
            result.add(new LinkedDevice(id, label, platform, createdAt, lastSeenAt, expiresAt));
        }
        return result.build();
    }

    static void parseRevoked(final Iq response, final String expectedDeviceId) {
        requireResult(response);
        final var revoked = response.getOnlyExtension(Revoked.class);
        if (revoked == null
                || !isOpaqueId(revoked.getDeviceId())
                || !revoked.getDeviceId().equals(expectedDeviceId)) {
            throw malformed();
        }
    }

    private static void requireResult(final Iq response) {
        if (response == null || response.getType() != Iq.Type.RESULT) {
            throw malformed();
        }
    }

    private static String requireDisplayText(final String value, final int maximumLength) {
        if (value == null || value.isBlank() || value.length() > maximumLength) {
            throw malformed();
        }
        for (int i = 0; i < value.length(); i++) {
            if (Character.isISOControl(value.charAt(i))) {
                throw malformed();
            }
        }
        return value;
    }

    private static String requirePlatform(final String value) {
        if (value == null || !PLATFORM.matcher(value).matches()) {
            throw malformed();
        }
        return value;
    }

    private static boolean isOpaqueId(final String value) {
        return value != null && OPAQUE_ID.matcher(value).matches();
    }

    private static void requireCanonicalDomain(final Jid domain) {
        if (!isCanonicalDomain(domain)) {
            throw new IllegalArgumentException("Pairing is restricted to the MAER domain");
        }
    }

    private static boolean isCanonicalDomain(final Jid domain) {
        return domain != null && MaerPairingUri.CANONICAL_HOST.equals(domain.toString());
    }

    private static MalformedResponseException malformed() {
        return new MalformedResponseException("Malformed pairing response");
    }

    public static final class MalformedResponseException extends IllegalArgumentException {

        public MalformedResponseException(final String message) {
            super(message);
        }
    }

    public static final class UnavailableException extends IllegalStateException {

        public UnavailableException(final String message) {
            super(message);
        }
    }
}
