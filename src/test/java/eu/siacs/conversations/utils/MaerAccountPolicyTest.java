package eu.siacs.conversations.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import eu.siacs.conversations.xmpp.Jid;
import org.junit.Test;

public class MaerAccountPolicyTest {

    @Test
    public void acceptsCanonicalAccount() {
        assertTrue(MaerAccountPolicy.isCanonical(Jid.of("emilien@xmpp.maer.fr")));
    }

    @Test
    public void rejectsLegacyAndMissingAccounts() {
        assertFalse(MaerAccountPolicy.isCanonical(Jid.of("emilien@legacy.example")));
        assertFalse(MaerAccountPolicy.isCanonical(null));
    }
}
