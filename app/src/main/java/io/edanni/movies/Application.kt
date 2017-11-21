package io.edanni.movies

import com.jakewharton.threetenabp.AndroidThreeTen

/**
 * Created by eduardo on 18/11/2017.
 */
class Application : android.app.Application() {
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