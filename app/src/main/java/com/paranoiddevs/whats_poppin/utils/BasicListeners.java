package com.paranoiddevs.whats_poppin.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;

import com.paranoiddevs.whats_poppin.BuildConfig;

/**
 * <p>Created by Alcha on Feb 26, 2018 @ 18:16.</p>
 */

public class BasicListeners {
  public static View.OnClickListener permissionDeniedSnackbarListener(final Activity activity) {
    return new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
      }
    };
  }
}
