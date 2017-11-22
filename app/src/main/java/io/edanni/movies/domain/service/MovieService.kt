package io.edanni.movies.domain.service

import io.edanni.movies.infrastructure.api.MovieApi
import io.edanni.movies.infrastructure.api.dto.Configuration
import io.edanni.movies.infrastructure.api.dto.Genre
import io.edanni.movies.infrastructure.api.dto.Movie
import io.edanni.movies.infrastructure.api.dto.Movies
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import retrofit2.Retrofit
import javax.inject.Inject

/**
 *
 */
class MovieService
@Inject constructor(retrofit: Retrofit) {
    private val movieApi: MovieApi = retrofit.create(MovieApi::class.java)
    private var configuration: Configuration? = null
    private var genres: List<Genre>? = null

    /**
     * Lists upcoming movies by page, optionally filtering the results.
     */
    fun getUpcomingMovies(page: Int, filter: String): Observable<Movies> =
            Observables.zip(
                    fetchConfiguration(),
                    fetchGenres(),
                    movieApi.getUpcomingMovies(page),
                    { configuration, genres: List<Genre>, movies: Movies -> Triple(configuration, genres, movies) })
                    .map { (configuration, genres, movies) ->
                        movies.copy(
                                results = movies.results
                                        .filter { it.title.contains(filter, true) }
                                        .map { movie -> correctImagePaths(movie, configuration) }
                                        .map { movie -> setGenres(movie, genres) }
                        )
                    }
                    .observeOn(AndroidSchedulers.mainThread())


    /**
     * Finds the details of a given movie.
     */
    fun getMovieDetails(id: Long): Observable<Movie> =
            Observables.zip(fetchConfiguration(),
                    movieApi.getMovie(id),
                    { configuration, movie: Movie -> Pair(configuration, movie) })
                    .map { (configuration, movie) -> correctImagePaths(movie, configuration) }
                    .observeOn(AndroidSchedulers.mainThread())

    /**
     * Corrects the movie image paths so that the are full URLs
     */
    private fun correctImagePaths(movie: Movie, configuration: Configuration) =
            movie.copy(
                    posterPath = if (movie.posterPath == null) null else configuration.images.secureBaseUrl + preferredPosterSize(configuration) + movie.posterPath,
                    backdropPath = if (movie.backdropPath == null) null else configuration.images.secureBaseUrl + preferredBackdropSize(configuration) + movie.backdropPath
            )

    private fun setGenres(movie: Movie, genres: List<Genre>) =
            movie.copy(genres = movie.genreIds.map { id -> genres.find { it.id == id }!! })

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
                        .map { this.configuration = it; it }
            }

    private fun fetchGenres(): Observable<List<Genre>> =
            if (genres != null) {
                Observable.just(genres)
            } else {
                movieApi.getGenres()
                        .map { genres -> genres.genres }
                        .map { this.genres = it; it }
            }
}
