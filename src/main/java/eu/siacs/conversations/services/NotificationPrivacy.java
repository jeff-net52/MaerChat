package eu.siacs.conversations.services;

import android.app.Notification;
import android.content.Context;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.NotificationCompat;
import eu.siacs.conversations.R;

/** Applies a lock-screen-safe public representation to message notifications. */
final class NotificationPrivacy {

    private NotificationPrivacy() {}

    static void protectMessage(
            final Context context,
            final NotificationCompat.Builder privateBuilder,
            final String channelId,
            final int messageCount) {
        privateBuilder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE);
        privateBuilder.setPublicVersion(buildPublicMessage(context, channelId, messageCount));
    }

    @VisibleForTesting
    static Notification buildPublicMessage(
            final Context context, final String channelId, final int messageCount) {
        final int safeCount = Math.max(1, messageCount);
        return new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_app_icon_notification)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(
                        context.getResources()
                                .getQuantityString(R.plurals.x_messages, safeCount, safeCount))
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .build();
    }
}
