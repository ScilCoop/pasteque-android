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
import fr.postech.client.data.CompositionData;
import fr.postech.client.models.Product;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.widget.GridView;

public class ProductBtnItem extends RelativeLayout {

    private Product product;

    private TextView label;
    private ImageView icon;

    public ProductBtnItem (Context context, Product product) {
        super(context);
        Resources r = context.getResources();
        int width = (int) r.getDimension(R.dimen.bigBtnWidth);
        int height = (int) r.getDimension(R.dimen.bigBtnHeight);
        GridView.LayoutParams lp = new GridView.LayoutParams(width, height);
        this.setLayoutParams(lp);
        this.setBackgroundResource(R.drawable.btn_bg_gradient);
        LayoutInflater.from(context).inflate(R.layout.product_btn_item,
                                                this,
                                                true);
        this.label = (TextView) this.findViewById(R.id.product_label);
        this.icon = (ImageView) this.findViewById(R.id.product_icon);

        this.reuse(product);
    }

    public void reuse(Product p) {
        this.product = p;
        this.label.setText(this.product.getLabel());
        if (this.product.getIcon() != null) {
            this.icon.setImageDrawable(this.product.getIcon());
        } else {
            if (CompositionData.isComposition(this.product)) {
                this.icon.setImageResource(R.drawable.prd_default);
            } else {
                this.icon.setImageResource(R.drawable.prd_default);
            }
        }
    }

    public Product getProduct() {
        return this.product;
    }
}
