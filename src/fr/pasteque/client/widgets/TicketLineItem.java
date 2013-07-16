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
import fr.pasteque.client.TicketLineEditListener;
import fr.pasteque.client.models.TicketLine;
import fr.pasteque.client.models.Product;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.RelativeLayout;

public class TicketLineItem extends RelativeLayout {

    private TicketLine line;
    private TicketLineEditListener listener;

    private Product p;
    private TextView label;
    private TextView quantity;

    public TicketLineItem (Context context, TicketLine line) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.ticket_line_item,
                                                this,
                                                true);
        this.p = line.getProduct();
        this.label = (TextView) this.findViewById(R.id.product_label);
        this.quantity = (TextView) this.findViewById(R.id.product_quantity);
        View add = this.findViewById(R.id.product_add);
        add.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    add();
                }
            });
        View remove = this.findViewById(R.id.product_remove);
        remove.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    remove();
                }
            });
        View modify = this.findViewById(R.id.product_modify);
        modify.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    modify();
                }
            });
        View delete = this.findViewById(R.id.product_delete);
        delete.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    delete();
                }
            });
            if (p.isScaled()) {
            /* If the product is scaled, replaces the add/remove button
             * by a scale button */
            add.setVisibility(GONE);
            remove.setVisibility(GONE);
            LayoutParams params = (LayoutParams)quantity.getLayoutParams();
            params.width = 100;
            params.addRule(RelativeLayout.LEFT_OF, R.id.product_modify);
            quantity.setLayoutParams(params);
        } else {
            modify.setVisibility(GONE);
        }
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

    /** Modifies the weight of the product*/
    public void modify() {
        if (this.listener != null) {
            this.listener.mdfyQty(this.line);
        }
    }

    public void delete() {
        if (this.listener != null) {
            this.listener.delete(this.line);
        }
    }
}
