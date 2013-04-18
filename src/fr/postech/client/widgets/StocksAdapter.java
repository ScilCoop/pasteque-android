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
package fr.postech.client.widgets;

import fr.postech.client.models.Catalog;
import fr.postech.client.models.Category;
import fr.postech.client.models.Product;
import fr.postech.client.models.Stock;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StocksAdapter extends BaseAdapter {

    private List<Stock> stocks;
    private List<Product> products;

    public StocksAdapter(Map<String, Stock> stocks, Catalog cat) {
        super();
        this.stocks = new ArrayList<Stock>();
        this.products = new ArrayList<Product>();
        for (Category c : cat.getRootCategories()) {
            parseCategory(c, cat, stocks);
        }
    }

    private void parseCategory(Category c, Catalog cat,
            Map<String, Stock> stocks) {
        for (Product p : cat.getProducts(c)) {
            if (stocks.containsKey(p.getId())) {
                Stock s = stocks.get(p.getId());
                if (s.isManaged()) {
                    this.stocks.add(stocks.get(p.getId()));
                    this.products.add(p);
                }
            }
        }
        /*for (Category sub : c.getSubcategories()) {
            parseCategory(sub, cat, stocks);
        }*/
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return this.stocks.get(position);
    }

    @Override
    public int getCount() {
        return this.stocks.size();
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Stock stock = this.stocks.get(position);
        Product prd = this.products.get(position);
        if (convertView != null && convertView instanceof StockItem) {
            // Reuse the view
            StockItem item = (StockItem) convertView;
            item.reuse(stock, prd);
            return item;
        } else {
            // Create the view
            Context ctx = parent.getContext();
            StockItem item = new StockItem(ctx, stock, prd);
            return item;
        }
    }
}
