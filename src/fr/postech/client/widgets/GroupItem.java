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
import fr.postech.client.models.Composition.Group;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.Gallery;
import android.widget.TextView;
import android.widget.RelativeLayout;

public class GroupItem extends RelativeLayout {

    private Group group;

    private TextView name;
    private ImageView icon;

    public GroupItem(Context context, Group group) {
        super(context);
        Resources r = context.getResources();
        int width = r.getDimensionPixelSize(R.dimen.bigBtnWidth);
        int height = r.getDimensionPixelSize(R.dimen.bigBtnHeight);
        this.setLayoutParams(new Gallery.LayoutParams(width, height));
        LayoutInflater.from(context).inflate(R.layout.group_item,
                                                this,
                                                true);
        this.name = (TextView) this.findViewById(R.id.group_name);
        this.icon = (ImageView) this.findViewById(R.id.group_icon);

        this.reuse(group, context);
    }

    public void reuse(Group g, Context ctx) {
        this.group = g;
        this.name.setText(this.group.getLabel());
        this.icon.setImageResource(R.drawable.category_default);
    }

    public Group getGroup() {
        return this.group;
    }
}
