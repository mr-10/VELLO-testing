package com.mr10.vello

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VelloApplication : Application() {
    companion object {
        const val AGORA_APP_ID = "YOUR_AGORA_APP_ID_HERE" // Placeholder
    }
}
