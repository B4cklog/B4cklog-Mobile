package org.b4cklog.mobile

import android.app.Application
import org.b4cklog.mobile.network.ApiClient

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
    }
}