package io.edanni.movies.infrastructure.api.dto

import java.io.Serializable

/**
 * Created by eduardo on 19/11/2017.
 */
data class Genres(val genres: List<Genre>) : Serializable

data class Genre(val id: Long, val name: String) : Serializable