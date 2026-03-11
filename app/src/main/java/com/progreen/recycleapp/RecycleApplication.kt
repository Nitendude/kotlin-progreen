package com.progreen.recycleapp

import android.app.Application
import com.progreen.recycleapp.data.local.PrefsManager

class RecycleApplication : Application() {
    lateinit var prefsManager: PrefsManager
        private set

    override fun onCreate() {
        super.onCreate()
        prefsManager = PrefsManager(this)
    }
}
