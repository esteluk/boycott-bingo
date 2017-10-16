package uk.co.nathanwong.boycottbingo.manager

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.games.Games
import uk.co.nathanwong.boycottbingo.R
import uk.co.nathanwong.boycottbingo.utils.GameUtils

class PlayServicesManager(val context: Activity): GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private val rcSignIn = 9001

    private var googleApiClient: GoogleApiClient? = null

    var delegate: PlayServicesManagerDelegate? = null
    private var state: PlayServicesManagerState = PlayServicesManagerState.NOT_AVAILABLE
        set(value) {
            field = value
            delegate?.playServicesStateDidUpdate(value)
        }

    init {

        val availability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        if (availability == ConnectionResult.SUCCESS) {
            googleApiClient = GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                    .build()
            state = PlayServicesManagerState.CAN_SIGN_IN
        } else {
            state = PlayServicesManagerState.NOT_AVAILABLE
        }

    }

    private var autoStartSignInFlow = false
    private var resolvingConnectionFailure = false
    private var signInClicked = false

    val isSignedIn: Boolean
        get() {
            return googleApiClient?.isConnected ?: false
        }

    //region Lifecycle
    fun connectIfAvailable() {
        googleApiClient?.connect()
    }

    fun disconnectIfAvailable() {
        googleApiClient?.disconnect()
    }

    fun clickSignIn() {
        signInClicked = true
        connectIfAvailable()
    }

    fun signOut() {
        val googleApiClient = googleApiClient?.let { it } ?: return
        Games.signOut(googleApiClient)
        state = PlayServicesManagerState.CAN_SIGN_IN
    }
    //endregion

    //region Utility methods

    fun processActivityResult(activity: Activity, requestCode: Int, resultCode: Int): Boolean {
        if (requestCode == rcSignIn) {
            signInClicked = false
            resolvingConnectionFailure = false
            if (resultCode == Activity.RESULT_OK) {
                googleApiClient?.connect()
            } else {
                showActivityResultError(activity, requestCode, resultCode, R.string.unable_to_sign_in)
            }
            return true
        }
        return false
    }

    fun buildLeaderboardIntent(): Intent? {
        val googleApiClient = googleApiClient?.let { it } ?: return null
        if (googleApiClient.isConnected) {
            return Games.Leaderboards.getLeaderboardIntent(googleApiClient, context.getString(R.string.leaderboard_id))
        }
        return null
    }

    private fun resolveConnectionFailure(activity: Activity, connectionResult: ConnectionResult, requestCode: Int): Boolean {
        val googleApiClient = googleApiClient?.let { it } ?: return false
        return GameUtils.resolveConnectionFailure(activity, googleApiClient, connectionResult, requestCode)
    }

    private fun showActivityResultError(activity: Activity, requestCode: Int, resultCode: Int, @StringRes errorDescription: Int) {
        GameUtils.showActivityResultError(activity, requestCode, resultCode, errorDescription)
    }

    fun submitScore(leaderboardId: String, score: Long) {
        val googleApiClient = googleApiClient?.let { it } ?: return
        if (isSignedIn) {
            Games.Leaderboards.submitScore(googleApiClient, leaderboardId, score)
        }
    }
    //endregion

    //region GoogleApiConnection callbacks
    override fun onConnected(p0: Bundle?) {
        state = PlayServicesManagerState.SIGNED_IN
    }

    override fun onConnectionSuspended(p0: Int) {
        googleApiClient?.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        if (resolvingConnectionFailure) {
            return
        }

        if (signInClicked || autoStartSignInFlow) {
            autoStartSignInFlow = false
            resolvingConnectionFailure = true
            signInClicked = false

            if (!resolveConnectionFailure(context, connectionResult, rcSignIn)) {
                resolvingConnectionFailure = false
            }
        }

        state = PlayServicesManagerState.CAN_SIGN_IN
    }
    //endregion
}

interface PlayServicesManagerDelegate {
    fun playServicesStateDidUpdate(state: PlayServicesManagerState)
}