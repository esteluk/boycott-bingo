package uk.co.nathanwong.boycottbingo.interfaces

interface BingoDataProvider {
    fun randomBingoSquares(): List<BingoSquare>
}