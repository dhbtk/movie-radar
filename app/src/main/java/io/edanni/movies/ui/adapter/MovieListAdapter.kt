package io.edanni.movies.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import io.edanni.movies.R
import io.edanni.movies.infrastructure.api.dto.Movie
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.ZoneId
import java.text.DateFormat
import java.util.*

/**
 * Created by eduardo on 20/11/2017.
 */
class MovieListAdapter(private val context: Context) : BaseAdapter() {
    var movies: List<Movie> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout: ViewGroup = convertView as ViewGroup? ?: (inflater.inflate(R.layout.movie_list_item, parent, false) as ViewGroup)
        bindView(movies[position], layout)
        return layout
    }

    override fun getItem(i: Int): Any = movies[i]

    override fun getItemId(i: Int): Long = i.toLong()

    override fun getCount(): Int = movies.size

    private fun bindView(movie: Movie, layout: ViewGroup) {
        val image = layout.findViewById<ImageView>(R.id.moviePoster)
        val movieTitle = layout.findViewById<TextView>(R.id.movieTitle)
        val releaseDate = layout.findViewById<TextView>(R.id.releaseDate)
        if (movie.posterPath != null) {
            val picasso = Picasso.with(context)
            picasso.setIndicatorsEnabled(true)
            picasso.load(movie.posterPath).into(image)
            movieTitle.visibility = View.INVISIBLE
        } else {
            movieTitle.visibility = View.VISIBLE
            image.setImageDrawable(context.resources.getDrawable(R.drawable.poster_placeholder, context.theme))
        }
        movieTitle.text = movie.title.toUpperCase()

        val dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())
        val releaseDateAsDate = DateTimeUtils.toDate(movie.releaseDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        releaseDate.text = String.format(
                context.resources.getString(R.string.movie_releases_at),
                "\n" + dateFormat.format(releaseDateAsDate)).toUpperCase()
    }
}