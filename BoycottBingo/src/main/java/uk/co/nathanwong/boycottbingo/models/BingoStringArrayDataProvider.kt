package uk.co.nathanwong.boycottbingo.models

import uk.co.nathanwong.boycottbingo.interfaces.BingoDataProvider
import uk.co.nathanwong.boycottbingo.interfaces.BingoSquare
import java.util.*

class BingoStringArrayDataProvider(stringData: List<String>, val numberOfSquares: Int) : BingoDataProvider {

    val data: List<BingoSquare>

    init {
        data = stringData.map { BingoSquareStringImpl(it) }
    }

    override fun bingoForPosition(position: Int): BingoSquare {
        return data[position]
    }

    override fun bingoSquares(): List<BingoSquare> {
        return data.subList(0, numberOfSquares)
    }

    override fun shuffle() {
        Collections.shuffle(data)
    }

}