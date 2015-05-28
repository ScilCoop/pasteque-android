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
import fr.pasteque.client.models.Category;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.RelativeLayout;
import java.io.IOException;

public class CategoryItem extends RelativeLayout {

    private Category category;

    private TextView name;
    private ImageView icon;

    public CategoryItem(Context context, Category category) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.category_item, this, true);
        this.name = (TextView) this.findViewById(R.id.category_name);
        this.icon = (ImageView) this.findViewById(R.id.category_icon);

        this.reuse(category, context);
    }

    public void reuse(Category cat, Context ctx) {
        this.category = cat;
        this.name.setText(this.category.getLabel());
        Bitmap icon = null;
        try {
            icon = ImagesData.getCategoryImage(ctx, this.category.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (icon != null) {
            this.icon.setImageBitmap(icon);
        }
    }

    public Category getCategory() {
        return this.category;
    }
}