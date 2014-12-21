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

import fr.pasteque.client.models.User;

import android.content.Context;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ResourceData {

    private static final String FILEPREFIX = "res.";

    /** Save a string resource */
    public static boolean save(Context ctx, String resName, String data)
        throws IOException {
        FileOutputStream fos = ctx.openFileOutput(FILEPREFIX + resName,
                ctx.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(data);
        oos.close();
        return true;
    }

    /** Load a stored resource as String. Returns null if not saved */
    public static String loadString(Context ctx, String resName)
        throws IOException {
        String data = null;
        FileInputStream fis = ctx.openFileInput(FILEPREFIX + resName);
        ObjectInputStream ois = new ObjectInputStream(fis);
        try {
            data = (String) ois.readObject();
        } catch (ClassNotFoundException cnfe) {
            // Should never happen
        }
        ois.close();
        return data;
    }

    public static boolean delete(Context ctx, String resName) {
        return ctx.deleteFile(FILEPREFIX + resName);
    }
}
