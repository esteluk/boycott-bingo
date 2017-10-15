package uk.co.nathanwong.boycottbingo.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import uk.co.nathanwong.boycottbingo.interfaces.BingoDataProvider
import uk.co.nathanwong.boycottbingo.interfaces.BingoSquare

class BingoView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = LinearLayout.VERTICAL
    }

    var dataProvider: BingoDataProvider? = null
        set(value) {
            buildBingoCard(value)
            field = value
        }

    private var bingoSquares: List<BingoSquare>? = null

    private val squaresPerRow: Int
        get() {
            return Math.ceil(Math.sqrt(bingoSquares?.count()?.toDouble() ?: 0.0)).toInt()
        }

    fun regenerate() {
        buildBingoCard(dataProvider)
    }

    private fun buildBingoCard(dataProvider: BingoDataProvider?) {
        removeAllViews()

        val realDataProvider = dataProvider?.let { it } ?: return
        realDataProvider.shuffle()
        val models = realDataProvider.bingoSquares()
        bingoSquares = models

        val views = models.map { BingoSquareView(context, it) }
        val rows = ArrayList<LinearLayout>()

        var currentRow: LinearLayout? = null

        for (view in views) {
            if (currentRow == null) {
                currentRow = LinearLayout(context)
                currentRow.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1F)
                currentRow.orientation = LinearLayout.HORIZONTAL
            }

            currentRow.addView(view)

            if (currentRow.childCount >= squaresPerRow) {
                rows.add(currentRow)
                currentRow = null
            }

        }

        if (currentRow != null) {
            rows.add(currentRow)
        }

        for (row in rows) {
            addView(row)
        }
    }

}