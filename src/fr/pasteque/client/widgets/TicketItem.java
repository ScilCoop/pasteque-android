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
import fr.pasteque.client.models.Ticket;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.RelativeLayout;

public class TicketItem extends RelativeLayout {

    private Ticket ticket;

    private TextView label;

    public TicketItem(Context context, Ticket t) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.ticket_item,
                                             this,
                                             true);
        this.label = (TextView) this.findViewById(R.id.ticket_label);
        this.reuse(t);
    }

    public void reuse(Ticket t) {
	this.ticket = t;
	String label = this.getContext().getString(R.string.ticket_label,
						   this.ticket.getLabel());
        this.label.setText(label);
    }

    public Ticket getTicket() {
        return this.ticket;
    }

}