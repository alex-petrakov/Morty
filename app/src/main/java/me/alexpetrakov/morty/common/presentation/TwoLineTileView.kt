package me.alexpetrakov.morty.common.presentation

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.StringRes
import me.alexpetrakov.morty.R
import me.alexpetrakov.morty.databinding.LayoutTwoLineTileBinding
import kotlin.math.roundToInt

class TwoLineTileView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding = LayoutTwoLineTileBinding.inflate(
        LayoutInflater.from(context),
        this
    )

    val title: CharSequence
        get() = binding.titleTextView.text

    val body: CharSequence
        get() = binding.bodyTextView.text

    init {
        orientation = VERTICAL
        setPaddingRelative(16.dp, 4.dp, 16.dp, 4.dp)
        minimumHeight = 56.dp

        getStyledAttributes(context, attrs).use { styledAttrs ->
            val titleText = styledAttrs.getString(R.styleable.TwoLineTileView_tltv_title) ?: ""
            val bodyText = styledAttrs.getString(R.styleable.TwoLineTileView_tltv_body) ?: ""
            with(binding) {
                titleTextView.text = titleText
                bodyTextView.text = bodyText
            }
        }
    }

    private fun getStyledAttributes(context: Context, attrs: AttributeSet?): TypedArray {
        return context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.TwoLineTileView,
            0,
            0
        )
    }

    private inline fun <R> TypedArray.use(block: (TypedArray) -> R): R {
        return try {
            block(this)
        } finally {
            this.recycle()
        }
    }

    private val Int.dp: Int
        get() = (context.resources.displayMetrics.density * this).roundToInt()

    fun setTitle(@StringRes titleResId: Int) {
        setTitle(context.getString(titleResId))
    }

    fun setTitle(title: CharSequence) {
        binding.titleTextView.text = title
    }

    fun setBody(@StringRes bodyResId: Int) {
        setBody(context.getString(bodyResId))
    }

    fun setBody(body: CharSequence) {
        binding.bodyTextView.text = body
    }
}