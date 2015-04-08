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
import fr.pasteque.client.models.Customer;
import fr.pasteque.client.models.Ticket;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomersAdapter extends BaseAdapter {

    private List<Customer> customers;
    private Context ctx;

    public CustomersAdapter(List<Customer> c, Context ctx) {
        super();
        this.customers = c;
        this.ctx = ctx;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return this.customers.get(position);
    }

    @Override
    public int getCount() {
        return this.customers.size();
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Customer c = this.customers.get(position);
        if (convertView == null) {
            // Create the view
            LayoutInflater inflater = (LayoutInflater) this.ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.customer_item, parent, false);
        }
        // Reuse the view
        if (c!=null) {
            ((TextView) convertView.findViewById(R.id.customer_name)).setText(c.getName());
        } else {
            ((TextView) convertView.findViewById(R.id.customer_name)).setText(ctx.getString(R.string.customer_none));
        }

        return convertView;

    }
}
