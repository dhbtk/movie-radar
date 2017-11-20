package io.edanni.movies.infrastructure.api.dto

/**
 * Created by diana on 19/11/2017.
 */
data class Genres(val genres: List<Genre>)

data class Genre(val id: Long, val name: String)