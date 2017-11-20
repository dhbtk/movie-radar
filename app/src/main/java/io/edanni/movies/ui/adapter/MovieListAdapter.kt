package io.edanni.movies.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.FrameLayout
import io.edanni.movies.R
import io.edanni.movies.infrastructure.api.dto.Movie
import io.edanni.movies.ui.binder.MovieListBinder

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
        val layout: FrameLayout = convertView as FrameLayout? ?: (inflater.inflate(R.layout.movie_list_item, parent, false) as FrameLayout)
        MovieListBinder.bind(movies[position], layout, context)
        return layout
    }

    override fun getItem(i: Int): Any = movies[i]

    override fun getItemId(i: Int): Long = i.toLong()

    override fun getCount(): Int = movies.size
}