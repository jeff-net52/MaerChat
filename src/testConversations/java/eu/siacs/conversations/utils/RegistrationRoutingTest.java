package eu.siacs.conversations.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import eu.siacs.conversations.Config;
import eu.siacs.conversations.ui.WelcomeActivity;
import eu.siacs.conversations.xmpp.Jid;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.ConscryptMode;

@RunWith(RobolectricTestRunner.class)
@ConscryptMode(ConscryptMode.Mode.OFF)
public class RegistrationRoutingTest {

    @Test
    public void tokenRegistryAndRegistrationUiRemainDisabled() {
        assertTrue(Config.DISALLOW_REGISTRATION_IN_UI);
        assertFalse(SignupUtils.isSupportTokenRegistry());
    }

    @Test
    public void tokenRegistrationIntentRoutesToExistingAccountLoginWithoutToken() {
        final Context context = RuntimeEnvironment.getApplication();

        final Intent intent =
                SignupUtils.getTokenRegistrationIntent(
                        context, Jid.of("alice@example.com"), "sensitive-pre-auth-token");

        assertNotNull(intent.getComponent());
        assertEquals(WelcomeActivity.class.getName(), intent.getComponent().getClassName());
        assertNull(intent.getExtras());
    }

    @Test
    public void serverChooserRequestAlsoRoutesToExistingAccountLogin() {
        final Context context = RuntimeEnvironment.getApplication();

        final Intent intent = SignupUtils.getSignUpIntent(context, true);

        assertNotNull(intent.getComponent());
        assertEquals(WelcomeActivity.class.getName(), intent.getComponent().getClassName());
    }
}
