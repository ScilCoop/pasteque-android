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

import fr.pasteque.client.models.Composition.Group;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.List;

public class GroupsAdapter extends BaseAdapter {

    List<Group> groups;

    public GroupsAdapter(List<Group> groups) {
        super();
        this.groups = groups;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return this.groups.get(position);
    }

    @Override
    public int getCount() {
        return this.groups.size();
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Group g = this.groups.get(position);
        if (convertView != null) {
            // Reuse the view
            GroupItem item = (GroupItem) convertView;
            item.reuse(g, parent.getContext());
            return item;
        } else {
            // Create the view
            Context ctx = parent.getContext();
            GroupItem item = new GroupItem(ctx, g);
            return item;
        }
    }
}
