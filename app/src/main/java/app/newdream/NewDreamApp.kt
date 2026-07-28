package app.newdream

import android.app.Application
import app.newdream.data.local.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NewDreamApp : Application() {

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val settings by lazy { AppSettings(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: NewDreamApp
            private set
    }
}
