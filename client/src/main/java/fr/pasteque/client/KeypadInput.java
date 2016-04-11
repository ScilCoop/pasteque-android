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

import fr.pasteque.client.activities.TrackedActivity;
import fr.pasteque.client.widgets.NumKeyboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;

public class KeypadInput extends TrackedActivity implements Handler.Callback {

    private static final String LOG_TAG = "Pasteque/KeypadInput";

    public static final int ADD = 1;
    public static final int BARCODE = 2;
    
    private NumKeyboard keyboard;
    private EditText input;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.keypad_input);
        this.keyboard = (NumKeyboard) this.findViewById(R.id.numkeyboard);
        this.keyboard.setKeyHandler(new Handler(this));
        this.input = (EditText) this.findViewById(R.id.input);
    }

    private void refreshInput() {
        this.input.setText(this.keyboard.getRawValue());
    }

    public void resetInput() {
        this.keyboard.clear();
        this.refreshInput();
    }

    public void correct(View v) {
        this.keyboard.correct();
        this.refreshInput();
    }

    public void clear(View v) {
        this.resetInput();
    }

    public void plus(View v) {
        Intent i = new Intent();
        i.putExtra("action", ADD);
        i.putExtra("input", Double.parseDouble(this.input.getText().toString()));
        this.setResult(Activity.RESULT_OK, i);
        this.finish();
    }
    public void minus(View v) {
        Intent i = new Intent();
        i.putExtra("action", ADD);
        i.putExtra("input", Double.parseDouble(this.input.getText().toString()) * -1);
        this.setResult(Activity.RESULT_OK, i);
        this.finish();
    }
    public void barcode(View v) {
        Intent i = new Intent();
        i.putExtra("action", BARCODE);
        i.putExtra("input", this.input.getText().toString());             
        this.setResult(Activity.RESULT_OK, i);
        this.finish();
    }

    @Override
	public boolean handleMessage(Message m) {
        switch (m.what) {
        case NumKeyboard.KEY_ENTER:
            
            break;
        default:
            this.refreshInput();
            break;
        }
        return true;
    }

}
