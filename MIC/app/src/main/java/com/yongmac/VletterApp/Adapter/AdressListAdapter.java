package com.yongmac.VletterApp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yongmac.VletterApp.R;

import java.util.ArrayList;

public class AdressListAdapter extends BaseAdapter {
    private LayoutInflater _inInflater;
    private ArrayList _profiles;
    private int _layout;


    public AdressListAdapter(Context context, int layout, ArrayList profiles) {
        _inInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _profiles = profiles;
        _layout = layout;
    }

    @Override
    public int getCount() {
        return _profiles.size();
    }

    @Override
    public Object getItem(int pos) {
        return _profiles.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = _inInflater.inflate(_layout, parent, false);
        }
        Adress adress = (Adress) _profiles.get(pos);

        TextView name = (TextView) convertView.findViewById(R.id.phoneName);
        TextView phoneNumber = (TextView) convertView.findViewById(R.id.phoneNumber);


        name.setText(adress.getName());
        phoneNumber.setText(adress.getPhoneNumber());

        return convertView;

    }
}
