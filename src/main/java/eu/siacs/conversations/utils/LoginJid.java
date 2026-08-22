package eu.siacs.conversations.utils;

import eu.siacs.conversations.xmpp.Jid;

/** Builds the bare JID used by the simplified Maer Chat sign-in flow. */
public final class LoginJid {

    private LoginJid() {}

    /**
     * Builds a bare account JID from user input.
     *
     * <p>In the default mode, {@code input} is a local username and {@code defaultDomain} is
     * appended. In advanced mode, {@code input} must already be a complete bare JID. Account
     * resources are deliberately rejected because Conversations assigns its own resource.
     *
     * @throws IllegalArgumentException when the input cannot identify an XMPP user account
     */
    public static Jid build(
            final String input, final boolean completeJid, final String defaultDomain) {
        final String normalizedInput = trimmedNonEmpty(input, "The account identifier is empty");
        if (completeJid) {
            final Jid jid = Jid.ofUserInput(normalizedInput);
            if (jid.getLocal() == null || !jid.isBareJid() || jid.isFullJid()) {
                throw new IllegalArgumentException("A complete bare user JID is required");
            }
            return jid.asBareJid();
        }

        if (normalizedInput.indexOf('@') >= 0 || normalizedInput.indexOf('/') >= 0) {
            throw new IllegalArgumentException("Only a local username is accepted");
        }
        final Jid domain = Jid.ofUserInput(trimmedNonEmpty(defaultDomain, "The domain is empty"));
        if (!domain.isDomainJid() || !domain.isBareJid() || domain.isFullJid()) {
            throw new IllegalArgumentException("The default domain is invalid");
        }
        return Jid.ofLocalAndDomain(normalizedInput, domain.toString()).asBareJid();
    }

    private static String trimmedNonEmpty(final String value, final String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        final String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return trimmed;
    }
}
