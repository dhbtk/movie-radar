package io.edanni.movies.domain.service

import io.edanni.movies.infrastructure.api.MovieApi
import io.edanni.movies.infrastructure.api.dto.Configuration
import io.edanni.movies.infrastructure.api.dto.Genre
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
    private var genres: List<Genre>? = null

    /**
     * Lists upcoming movies by page, optionally filtering the results.
     */
    fun getUpcomingMovies(page: Int, filter: String): Observable<Movies> =
            Observable.create({ subscriber ->
                Observable.zip(fetchConfiguration(), fetchGenres(), { configuration, genre -> Pair(configuration, genre) })
                        .subscribe({ (configuration, genres) ->
                            movieApi.getUpcomingMovies(page)
                                    .subscribeOn(Schedulers.newThread())
                                    .subscribe({ movies ->
                                        subscriber.onStart()
                                        subscriber.onNext(
                                                movies.copy(
                                                        results = movies.results
                                                                .filter { it.title.contains(filter, true) }
                                                                .map { it -> processMovie(it, configuration, genres) }
                                                )
                                        )
                                        subscriber.onCompleted()
                                    }, { subscriber.onError(it) })
                        })
            })

    /**
     * Finds the details of a given movie.
     */
    fun getMovieDetails(id: Long): Observable<Movie> =
            Observable.create({ subscriber ->
                Observable.zip(fetchConfiguration(), fetchGenres(), { configuration, genre -> Pair(configuration, genre) })
                        .subscribe({ (configuration, genres) ->
                            movieApi.getMovie(id)
                                    .subscribeOn(Schedulers.newThread())
                                    .subscribe({ movie ->
                                        subscriber.onStart()
                                        subscriber.onNext(processMovie(movie, configuration, genres))
                                        subscriber.onCompleted()
                                    })
                        }, { subscriber.onError(it) })
            })

    private fun processMovie(movie: Movie, configuration: Configuration, genres: List<Genre>) =
            movie.copy(
                    posterPath = if (movie.posterPath == null) null else configuration.images.secureBaseUrl + preferredPosterSize(configuration) + movie.posterPath,
                    backdropPath = if (movie.backdropPath == null) null else configuration.images.secureBaseUrl + preferredBackdropSize(configuration) + movie.backdropPath,
                    genres = movie.genreIds.map { id -> genres.find { it.id == id } ?: Genre(id, "[Unknown]") }
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
                Observable.create({ subscriber ->
                    movieApi.getConfiguration()
                            .subscribeOn(Schedulers.newThread())
                            .subscribe({ configuration ->
                                subscriber.onStart()
                                subscriber.onNext(configuration)
                                subscriber.onCompleted()
                            }, { subscriber.onError(it) })
                })
            }

    /**
     * Lazily fetches and caches the genre list.
     */
    private fun fetchGenres(): Observable<List<Genre>> =
            if (genres != null) {
                Observable.just(genres)
            } else {
                Observable.create({ subscriber ->
                    movieApi.getGenres()
                            .subscribeOn(Schedulers.newThread())
                            .subscribe({ genres ->
                                subscriber.onStart()
                                subscriber.onNext(genres.genres)
                                subscriber.onCompleted()
                            }, { subscriber.onError(it) })
                })
            }
}