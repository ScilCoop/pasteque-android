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

import fr.pasteque.client.models.interfaces.Item;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public class ItemAdapter extends BaseAdapter {

    private List<?extends Item> item;

    public ItemAdapter(List<?extends Item> item) {
        super();
        this.item = item;
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
        return this.item.get(position);
    }

    @Override
    public int getCount() {
        return this.item.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Item p = this.item.get(position);
        if (convertView != null) {
            ((BtnItem) convertView).reuse(parent.getContext(), p);
            return convertView;
        }
        return new BtnItem(parent.getContext(), p);
    }

    public void updateView(List<?extends Item> item) {
        this.item = item;
        this.notifyDataSetChanged();
    }
}
