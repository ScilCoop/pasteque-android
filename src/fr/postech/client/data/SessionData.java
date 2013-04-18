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
package fr.postech.client.data;

import fr.postech.client.models.Session;

import android.content.Context;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SessionData {

    private static final String FILENAME = "session.data";

    private static Session currentSession;

    public static Session currentSession(Context ctx) {
        if (currentSession == null) {
            try {
                loadSession(ctx);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return currentSession;
    }

    public static void newSessionIfEmpty() {
        if (currentSession == null) {
            currentSession = new Session();
        }
    }

    public static boolean saveSession(Context ctx)
        throws IOException {
        FileOutputStream fos = ctx.openFileOutput(FILENAME, ctx.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(currentSession);
        oos.close();
        return true;
    }

    public static void loadSession(Context ctx)
        throws IOException {
        FileInputStream fis = ctx.openFileInput(FILENAME);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Session s = null;
        try {
            currentSession = (Session) ois.readObject();
        } catch (ClassNotFoundException cnfe) {
            // Should never happen
        }
        ois.close();
    }

    public static void clear(Context ctx) {
        currentSession = new Session();
        ctx.deleteFile(FILENAME);
    }
}
