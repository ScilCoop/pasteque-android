/*
    POS-Tech Android client
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
import fr.postech.client.models.TicketLine;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.RelativeLayout;

public class TicketLineItem extends RelativeLayout {

    private TicketLine line;

    private TextView label;
    private TextView quantity;

    public TicketLineItem (Context context, TicketLine line) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.ticket_line_item,
                                                this,
                                                true);
        this.label = (TextView) this.findViewById(R.id.product_label);
        this.quantity = (TextView) this.findViewById(R.id.product_quantity);

        this.reuse(line);
    }

    public void reuse(TicketLine line) {
        this.line = line;
        this.label.setText(this.line.getProduct().getLabel());
        this.quantity.setText(String.valueOf(this.line.getQuantity()));
    }

    public TicketLine getLine() {
        return this.line;
    }
}