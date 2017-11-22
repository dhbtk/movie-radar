package io.edanni.movies.infrastructure.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import org.threeten.bp.LocalDate
import java.io.Serializable

/**
 * Created by eduardo on 19/11/2017.
 */
data class Movie(
        val posterPath: String? = null,
        val adult: Boolean,
        val overview: String,
        val releaseDate: LocalDate? = null,
        val genreIds: List<Long> = emptyList(),
        val genres: List<Genre> = emptyList(),
        val id: Long,
        val originalTitle: String,
        val originalLanguage: String,
        val title: String,
        val backdropPath: String? = null,
        val popularity: Double,
        val voteCount: Long,
        val video: Boolean,
        val voteAverage: Long,
        val imdbId: String? = "",
        val productionCompanies: List<ProductionCompany> = emptyList(),
        val productionCountries: List<ProductionCountry> = emptyList(),
        val revenue: Long = 0,
        val runtime: Int = 0,
        val spokenLanguages: List<SpokenLanguage> = emptyList(),
        val status: String = "",
        val tagline: String = ""
) : Serializable

data class Movies(
        val results: List<Movie>,
        val page: Int,
        val totalResults: Long,
        val dates: Dates? = null,
        val totalPages: Int
) : Serializable

data class MovieList(val list: List<Movie>) : Serializable

data class Dates(
        val minimum: LocalDate,
        val maximum: LocalDate
) : Serializable

data class ProductionCompany(val id: Long, val name: String) : Serializable
data class ProductionCountry(@JsonProperty("iso_3166_1") val iso31661: String, val name: String) : Serializable
data class SpokenLanguage(@JsonProperty("iso_639_1") val iso6391: String, val name: String) : Serializable