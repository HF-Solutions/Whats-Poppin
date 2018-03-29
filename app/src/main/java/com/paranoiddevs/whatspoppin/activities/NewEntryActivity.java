package com.paranoiddevs.whatspoppin.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.paranoiddevs.whatspoppin.R;

public class NewEntryActivity extends AppCompatActivity {
    private EditText mLocationName;
    private EditText mLocationDesc;
    private Button mSubmitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_entry);
        setupButton();
        setupViews();
    }

    private void setupButton() {
        mSubmitBtn = findViewById(R.id.btn_submit_entry);
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLocationName.getText().length() == 0) {
                    Toast.makeText(getBaseContext(), "You must provide a location name.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getBaseContext(), "Congrats, you've added your location to the database!", Toast.LENGTH_LONG).show();
                    System.out.println("mLocationName.getText().toString() = " + mLocationName.getText().toString());
                    System.out.println("mLocationDesc.getText().toString() = " + mLocationDesc.getText().toString());
                    finish();
                }
            }
        });
    }

    private void setupViews() {
        mLocationName = findViewById(R.id.edit_text_location_name);
        mLocationDesc = findViewById(R.id.edit_text_location_desc);
    }
}
