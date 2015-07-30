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
import fr.pasteque.client.data.ImagesData;
import fr.pasteque.client.models.Product;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.RelativeLayout;

public class ProductBtnItem extends RelativeLayout {

    private Product product;

    private TextView label;
    private ImageView icon;
    private RelativeLayout border;

    public ProductBtnItem(Context context, Product product) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.product_item, this, true);
        this.label = (TextView) this.findViewById(R.id.product_label);
        this.icon = (ImageView) this.findViewById(R.id.product_icon);
        this.border = (RelativeLayout) this.getRootView();

        this.reuse(context, product);
    }

    public void reuse(Context ctx, Product p) {
        this.product = p;
        this.label.setText(this.product.getLabel());
        Bitmap icon;
        if (this.product.hasImage() &&
                null != (icon = ImagesData.getProductImage(ctx, this.product.getId()))) {
            this.icon.setImageBitmap(icon);
        } else {
            this.icon.setImageResource(R.drawable.ic_placeholder_img);
        }
        this.border.setBackgroundResource(R.color.product_item_outer_bg);
    }

    public Product getProduct() {
        return this.product;
    }
}
