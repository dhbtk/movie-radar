package io.edanni.movies.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.squareup.picasso.Picasso
import io.edanni.movies.R
import io.edanni.movies.infrastructure.api.dto.Movie
import kotlinx.android.synthetic.main.activity_movie_detail.*
import kotlinx.android.synthetic.main.content_movie_detail.*

class MovieDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)
        setSupportActionBar(this.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val movie = intent.extras.getSerializable("movie") as Movie
        bindViews(movie)
    }

    private fun bindViews(movie: Movie) {
        Picasso.with(this).load(movie.backdropPath).into(this.movieBackground)
        this.toolbarLayout.title = movie.title
        this.overviewText.text = movie.overview
    }
}
