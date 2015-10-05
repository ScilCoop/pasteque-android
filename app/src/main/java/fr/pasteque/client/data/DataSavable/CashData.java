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

import fr.pasteque.client.data.Data;
import fr.pasteque.client.models.Cash;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class CashData extends AbstractObjectDataSavable {

    private static final String FILENAME = "cash.data";

    private Cash currentCash;
    public Boolean dirty = new Boolean(false);

    public Cash currentCash(Context ctx) {
        if (this.currentCash == null) {
            this.loadNoMatterWhat(ctx);
        }
        return currentCash;
    }

    public void setCash(Cash c) {
        currentCash = c;
    }

    @Override
    protected String getFileName() {
        return Data.Cash.FILENAME;
    }

    @Override
    protected List<Object> getObjectList() {
        List<Object> result = new ArrayList();
        result.add(currentCash);
        result.add(dirty);
        return result;
    }

    @Override
    protected int getNumberOfObjects() {
        return 2;
    }

    @Override
    protected void recoverObjects(List<Object> objs) {
        currentCash = (Cash) objs.get(0);
        dirty = (Boolean) objs.get(1);
    }

    /** Delete current cash */
    public void clear(Context ctx) {
        currentCash = null;
        dirty = false;
        ctx.deleteFile(FILENAME);
    }

    /** Merge a new cash to the current (if equals).
     * Updates open and close date to the most recent one.
     */
    public boolean mergeCurrent(Cash c) {
        if (c.equals(currentCash)) {
            if (c.getOpenDate() != currentCash.getOpenDate()
                || c.getCloseDate() != currentCash.getCloseDate()) {
                dirty = true;
            }
            long open = Math.max(c.getOpenDate(), currentCash.getOpenDate());
            long close = Math.max(c.getCloseDate(), currentCash.getCloseDate());
            currentCash = new Cash(currentCash.getId(),
                    currentCash.getCashRegisterId(), currentCash.getSequence(),
                    open, close);
            return true;
        } else {
            if (currentCash.getId() == null
                    && currentCash.getCashRegisterId() == c.getCashRegisterId()) {
                // Merge with incoming data
                long open = Math.max(c.getOpenDate(),
                        currentCash.getOpenDate());
                long close = Math.max(c.getCloseDate(),
                        currentCash.getCloseDate());
                currentCash = new Cash(c.getId(),
                        currentCash.getCashRegisterId(), c.getSequence(),
                        open, close);
                currentCash = c;
                return true;
            }
            return false;
        }
    }
}
