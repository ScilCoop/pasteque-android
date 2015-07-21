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

import android.view.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/** Handle key events to read barcode. */
public class BarcodeInput {

    private List<Integer> string;
    private boolean terminated;

    public BarcodeInput() {
        this.string = new ArrayList<Integer>();
    }

    /** Process a key event. If input is terminated, a new one is stared,
     * discarding the latest.
     * @return True if event is recognized, false otherwise
     */
    public boolean append(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_0:
        case KeyEvent.KEYCODE_1:
        case KeyEvent.KEYCODE_2:
        case KeyEvent.KEYCODE_3:
        case KeyEvent.KEYCODE_4:
        case KeyEvent.KEYCODE_5:
        case KeyEvent.KEYCODE_6:
        case KeyEvent.KEYCODE_7:
        case KeyEvent.KEYCODE_8:
        case KeyEvent.KEYCODE_9:
            if (this.isTerminated()) {
                this.terminated = false;
                this.string.clear();
            }
            this.string.add(keyCode);
            return true;
        case KeyEvent.KEYCODE_ENTER:
            this.terminated = true;
            return true;
        default:
            return false;
        }
    }

    /** Check if the last event terminated reading.
     * @return True if input is terminated and can be read.
     */
    public boolean isTerminated() {
        return this.terminated;
    }

    /** Get the input as string. Input must be terminated.
     * @return The input string. Null if not terminated.
     */
    public String getInput() {
        if (!this.isTerminated()) {
            return null;
        }
        char[] out = new char[this.string.size()];
        for (int i = 0; i < this.string.size(); i++) {
            int key = this.string.get(i);
            switch (key) {
            case KeyEvent.KEYCODE_0:
                out[i] = '0'; break;
            case KeyEvent.KEYCODE_1:
                out[i] = '1'; break;
            case KeyEvent.KEYCODE_2:
                out[i] = '2'; break;
            case KeyEvent.KEYCODE_3:
                out[i] = '3'; break;
            case KeyEvent.KEYCODE_4:
                out[i] = '4'; break;
            case KeyEvent.KEYCODE_5:
                out[i] = '5'; break;
            case KeyEvent.KEYCODE_6:
                out[i] = '6'; break;
            case KeyEvent.KEYCODE_7:
                out[i] = '7'; break;
            case KeyEvent.KEYCODE_8:
                out[i] = '8'; break;
            case KeyEvent.KEYCODE_9:
                out[i] = '9'; break;
            }
        }
        return new String(out);
    }
}
