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

import fr.pasteque.client.models.Session;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class SessionData extends AbstractDataSavable {

    private static final String LOG_TAG = "SessionData";
    private static final String FILENAME = "session.data";

    private Session currentSession;

    public Session currentSession(Context ctx) {
        if (currentSession == null) {
            this.loadNoMatterWhat(ctx);
        }
        return currentSession;
    }

    public void newSessionIfEmpty() {
        if (currentSession == null) {
            currentSession = new Session();
        }
    }

    public void clear(Context ctx) {
        currentSession = new Session();
        ctx.deleteFile(FILENAME);
    }

    @Override
    protected String getFileName() {
        return SessionData.FILENAME;
    }

    @Override
    protected List<Object> getObjectList() {
        List<Object> result = new ArrayList<>();
        result.add(currentSession);
        return result;
    }

    @Override
    protected int getNumberOfObjects() {
        return 1;
    }

    @Override
    protected void recoverObjects(List<Object> objs) {
        currentSession = (Session) objs.get(0);
    }
}
