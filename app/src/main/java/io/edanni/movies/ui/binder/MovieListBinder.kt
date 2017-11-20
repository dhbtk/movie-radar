package io.edanni.movies.ui.binder

import android.content.Context
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import io.edanni.movies.R
import io.edanni.movies.infrastructure.api.dto.Movie

/**
 * Created by eduardo on 20/11/2017.
 */
class MovieListBinder {
    companion object {
        fun bind(movie: Movie, layout: FrameLayout, context: Context) {
            val image = layout.findViewById<ImageView>(R.id.moviePoster)
            val label = layout.findViewById<TextView>(R.id.movieTitle)
            if (movie.posterPath != null) {
                val picasso = Picasso.with(context)
                picasso.setIndicatorsEnabled(true)
                picasso.load(movie.posterPath).into(image)
            } else {
                image.setBackgroundColor(context.resources.getColor(R.color.colorPrimaryDark, context.theme))
            }
            label.text = movie.title
        }
    }
}