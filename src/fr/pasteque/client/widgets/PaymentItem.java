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

import fr.pasteque.client.PaymentEditListener;
import fr.pasteque.client.R;
import fr.pasteque.client.models.Payment;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.RelativeLayout;

public class PaymentItem extends RelativeLayout {

    private Payment payment;
    private PaymentEditListener listener;

    private TextView type;
    private TextView amount;

    public PaymentItem(Context context, Payment payment) {
        super(context);
        Resources r = context.getResources();
        int width = r.getDimensionPixelSize(R.dimen.bigBtnWidth);
        int height = r.getDimensionPixelSize(R.dimen.bigBtnHeight);
        LayoutInflater.from(context).inflate(R.layout.payment_item,
                                                this,
                                                true);
        this.type = (TextView) this.findViewById(R.id.payment_type);
        this.amount = (TextView) this.findViewById(R.id.payment_amount);

        View delete = this.findViewById(R.id.payment_delete);
        delete.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    delete();
                }
            });

        this.reuse(payment, context);
    }

    public void reuse(Payment p, Context ctx) {
        this.payment = p;
        this.type.setText(this.payment.getMode().getLabel());
        this.amount.setText(String.format("%.2f", this.payment.getAmount()));
    }

    public Payment getPayment() {
        return this.payment;
    }

    public void setEditListener(PaymentEditListener l) {
        this.listener = l;
    }

    public void delete() {
        if (this.listener != null) {
            this.listener.deletePayment(this.payment);
        }
    }
}