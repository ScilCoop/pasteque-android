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
import fr.pasteque.client.data.CompositionData;
import fr.pasteque.client.data.ImagesData;
import fr.pasteque.client.models.Product;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.widget.GridView;
import java.io.IOException;

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
        this.setBackgroundResource(R.drawable.btn_product);
        LayoutInflater.from(context).inflate(R.layout.product_btn_item,
                                                this,
                                                true);
        this.label = (TextView) this.findViewById(R.id.product_label);
        this.icon = (ImageView) this.findViewById(R.id.product_icon);

        this.reuse(product, context);
    }

    public void reuse(Product p, Context ctx) {
        this.product = p;
        this.label.setText(this.product.getLabel());
        Bitmap icon = null;
        try {
            icon = ImagesData.getProductImage(ctx, this.product.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (icon != null) {
            this.icon.setImageBitmap(icon);
        }
    }

    public Product getProduct() {
        return this.product;
    }
}
