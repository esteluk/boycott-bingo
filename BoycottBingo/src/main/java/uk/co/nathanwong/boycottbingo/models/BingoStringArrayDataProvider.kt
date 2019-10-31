package uk.co.nathanwong.boycottbingo.models

import uk.co.nathanwong.boycottbingo.interfaces.BingoDataProvider
import uk.co.nathanwong.boycottbingo.interfaces.BingoSquare

class BingoStringArrayDataProvider(stringData: List<String>, private val numberOfSquares: Int) : BingoDataProvider {

    private val data = stringData.map { BingoSquareStringImpl(it) }

    override fun randomBingoSquares(): List<BingoSquare> {
        return data.shuffled().take(numberOfSquares).map { it.copy() }
    }

}