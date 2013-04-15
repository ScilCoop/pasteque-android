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
import fr.postech.client.models.Receipt;
import fr.postech.client.models.TicketLine;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.RelativeLayout;

public class ReceiptItem extends RelativeLayout {

    private Receipt receipt;

    private TextView label;
    private TextView content;

    public ReceiptItem(Context context, Receipt r) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.receipt_item,
                this, true);
        this.label = (TextView) this.findViewById(R.id.receipt_label);
        this.content = (TextView) this.findViewById(R.id.receipt_content);
        this.reuse(r);
    }

    public void reuse(Receipt r) {
        this.receipt = r;
        String label = this.getContext().getString(R.string.ticket_label,
                this.receipt.getTicket().getLabel());
        this.label.setText(label);
        String content = "";
        for (TicketLine l : r.getTicket().getLines()) {
            content += l.getProduct().getLabel() + " x " + l.getQuantity() + ", ";
        }
        if (content != "") {
            content = content.substring(0, content.length() - 2);
        }
        this.content.setText(content);
    }

    public Receipt getReceipt() {
        return this.receipt;
    }

}
