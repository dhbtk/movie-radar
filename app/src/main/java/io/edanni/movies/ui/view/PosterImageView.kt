package io.edanni.movies.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

/**
 * ImageView that keeps the poster's aspect ratio (2x3)
 */
class PosterImageView : ImageView {
    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int) : super(ctx, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(widthMeasureSpec, (widthMeasureSpec / 2) * 3)
    }
}