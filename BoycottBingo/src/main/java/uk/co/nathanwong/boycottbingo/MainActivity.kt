package uk.co.nathanwong.boycottbingo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import uk.co.nathanwong.boycottbingo.manager.PlayServicesManager
import uk.co.nathanwong.boycottbingo.manager.PlayServicesManagerDelegate
import uk.co.nathanwong.boycottbingo.manager.PlayServicesManagerState
import uk.co.nathanwong.boycottbingo.models.BingoStringArrayDataProvider
import uk.co.nathanwong.boycottbingo.models.BingoViewModelState
import uk.co.nathanwong.boycottbingo.viewmodels.BingoViewViewModel
import uk.co.nathanwong.boycottbingo.views.BingoView

class MainActivity: AppCompatActivity(), BingoCompletionDelegate, PlayServicesManagerDelegate, View.OnClickListener {

    private val stateObserver = BingoStateObserver(this)

    private lateinit var bingoView: BingoView
    private lateinit var viewModel: BingoViewViewModel

    private lateinit var playServicesManager: PlayServicesManager
    private lateinit var alertDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playServicesManager = PlayServicesManager(this)
        playServicesManager.delegate = this

        findViewById<View>(R.id.main_signin).setOnClickListener(this)
        val strings = resources.getStringArray(R.array.boycottisms)
        val dataProvider = BingoStringArrayDataProvider(strings.asList(), 9)

        alertDialog = buildAlertDialog()

        viewModel = BingoViewViewModel(dataProvider)

        viewModel.completedObservable.subscribe(stateObserver)
        bingoView = findViewById(R.id.main_rows)
        bingoView.viewModel = viewModel

        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun onResume() {
        super.onResume()
        playServicesManager.silentSignIn()
    }

    override fun onDestroy() {
        bingoView.destroy()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        menu?.let {
            val refreshItem = menu.findItem(R.id.action_refresh)
            var drawable = refreshItem.icon
            drawable = DrawableCompat.wrap(drawable)
            DrawableCompat.setTint(drawable, ContextCompat.getColor(this, android.R.color.white))
            refreshItem.icon = drawable
        }

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val signedIn = playServicesManager.isSignedIn
        menu?.findItem(R.id.action_leaderboard)?.isVisible = signedIn
        menu?.findItem(R.id.action_logout)?.isVisible = signedIn
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_refresh -> {
                onRefreshButtonPress()
                true
            }
            R.id.action_leaderboard -> {
                openLeaderboard()
                true
            }
            R.id.action_about -> {
                openAbout()
                true
            }
            R.id.action_logout -> {
                playServicesManager.signOut()
                true
            }
            else -> false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        playServicesManager.processActivityResult(this, requestCode, resultCode, data)
    }

    private fun openAbout() {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)
    }

    private fun openLeaderboard() {
        playServicesManager.showLeaderboard()
    }

    private fun onRefreshButtonPress() {
        viewModel.newBingoBoard()
    }

    override fun bingoComplete() {
        alertDialog.show()
        playServicesManager.submitScore(getString(R.string.leaderboard_id), incrementScore())
    }

    override fun onClick(view: View?) {
        if (view?.id == R.id.main_signin) {
            playServicesManager.clickSignIn()
        }
    }

    private fun incrementScore(): Long {
        val settings = getSharedPreferences("score", Context.MODE_PRIVATE)
        val editor = settings.edit()
        var score = settings.getInt("score", 0)
        score++
        editor.putInt("score", score)
        editor.apply()
        return score.toLong()
    }

    private fun buildAlertDialog(): AlertDialog {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.main_dialog_title)
                .setMessage(R.string.main_dialog_text)
                .setCancelable(false)
                .setPositiveButton(R.string.main_dialog_positive) { _, _ ->
                    onRefreshButtonPress()
                }
                .setNegativeButton(R.string.main_dialog_negative) { dialog, _ ->
                    dialog.cancel()
                }
        return builder.create()
    }

    override fun playServicesStateDidUpdate(state: PlayServicesManagerState) {
        val view = findViewById<View>(R.id.main_signin)
        when (state) {
            PlayServicesManagerState.NOT_AVAILABLE, PlayServicesManagerState.SIGNED_IN -> view.visibility = GONE
            PlayServicesManagerState.CAN_SIGN_IN -> view.visibility = VISIBLE
        }

        invalidateOptionsMenu()
    }

}

class BingoStateObserver(private val delegate: BingoCompletionDelegate): Observer<BingoViewModelState> {
    override fun onSubscribe(d: Disposable) {}

    override fun onNext(state: BingoViewModelState) {
        if (state == BingoViewModelState.COMPLETE) {
            delegate.bingoComplete()
        }
    }

    override fun onError(e: Throwable) {}

    override fun onComplete() {}
}

interface BingoCompletionDelegate {
    fun bingoComplete()
}
