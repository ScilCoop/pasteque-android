/*
    Pasteque Android client
    Copyright (C) Pasteque contributors, see the COPYRIGHT file

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package fr.pasteque.client.widgets;

import fr.pasteque.client.R;
import fr.pasteque.client.models.TariffArea;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;

public class TariffAreasAdapter extends BaseAdapter {

    public static final float HEIGHT_DIP = 46.4f; // same as in xml with margin

    private List<TariffArea> areas;

    public TariffAreasAdapter(List<TariffArea> areas) {
        super();
        this.areas = areas;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return this.areas.get(position);
    }

    @Override
    public int getCount() {
        return this.areas.size();
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TariffArea a = this.areas.get(position);
        Context ctx = parent.getContext();
        if (convertView == null) {
            // Create the view
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.tariff_area_item, parent,
                    false);
        }
        // Reuse the view
        String label = null;
        if (a == null) {
            label = ctx.getString(R.string.default_tariff_area);
        } else {
            label = a.getLabel();
        }
        ((TextView)convertView.findViewById(R.id.tariff_area_label)).setText(label);
        return convertView;
    }
}
