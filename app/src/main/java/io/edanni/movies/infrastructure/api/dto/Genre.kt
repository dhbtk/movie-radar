package io.edanni.movies.infrastructure.api.dto

import java.io.Serializable

/**
 * Container class for the API genres endpoint.
 */
data class Genres(val genres: List<Genre>) : Serializable

data class Genre(val id: Long, val name: String) : Serializable