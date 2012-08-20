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
import fr.postech.client.models.Category;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.Gallery;
import android.widget.TextView;
import android.widget.RelativeLayout;

public class CategoryItem extends RelativeLayout {

    private Category category;

    private TextView name;
    private ImageView icon;

    public CategoryItem(Context context, Category category) {
        super(context);
        Resources r = context.getResources();
        int width = r.getDimensionPixelSize(R.dimen.bigBtnWidth);
        int height = r.getDimensionPixelSize(R.dimen.bigBtnHeight);
        this.setLayoutParams(new Gallery.LayoutParams(width, height));
        LayoutInflater.from(context).inflate(R.layout.category_item,
                                                this,
                                                true);
        this.name = (TextView) this.findViewById(R.id.category_name);
        this.icon = (ImageView) this.findViewById(R.id.category_icon);

        this.reuse(category, context);
    }

    public void reuse(Category cat, Context ctx) {
        this.category = cat;
        this.name.setText(this.category.getLabel());
        if (this.category.getIcon() != null) {
            this.icon.setImageDrawable(this.category.getIcon());
        } else {
            this.icon.setImageResource(R.drawable.category_default);
        }
    }

    public Category getCategory() {
        return this.category;
    }
}