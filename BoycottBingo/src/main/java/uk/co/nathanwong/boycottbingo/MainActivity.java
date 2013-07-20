package uk.co.nathanwong.boycottbingo;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.GridView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    GridView grid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] strings = getResources().getStringArray(R.array.boycottisms);
        List<String> list = Arrays.asList(strings);
        Collections.shuffle(list);

        grid = (GridView) findViewById(R.id.main_gridView);
        grid.setAdapter(new BoycottAdapter(this, list));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
