package uk.co.nathanwong.boycottbingo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.example.games.basegameutils.BaseGameActivity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends BaseGameActivity implements View.OnClickListener {

    List<String> list;
    LinearLayout rows;
    public int count = 0;
    Context c;

    private static final int BINGO_SIZE = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        c = this;

        String[] strings = getResources().getStringArray(R.array.boycottisms);
        list = Arrays.asList(strings);
        Collections.shuffle(list);

        rows = (LinearLayout) findViewById(R.id.main_rows);
        this.createBingo();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.action_refresh:
                onRefreshButtonPress(item);
                return true;
            case R.id.action_about:
                openAbout();
                return true;
            default:
                return false;
        }
    }

    private void openAbout() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    public boolean onRefreshButtonPress(MenuItem item) {
        rows.removeAllViews();
        this.createBingo();

        return true;
    }

    private void createBingo() {
        count = 0;
        Collections.shuffle(list);

        LinearLayout ll = new LinearLayout(this);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f));
        ll.setOrientation(LinearLayout.HORIZONTAL);

        int j = 0;
        for (int i = 0; i < BINGO_SIZE; i++) {
            if (j <  Math.ceil(Math.sqrt(BINGO_SIZE))) {
                // yes
            } else {
                j = 0;
                rows.addView(ll);
                ll = new LinearLayout(this);
                ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f));
                ll.setOrientation(LinearLayout.HORIZONTAL);
            }

            TextView text = new TextView(this);
            text.setText(list.get(i));
            text.setBackgroundDrawable(getResources().getDrawable(R.drawable.selecttransition));
            text.setPadding(10, 10, 10, 10);
            text.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));
            text.setGravity(Gravity.CENTER);
            text.setSelected(false);
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TransitionDrawable transition = (TransitionDrawable) view.getBackground();

                    if (!view.isSelected()) {
                        // Unselected
                        transition.startTransition(200);
                        view.setSelected(true);
                        count++;
                    } else {
                        transition.reverseTransition(200);
                        view.setSelected(false);
                        count--;
                    }

                    if (count == 9) {
                        // Success!
                        AlertDialog.Builder builder = new AlertDialog.Builder(c);
                        builder.setTitle(getString(R.string.main_dialog_title));
                        builder.setIcon(R.drawable.ic_good);
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

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }

                }
            });

            text.setTag(i);

            ll.addView(text);

            j++;
        }

        rows.addView(ll);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onSignInFailed() {

    }

    @Override
    public void onSignInSucceeded() {

    }
}
