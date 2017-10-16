package uk.co.nathanwong.boycottbingo.models

import uk.co.nathanwong.boycottbingo.interfaces.BingoDataProvider
import uk.co.nathanwong.boycottbingo.interfaces.BingoSquare
import java.util.*

class BingoStringArrayDataProvider(stringData: List<String>, val numberOfSquares: Int) : BingoDataProvider {

    val data: List<BingoSquare>

    init {
        data = stringData.map { BingoSquareStringImpl(it) }
    }

    override fun randomBingoSquares(): List<BingoSquare> {
        Collections.shuffle(data)
        val randomCollection = ArrayList<BingoSquare>()
        data.subList(0, numberOfSquares).forEach { randomCollection.add(it.copy()) }
        return randomCollection
    }

}