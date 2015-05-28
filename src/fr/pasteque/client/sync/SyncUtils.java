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
package fr.pasteque.client.sync;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import java.util.HashMap;
import java.util.Map;

import fr.pasteque.client.Configure;
import fr.pasteque.client.utils.HostParser;

public class SyncUtils {

    public static String apiUrl(Context ctx) {
        return HostParser.getHostFromPrefs(ctx) + "api.php";
    }

    public static Map<String, String> initParams(Context ctx, String service,
            String action) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("login", Configure.getUser(ctx));
        params.put("password", Configure.getPassword(ctx));
        params.put("p", service);
        if (action != null) {
            params.put("action", action);
        }
        return params;
    }

    public static void notifyListener(Handler listener, int what, Object obj) {
        if (listener != null) {
            Message m = listener.obtainMessage();
            m.what = what;
            m.obj = obj;
            m.sendToTarget();
        }
    }
    public static void notifyListener(Handler listener, int what) {
        notifyListener(listener, what, null);
    }
}