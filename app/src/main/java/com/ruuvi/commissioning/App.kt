package com.ruuvi.commissioning

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner

class App : Application(), LifecycleObserver {
    private var scanner: RuuviTagScanner? = null
    val permissionsInteractor = PermissionsInteractor(this)

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun StartScanning() {
        onEnterBackground()
        onEnterForeground()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground() {
        initScanner()
        scanner?.Start()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        scanner?.Stop()
    }

    fun initScanner() {
        if (scanner == null && !permissionsInteractor.permissionsNeeded()) {
            scanner = RuuviTagScanner()
            scanner?.Init(this)
        }
    }
}