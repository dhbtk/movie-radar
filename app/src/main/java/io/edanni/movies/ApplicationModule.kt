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
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import javax.inject.Singleton

/**
 * Created by eduardo on 18/11/2017.
 */
@Module
class ApplicationModule(private val application: Application) {
    @Provides
    @Singleton
    fun objectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper.registerModules(KotlinModule(), ThreeTenModule())
        objectMapper.propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
        return objectMapper
    }

    @Provides
    @Singleton
    fun okHttpClient(application: Application): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                val url = request.url().newBuilder()
                        .addQueryParameter("api_key", application.resources.getString(R.string.movie_api_key))
                        .build()
                chain.proceed(request.newBuilder().url(url).build())
            }
            .build()

    @Provides
    @Singleton
    fun retrofit(objectMapper: ObjectMapper, okHttpClient: OkHttpClient, application: Application): Retrofit =
            Retrofit.Builder()
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                    .client(okHttpClient)
                    .baseUrl(application.resources.getString(R.string.movie_api_base_url))
                    .build()


    @Provides
    @Singleton
    fun movieService(retrofit: Retrofit) = MovieService(retrofit)

    @Provides
    @Singleton
    fun application(): Application = application
}