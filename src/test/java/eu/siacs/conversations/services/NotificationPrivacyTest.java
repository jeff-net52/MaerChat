package eu.siacs.conversations.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import android.app.Notification;
import android.content.Context;
import androidx.core.app.NotificationCompat;
import eu.siacs.conversations.R;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.ConscryptMode;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35)
@ConscryptMode(ConscryptMode.Mode.OFF)
public class NotificationPrivacyTest {

    @Test
    public void publicMessageVersionContainsNoContactOrMessagePreview() {
        final Context context = RuntimeEnvironment.getApplication();
        final Notification notification =
                NotificationPrivacy.buildPublicMessage(context, "messages", 3);

        assertEquals(NotificationCompat.VISIBILITY_PUBLIC, notification.visibility);
        assertEquals(
                context.getString(R.string.app_name),
                notification.extras.getCharSequence(Notification.EXTRA_TITLE));
        assertEquals(
                context.getResources().getQuantityString(R.plurals.x_messages, 3, 3),
                notification.extras.getCharSequence(Notification.EXTRA_TEXT));
        final String rendered = notification.extras.toString();
        assertFalse(rendered.contains("Alice"));
        assertFalse(rendered.contains("secret preview"));
    }

    @Test
    public void protectMessageMakesPrivateNotificationAndAddsPublicVersion() {
        final Context context = RuntimeEnvironment.getApplication();
        final NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, "messages")
                        .setContentTitle("Alice")
                        .setContentText("secret preview")
                        .setSmallIcon(R.drawable.ic_app_icon_notification);

        NotificationPrivacy.protectMessage(context, builder, "messages", 1);

        final Notification notification = builder.build();
        assertEquals(NotificationCompat.VISIBILITY_PRIVATE, notification.visibility);
        assertEquals(NotificationCompat.VISIBILITY_PUBLIC, notification.publicVersion.visibility);
        assertFalse(notification.publicVersion.extras.toString().contains("Alice"));
        assertFalse(notification.publicVersion.extras.toString().contains("secret preview"));
    }
}
