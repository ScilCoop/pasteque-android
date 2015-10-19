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
package fr.pasteque.client.data.DataSavable;

import com.google.gson.reflect.TypeToken;
import fr.pasteque.client.models.PaymentMode;
import android.content.Context;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PaymentModeData extends AbstractJsonDataSavable {

    private static final String FILENAME = "paymentmodes.json";

    private List<PaymentMode> modes = new ArrayList<PaymentMode>();

    public void setPaymentModes(List<PaymentMode> p) {
        modes = p;
    }

    public List<PaymentMode> paymentModes(Context ctx) {
        return modes;
    }

    public PaymentMode get(int id, Context ctx) {
        for (PaymentMode mode : modes) {
            if (mode.getId() == id) {
                return mode;
            }
        }
        return null;
    }

    @Override
    protected String getFileName() {
        return PaymentModeData.FILENAME;
    }

    @Override
    protected List<Object> getObjectList() {
        List<Object> result = new ArrayList<>();
        result.add(modes);
        return result;
    }

    @Override
    protected List<Type> getClassList() {
        List<Type> result = new ArrayList<>();
        result.add(new TypeToken<List<PaymentMode>>(){}.getType());
        return result;
    }

    @Override
    protected int getNumberOfObjects() {
        return 1;
    }

    @Override
    protected void recoverObjects(List<Object> objs) {
        modes = (List<PaymentMode>) objs.get(0);
    }
}
