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
import fr.postech.client.models.TariffArea;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.RelativeLayout;

public class TariffAreaItem extends RelativeLayout {

    private TariffArea area;

    private TextView label;

    public TariffAreaItem(Context context, TariffArea ta) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.tariff_area_item,
                this, true);
        this.label = (TextView) this.findViewById(R.id.tariff_area_label);
        this.reuse(ta);
    }

    public void reuse(TariffArea area) {
        this.area = area;
        if (this.area != null) {
            this.label.setText(area.getLabel());
        } else {
            this.label.setText(this.getContext().getString(R.string.default_tariff_area));
        }
    }

    public TariffArea getArea() {
        return this.area;
    }

}
