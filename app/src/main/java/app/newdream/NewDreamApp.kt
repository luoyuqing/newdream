package app.newdream

import android.app.Application
import app.newdream.data.local.AppSettings
import app.newdream.data.local.BundledAssetsLoader
import app.newdream.data.model.populateFromSample
import app.newdream.data.vn.SampleVNScripts
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
        initializeBundledData()
    }

    private fun initializeBundledData() {
        appScope.launch {
            val bundle = BundledAssetsLoader.loadSampleWorlds(this@NewDreamApp)
            if (bundle != null) {
                val withContent = bundle.worlds.map { populateFromSample(it) }
                settings.initializeWorldsIfEmpty(withContent)
            }
            // Initialize VN starter scripts if empty
            settings.initializeVNIfEmpty(SampleVNScripts.starterScripts())
        }
    }

    companion object {
        lateinit var instance: NewDreamApp
            private set
    }
}
