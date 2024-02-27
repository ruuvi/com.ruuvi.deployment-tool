package com.ruuvi.commissioning

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner

class App : Application(), LifecycleObserver {
    lateinit var scanner: RuuviTagScanner

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        scanner = RuuviTagScanner()
        scanner.Init(this)
    }

    fun StartScanning() {
        onEnterBackground()
        onEnterForeground()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground() {
        scanner.Start()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        scanner.Stop()
    }
}