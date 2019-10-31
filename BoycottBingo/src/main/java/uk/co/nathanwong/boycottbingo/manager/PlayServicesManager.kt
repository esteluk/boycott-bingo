package uk.co.nathanwong.boycottbingo.manager

import android.app.Activity
import android.content.Intent
import androidx.annotation.StringRes
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.games.Games
import uk.co.nathanwong.boycottbingo.R
import uk.co.nathanwong.boycottbingo.utils.GameUtils

class PlayServicesManager(val context: Activity) {

    private val rcSignIn = 9001

    private var signInClient: GoogleSignInClient? = null
    private var signInAccount: GoogleSignInAccount? = null

    var delegate: PlayServicesManagerDelegate? = null
        set(value) {
            field = value
            delegate?.playServicesStateDidUpdate(state)
        }

    private var state: PlayServicesManagerState = PlayServicesManagerState.NOT_AVAILABLE
        set(value) {
            field = value
            delegate?.playServicesStateDidUpdate(value)
        }

    init {
        val availability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        when (availability) {
            ConnectionResult.SUCCESS -> {
                signInClient = buildSignInClient()
                state = PlayServicesManagerState.CAN_SIGN_IN
            }
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> {
                state = PlayServicesManagerState.CAN_SIGN_IN
            }
            else -> {
                state = PlayServicesManagerState.NOT_AVAILABLE
            }
        }
    }

    val isSignedIn: Boolean
        get() {
            return GoogleSignIn.getLastSignedInAccount(context) != null
        }

    private fun buildSignInClient(): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build()
        return GoogleSignIn.getClient(context, signInOptions)
    }

    //region Lifecycle
    fun silentSignIn() {
        signInClient?.silentSignIn()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                signInAccount = task.result
                state = PlayServicesManagerState.SIGNED_IN
            } else {
                state = PlayServicesManagerState.CAN_SIGN_IN
            }
        }

    }

    fun clickSignIn() {
        signInClient = buildSignInClient()
        val intent = signInClient!!.signInIntent
        context.startActivityForResult(intent, rcSignIn)
    }

    fun signOut() {
        signInClient?.signOut()
        state = PlayServicesManagerState.CAN_SIGN_IN
    }
    //endregion

    //region Utility methods

    fun processActivityResult(activity: Activity, requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
        if (requestCode == rcSignIn && intent != null) {

            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            try {
                signInAccount = task.getResult(ApiException::class.java)
                state = PlayServicesManagerState.SIGNED_IN
            } catch (e: ApiException) {
                showActivityResultError(activity, requestCode, resultCode, R.string.unable_to_sign_in)
            }
            return true
        }
        return false
    }

    fun showLeaderboard() {
        val signInAccount = signInAccount?.let { it } ?: return
        Games.getLeaderboardsClient(context, signInAccount)
                .getLeaderboardIntent(context.getString(R.string.leaderboard_id)).addOnSuccessListener { intent ->
                    context.startActivityForResult(intent, 12345)
                }
    }

    private fun showActivityResultError(activity: Activity, requestCode: Int, resultCode: Int, @StringRes errorDescription: Int) {
        GameUtils.showActivityResultError(activity, requestCode, resultCode, errorDescription)
    }

    fun submitScore(leaderboardId: String, score: Long) {
        val signInAccount = signInAccount?.let { it } ?: return
        Games.getLeaderboardsClient(context, signInAccount).submitScore(leaderboardId, score)
    }
    //endregion

}

interface PlayServicesManagerDelegate {
    fun playServicesStateDidUpdate(state: PlayServicesManagerState)
}