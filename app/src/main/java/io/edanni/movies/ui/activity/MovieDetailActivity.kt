package io.edanni.movies.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.squareup.picasso.Picasso
import io.edanni.movies.R
import io.edanni.movies.infrastructure.api.dto.Movie
import kotlinx.android.synthetic.main.activity_movie_detail.*
import kotlinx.android.synthetic.main.content_movie_detail.*
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.ZoneId
import java.text.DateFormat
import java.util.*

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
        Picasso.with(this).load(movie.posterPath).error(R.drawable.poster_placeholder).into(this.moviePoster)
        this.toolbarLayout.title = movie.title

        this.genreList.text = movie.genres.map { it.name }.joinToString()
        this.runtime.text = renderRuntime(movie)

        if (movie.releaseDate != null) {
            val dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())
            val releaseDateAsDate = DateTimeUtils.toDate(movie.releaseDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
            this.releaseDate.text = resources.getString(R.string.movie_release_date_label, dateFormat.format(releaseDateAsDate))
        } else {
            this.releaseDate.text = resources.getString(R.string.movie_release_date_unknown)
        }

        this.overviewText.text = movie.overview
    }

    private fun renderRuntime(movie: Movie): String {
        val hours = movie.runtime / 60
        val minutes = movie.runtime % 60
        val rendered = StringBuilder()
        if (hours == 1) {
            rendered.append(resources.getString(R.string.movie_runtime_hour, hours)).append(" ")
        } else if (hours > 1) {
            rendered.append(resources.getString(R.string.movie_runtime_hours, hours)).append(" ")
        }
        if (minutes == 1) {
            rendered.append(resources.getString(R.string.movie_runtime_minute, minutes))
        } else if (minutes > 1) {
            rendered.append(resources.getString(R.string.movie_runtime_minutes, minutes))
        }
        return rendered.toString()
    }
}
