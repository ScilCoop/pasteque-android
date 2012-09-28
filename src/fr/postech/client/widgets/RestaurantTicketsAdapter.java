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

import fr.postech.client.R;
import fr.postech.client.models.Floor;
import fr.postech.client.models.Place;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import java.util.List;

public class RestaurantTicketsAdapter extends BaseExpandableListAdapter {

    private List<Floor> floors;

    public RestaurantTicketsAdapter(List<Floor> floors) {
        super();
        this.floors = floors;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
    
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.floors.get(groupPosition).getPlaces().get(childPosition);
    }
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition * 1000 + childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView,
                             ViewGroup parent) {
        Place p = this.floors.get(groupPosition).getPlaces().get(childPosition);
        TextView v = new TextView(parent.getContext());
        v.setText(p.getName());
        return v;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.floors.get(groupPosition).getPlaces().size();
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public long getCombinedChildId(long groupId, long childId) {
        return groupId * 1000 + childId;
    }

    public long getCombinedGroupId(long groupId) {
        return groupId;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.floors.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.floors.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        Floor f = this.floors.get(groupPosition);
        TextView v = new TextView(parent.getContext());
        v.setText(f.getName());
        return v;
    }
}
