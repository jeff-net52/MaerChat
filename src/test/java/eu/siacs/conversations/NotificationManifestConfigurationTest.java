package eu.siacs.conversations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.ConscryptMode;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35)
@ConscryptMode(ConscryptMode.Mode.OFF)
public class NotificationManifestConfigurationTest {

    @Test
    public void notificationAndBackgroundPermissionsAreDeclared() throws Exception {
        final Context context = RuntimeEnvironment.getApplication();
        final PackageManager packageManager = context.getPackageManager();
        final PackageInfo packageInfo =
                packageManager.getPackageInfo(
                        context.getPackageName(), PackageManager.GET_PERMISSIONS);

        assertNotNull(packageInfo.requestedPermissions);
        final var permissions = Arrays.asList(packageInfo.requestedPermissions);
        assertTrue(permissions.contains(Manifest.permission.POST_NOTIFICATIONS));
        assertTrue(permissions.contains(Manifest.permission.RECEIVE_BOOT_COMPLETED));
        assertTrue(permissions.contains(Manifest.permission.FOREGROUND_SERVICE));
        assertTrue(permissions.contains(Manifest.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION));
    }

    @Test
    public void xmppServiceIsNotExported() throws Exception {
        final Context context = RuntimeEnvironment.getApplication();
        final PackageManager packageManager = context.getPackageManager();
        final ServiceInfo serviceInfo =
                packageManager.getServiceInfo(
                        new ComponentName(
                                context.getPackageName(),
                                "eu.siacs.conversations.services.XmppConnectionService"),
                        0);

        assertFalse(serviceInfo.exported);
        assertTrue(
                (serviceInfo.getForegroundServiceType()
                                & ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
                        != 0);
    }
}
