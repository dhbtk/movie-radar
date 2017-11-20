package io.edanni.movies

import dagger.Component
import io.edanni.movies.ui.activity.MovieListActivity
import javax.inject.Singleton

/**
 * Created by eduardo on 18/11/2017.
 */
@Singleton
@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationInjector {
    fun inject(movieListActivity: MovieListActivity)
}