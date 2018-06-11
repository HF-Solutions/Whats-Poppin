package com.hfsolutions.whatspoppin.util;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.location.places.Place;
import com.hfsolutions.whatspoppin.R;
import com.hfsolutions.whatspoppin.activities.IsItPoppinActivity;

import static android.support.v4.app.NotificationCompat.PRIORITY_DEFAULT;
import static com.hfsolutions.whatspoppin.util.Constants.PLACE_ID_KEY;
import static com.hfsolutions.whatspoppin.util.Constants.PLACE_POPPIN_KEY;

/**
 * <p>Created by Alcha on Jun 11, 2018 @ 13:05.</p>
 *
 * Contains a few methods that are used primarily within the {@link com.hfsolutions.whatspoppin.services.LocationService}
 * class for building the "Is this place poppin'?" notification.
 */
public class NotificationHelper {
    /** Channel id used for Android O+ notification channels */
    private static final String IS_IT_POPPIN_CHANNEL_ID = "isitpoppin'?";

    private static final int INTENT_FLAGS = PendingIntent.FLAG_CANCEL_CURRENT;

    /**
     * Create a {@link NotificationChannel} for use on devices running Android O or later when
     * building the "Is it Poppin'?" {@link Notification}.
     *
     * @param context The context for retrieving the {@link NotificationManager}
     */
    @TargetApi(Build.VERSION_CODES.O)
    private static void createChannel(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel channel = new NotificationChannel(IS_IT_POPPIN_CHANNEL_ID, "Is It Poppin'?", importance);
        channel.setDescription("Is it poppin'?");

        if (manager != null) manager.createNotificationChannel(channel);
    }

    /**
     * Builds the {@link Notification} that asks the user if the provided place is poppin' or not
     * and returns the built object. The context parameter is necessary for creating the {@link
     * NotificationManager} that is used for creating channels on devices running Android O or later
     * and creating the {@link PendingIntent}s that are used for the notification actions.
     *
     * @param place   The place where the user has been located
     * @param context The context of the {@link com.hfsolutions.whatspoppin.services.LocationService}
     *
     * @return The {@link Notification} to be displayed to the user
     */
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
        Intent intent = getIntent(context, place, true);

        int requestId = (int) System.currentTimeMillis();

        return PendingIntent.getActivity(context, requestId, intent, INTENT_FLAGS);
    }

    private static PendingIntent getNoIntent(Context context, Place place) {
        Intent intent = getIntent(context, place, false);

        int requestId = (int) System.currentTimeMillis();

        return PendingIntent.getActivity(context, requestId, intent, INTENT_FLAGS);
    }

    private static Intent getIntent(Context context, Place place, boolean isIt) {
        Intent intent = new Intent(context, IsItPoppinActivity.class);

        intent.putExtra(PLACE_ID_KEY, place.getId());
        intent.putExtra(PLACE_POPPIN_KEY, isIt);

        return intent;
    }
}
