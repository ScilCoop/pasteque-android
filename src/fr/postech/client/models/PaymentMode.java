/*
    POS-Tech Android
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
package fr.postech.client.models;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

import fr.postech.client.R;

public class PaymentMode implements Serializable {

    /** Give back money when to much is given */
    public static final int GIVE_BACK = 1;
    /** Must be assigned to a customer */
    public static final int CUST_ASSIGNED = 2;

    private String code;
    private int labelResource;
    private int iconResource;
    private int flags;

    public PaymentMode(String code, int labelResource, int iconResource,
                       int flags) {
        this.code = code;
        this.labelResource = labelResource;
        this.iconResource = iconResource;
        this.flags = flags;
    }

    public String getLabel(Context ctx) {
        return ctx.getString(this.labelResource);
    }

    public String getCode() {
        return this.code;
    }

    public Drawable getIcon(Context ctx) {
        return ctx.getResources().getDrawable(this.iconResource);
    }

    public boolean isGiveBack() {
        return (this.flags & GIVE_BACK) > 0;
    }

    private static List<PaymentMode> defaultModes;
    public static List<PaymentMode> defaultModes(Context ctx) {
        if (defaultModes == null) {
            initDefaultModes(ctx);
        }
        return defaultModes;
    }
    public static void initDefaultModes(Context ctx) {
        defaultModes = new ArrayList<PaymentMode>();
        defaultModes.add(new PaymentMode("cash", R.string.pm_cash,
                                         R.drawable.cash, GIVE_BACK));
        defaultModes.add(new PaymentMode("cheque", R.string.pm_cheque,
                                         R.drawable.cheque, 0));
        defaultModes.add(new PaymentMode("magcard", R.string.pm_magcard,
                                         R.drawable.magcard, 0));
        defaultModes.add(new PaymentMode("paperin", R.string.pm_paper,
                                         R.drawable.paper, 0));
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("code", this.code);
        return o;
    }
}