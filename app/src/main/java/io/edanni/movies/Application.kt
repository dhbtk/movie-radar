package io.edanni.movies

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
        injector = DaggerApplicationInjector.builder().applicationModule(ApplicationModule(this)).build()
    }
}