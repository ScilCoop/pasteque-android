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
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import fr.pasteque.client.models.Catalog;
import fr.pasteque.client.models.Composition;
import fr.pasteque.client.models.Composition.Group;
import fr.pasteque.client.models.CompositionInstance;
import fr.pasteque.client.models.Product;
import fr.pasteque.client.utils.TrackedActivity;
import fr.pasteque.client.widgets.GroupsAdapter;
import fr.pasteque.client.widgets.BtnItem;
import fr.pasteque.client.widgets.ItemAdapter;

public class CompositionInput extends TrackedActivity
implements AdapterView.OnItemSelectedListener {

    private static final String LOG_TAG = "Pasteque/CompositionInput";

    private Catalog catalog;
    private Composition composition;
    private Group currentGroup;
    private CompositionInstance compoInstance;

    private TextView compoLbl;
    private Gallery groups;
    private GridView products;

    private static Catalog catalogInit;
    private static Composition compositionInit;
    public static void setup(Catalog catalog, Composition c) {
        catalogInit = catalog;
        compositionInit = c;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        // Init data
        boolean open = false;
        if (state != null) {
            // From state
            this.catalog = (Catalog) state.getSerializable("catalog");
            this.composition = (Composition) state.getSerializable("composition");
            this.compoInstance = (CompositionInstance) state.getSerializable("compoInstance");
        } else {
            // From scratch
            this.catalog = catalogInit;
            catalogInit = null;
            this.composition = compositionInit;
            compositionInit = null;
            this.compoInstance = new CompositionInstance(
                    this.catalog.getProduct(this.composition.getProductId()),
                    this.composition);
        }
        this.currentGroup = this.composition.getGroups().get(0);

        // Set views
        setContentView(R.layout.composition_input);
        this.compoLbl = (TextView) this.findViewById(R.id.composition);
        this.groups = (Gallery) this.findViewById(R.id.groupsGrid);
        this.products = (GridView) this.findViewById(R.id.productsGrid);
        GroupsAdapter adapt = new GroupsAdapter(this.composition.getGroups());
        this.groups.setAdapter(adapt);
        this.groups.setOnItemSelectedListener(this);
        this.groups.setSelection(0, false);
        ProductClickListener prdListnr = new ProductClickListener();
        this.products.setOnItemClickListener(prdListnr);
        this.updateTitle();
        this.updateProducts();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("catalog", this.catalog);
        outState.putSerializable("composition", this.composition);
        outState.putSerializable("compoInstance", this.compoInstance);
    }

    private void updateTitle() {
        this.compoLbl.setText(this.compoInstance.getLabel());
    }

    private void updateProducts() {
        List<Product> products = new ArrayList<Product>();
        for (String id : this.currentGroup.getProductIds()) {
            products.add(this.catalog.getProduct(id));
        }
        ItemAdapter prdAdapt = new ItemAdapter(products);
        this.products.setAdapter(prdAdapt);
    }

    private class ProductClickListener implements OnItemClickListener {
        @Override
		public void onItemClick(AdapterView<?> parent, View v,
                                int position, long id) {
            BtnItem item = (BtnItem) v;
            Product p = (Product) item.getItem();
            CompositionInput.this.compoInstance.setProduct(
                    CompositionInput.this.currentGroup, p);
            CompositionInput.this.updateTitle();
            if (CompositionInput.this.compoInstance.isFull()) {
                Intent i = new Intent();
                i.putExtra("composition", compoInstance);
                CompositionInput.this.setResult(Activity.RESULT_OK, i);
                CompositionInput.this.finish();
            } else {
                int currentPos = CompositionInput.this.groups.getSelectedItemPosition();
                if (currentPos < groups.getLastVisiblePosition()) {
                    groups.setSelection(currentPos + 1);
                }
            }
        }
    }

    /** Group selected */
    @Override
	public void onItemSelected(AdapterView<?> parent, View v,
                               int position, long id) {
        Group g = (Group) this.groups.getAdapter().getItem(position);
        this.currentGroup = g;
        this.updateProducts();
    }

    @Override
	public void onNothingSelected(AdapterView<?> parent) {
    }

}
