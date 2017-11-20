package io.edanni.movies.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.edanni.movies.Application
import io.edanni.movies.R
import io.edanni.movies.domain.service.MovieService
import io.edanni.movies.ui.adapter.MovieListAdapter
import kotlinx.android.synthetic.main.activity_movie_list.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

class MovieListActivity : AppCompatActivity() {
    val TAG = MovieListActivity::class.java.name

    @Inject
    lateinit var movieService: MovieService

    lateinit var movieListAdapter: MovieListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_list)
        Application.injector.inject(this)
        movieListAdapter = MovieListAdapter(this)
        this.gridView.adapter = movieListAdapter

        movieService.getUpcomingMovies(1, "")
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ movieListAdapter.movies = it.results })
    }
}