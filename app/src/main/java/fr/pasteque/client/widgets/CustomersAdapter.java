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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CustomersAdapter extends BaseAdapter implements Filterable {

    private Context ctx;
    private List<Customer> customers;
    private List<Customer> customersFiltered;
    private CustomerFilter customerFilter;

    public CustomersAdapter(List<Customer> c, Context ctx) {
        super();
        this.ctx = ctx;
        this.customers = c;
        this.customersFiltered = c;
        getFilter();
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
        return this.customersFiltered.get(position);
    }

    @Override
    public int getCount() {
        return this.customersFiltered.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Customer c = this.customersFiltered.get(position);
        if (convertView == null) {
            // Create the view
            LayoutInflater inflater = (LayoutInflater) this.ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.customer_item, parent, false);
        }
        // Reuse the view
        if (c != null) {
            ((TextView) convertView.findViewById(R.id.customer_name)).setText(c.getName());
        } else {
            ((TextView) convertView.findViewById(R.id.customer_name)).setText(ctx.getString(R.string.customer_none));
        }

        return convertView;

    }

    @Override
    public Filter getFilter() {
        if (this.customerFilter == null) {
            this.customerFilter = new CustomerFilter();
        }
        return this.customerFilter;
    }

    private class CustomerFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraintSequence) {
            FilterResults results = new FilterResults();
            if (constraintSequence != null && constraintSequence.length() > 0) {
                String constraint = constraintSequence.toString().toLowerCase();
                ArrayList<Customer> filterList = new ArrayList<>();

                for (Customer cus : CustomersAdapter.this.customers) {
                    if (cus == null || cus.getFirstName().toLowerCase().contains(constraint)
                            || cus.getLastName().contains(constraint)) {
                        filterList.add(cus);
                    }
                }
                results.count = filterList.size();
                results.values = filterList;
                return results;
            }
            results.count = CustomersAdapter.this.customers.size();
            results.values = CustomersAdapter.this.customers;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            CustomersAdapter.this.customersFiltered = (ArrayList<Customer>) results.values;
            notifyDataSetChanged();
        }
    }
}
