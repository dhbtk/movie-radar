package io.edanni.movies.ui.activity

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.View.*
import android.widget.AbsListView
import android.widget.SearchView
import com.jakewharton.rxbinding2.widget.queryTextChanges
import io.edanni.movies.Application
import io.edanni.movies.R
import io.edanni.movies.domain.service.MovieService
import io.edanni.movies.infrastructure.api.dto.Movie
import io.edanni.movies.infrastructure.api.dto.MoviePage
import io.edanni.movies.ui.adapter.MovieListAdapter
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import kotlinx.android.synthetic.main.activity_movie_list.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import java.io.Serializable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 *
 */
class MovieListActivity : AppCompatActivity() {
    val TAG = MovieListActivity::class.qualifiedName

    /**
     * MovieService responsible for interacting with the API.
     */
    @Inject
    lateinit var movieService: MovieService

    /**
     * GridView adapter for showing the movies.
     */
    lateinit private var movieListAdapter: MovieListAdapter

    /**
     * Currently loaded page of data.
     */
    private var moviePage: MoviePage? = null

    /**
     * If we are currently loading data.
     */
    private var loadingMovies = false

    /**
     * If we are loading movie details in preparation for launching the MovieDetailActivity.
     */
    private var loadingMovieDetail = false

    /**
     * Current search filters.
     */
    private var filter = ""

    /**
     * Emmiter for loading events. Used for debouncing.
     */
    lateinit private var loadingEmitter: Emitter<LoadingEvent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_list)
        Application.injector.inject(this)

        movieListAdapter = MovieListAdapter(this)
        movieListAdapter.movieClickHandler = { showMovieDetail(it) }
        this.gridView.adapter = movieListAdapter
        this.gridView.setOnScrollListener(GridScrollListener())

        this.refreshLayout.setOnRefreshListener { this.loadMovies(swipe = true) }

        // Debouncer for loading events. This is to prevent flickering if the network is fast enough.
        Observable.create({ emitter: ObservableEmitter<LoadingEvent> ->
            this.loadingEmitter = emitter
        }).debounce(150, TimeUnit.MILLISECONDS).subscribe { (type, swipe, initial) ->
            Handler(Looper.getMainLooper()).post {
                if (type == LoadingEventType.START) {
                    this.noMoviesFoundLayout.visibility = GONE
                    this.errorLayout.visibility = GONE
                    if (!swipe && !initial) {
                        this.bottomProgressBar.visibility = VISIBLE
                    }
                    if (initial) {
                        this.topProgressBar.visibility = VISIBLE
                    }
                } else {
                    this.bottomProgressBar.visibility = INVISIBLE
                    this.topProgressBar.visibility = INVISIBLE
                    this.refreshLayout.isRefreshing = false
                }
            }
        }

        if (savedInstanceState == null || !savedInstanceState.containsKey("moviePage")) {
            loadMovies(initial = true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu!!.findItem(R.id.search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.isSubmitButtonEnabled = false
        searchView.queryTextChanges()
                .filter { it.isEmpty() || it.length >= 3 }.debounce(250, TimeUnit.MILLISECONDS)
                .subscribe { text ->
                    filter = text.toString()
                    loadMovies(page = 1, initial = true)
                }
        searchView.setOnCloseListener { false }
        return true
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        moviePage = savedInstanceState?.getSerializable("moviePage") as MoviePage?
        movieListAdapter.movies = (savedInstanceState?.getSerializable("movies") as MovieList?)?.list!!
        filter = savedInstanceState?.getString("filter")!!
        val firstVisibleIndex: Int = savedInstanceState.getInt("firstVisibleIndex")
        this.gridView.smoothScrollToPosition(firstVisibleIndex)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putSerializable("moviePage", moviePage)
        outState?.putSerializable("movies", MovieList(movieListAdapter.movies))
        outState?.putString("filter", filter)
        outState?.putInt("firstVisibleIndex", this.gridView.firstVisiblePosition)
        super.onSaveInstanceState(outState)
    }

    private fun showMovieDetail(listMovie: Movie) {
        if (!loadingMovieDetail) {
            loadingMovieDetail = true
            startLoading(initial = true)
            movieService.getMovieDetails(listMovie.id)
                    .subscribe({ movie ->
                        stopLoading()
                        startActivity(intentFor<MovieDetailActivity>("movie" to movie))
                        loadingMovieDetail = false
                    }, {
                        stopLoading()
                        loadingMovieDetail = false
                        showErrorToast(it)
                    })
        }
    }

    private fun loadMovies(swipe: Boolean = false, initial: Boolean = false, page: Int = 1) {
        if (!loadingMovies) {
            startLoading(swipe, initial)
            val observable =
                    if (this.filter.trim().isNotEmpty()) {
                        movieService.searchMovies(this.filter, page)
                    } else {
                        movieService.getUpcomingMovies(page)
                    }
            observable.subscribe({ movies ->
                this.moviePage = movies
                if (page > 1) {
                    movieListAdapter.movies = movieListAdapter.movies + movies.results
                } else {
                    movieListAdapter.movies = movies.results
                }
                if (movies.totalResults == 0L) {
                    this.noMoviesFoundLayout.visibility = VISIBLE
                }
                stopLoading()
            }, {
                stopLoading()
                showError(it)
            })
        }
    }

    private fun startLoading(swipe: Boolean = false, initial: Boolean = false) {
        loadingMovies = true
        loadingEmitter.onNext(LoadingEvent(LoadingEventType.START, swipe, initial))
    }

    private fun stopLoading() {
        loadingMovies = false
        loadingEmitter.onNext(LoadingEvent(LoadingEventType.STOP, true, true))
    }

    private fun showError(error: Throwable) {
        Log.e(TAG, "Error fetching movies", error)
        if (movieListAdapter.movies.isNotEmpty()) {
            toast(resources.getString(R.string.error_loading_movies))
        } else {
            this.errorLayout.visibility = VISIBLE
        }
    }

    private fun showErrorToast(error: Throwable) {
        Log.e(TAG, "Error fetching movie", error)
        toast(resources.getString(R.string.error_loading_movie))
    }

    /**
     * Infinite scroll listener for the grid view.
     */
    inner class GridScrollListener : AbsListView.OnScrollListener {
        override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
            val lastItemCount = firstVisibleItem + visibleItemCount
            if (lastItemCount == totalItemCount && !loadingMovies) {
                val currentPage = moviePage
                if (currentPage != null && currentPage.totalPages > currentPage.page) {
                    loadMovies(page = currentPage.page + 1)
                }
            }
        }

        override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
            // nothing
        }

    }

    /**
     * Loading event types for the loading bar debounce.
     */
    enum class LoadingEventType {
        START, STOP
    }

    /**
     * Loading event for the loading bar debounce.
     */
    data class LoadingEvent(val type: LoadingEventType, val swipe: Boolean, val initial: Boolean)

    /**
     * Serializable wrapper for the current movie list, since kotlin lists aren't serializable.
     */
    data class MovieList(val list: List<Movie>) : Serializable
}

