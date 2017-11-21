package io.edanni.movies.ui.activity

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
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
import kotlinx.android.synthetic.main.activity_movie_list.*
import org.jetbrains.anko.intentFor
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

class MovieListActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    val TAG = MovieListActivity::class.java.name

    @Inject
    lateinit var movieService: MovieService

    lateinit private var movieListAdapter: MovieListAdapter

    private var moviePage: Movies? = null

    private var loadingMovies = false

    private var loadingMovie = false

    private var filter = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_list)
        Application.injector.inject(this)

        movieListAdapter = MovieListAdapter(this)
        movieListAdapter.movieClickHandler = { showMovieDetail(it) }
        this.gridView.adapter = movieListAdapter
        this.gridView.setOnScrollListener(GridScrollListener())

        this.refreshLayout.setOnRefreshListener { this.refreshMovieList(swipe = true) }

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
        searchView.setOnCloseListener { filter = ""; refreshMovieList(); false }
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
        if (!loadingMovie) {
            loadingMovie = true
            startLoading(top = true)
            movieService.getMovieDetails(listMovie.id)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ movie ->
                        stopLoading()
                        startActivity(intentFor<MovieDetailActivity>("movie" to movie))
                    }, {
                        stopLoading()
                        showError(it)
                    })
        }
    }

    private fun refreshMovieList(filter: String = this.filter, swipe: Boolean = false, initial: Boolean = false) {
        if (!loadingMovies) {
            startLoading(swipe, initial)
            movieService.getUpcomingMovies(1, filter)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
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

    private fun startLoading(swipe: Boolean = false, top: Boolean = false) {
        loadingMovies = true
        if (!swipe && !top) {
            this.bottomProgressBar.visibility = VISIBLE
        }
        if (top) {
            this.topProgressBar.visibility = VISIBLE
        }
    }

    private fun stopLoading() {
        this.bottomProgressBar.visibility = INVISIBLE
        this.topProgressBar.visibility = INVISIBLE
        this.refreshLayout.isRefreshing = false
        loadingMovies = false
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
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
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