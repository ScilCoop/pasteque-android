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
import fr.postech.client.models.PaymentMode;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.RelativeLayout;

public class PaymentModeItem extends RelativeLayout {

    private PaymentMode mode;

    private TextView name;

    public PaymentModeItem(Context context, PaymentMode mode) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.payment_mode_item,
                                                this,
                                                true);
        this.name = (TextView) this.findViewById(R.id.mode_name);

        this.reuse(mode);
    }

    public void reuse(PaymentMode m) {
        this.mode = m;
        this.name.setText(this.mode.getCode());
    }

    public PaymentMode getMode() {
        return this.mode;
    }
}