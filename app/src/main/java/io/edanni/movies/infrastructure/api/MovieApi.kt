package io.edanni.movies.infrastructure.api

import io.edanni.movies.infrastructure.api.dto.Configuration
import io.edanni.movies.infrastructure.api.dto.Genres
import io.edanni.movies.infrastructure.api.dto.Movie
import io.edanni.movies.infrastructure.api.dto.Movies
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface for the TMDB API.
 */
interface MovieApi {
    @GET("configuration")
    fun getConfiguration(): Observable<Configuration>

    @GET("genre/movie/list")
    fun getGenres(): Observable<Genres>

    @GET("movie/upcoming")
    fun getUpcomingMovies(@Query("page") page: Int): Observable<Movies>

    @GET("search/movie")
    fun searchMovies(@Query("query") filter: String, @Query("page") page: Int): Observable<Movies>

    @GET("movie/{id}")
    fun getMovie(@Path("id") id: Long): Observable<Movie>
}