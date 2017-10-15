package uk.co.nathanwong.boycottbingo.models

import uk.co.nathanwong.boycottbingo.interfaces.BingoSquare

class BingoSquareStringImpl(val title: String): BingoSquare {
    override var isSelected: Boolean = false

    override fun text(): String {
        return title
    }

}