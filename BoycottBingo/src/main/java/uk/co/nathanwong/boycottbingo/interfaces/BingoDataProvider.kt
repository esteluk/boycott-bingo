package uk.co.nathanwong.boycottbingo.interfaces

interface BingoDataProvider {
    fun bingoForPosition(position: Int): BingoSquare
    fun bingoSquares(): List<BingoSquare>
    fun shuffle()
}