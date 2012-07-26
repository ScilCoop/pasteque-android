/*
    POS-Tech Android
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
package fr.postech.client;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import fr.postech.client.models.Product;
import fr.postech.client.models.Ticket;
import fr.postech.client.widgets.ProductBtnItem;
import fr.postech.client.widgets.ProductsBtnAdapter;

public class TicketInput extends Activity {

    private List<Product> catalog;
    private Ticket ticket;

    private TextView ticketLabel;
    private TextView ticketArticles;
    private TextView ticketTotal;

    private static List<Product> catalogInit;
    private static Ticket ticketInit;
    public static void setup(List<Product> catalog, Ticket ticket) {
        catalogInit = catalog;
        ticketInit = ticket;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle state)
    {
        super.onCreate(state);
        if (state != null) {
            this.catalog = new ArrayList<Product>();
            int size = state.getInt("size");
            for (int i = 0; i < size; i++) {
                Product p = (Product) state.getSerializable("prd" + i);
                this.catalog.add(p);
            }
        } else {
            this.catalog = catalogInit;
            catalogInit = null;
            if (ticketInit == null) {
                this.ticket = new Ticket();
            } else {
                this.ticket = ticketInit;
                ticketInit = null;
            }
        }
        setContentView(R.layout.products);
        this.ticketLabel = (TextView) this.findViewById(R.id.ticket_label);
        this.ticketArticles = (TextView) this.findViewById(R.id.ticket_articles);
        this.ticketTotal = (TextView) this.findViewById(R.id.ticket_total);
        GridView products = (GridView) this.findViewById(R.id.productsGrid);
        ProductsBtnAdapter adapt = new ProductsBtnAdapter(this.catalog);
        products.setAdapter(adapt);
        products.setOnItemClickListener(new ProductClickListener());
    }

    @Override
    public void onResume() {
        super.onResume();
        this.updateTicketView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("size", this.catalog.size());
        for (int i = 0; i < this.catalog.size(); i++) {
            outState.putSerializable("prd" + i, this.catalog.get(i));
        }
    }

    private void updateTicketView() {
        String count = this.getString(R.string.ticket_articles,
                                      this.ticket.getArticlesCount());
        String total = this.getString(R.string.ticket_total,
                                      this.ticket.getTotalPrice());
        this.ticketArticles.setText(count);
        this.ticketTotal.setText(total);
    }

    private class ProductClickListener implements OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View v,
                                int position, long id) {
            ProductBtnItem item = (ProductBtnItem) v;
            Product p = item.getProduct();
            TicketInput.this.ticket.addProduct(p);
            TicketInput.this.updateTicketView();
        }
    }
}
