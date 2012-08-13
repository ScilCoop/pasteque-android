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
import fr.postech.client.TicketLineEditListener;
import fr.postech.client.models.TicketLine;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.RelativeLayout;

public class TicketLineItem extends RelativeLayout {

    private TicketLine line;
    private TicketLineEditListener listener;

    private TextView label;
    private TextView quantity;

    public TicketLineItem (Context context, TicketLine line) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.ticket_line_item,
                                                this,
                                                true);
        this.label = (TextView) this.findViewById(R.id.product_label);
        this.quantity = (TextView) this.findViewById(R.id.product_quantity);
        Button add = (Button) this.findViewById(R.id.product_add);
        add.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    add();
                }
            });
        Button remove = (Button) this.findViewById(R.id.product_remove);
        remove.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    remove();
                }
            });
        ImageButton delete = (ImageButton) this.findViewById(R.id.product_delete);
        delete.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    delete();
                }
            });
        this.reuse(line);
    }

    public void reuse(TicketLine line) {
        this.line = line;
        this.label.setText(this.line.getProduct().getLabel());
        this.quantity.setText(String.valueOf(this.line.getQuantity()));
    }

    public void setEditListener(TicketLineEditListener l) {
        this.listener = l;
    }

    public TicketLine getLine() {
        return this.line;
    }

    public void add() {
        if (this.listener != null) {
            this.listener.addQty(this.line);
        }
    }

    public void remove() {
        if (this.listener != null) {
            this.listener.remQty(this.line);
        }
    }

    public void delete() {
        if (this.listener != null) {
            this.listener.delete(this.line);
        }
    }
}