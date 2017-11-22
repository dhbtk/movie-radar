package io.edanni.movies.infrastructure.api

import io.edanni.movies.infrastructure.api.dto.Configuration
import io.edanni.movies.infrastructure.api.dto.Genres
import io.edanni.movies.infrastructure.api.dto.Movie
import io.edanni.movies.infrastructure.api.dto.MoviePage
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface for the TMDB API.
 */
interface MovieApi {
    /**
     * Endpoint for configuration.
     */
    @GET("configuration")
    fun getConfiguration(): Observable<Configuration>

    /**
     * Endpoint for the genre list.
     */
    @GET("genre/movie/list")
    fun getGenres(): Observable<Genres>

    /**
     * Endpoint for the upcoming movie list.
     */
    @GET("movie/upcoming")
    fun getUpcomingMovies(@Query("page") page: Int): Observable<MoviePage>

    /**
     * Endpoint for the movie search.
     */
    @GET("search/movie")
    fun searchMovies(@Query("query") filter: String, @Query("page") page: Int): Observable<MoviePage>

    /**
     * Endpoint for movie details.
     */
    @GET("movie/{id}")
    fun getMovie(@Path("id") id: Long): Observable<Movie>
}