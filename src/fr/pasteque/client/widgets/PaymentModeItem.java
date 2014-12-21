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
import fr.pasteque.client.models.PaymentMode;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.Gallery;
import android.widget.TextView;
import android.widget.RelativeLayout;
import java.io.IOException;

public class PaymentModeItem extends RelativeLayout {

    private PaymentMode mode;

    private TextView name;
    private ImageView icon;

    public PaymentModeItem(Context context, PaymentMode mode) {
        super(context);
        Resources r = context.getResources();
        int width = r.getDimensionPixelSize(R.dimen.bigBtnWidth);
        int height = r.getDimensionPixelSize(R.dimen.bigBtnHeight);
        this.setLayoutParams(new Gallery.LayoutParams(width, height));
        LayoutInflater.from(context).inflate(R.layout.payment_mode_item,
                                                this,
                                                true);
        this.name = (TextView) this.findViewById(R.id.mode_name);
        this.icon = (ImageView) this.findViewById(R.id.mode_icon);

        this.reuse(mode, context);
    }

    public void reuse(PaymentMode m, Context ctx) {
        this.mode = m;
        this.name.setText(this.mode.getLabel());
        if (this.mode.hasImage()) {
            Bitmap icon = null;
            try {
                icon = ImagesData.getPaymentModeImage(ctx, this.mode.getId());
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.icon.setImageBitmap(icon);
        } else {
            this.icon.setImageDrawable(null);
        }
    }

    public PaymentMode getMode() {
        return this.mode;
    }
}