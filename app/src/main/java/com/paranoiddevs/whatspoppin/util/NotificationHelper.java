package com.paranoiddevs.whatspoppin.util;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.location.places.Place;
import com.paranoiddevs.whatspoppin.R;
import com.paranoiddevs.whatspoppin.activities.IsItPoppinActivity;

import static android.support.v4.app.NotificationCompat.PRIORITY_DEFAULT;
import static com.paranoiddevs.whatspoppin.util.Constants.PLACE_ID_KEY;
import static com.paranoiddevs.whatspoppin.util.Constants.PLACE_POPPIN_KEY;

/**
 * <p>Created by Alcha on Jun 11, 2018 @ 13:05.</p>
 *
 * Contains a few methods that are used primarily within the {@link com.paranoiddevs.whatspoppin.services.LocationService}
 * class for building the "Is this place poppin'?" notification.
 */
public class NotificationHelper {
    private static final String IS_IT_POPPIN_CHANNEL_ID = "isitpoppin'?";

    @TargetApi(Build.VERSION_CODES.O)
    private static void createChannel(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel channel = new NotificationChannel(IS_IT_POPPIN_CHANNEL_ID, "Is It Poppin'?", importance);
        channel.setDescription("Is it poppin'?");

        if (manager != null) manager.createNotificationChannel(channel);
    }

    public static Notification buildNotification(Place place, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, IS_IT_POPPIN_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_local_bar_purple)
                .setContentTitle(place.getName().toString())
                .setContentText("Is this place poppin'?")
                .addAction(R.drawable.ic_check_green, "Yes", getYesIntent(context, place))
                .addAction(R.drawable.ic_close_red, "No", getNoIntent(context, place))
                .setPriority(PRIORITY_DEFAULT);

        return builder.build();
    }

    private static PendingIntent getYesIntent(Context context, Place place) {
        Intent intent = new Intent(context, IsItPoppinActivity.class);

        intent.putExtra(PLACE_ID_KEY, place.getId());
        intent.putExtra(PLACE_POPPIN_KEY, true);

        int requestId = (int) System.currentTimeMillis();
        int flags = PendingIntent.FLAG_CANCEL_CURRENT;

        return PendingIntent.getActivity(context, requestId, intent, flags);
    }

    private static PendingIntent getNoIntent(Context context, Place place) {
        Intent intent = new Intent(context, IsItPoppinActivity.class);

        intent.putExtra(PLACE_ID_KEY, place.getId());
        intent.putExtra(PLACE_POPPIN_KEY, false);

        int requestId = (int) System.currentTimeMillis();
        int flags = PendingIntent.FLAG_CANCEL_CURRENT;

        return PendingIntent.getActivity(context, requestId, intent, flags);
    }

    private static Bundle getNotificationExtras(Place place) {
        Bundle extras = new Bundle();
        extras.putString("PLACE_ID", place.getId());

        return extras;
    }
}
