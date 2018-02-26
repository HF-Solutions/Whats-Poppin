package com.paranoiddevs.whats_poppin.activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.paranoiddevs.whats_poppin.R;

/**
 * <p>Created by Alcha on Feb 25, 2018 @ 21:11.</p>
 */

public class BaseActivity extends AppCompatActivity {
    protected void setupActionBar() {
        ActionBar bar = getSupportActionBar();
        LayoutInflater inflater = LayoutInflater.from(this);
        View customView = inflater.inflate(R.layout.action_bar, null);
        TextView textView = customView.findViewById(R.id.appbar_title);
        textView.setText(R.string.app_name);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(35);
        textView.setTypeface(Typeface.createFromAsset(getAssets(), "font/Lobster.ttf"));

        if (bar != null) {
            bar.setDisplayShowHomeEnabled(false);
            bar.setDisplayShowTitleEnabled(false);
            bar.setDisplayShowCustomEnabled(true);
            bar.setCustomView(customView);
        } else System.out.println("bar == null");
    }
}
