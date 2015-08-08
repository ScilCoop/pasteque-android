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

import android.content.Context;
import fr.pasteque.client.models.Cash;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.utils.PastequeAssert;

import java.io.*;
import java.util.List;

/** Stores finalized tickets */
public class CashArchive {

    private static final String ARCHIVESDIR = "archives";
    private static final String FILENAME = "tickets.data";

    /** Create an unique stable id from cash without id. */
    private static String cashId(Cash c) {
        return c.getCashRegisterId() + "-" + c.getOpenDate()
                + "-" + c.getCloseDate();
    }

    public static boolean archiveCurrent(Context ctx)
        throws IOException {
        Cash cash = CashData.currentCash(ctx);
        List<Receipt> receipts = ReceiptData.getReceipts(ctx);
        File dir = ctx.getDir(ARCHIVESDIR, Context.MODE_PRIVATE);
        File archive = new File(dir, cashId(cash));
        archive.createNewFile();
        FileOutputStream fos = new FileOutputStream(archive);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(cash);
        oos.writeObject(receipts);
        oos.close();
        return true;
    }

    public static int getArchiveCount(Context ctx) {
        File dir = ctx.getDir(ARCHIVESDIR, Context.MODE_PRIVATE);
        return dir.list().length;
    }

    public static boolean deleteArchive(Context ctx, Cash c) {
        File dir = ctx.getDir(ARCHIVESDIR, Context.MODE_PRIVATE);
        File archive = new File(dir, cashId(c));
        return archive.exists() && archive.delete();
    }

    public static synchronized void updateArchive(Context ctx, Cash cash,
            List<Receipt> receipts) throws IOException {
        File dir = ctx.getDir(ARCHIVESDIR, Context.MODE_PRIVATE);
        File archive = new File(dir, cashId(cash));
        archive.delete();
        archive.createNewFile();
        FileOutputStream fos = new FileOutputStream(archive);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(cash);
        oos.writeObject(receipts);
        oos.close();
    }

    /** Load archive. Cash is array[0], List<Receipt> array[1]. */
    public static Object[] loadArchive(Context ctx)
        throws IOException {
        File dir = ctx.getDir(ARCHIVESDIR, Context.MODE_PRIVATE);
        String[] names = dir.list();
        Cash c = null;
        List<Receipt> receipts = null;
        if (names.length > 0) {
            File archive = new File(dir, names[0]);
            FileInputStream fis = new FileInputStream(archive);
            ObjectInputStream ois = new ObjectInputStream(fis);
            try {
                c = (Cash) ois.readObject();
                //noinspection unchecked ClassNotFound should occure in this case
                receipts = (List<Receipt>) ois.readObject();
            } catch (ClassNotFoundException cnfe) {
                PastequeAssert.runtimeException();
            }
            ois.close();
        }
        return new Object[]{c, receipts};
    }

    /* Kaboom */
    public static void clear(Context ctx) throws IOException {
        File dir = ctx.getDir(ARCHIVESDIR, Context.MODE_PRIVATE);
        String[] list = dir.list();
        for (String fname : list) {
            File f = new File(dir, fname);
            f.delete();
        }
    }
}
