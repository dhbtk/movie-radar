package io.edanni.movies

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.datatype.threetenbp.ThreeTenModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dagger.Module
import dagger.Provides
import io.edanni.movies.domain.service.MovieService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import javax.inject.Singleton

/**
 * Dagger bean declarations. This is all used to bootstrap the MovieService class.
 */
@Module
class ApplicationModule(private val application: Application) {
    /**
     * Jackson ObjectMapper for (de)serialization.
     */
    @Provides
    @Singleton
    fun objectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper.registerModules(KotlinModule(), ThreeTenModule())
        objectMapper.propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
        return objectMapper
    }

    /**
     * OkHttpClient configuration. We add an API key on every request here, and configure request
     * logging.
     */
    @Provides
    @Singleton
    fun okHttpClient(application: Application): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC
        return OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor { chain ->
                    val request = chain.request()
                    val url = request.url().newBuilder()
                            .addQueryParameter("api_key", application.resources.getString(R.string.movie_api_key))
                            .build()
                    chain.proceed(request.newBuilder().url(url).build())
                }
                .build()
    }

    /**
     * Retrofit. We configure RxJava integration, serialization and the HTTP client here.
     */
    @Provides
    @Singleton
    fun retrofit(objectMapper: ObjectMapper, okHttpClient: OkHttpClient, application: Application): Retrofit =
            Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                    .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                    .client(okHttpClient)
                    .baseUrl(application.resources.getString(R.string.movie_api_base_url))
                    .build()


    /**
     * MovieService, responsible for talking to the TMDB API.
     */
    @Provides
    @Singleton
    fun movieService(retrofit: Retrofit) = MovieService(retrofit)

    /**
     * Our main application class.
     */
    @Provides
    @Singleton
    fun application(): Application = application
}