package uk.co.nathanwong.boycottbingo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

import java.util.Arrays;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import uk.co.nathanwong.boycottbingo.interfaces.BingoDataProvider;
import uk.co.nathanwong.boycottbingo.models.BingoStringArrayDataProvider;
import uk.co.nathanwong.boycottbingo.models.BingoViewModelState;
import uk.co.nathanwong.boycottbingo.utils.GameUtils;
import uk.co.nathanwong.boycottbingo.viewmodels.BingoViewViewModel;
import uk.co.nathanwong.boycottbingo.views.BingoView;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    BingoView bingoView;
    BingoViewViewModel viewModel;
    Toolbar toolbar;

    SharedPreferences settings = null;
    SharedPreferences.Editor editor = null;
    int score = 0;

    private GoogleApiClient mGoogleApiClient;
    private AlertDialog mAlertDialog;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInflow = false;
    private boolean mSignInClicked = false;

    private static int RC_SIGN_IN = 9001;

    private static final int BINGO_SIZE = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        updateViewState();

        findViewById(R.id.main_signin).setOnClickListener(this);
        findViewById(R.id.main_leaderboard).setOnClickListener(this);

        String[] strings = getResources().getStringArray(R.array.boycottisms);
        BingoDataProvider dataProvider = new BingoStringArrayDataProvider(Arrays.asList(strings), BINGO_SIZE);

        mAlertDialog = buildAlertDialog();

        viewModel = new BingoViewViewModel(dataProvider);
        viewModel.getCompletedObservable().subscribe(stateObserver);
        bingoView = findViewById(R.id.main_rows);
        bingoView.setViewModel(viewModel);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        Drawable drawable = menu.findItem(R.id.action_refresh).getIcon();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(this, android.R.color.white));
        menu.findItem(R.id.action_refresh).setIcon(drawable);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_leaderboard).setVisible(isSignedIn());
        menu.findItem(R.id.action_logout).setVisible(isSignedIn());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.action_refresh:
                onRefreshButtonPress(item);
                return true;
            case R.id.action_leaderboard:
                openLeaderboard();
                return true;
            case R.id.action_about:
                openAbout();
                return true;
            case R.id.action_logout:
                Games.signOut(mGoogleApiClient);
                updateViewState();
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                // Bring up error dialog
                GameUtils.showActivityResultError(this, requestCode, resultCode, R.string.unable_to_sign_in);
            }
        }
    }

    private Observer<BingoViewModelState> stateObserver = new Observer<BingoViewModelState>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(BingoViewModelState bingoViewModelState) {
            if (bingoViewModelState == BingoViewModelState.COMPLETE) {
                bingoComplete();
            }
        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onComplete() {

        }
    };

    private void openAbout() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private void openLeaderboard() {
        if (mGoogleApiClient.isConnected()) {
            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, getString(R.string.leaderboard_id)), 12345);
        }
    }

    public void onRefreshButtonPress(MenuItem item) {
        viewModel.newBingoBoard();
    }

    private void bingoComplete() {
        mAlertDialog.show();
        if (isSignedIn()) {
            Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderboard_id), incrementScore());
        }
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.main_signin) {
            mSignInClicked = true;
            mGoogleApiClient.connect();
        } else if (view.getId() == R.id.main_leaderboard) {
            openLeaderboard();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // The player is signed in
        findViewById(R.id.main_signin).setVisibility(View.GONE);
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            // already resolving
            return;
        }

        // if we clicked sign-in or if auto was enabled, then launch the sign-in flow
        if (mSignInClicked || mAutoStartSignInflow) {
            mAutoStartSignInflow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the failure in Game utils
            if (!GameUtils.resolveConnectionFailure(this, mGoogleApiClient, connectionResult, RC_SIGN_IN)) {
                mResolvingConnectionFailure = false;
            }
        }

        findViewById(R.id.main_signin).setVisibility(View.VISIBLE);
        findViewById(R.id.main_leaderboard).setVisibility(View.GONE);
    }

    void updateViewState() {
        if (!GameUtils.isGooglePlayServicesAvailable(this)) {
            findViewById(R.id.main_signin).setVisibility(View.GONE);
            findViewById(R.id.main_leaderboard).setVisibility(View.GONE);
        } if (isSignedIn()) {
            findViewById(R.id.main_signin).setVisibility(View.GONE);
            findViewById(R.id.main_leaderboard).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.main_signin).setVisibility(View.VISIBLE);
            findViewById(R.id.main_leaderboard).setVisibility(View.GONE);
        }
    }

    boolean isSignedIn() {
        return mGoogleApiClient != null && mGoogleApiClient.isConnected();
    }

    private int incrementScore() {
        settings = getSharedPreferences("score", Context.MODE_PRIVATE);
        editor = settings.edit();
        score = settings.getInt("score", 0);
        score++;
        editor.putInt("score", score);
        editor.apply();
        return score;
    }

    private AlertDialog buildAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.main_dialog_title));
        builder.setMessage(getString(R.string.main_dialog_text))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.main_dialog_positive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.this.onRefreshButtonPress(null);
                    }
                })
                .setNegativeButton(getString(R.string.main_dialog_negative), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        return builder.create();
    }
}
