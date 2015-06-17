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
package fr.pasteque.client;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class About {

    public static void showAbout(Context ctx) {
        AlertDialog.Builder b = new AlertDialog.Builder(ctx);
        View content = LayoutInflater.from(ctx).inflate(R.layout.about,
                                                        null,
                                                        false);
        b.setView(content);
        b.setTitle(ctx.getString(R.string.about_title));
        b.setIcon(android.R.drawable.ic_dialog_info);

        TextView version = (TextView) content.findViewById(R.id.about_version);
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo info = pm.getPackageInfo(ctx.getPackageName(), 0);
            String v = info.versionName;
            String name = ctx.getString(R.string.app_name);
            String codeName = ctx.getString(R.string.app_codename);
            version.setText(name + " - " + codeName + " " + v);
        } catch( PackageManager.NameNotFoundException nnfe ) {
            // unreachable
        }
        
        b.show();
    }
}
