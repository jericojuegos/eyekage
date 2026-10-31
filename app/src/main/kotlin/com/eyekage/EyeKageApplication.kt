package com.eyekage

import android.app.Application
import com.eyekage.di.AppContainer
import com.eyekage.di.DefaultAppContainer

class EyeKageApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
