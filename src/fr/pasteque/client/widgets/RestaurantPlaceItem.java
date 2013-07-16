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

import fr.pasteque.client.data.SessionData;
import fr.pasteque.client.R;
import fr.pasteque.client.models.Place;
import fr.pasteque.client.models.Session;
import fr.pasteque.client.models.Ticket;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.RelativeLayout;

public class RestaurantPlaceItem extends RelativeLayout {

    private Place place;

    private TextView label;
    private ImageView occupied;
    private Context ctx;

    public RestaurantPlaceItem(Context context, Place p) {
        super(context);
        this.ctx = context;
        LayoutInflater.from(context).inflate(R.layout.restaurant_place_item,
                                             this,
                                             true);
        this.label = (TextView) this.findViewById(R.id.place_label);
        this.occupied = (ImageView) this.findViewById(R.id.place_occupied);
        this.reuse(p);
    }

    public void reuse(Place p) {
        this.place = p;
        this.label.setText(this.place.getName());
        // TODO: should write a more elegant way to check if the table
        // is occupied
        for (Ticket t : SessionData.currentSession(this.ctx).getTickets()) {
            if (t.getLabel().equals(p.getName())) {
                this.occupied.setVisibility(View.VISIBLE);
                return;
            }
        }
        this.occupied.setVisibility(View.INVISIBLE);
    }

    public Place getPlace() {
        return this.place;
    }

}
