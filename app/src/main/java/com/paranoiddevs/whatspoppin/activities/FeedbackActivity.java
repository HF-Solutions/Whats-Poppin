package com.paranoiddevs.whatspoppin.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.paranoiddevs.whatspoppin.R;

public class FeedbackActivity extends AppCompatActivity {
    private static final String LOG_TAG = "FeedbackActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        setupActionBar();

        Button submitBtn = findViewById(R.id.btn_submit);
        submitBtn.setOnClickListener(getSubmitListener());
    }

    private View.OnClickListener getSubmitListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText contentEditText = findViewById(R.id.edit_text_feedback);
                Intent emailIntent = new Intent(Intent.ACTION_SEND);

                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, "alcha@paranoiddevs.com");
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "What's Poppin'? Feedback");
                emailIntent.putExtra(Intent.EXTRA_TEXT, getEmailContent(contentEditText));

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send email..."));
                    finish();
                    Log.i(LOG_TAG, "onClick: Finished sending email...");
                } catch (android.content.ActivityNotFoundException e) {
                    Toast.makeText(FeedbackActivity.this, "There is no email client installed.", Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    private String getEmailContent(EditText contentEditable) {
        StringBuilder builder = new StringBuilder(contentEditable.getText().toString());
        builder.append("\n\n");

        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            builder.append("App Version Information:\n");
            builder.append("Package Name - ");
            builder.append(info.packageName);
            builder.append("\nVersion Code - ");
            builder.append(info.versionCode);
            builder.append("\nVersion Name - ");
            builder.append(info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, "getEmailContent: manager.getPackageInfo failed.", e);
        }

        return builder.toString();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
