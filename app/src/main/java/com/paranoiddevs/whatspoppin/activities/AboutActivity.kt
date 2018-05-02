package com.paranoiddevs.whatspoppin.activities

import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.paranoiddevs.whatspoppin.R

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        val manager = this.packageManager
        val info = manager.getPackageInfo(this.packageName, COMPONENT_ENABLED_STATE_DEFAULT)
        val versionNameField = findViewById<TextView>(R.id.about_version_name)
        val versionCodeField = findViewById<TextView>(R.id.about_version_code)
        versionNameField.text = info.versionName
        versionCodeField.text = info.versionCode.toString()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
