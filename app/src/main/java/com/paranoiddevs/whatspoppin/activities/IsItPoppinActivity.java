package com.paranoiddevs.whatspoppin.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;

import static com.paranoiddevs.whatspoppin.util.Constants.PLACE_ID_KEY;
import static com.paranoiddevs.whatspoppin.util.Constants.PLACE_POPPIN_KEY;
import static com.paranoiddevs.whatspoppin.util.Constants.PLACE_POPPIN_NOTI_ID;

/**
 * <p>Created by Alcha on Jun 11, 2018 @ 13:24.</p>
 */
public class IsItPoppinActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.out.println("getIntent().getStringExtra(PLACE_ID_KEY); = " + getIntent().getStringExtra(PLACE_ID_KEY));
        System.out.println("getIntent().getStringExtra(PLACE_POPPIN_KEY); = " + getIntent().getBooleanExtra(PLACE_POPPIN_KEY, false));

        NotificationManagerCompat.from(this).cancel(PLACE_POPPIN_NOTI_ID);

        finish();
    }
}
