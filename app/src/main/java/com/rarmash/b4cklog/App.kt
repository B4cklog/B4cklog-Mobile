package com.rarmash.b4cklog

import android.app.Application
import com.rarmash.b4cklog.network.ApiClient

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
    }
}