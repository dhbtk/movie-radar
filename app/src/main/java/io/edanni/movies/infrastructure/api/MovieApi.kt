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
 * Created by eduardo on 20/11/2017.
 */
interface MovieApi {
    @GET("configuration")
    fun getConfiguration(): Observable<Configuration>

    @GET("genre/movie/list")
    fun getGenres(): Observable<Genres>

    @GET("movie/upcoming")
    fun getUpcomingMovies(@Query("page") page: Int): Observable<Movies>

    @GET("movie/{id}")
    fun getMovie(@Path("id") id: Long): Observable<Movie>
}