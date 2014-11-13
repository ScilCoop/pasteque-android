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
package fr.pasteque.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.pasteque.client.data.CustomerData;
import fr.pasteque.client.data.SessionData;
import fr.pasteque.client.models.Customer;
import fr.pasteque.client.models.Ticket;
import fr.pasteque.client.widgets.CustomersAdapter;

public class CustomerSelect extends Activity
    implements AdapterView.OnItemClickListener {

    private static final String LOG_TAG = "Pasteque/CustomerSelect";
    public static final int CODE_CUSTOMER = 3;

    public static void setup(boolean nullable) {
        nullableInitializer = nullable;
    }
    private static boolean nullableInitializer;

    private ListView list;
    private boolean nullable;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        if (state != null) {
            this.nullable = state.getBoolean("nullable");
        } else {
            this.nullable = nullableInitializer;
        }
        // Set views
        setContentView(R.layout.customer_select);
        this.list = (ListView) this.findViewById(R.id.customers_list);
        List<Customer> data = null;
        if (this.nullable) {
            data = new ArrayList<Customer>();
            data.add(null);
            data.addAll(CustomerData.customers);
        } else {
            data = CustomerData.customers;
        }
        this.list.setAdapter(new CustomersAdapter(data, this));
        this.list.setOnItemClickListener(this);
    }

    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putBoolean("nullable", this.nullable);
    }

    public void onItemClick(AdapterView<?> parent, View v, int position,
                            long id) {
        Ticket t = SessionData.currentSession(this).getCurrentTicket();
        Customer c = (Customer) this.list.getAdapter().getItem(position);
        t.setCustomer(c);
        this.setResult(Activity.RESULT_OK);
        // Kill
        this.finish();
    }
}
