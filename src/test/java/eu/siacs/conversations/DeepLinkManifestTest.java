package eu.siacs.conversations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import eu.siacs.conversations.ui.YuriLauncherActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.ConscryptMode;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35)
@ConscryptMode(ConscryptMode.Mode.OFF)
public class DeepLinkManifestTest {

    @Test
    public void onlyCanonicalXmppSchemeIsExportedByYuriLauncher() {
        final Context context = RuntimeEnvironment.getApplication();
        final PackageManager packageManager = context.getPackageManager();
        final ComponentName component = new ComponentName(context, YuriLauncherActivity.class);

        assertTrue(matches(packageManager, component, "xmpp:alice@xmpp.maer.fr"));
        assertFalse(matches(packageManager, component, "https://conversations.im/i/token"));
        assertFalse(matches(packageManager, component, "https://invite.joinjabber.org/#token"));
        assertFalse(matches(packageManager, component, "https://xmpp.link/#token"));
        assertFalse(matchesSendTo(packageManager, component, "imto://xmpp/alice@xmpp.maer.fr"));
        assertFalse(matchesSendTo(packageManager, component, "imto://jabber/alice@xmpp.maer.fr"));
    }

    private static boolean matches(
            final PackageManager packageManager, final ComponentName component, final String uri) {
        final Intent intent =
                new Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                        .setPackage(component.getPackageName());
        return packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                .stream()
                .anyMatch(info -> component.getClassName().equals(info.activityInfo.name));
    }

    private static boolean matchesSendTo(
            final PackageManager packageManager, final ComponentName component, final String uri) {
        final Intent intent =
                new Intent(Intent.ACTION_SENDTO, Uri.parse(uri))
                        .setPackage(component.getPackageName());
        return packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                .stream()
                .anyMatch(info -> component.getClassName().equals(info.activityInfo.name));
    }
}
