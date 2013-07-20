package uk.co.nathanwong.boycottbingo;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by nathan on 20/07/2013.
 */
public class BoycottAdapter extends BaseAdapter {
    private Context mContext;
    public List<String> mStrings;

    public BoycottAdapter(Context context, List<String> strings) {
        mContext = context;
        mStrings = strings;
    }

    public int getCount() {
        return 9;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        TextView text = null;
        if (convertView == null) {
            // If it's not recycled, we'll initialise it
            text = new TextView(mContext);
            text.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT));
            text.setText(mStrings.get(position));
            text.setPadding(10, 10, 10, 10);
        } else {
            text = (TextView) convertView;
        }

        return text;
    }

}
