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
import fr.pasteque.client.interfaces.InventoryLineEditListener;
import fr.pasteque.client.data.CatalogData;
import fr.pasteque.client.models.Inventory;
import fr.pasteque.client.models.Inventory.InventoryItem;
import fr.pasteque.client.models.Product;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class InventoryAdapter extends BaseAdapter {

    private Context ctx;
    private Inventory inventory;
    private int stockType;
    private InventoryLineEditListener listener;

    public InventoryAdapter(Inventory inv, int stockType,
            InventoryLineEditListener l, Context ctx) {
        super();
        this.inventory = inv;
        this.stockType = stockType;
        this.listener = l;
        this.ctx = ctx;
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
        return this.inventory.getItemAt(position, this.stockType);
    }

    @Override
    public int getCount() {
        return this.inventory.size(this.stockType);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final InventoryItem item = this.inventory.getItemAt(position,
                this.stockType);
        Product p = CatalogData.catalog(this.ctx).getProduct(item.getProductId());
        if (convertView == null) {
            // Create the view
            LayoutInflater inflater = (LayoutInflater) this.ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.ticket_item_line, parent,
                    false);
        }
        // Reuse the view
        ((TextView)convertView.findViewById(R.id.product_label)).setText(p.getLabel());
        ((TextView)convertView.findViewById(R.id.product_quantity)).setText(String.valueOf(item.getQuantity()));
        View add = convertView.findViewById(R.id.product_add);
        add.setOnClickListener(new View.OnClickListener() {
                @Override
				public void onClick(View v) {
                    if (InventoryAdapter.this.listener != null) {
                        InventoryAdapter.this.listener.addQty(item);
                    }
                }
            });
        View remove = convertView.findViewById(R.id.product_subtract);
        remove.setOnClickListener(new View.OnClickListener() {
                @Override
				public void onClick(View v) {
                    if (InventoryAdapter.this.listener != null) {
                        InventoryAdapter.this.listener.remQty(item);
                    }
                }
            });
        View modify = convertView.findViewById(R.id.product_edit);
        modify.setOnClickListener(new View.OnClickListener() {
                @Override
				public void onClick(View v) {
                    if (InventoryAdapter.this.listener != null) {
                        InventoryAdapter.this.listener.mdfyQty(item);
                    }

                }
            });
        View delete = convertView.findViewById(R.id.product_delete);
        delete.setOnClickListener(new View.OnClickListener() {
                @Override
				public void onClick(View v) {
                    if (InventoryAdapter.this.listener != null) {
                        InventoryAdapter.this.listener.delete(item);
                    }
                }
            });
        return convertView;
    }
}
