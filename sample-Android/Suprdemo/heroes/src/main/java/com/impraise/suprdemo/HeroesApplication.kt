package com.impraise.suprdemo

import android.app.Application
import com.impraise.suprdemo.scenes.di.applicationComponent
import org.koin.android.ext.android.startKoin

class HeroesApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin(this, applicationComponent)
    }
}
