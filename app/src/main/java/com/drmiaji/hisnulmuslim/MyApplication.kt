package com.drmiaji.hisnulmuslim

import android.app.Application
import com.drmiaji.hisnulmuslim.utils.ThemeUtils

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeUtils.applyTheme(this)
    }
}