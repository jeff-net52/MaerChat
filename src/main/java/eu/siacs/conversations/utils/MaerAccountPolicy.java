package eu.siacs.conversations.utils;

import eu.siacs.conversations.BuildConfig;
import eu.siacs.conversations.xmpp.Jid;

/** Central account-domain policy for the MAER Chat distribution. */
public final class MaerAccountPolicy {

    private MaerAccountPolicy() {}

    public static boolean isCanonical(final Jid jid) {
        return jid != null && BuildConfig.DEFAULT_XMPP_DOMAIN.equals(jid.getDomain().toString());
    }
}
