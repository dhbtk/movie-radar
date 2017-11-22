package io.edanni.movies

import dagger.Component
import io.edanni.movies.ui.activity.MovieListActivity
import javax.inject.Singleton

/**
 * Dagger component to inject activities.
 */
@Singleton
@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationInjector {
    fun inject(movieListActivity: MovieListActivity)
}