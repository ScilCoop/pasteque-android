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
package fr.pasteque.client.utils;

import android.content.Context;
import fr.pasteque.client.Configure;

public class HostParser {

    public static String getHostFromPrefs(Context ctx) {
        String host = Configure.getHost(ctx);
        boolean ssl = Configure.getSsl(ctx);
        if (!host.startsWith("http://") || !host.startsWith("https://")) {
            if (ssl) {
                host = "https://" + host;
            } else {
                host = "http://" + host;
            }
        } else if (host.startsWith("http://") && ssl) {
            // Force https
            host = "https://" + host.substring(7);
        }
        if (!host.endsWith("/")) {
            host += "/";
        }
        return host;
    }
}
