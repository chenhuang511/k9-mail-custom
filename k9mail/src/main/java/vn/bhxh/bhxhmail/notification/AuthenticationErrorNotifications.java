package vn.bhxh.bhxhmail.notification;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.NotificationManagerCompat;

import vn.bhxh.bhxhmail.Account;
import vn.bhxh.bhxhmail.activity.setup.AccountSetupIncoming;
import vn.bhxh.bhxhmail.activity.setup.AccountSetupOutgoing;


class AuthenticationErrorNotifications {
    private final NotificationController controller;


    public AuthenticationErrorNotifications(NotificationController controller) {
        this.controller = controller;
    }

    public void showAuthenticationErrorNotification(Account account, boolean incoming) {
        int notificationId = NotificationIds.getAuthenticationErrorNotificationId(account, incoming);
        Context context = controller.getContext();

        PendingIntent editServerSettingsPendingIntent = createContentIntent(context, account, incoming);
        String title = context.getString(vn.bhxh.bhxhmail.R.string.notification_authentication_error_title);
        String text = context.getString(vn.bhxh.bhxhmail.R.string.notification_authentication_error_text, account.getDescription());

        NotificationCompat.Builder builder = controller.createNotificationBuilder()
                .setSmallIcon(vn.bhxh.bhxhmail.R.drawable.notification_icon_warning)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setTicker(title)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(editServerSettingsPendingIntent)
                .setStyle(new BigTextStyle().bigText(text))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        controller.configureNotification(builder, null, null,
                NotificationController.NOTIFICATION_LED_FAILURE_COLOR,
                NotificationController.NOTIFICATION_LED_BLINK_FAST, true);

        getNotificationManager().notify(notificationId, builder.build());
    }

    public void clearAuthenticationErrorNotification(Account account, boolean incoming) {
        int notificationId = NotificationIds.getAuthenticationErrorNotificationId(account, incoming);
        getNotificationManager().cancel(notificationId);
    }

    PendingIntent createContentIntent(Context context, Account account, boolean incoming) {
        Intent editServerSettingsIntent = incoming ?
                AccountSetupIncoming.intentActionEditIncomingSettings(context, account) :
                AccountSetupOutgoing.intentActionEditOutgoingSettings(context, account);

        return PendingIntent.getActivity(context, account.getAccountNumber(), editServerSettingsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private NotificationManagerCompat getNotificationManager() {
        return controller.getNotificationManager();
    }
}
