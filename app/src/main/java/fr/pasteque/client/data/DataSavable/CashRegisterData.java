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

import fr.pasteque.client.models.CashRegister;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class CashRegisterData extends AbstractDataSavable {

    private static final String FILENAME = "cashreg.data";

    private CashRegister cashRegister;

    public CashRegister current(Context ctx) {
        if (cashRegister == null) {
            this.loadNoMatterWhat(ctx);
        }
        return cashRegister;
    }

    public void set(CashRegister c) {
        cashRegister = c;
    }

    /** Delete cashRegister cash */
    public void clear(Context ctx) {
        cashRegister = null;
        ctx.deleteFile(FILENAME);
    }

    @Override
    protected String getFileName() {
        return CashRegisterData.FILENAME;
    }

    @Override
    protected List<Object> getObjectList() {
        List<Object> result = new ArrayList<>();
        result.add(cashRegister);
        return result;
    }

    @Override
    protected int getNumberOfObjects() {
        return 1;
    }

    @Override
    protected void recoverObjects(List<Object> objs) {
        this.cashRegister = (CashRegister) objs.get(0);
    }
}
