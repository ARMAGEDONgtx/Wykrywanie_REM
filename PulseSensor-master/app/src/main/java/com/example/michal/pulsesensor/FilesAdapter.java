package com.example.michal.pulsesensor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by kuba on 2018-01-10.
 */

public class FilesAdapter extends ArrayAdapter<String> {

    public FilesAdapter(Context context, ArrayList<String > adapter){
        super(context,0,adapter);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.spinnerlayout, parent, false);
        }
        TextView textView = (TextView)convertView.findViewById(R.id.textView2);
        String currentStr = getItem(position);
        if (currentStr!=null){
            textView.setText(currentStr);
        }
        return convertView;
    }
}
