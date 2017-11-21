package io.edanni.movies.domain.service

import io.edanni.movies.infrastructure.api.MovieApi
import io.edanni.movies.infrastructure.api.dto.Configuration
import io.edanni.movies.infrastructure.api.dto.Movie
import io.edanni.movies.infrastructure.api.dto.Movies
import retrofit2.Retrofit
import rx.Observable
import rx.schedulers.Schedulers
import javax.inject.Inject

/**
 *
 */
class MovieService
@Inject constructor(retrofit: Retrofit) {
    private val movieApi: MovieApi = retrofit.create(MovieApi::class.java)
    private var configuration: Configuration? = null

    /**
     * Lists upcoming movies by page, optionally filtering the results.
     */
    fun getUpcomingMovies(page: Int, filter: String): Observable<Movies> =
            Observable.zip(fetchConfiguration(),
                    movieApi.getUpcomingMovies(page).subscribeOn(Schedulers.newThread()),
                    { configuration, movies -> Pair(configuration, movies) })
                    .map { (configuration, movies) ->
                        movies.copy(
                                results = movies.results
                                        .filter { it.title.contains(filter, true) }
                                        .map { it -> correctImagePaths(it, configuration) }
                        )
                    }

    /**
     * Finds the details of a given movie.
     */
    fun getMovieDetails(id: Long): Observable<Movie> =
            Observable.zip(fetchConfiguration(),
                    movieApi.getMovie(id).subscribeOn(Schedulers.newThread()),
                    { configuration, movie -> Pair(configuration, movie) })
                    .map { (configuration, movie) -> correctImagePaths(movie, configuration) }

    /**
     * Corrects the movie image paths so that the are full URLs
     */
    private fun correctImagePaths(movie: Movie, configuration: Configuration) =
            movie.copy(
                    posterPath = if (movie.posterPath == null) null else configuration.images.secureBaseUrl + preferredPosterSize(configuration) + movie.posterPath,
                    backdropPath = if (movie.backdropPath == null) null else configuration.images.secureBaseUrl + preferredBackdropSize(configuration) + movie.backdropPath
            )

    /**
     * Probably should be chosen depending on device size
     */
    private fun preferredBackdropSize(configuration: Configuration) = configuration.images.backdropSizes.find { it == "w1280" } ?: "original"

    /**
     * Also should vary on device size
     */
    private fun preferredPosterSize(configuration: Configuration) = configuration.images.posterSizes.find { it == "w342" } ?: "original"

    /**
     * Lazily fetches and caches the API configuration.
     */
    private fun fetchConfiguration(): Observable<Configuration> =
            if (configuration != null) {
                Observable.just(configuration)
            } else {
                movieApi.getConfiguration()
                        .subscribeOn(Schedulers.newThread())
            }
}