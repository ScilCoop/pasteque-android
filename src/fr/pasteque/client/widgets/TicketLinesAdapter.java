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

import fr.pasteque.client.TicketLineEditListener;
import fr.pasteque.client.models.Product;
import fr.pasteque.client.models.Ticket;
import fr.pasteque.client.models.TicketLine;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class TicketLinesAdapter extends BaseAdapter {

    private Product p;
    private Ticket ticket;
    private TicketLineEditListener listener;
    private boolean editable;

    public TicketLinesAdapter(Ticket ticket, TicketLineEditListener l, boolean editable) {
        super();
        this.ticket = ticket;
        this.listener = l;
        this.editable = editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
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
        return this.ticket.getLineAt(position);
    }

    @Override
    public int getCount() {
        return this.ticket.getLines().size();
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TicketLine line = this.ticket.getLineAt(position);
        p = line.getProduct();
        if (convertView != null && convertView instanceof TicketLineItem) {
            // Reuse the view
            TicketLineItem item = (TicketLineItem) convertView;
            item.reuse(line, this.ticket.getTariffArea(), this.editable);
            return item;
        } else {
            // Create the view
            Context ctx = parent.getContext();
            TicketLineItem item = new TicketLineItem(ctx, line,
                    this.ticket.getTariffArea(), this.editable);
            item.setEditListener(this.listener);
            return item;
        }
    }
}
