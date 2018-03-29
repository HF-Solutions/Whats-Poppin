package com.paranoiddevs.whatspoppin.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;

/**
 * <p>Created by Alcha on Mar 28, 2018 @ 07:16.</p>
 */

public class WhatsPoppinBuilder {
    private Context mContext;

    public WhatsPoppinBuilder(Context context) {
        mContext = context;
    }

    static View.OnClickListener getFabListener(final Context context) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Add new Location");
                builder.setMessage("Is this place poppin'?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        context.startActivity(new Intent(context, NewEntryActivity.class));
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Do nothing. :'(
                    }
                });

                builder.create().show();
            }
        };
    }
}
