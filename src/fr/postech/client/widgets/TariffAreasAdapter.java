/*
    POS-Tech Android
    Copyright (C) 2012 SARL SCOP Scil (contact@scil.coop)

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
package fr.postech.client.widgets;

import fr.postech.client.models.TariffArea;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.List;

public class TariffAreasAdapter extends BaseAdapter {

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
        if (convertView != null && convertView instanceof TariffAreaItem) {
            // Reuse the view
            TariffAreaItem item = (TariffAreaItem) convertView;
            item.reuse(a);
            return item;
        } else {
            // Create the view
            Context ctx = parent.getContext();
            TariffAreaItem item = new TariffAreaItem(ctx, a);
            return item;
        }
    }
}
