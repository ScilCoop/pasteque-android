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
import fr.postech.client.models.Customer;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.RelativeLayout;

public class CustomerItem extends RelativeLayout {

    private Customer customer;

    private TextView name;

    public CustomerItem(Context context, Customer c) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.customer_item,
                                             this,
                                             true);
        this.name = (TextView) this.findViewById(R.id.customer_name);
        this.reuse(c);
    }

    public void reuse(Customer c) {
        this.customer = c;
        if (this.customer != null) {
            this.name.setText(c.getName());
        } else {
            this.name.setText(this.getContext().getString(R.string.customer_none));
        }
    }

    public Customer getCustomer() {
        return this.customer;
    }

}
