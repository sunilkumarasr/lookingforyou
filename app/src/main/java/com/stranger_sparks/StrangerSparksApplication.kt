package com.stranger_sparks

import android.app.Application
import com.stranger_sparks.api_dragger_flow.di.ApplicationComponent
import com.stranger_sparks.api_dragger_flow.di.DaggerApplicationComponent


class StrangerSparksApplication: Application() {

    lateinit var applicationComponent : ApplicationComponent
    override fun onCreate() {
        super.onCreate()
        applicationComponent = DaggerApplicationComponent.builder().build()
    }
}