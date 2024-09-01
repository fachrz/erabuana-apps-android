package com.jayee.erabuana_apps

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.jayee.erabuana_apps.helpers.AuthenticationHelper

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = AuthenticationHelper.authenticate(this)
        if (auth) {
            startActivity(Intent(this, TabsActivity::class.java))
        }
        finish()
    }
}