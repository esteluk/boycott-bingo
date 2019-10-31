package uk.co.nathanwong.boycottbingo.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import uk.co.nathanwong.boycottbingo.R
import uk.co.nathanwong.boycottbingo.interfaces.BingoSquare

@SuppressLint("ViewConstructor")
class BingoSquareView(context: Context, attrs: AttributeSet?, defStyleAttr: Int, private val model: BingoSquare) : FrameLayout(context, attrs, defStyleAttr), View.OnClickListener {
    constructor(context: Context, model: BingoSquare) : this(context, null, model)
    constructor(context: Context, attrs: AttributeSet?, model: BingoSquare) : this(context, attrs, 0, model)

    init {
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1F)
        background = ContextCompat.getDrawable(context, R.drawable.selecttransition)
        foreground = getSelectableItemForeground()
        View.inflate(context, R.layout.view_bingo, this)

        val textView: TextView = findViewById(R.id.text)
        textView.text = model.text()
        isSelected = model.isSelected
        isClickable = true
        isFocusable = true
        setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        val transition: TransitionDrawable = background as TransitionDrawable
        isSelected = if (!isSelected) {
            transition.startTransition(200)
            true
        } else {
            transition.reverseTransition(200)
            false
        }
        model.isSelected = isSelected
    }

    private fun getSelectableItemForeground(): Drawable? {
        val attrs: IntArray = intArrayOf(R.attr.selectableItemBackgroundBorderless)
        val typedArray = context.obtainStyledAttributes(attrs)
        val drawable = typedArray.getDrawable(0)
        typedArray.recycle()
        return drawable
    }
}