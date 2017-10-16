package uk.co.nathanwong.boycottbingo.interfaces

interface BingoSquare {
    var selectedListener: BingoSquareListener?
    var isSelected: Boolean

    fun copy(): BingoSquare
    fun text(): String
}

interface BingoSquareListener {
    fun bingoSquareSelected(isSelected: Boolean)
}