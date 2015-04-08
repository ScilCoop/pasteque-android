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

import fr.pasteque.client.utils.TrackedActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class Error {

    public static void showError(int message, TrackedActivity ctx) {
        if (ctx.isFront()) {
            AlertDialog.Builder b = new AlertDialog.Builder(ctx);
            b.setTitle(R.string.error_title);
            b.setMessage(message);
            b.setIcon(android.R.drawable.ic_dialog_alert);
            b.setCancelable(true);
            b.setNegativeButton(android.R.string.ok, new DismissListener());
            b.show();
        } else {
            ctx.setPendingError(message);
        }
    }

    public static void showError(String message, TrackedActivity ctx) {
        if (ctx.isFront()) {
            AlertDialog.Builder b = new AlertDialog.Builder(ctx);
            b.setTitle(R.string.error_title);
            b.setMessage(message);
            b.setIcon(android.R.drawable.ic_dialog_alert);
            b.setCancelable(true);
            b.setNegativeButton(android.R.string.ok, new DismissListener());
            b.show();
        } else {
            ctx.setPendingError(message);
        }
    }

    private static class DismissListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    }
}
