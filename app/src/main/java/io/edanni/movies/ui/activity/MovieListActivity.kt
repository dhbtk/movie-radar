package io.edanni.movies.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.AbsListView
import io.edanni.movies.Application
import io.edanni.movies.R
import io.edanni.movies.domain.service.MovieService
import io.edanni.movies.infrastructure.api.dto.Movies
import io.edanni.movies.ui.adapter.MovieListAdapter
import kotlinx.android.synthetic.main.activity_movie_list.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

class MovieListActivity : AppCompatActivity() {
    val TAG = MovieListActivity::class.java.name

    @Inject
    lateinit var movieService: MovieService

    lateinit private var movieListAdapter: MovieListAdapter

    private var moviePage: Movies? = null

    private var loadingMovies: Boolean = false

    private var filter: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_list)
        Application.injector.inject(this)

        movieListAdapter = MovieListAdapter(this)
        this.gridView.adapter = movieListAdapter
        this.gridView.setOnScrollListener(GridScrollListener())

        this.refreshLayout.setOnRefreshListener { this.refreshMovieList(swipe = true) }

        refreshMovieList()
    }


    private fun refreshMovieList(filter: String = "", swipe: Boolean = false) {
        if (!loadingMovies) {
            startLoading(swipe)
            movieService.getUpcomingMovies(1, filter)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ movies ->
                        this.moviePage = movies
                        movieListAdapter.movies = movies.results
                        stopLoading()
                    }, {
                        stopLoading()
                        throw it
                    })
        }
    }

    private fun startLoading(swipe: Boolean = false) {
        loadingMovies = true
        if (!swipe) {
            this.progressBar.visibility = VISIBLE
        }
    }

    private fun stopLoading() {
        this.progressBar.visibility = INVISIBLE
        this.refreshLayout.isRefreshing = false
        loadingMovies = false
    }

    inner class GridScrollListener : AbsListView.OnScrollListener {
        override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
            val lastItemCount = firstVisibleItem + visibleItemCount
            if (lastItemCount == totalItemCount && !loadingMovies) {
                val currentPage = moviePage
                if (currentPage != null && currentPage.totalPages > currentPage.page) {
                    startLoading()
                    movieService.getUpcomingMovies(currentPage.page + 1, filter)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ movies ->
                                moviePage = movies
                                movieListAdapter.movies = movieListAdapter.movies + movies.results
                                stopLoading()
                            }, {
                                stopLoading()
                                throw it
                            })
                }
            }
        }

        override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
            // nothing
        }

    }
}