package info.batiste.localtips;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Hashtable;

/**
 * Created by batiste on 20.11.16.
 */

public class TipListAdapter extends BaseAdapter {

    private Hashtable<String, TipRepresentation> mData = new Hashtable<>();
    private String[] mKeys;
    public TipListAdapter(Hashtable<String, TipRepresentation> data){
        updateData(data);
    }

    public void updateData(Hashtable<String, TipRepresentation> data) {
        mData  = data;
        mKeys = mData.keySet().toArray(new String[data.size()]);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public TipRepresentation getItem(int position) {
        return mData.get(mKeys[position]);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        String key = mKeys[pos];
        TipRepresentation item = getItem(pos);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.tip_item, parent, false);
        }
        // Lookup view for data population
        ImageView image = (ImageView) convertView.findViewById(R.id.imageView);
        TextView text = (TextView) convertView.findViewById(R.id.textView);
        TextView category = (TextView) convertView.findViewById(R.id.category);

        // Populate the data into the template view using the data object
        if(item.tip != null) {
            text.setText(item.tip.description);
            category.setText(item.tip.category);
            if(item.bitmap != null) {
                image.setImageBitmap(item.bitmap);
            }
        } else {
            Log.d("getView", "item.tip is null?");
        }

        return convertView;
    }
}
