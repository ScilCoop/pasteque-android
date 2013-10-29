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
package fr.pasteque.client.data;

import fr.pasteque.client.models.Cash;

import android.content.Context;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class CashData {

    private static final String FILENAME = "cash.data";

    private static Cash currentCash;
    public static boolean dirty;

    public static Cash currentCash(Context ctx) {
        if (currentCash == null) {
           try {
               load(ctx);
           } catch (IOException e) {
               e.printStackTrace();
           }
        }
        return currentCash;
    }

    public static void setCash(Cash c) {
        currentCash = c;
    }

    public static boolean save(Context ctx)
        throws IOException {
        FileOutputStream fos = ctx.openFileOutput(FILENAME, ctx.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(currentCash);
        oos.writeBoolean(dirty);
        oos.close();
        return true;
    }

    public static boolean load(Context ctx)
        throws IOException {
        FileInputStream fis = ctx.openFileInput(FILENAME);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Cash c = null;
        try {
            c = (Cash) ois.readObject();
            dirty = ois.readBoolean();
            currentCash = c;
            ois.close();
            return true;
        } catch (ClassNotFoundException cnfe) {
            // Should never happen
        }
        ois.close();
        return false;
    }

    /** Delete current cash */
    public static void clear(Context ctx) {
        currentCash = null;
        dirty = false;
        ctx.deleteFile(FILENAME);
    }

    /** Merge a new cash to the current (if equals).
     * Updates open and close date to the most recent one.
     */
    public static boolean mergeCurrent(Cash c) {
        if (c.equals(currentCash)) {
            if (c.getOpenDate() != currentCash.getOpenDate()
                || c.getCloseDate() != currentCash.getCloseDate()) {
                dirty = true;
            }
            long open = Math.max(c.getOpenDate(), currentCash.getOpenDate());
            long close = Math.max(c.getCloseDate(), currentCash.getCloseDate());
            currentCash = new Cash(currentCash.getId(),
                                   currentCash.getMachineName(),
                                   open, close);
            return true;
        } else {
            if (currentCash.getId() == null
                    && currentCash.getMachineName().equals(c.getMachineName())) {
                // Merge with incoming data
                long open = Math.max(c.getOpenDate(),
                        currentCash.getOpenDate());
                long close = Math.max(c.getCloseDate(),
                        currentCash.getCloseDate());
                currentCash = new Cash(c.getId(),
                                   currentCash.getMachineName(),
                                   open, close);
                currentCash = c;
                return true;
            }
            return false;
        }
    }
}
