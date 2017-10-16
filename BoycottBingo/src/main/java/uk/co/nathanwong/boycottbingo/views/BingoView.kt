package uk.co.nathanwong.boycottbingo.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import uk.co.nathanwong.boycottbingo.interfaces.BingoSquare
import uk.co.nathanwong.boycottbingo.viewmodels.BingoViewViewModel

class BingoView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = LinearLayout.VERTICAL
    }

    var viewModel: BingoViewViewModel? = null
        set(value) {
            field = value
            value?.observable?.subscribe { bingoSquares ->
                buildBingoCard(bingoSquares)
            }
        }

    private fun buildBingoCard(bingoSquares: List<BingoSquare>) {
        removeAllViews()

        val viewModel = viewModel?.let { it } ?: return
        val views = bingoSquares.map { BingoSquareView(context, it) }
        val rows = ArrayList<LinearLayout>()

        var currentRow: LinearLayout? = null

        for (view in views) {
            if (currentRow == null) {
                currentRow = LinearLayout(context)
                currentRow.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1F)
                currentRow.orientation = LinearLayout.HORIZONTAL
            }

            currentRow.addView(view)

            if (currentRow.childCount >= viewModel.numberOfEntriesPerRow) {
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