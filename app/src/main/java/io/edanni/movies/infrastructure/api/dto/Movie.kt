package io.edanni.movies.infrastructure.api.dto

import org.threeten.bp.LocalDate
import java.io.Serializable

/**
 * DTO for the movie entity from the API. There are more fields in the response, but these are the
 * ones we care about.
 */
data class Movie(
        val posterPath: String? = null,
        val overview: String,
        val releaseDate: LocalDate? = null,
        val genreIds: List<Long> = emptyList(),
        val genres: List<Genre> = emptyList(),
        val id: Long,
        val title: String,
        val backdropPath: String? = null,
        val runtime: Int = 0
) : Serializable

/**
 * Container for the search and upcoming movie endpoints.
 */
data class MoviePage(
        val results: List<Movie>,
        val page: Int,
        val totalResults: Long,
        val totalPages: Int
) : Serializable
