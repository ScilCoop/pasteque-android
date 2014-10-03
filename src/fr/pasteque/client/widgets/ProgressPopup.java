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
package fr.pasteque.client.widgets;

import android.app.ProgressDialog;
import android.content.Context;

/** Popup with progress bar */
public class ProgressPopup extends ProgressDialog {

    private int total;

    public ProgressPopup(Context ctx) {
        super(ctx);
        this.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        this.setCancelable(false);
    }

    /** Increment progress and automatically dismiss the dialo
     * when finished.
     * @return True if popup is still alive, false if closed or should be.
     */
    public boolean increment(boolean kill) {
        this.incrementProgressBy(1);
        if (this.getProgress() == this.getMax()) {
            if (kill) {
                this.dismiss();
            }
            return false;
        }
        return true;
    }
}