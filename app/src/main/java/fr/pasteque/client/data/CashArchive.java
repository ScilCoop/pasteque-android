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
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.models.Cash;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.utils.exception.NoArchiveException;
import fr.pasteque.client.utils.exception.SaveArchiveException;
import fr.pasteque.client.utils.exception.loadArchiveException;
import fr.pasteque.client.utils.file.File;
import fr.pasteque.client.utils.file.InternalFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Stores finalized tickets
 */
public class CashArchive {

    private static final String ARCHIVESDIR = "archives";
    private static final String FILENAME = "tickets.data";
    private static String LOG_TAG = "Pasteque/CashArchive";

    /**
     * Create an unique stable id from cash without id.
     */
    private static String cashId(Cash c) {
        return c.getCashRegisterId() + "-" + c.getOpenDate()
                + "-" + c.getCloseDate();
    }

    protected static void saveArchive(Cash cash, List<Receipt> receipts) throws SaveArchiveException {
        File file = getFile(cash);
        JsonArray jsonArray = getJson(cash, receipts);
        try {
            file.write(jsonArray.toString());
        } catch (FileNotFoundException e) {
            throw new SaveArchiveException(e);
        }
    }

    private static JsonArray getJson(Cash cash, List<Receipt> receipts) {
        Gson gson = getGson();
        JsonParser parser = new JsonParser();
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(parser.parse(gson.toJson(cash)));
        jsonArray.add(parser.parse(gson.toJson(receipts)));
        return jsonArray;
    }

    private static Object[] getObjects(File file) throws FileNotFoundException {
        Gson gson = getGson();
        JsonParser parser = new JsonParser();
        Object[] result = new Object[2];
        JsonElement jsonElement = parser.parse(file.read());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        result[0] = gson.fromJson(jsonArray.get(0), Cash.class);
        result[1] = gson.fromJson(jsonArray.get(1), new TypeToken<List<Receipt>>() {}.getType());
        return result;
    }

    private static Gson getGson() {
        return new GsonBuilder().serializeNulls().create();
    }

    public static boolean archiveCurrent()
            throws IOException {
        Context ctx = Pasteque.getAppContext();
        try {
            saveArchive(Data.Cash.currentCash(ctx), Data.Receipt.getReceipts(ctx));
        } catch (SaveArchiveException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static File getFile(Cash cash) {
        return new InternalFile(ARCHIVESDIR, cashId(cash));
    }

    public static int getArchiveCount(Context ctx) {
        java.io.File dir = ctx.getDir(ARCHIVESDIR, Context.MODE_PRIVATE);
        return dir.list().length;
    }

    public static boolean deleteArchive(Context ctx, Cash c) {
        File archive = getFile(c);
        return archive.exists() && archive.delete();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static synchronized void updateArchive(Cash cash,
                                                  List<Receipt> receipts) throws IOException, SaveArchiveException {
        saveArchive(cash, receipts);
    }

    /**
     * Load archive. Cash is array[0], List<Receipt> array[1].
     */
    public static Object[] loadAnArchive() throws loadArchiveException {
        try {
            File file = new InternalFile(ARCHIVESDIR, getAFileArchive());
            return getObjects(file);
        } catch (NoArchiveException | FileNotFoundException e) {
            throw new loadArchiveException();
        }
    }

    public static boolean hasArchives(Context ctx) {
        return getArchiveCount(ctx) > 0;
    }

    /* Kaboom */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void clear(Context ctx) {
        java.io.File dir = Pasteque.getAppContext().getDir(ARCHIVESDIR, Context.MODE_PRIVATE);
        String[] list = dir.list();
        for (String fname : list) {
            new InternalFile(ARCHIVESDIR, fname).delete();
        }
    }

    public static String getAFileArchive() throws NoArchiveException {
        String[] file = Pasteque.getAppContext().getDir(ARCHIVESDIR, Context.MODE_PRIVATE).list();
        if (file.length > 0) {
            return file[0];
        } else {
            throw new NoArchiveException();
        }
    }
}
