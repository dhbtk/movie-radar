package io.edanni.movies.infrastructure.api.dto

/**
 * Created by eduardo on 18/11/2017.
 */
data class Configuration(
        val images: Images,
        val changeKeys: List<String>
)

data class Images(
        val baseUrl: String,
        val secureBaseUrl: String,
        val backdropSizes: List<String>,
        val logoSizes: List<String>,
        val posterSizes: List<String>,
        val profileSizes: List<String>,
        val stillSizes: List<String>
)