package io.edanni.movies.ui.activity

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.AbsListView
import android.widget.SearchView
import android.widget.Toast
import io.edanni.movies.Application
import io.edanni.movies.R
import io.edanni.movies.domain.service.MovieService
import io.edanni.movies.infrastructure.api.dto.Movie
import io.edanni.movies.infrastructure.api.dto.MovieList
import io.edanni.movies.infrastructure.api.dto.Movies
import io.edanni.movies.ui.adapter.MovieListAdapter
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import kotlinx.android.synthetic.main.activity_movie_list.*
import org.jetbrains.anko.intentFor
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 *
 */
class MovieListActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
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
    private var moviePage: Movies? = null

    /**
     * If we are currently loading data.
     */
    private var loadingMovies = false

    /**
     * If we are loading movie details in preparation for launching the MovieDetailActivity
     */
    private var loadingMovieDetail = false

    /**
     * Current search filters.
     */
    private var filter = ""

    /**
     * Emmiter for loading events
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

        this.refreshLayout.setOnRefreshListener { this.refreshMovieList(swipe = true) }

        // Debouncer for loading events. This is mostly necessary for the search since the filtering
        // is done client-side. This probably pretty far from the ideal solution in many aspects,
        // but you can't filter by upcoming movies in the API search endpoint.
        Observable.create({ emitter: ObservableEmitter<LoadingEvent> ->
            this.loadingEmitter = emitter
        }).debounce(250, TimeUnit.MILLISECONDS).subscribe { (type, swipe, initial) ->
            Handler(Looper.getMainLooper()).post {
                if (type == LoadingEventType.START) {
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

        if (savedInstanceState == null) {
            refreshMovieList(initial = true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu!!.findItem(R.id.search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.isSubmitButtonEnabled = false
        searchView.setOnQueryTextListener(this)
        searchView.setOnCloseListener { false }
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        filter = query!!
        refreshMovieList()
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText == "") {
            filter = ""
            refreshMovieList()
        }
        return true
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        moviePage = savedInstanceState?.getSerializable("moviePage") as Movies?
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
                        showError(it)
                    })
        }
    }

    private fun refreshMovieList(filter: String = this.filter, swipe: Boolean = false, initial: Boolean = false) {
        if (!loadingMovies) {
            startLoading(swipe, initial)
            movieService.getUpcomingMovies(1, filter)
                    .subscribe({ movies ->
                        this.moviePage = movies
                        movieListAdapter.movies = movies.results
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
        Toast.makeText(this, this.resources.getString(R.string.error_loading_movies, error.message), Toast.LENGTH_LONG).show()
    }

    inner class GridScrollListener : AbsListView.OnScrollListener {
        override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
            val lastItemCount = firstVisibleItem + visibleItemCount
            if (lastItemCount == totalItemCount && !loadingMovies) {
                val currentPage = moviePage
                if (currentPage != null && currentPage.totalPages > currentPage.page) {
                    startLoading()
                    movieService.getUpcomingMovies(currentPage.page + 1, filter)
                            .subscribe({ movies ->
                                moviePage = movies
                                movieListAdapter.movies = movieListAdapter.movies + movies.results
                                stopLoading()
                            }, {
                                stopLoading()
                                showError(it)
                            })
                }
            }
        }

        override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
            // nothing
        }

    }
}

enum class LoadingEventType {
    START, STOP
}

data class LoadingEvent(val type: LoadingEventType, val swipe: Boolean, val initial: Boolean)