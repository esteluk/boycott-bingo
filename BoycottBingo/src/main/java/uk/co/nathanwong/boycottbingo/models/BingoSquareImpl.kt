package uk.co.nathanwong.boycottbingo.models

import uk.co.nathanwong.boycottbingo.interfaces.BingoSquare
import uk.co.nathanwong.boycottbingo.interfaces.BingoSquareListener

data class BingoSquareStringImpl(val title: String): BingoSquare {
    override var selectedListener: BingoSquareListener? = null

    override var isSelected: Boolean = false
        set(value) {
            field = value
            selectedListener?.bingoSquareSelected(value)
        }

    override fun copy(): BingoSquare {
        return copy(title)
    }

    override fun text(): String {
        return title
    }

}