package com.finprov.plapofy

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PlapofyApp : Application(), androidx.work.Configuration.Provider {
    
    @javax.inject.Inject
    lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory

    override val workManagerConfiguration: androidx.work.Configuration
        get() = androidx.work.Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}

