package uk.co.nathanwong.boycottbingo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import uk.co.nathanwong.boycottbingo.interfaces.BingoDataProvider;
import uk.co.nathanwong.boycottbingo.manager.PlayServicesManager;
import uk.co.nathanwong.boycottbingo.manager.PlayServicesManagerDelegate;
import uk.co.nathanwong.boycottbingo.manager.PlayServicesManagerState;
import uk.co.nathanwong.boycottbingo.models.BingoStringArrayDataProvider;
import uk.co.nathanwong.boycottbingo.models.BingoViewModelState;
import uk.co.nathanwong.boycottbingo.viewmodels.BingoViewViewModel;
import uk.co.nathanwong.boycottbingo.views.BingoView;

public class MainActivity extends AppCompatActivity
        implements PlayServicesManagerDelegate, View.OnClickListener {

    private BingoView bingoView;
    private BingoViewViewModel viewModel;
    private Toolbar toolbar;

    private SharedPreferences settings = null;
    private SharedPreferences.Editor editor = null;
    private int score = 0;

    private PlayServicesManager mPlayServicesManager;
    private AlertDialog mAlertDialog;

    private static final int BINGO_SIZE = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPlayServicesManager = new PlayServicesManager(this);
        mPlayServicesManager.setDelegate(this);

        findViewById(R.id.main_signin).setOnClickListener(this);

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
    protected void onResume() {
        super.onResume();
        mPlayServicesManager.silentSignIn();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        bingoView.destroy();
        super.onDestroy();
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
        menu.findItem(R.id.action_leaderboard).setVisible(mPlayServicesManager.isSignedIn());
        menu.findItem(R.id.action_logout).setVisible(mPlayServicesManager.isSignedIn());
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
                mPlayServicesManager.signOut();
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPlayServicesManager.processActivityResult(this, requestCode, resultCode, data);
    }

    private final Observer<BingoViewModelState> stateObserver = new Observer<BingoViewModelState>() {
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
        mPlayServicesManager.showLeaderboard();
    }

    private void onRefreshButtonPress(MenuItem item) {
        viewModel.newBingoBoard();
    }

    private void bingoComplete() {
        mAlertDialog.show();
        mPlayServicesManager.submitScore(getString(R.string.leaderboard_id), incrementScore());
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.main_signin) {
            mPlayServicesManager.clickSignIn();
        }
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

    //region PlayServicesManagerDelegate methods
    @Override
    public void playServicesStateDidUpdate(@NotNull PlayServicesManagerState state) {
        switch (state) {
            case NOT_AVAILABLE:
            case SIGNED_IN:
                findViewById(R.id.main_signin).setVisibility(View.GONE);
                break;
            case CAN_SIGN_IN:
                findViewById(R.id.main_signin).setVisibility(View.VISIBLE);
                break;
        }
        supportInvalidateOptionsMenu();
    }

    //endregion
}
