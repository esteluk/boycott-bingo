package uk.co.nathanwong.boycottbingo;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    GridView grid;

    private static final int BINGO_SIZE = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] strings = getResources().getStringArray(R.array.boycottisms);
        List<String> list = Arrays.asList(strings);
        Collections.shuffle(list);

        LinearLayout rows = (LinearLayout) findViewById(R.id.main_rows);

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
            text.setPadding(10, 10, 10, 10);
            text.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));
            text.setGravity(Gravity.CENTER);

            ll.addView(text);

            j++;
        }

        rows.addView(ll);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
