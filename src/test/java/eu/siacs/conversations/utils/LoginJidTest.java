package eu.siacs.conversations.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LoginJidTest {

    private static final String DEFAULT_DOMAIN = "xmpp.maer.fr";

    @Test
    public void appendsDefaultDomainToUsername() {
        assertEquals(
                "emilien@xmpp.maer.fr",
                LoginJid.build("emilien", false, DEFAULT_DOMAIN).toString());
    }

    @Test
    public void trimsInputBeforeBuildingJid() {
        assertEquals(
                "emilien@xmpp.maer.fr",
                LoginJid.build("  emilien\n", false, " xmpp.maer.fr ").toString());
    }

    @Test
    public void appliesXmppNormalization() {
        assertEquals(
                "emilien@xmpp.maer.fr",
                LoginJid.build("EMILIEN", false, DEFAULT_DOMAIN).toString());
    }

    @Test
    public void acceptsUnicodeUsername() {
        assertEquals(
                "émilie@xmpp.maer.fr",
                LoginJid.build("Émilie", false, DEFAULT_DOMAIN).toString());
    }

    @Test
    public void acceptsBareJidInAdvancedMode() {
        assertEquals(
                "someone@example.org",
                LoginJid.build("someone@example.org", true, DEFAULT_DOMAIN).toString());
    }

    @Test
    public void acceptsInternationalizedDomainInAdvancedMode() {
        assertEquals(
                "someone@ουτοπία.δπθ.gr",
                LoginJid.build("someone@xn--kxae4bafwg.xn--pxaix.gr", true, DEFAULT_DOMAIN)
                        .toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNullUsername() {
        LoginJid.build(null, false, DEFAULT_DOMAIN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsBlankUsername() {
        LoginJid.build(" \n\t ", false, DEFAULT_DOMAIN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsCompleteJidInUsernameMode() {
        LoginJid.build("emilien@xmpp.maer.fr", false, DEFAULT_DOMAIN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsResourceInUsernameMode() {
        LoginJid.build("emilien/phone", false, DEFAULT_DOMAIN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsDomainOnlyInAdvancedMode() {
        LoginJid.build("xmpp.maer.fr", true, DEFAULT_DOMAIN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsResourceInAdvancedMode() {
        LoginJid.build("emilien@xmpp.maer.fr/phone", true, DEFAULT_DOMAIN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidAdvancedDomain() {
        LoginJid.build("emilien@not a domain", true, DEFAULT_DOMAIN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMissingDefaultDomain() {
        LoginJid.build("emilien", false, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsJidAsDefaultDomain() {
        LoginJid.build("emilien", false, "other@example.org");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsResourceAsDefaultDomain() {
        LoginJid.build("emilien", false, "example.org/phone");
    }
}
