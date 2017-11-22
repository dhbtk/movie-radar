package io.edanni.movies

import android.support.multidex.MultiDexApplication
import com.jakewharton.threetenabp.AndroidThreeTen

/**
 * Main application class. Used to install MultiDex and to bootstrap Dagger.
 */
class Application : MultiDexApplication() {
    companion object {
        @JvmStatic
        lateinit var injector: ApplicationInjector
    }

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        injector = DaggerApplicationInjector.builder().applicationModule(ApplicationModule(this)).build()
    }
}