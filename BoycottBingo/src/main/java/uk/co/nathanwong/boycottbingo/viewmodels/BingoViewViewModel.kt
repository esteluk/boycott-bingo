package uk.co.nathanwong.boycottbingo.viewmodels

import android.util.Log
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import uk.co.nathanwong.boycottbingo.interfaces.BingoDataProvider
import uk.co.nathanwong.boycottbingo.interfaces.BingoSquare
import uk.co.nathanwong.boycottbingo.interfaces.BingoSquareListener
import uk.co.nathanwong.boycottbingo.models.BingoViewModelState
import java.util.*

class BingoViewViewModel(private val dataProvider: BingoDataProvider): BingoSquareListener {

    var observable = BehaviorSubject.create<List<BingoSquare>>()
    var completedObservable = PublishSubject.create<BingoViewModelState>()

    private var currentBingoSquares: List<BingoSquare> = ArrayList<BingoSquare>()
        set(value) {
            field = value
            observable.onNext(value)
            value.forEach { it.selectedListener = this }
        }

    init {
        currentBingoSquares = dataProvider.randomBingoSquares()
    }

    fun newBingoBoard() {
        currentBingoSquares = dataProvider.randomBingoSquares()
    }

    val numberOfEntriesPerRow: Int
        get() {
            return Math.ceil(Math.sqrt(currentBingoSquares.count().toDouble())).toInt()
        }

    override fun bingoSquareSelected(isSelected: Boolean) {
        val completeCount = currentBingoSquares.count { it.isSelected }
        if (completeCount == currentBingoSquares.count()) {
            completedObservable.onNext(BingoViewModelState.COMPLETE)
        } else if (completeCount == 0) {
            completedObservable.onNext(BingoViewModelState.NOT_STARTED)
        } else {
            completedObservable.onNext(BingoViewModelState.IN_PROGRESS)
        }
        Log.d("BingoViewViewModel", "Total of ${completeCount} selected")
    }
}