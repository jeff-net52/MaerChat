package eu.siacs.conversations.entities;

import static org.junit.Assert.assertFalse;

import eu.siacs.conversations.xmpp.Jid;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.ConscryptMode;

@RunWith(RobolectricTestRunner.class)
@ConscryptMode(ConscryptMode.Mode.OFF)
public class AccountRegistrationDisabledTest {

    @Test
    public void registrationOptionCannotBeEnabled() {
        final Account account = new Account(Jid.of("alice@example.com"), "password");

        assertFalse(account.setOption(Account.OPTION_REGISTER, true));
        assertFalse(account.isOptionSet(Account.OPTION_REGISTER));
        assertFalse(
                (account.getContentValues().getAsInteger(Account.OPTIONS)
                                & (1 << Account.OPTION_REGISTER))
                        != 0);
    }

    @Test
    public void preAuthRegistrationTokenIsDiscarded() {
        final Account account = new Account(Jid.of("alice@example.com"), "password");

        account.setPreAuthRegistrationToken("sensitive-registration-token");

        assertFalse(account.getPreAuthRegistrationToken().isPresent());
        assertFalse(
                account.getContentValues()
                        .getAsString(Account.KEYS)
                        .contains("sensitive-registration-token"));
        assertFalse(
                account.getContentValues()
                        .getAsString(Account.KEYS)
                        .contains("pre_auth_registration"));
    }
}
