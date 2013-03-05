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
package fr.postech.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import fr.postech.client.data.CashData;
import fr.postech.client.data.CatalogData;
import fr.postech.client.data.DataLoader;
import fr.postech.client.data.ReceiptData;
import fr.postech.client.data.SessionData;
import fr.postech.client.data.UserData;
import fr.postech.client.models.Cash;
import fr.postech.client.models.Catalog;
import fr.postech.client.models.User;
import fr.postech.client.models.Session;
import fr.postech.client.models.Ticket;
import fr.postech.client.utils.Hashcypher;
import fr.postech.client.utils.StringUtils;

public class Password extends Activity {


    private static final String LOG_TAG = "POS-Tech/Password";
    private static final int SCROLL_WHAT = 90; // Be sure not to conflict with keyboard whats

    /** Static variables for password detection.*/
    private static final int HAS_PASSWORD = 0;
    private static final int HAS_WRONG_PASSWORD = 1;

    private Button userButton;
    private Button clearButton;
    private Button validateButton;
    private CheckBox hidePass;
    private EditText passEdit;
    private int people;
    private Hashcypher hash;
    private String dbPass;
    private User user;

    /** Called when the activity is first created.*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password);
        hash = new Hashcypher();
        Intent i = getIntent();
        user = (User) i.getSerializableExtra("User");
        dbPass = user.getPassword();
        this.userButton = (Button) this.findViewById(R.id.password_UserButton);
        this.userButton.setText(user.getName());
        this.passEdit = (EditText) this.findViewById(R.id.password_PassEdit);
        this.hidePass = (CheckBox) this.findViewById(R.id.password_HidePass);
        hidePass.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                hidePassOnCheckedChanged(buttonView, isChecked);
            }
        });
        this.validateButton = (Button) this.findViewById(R.id.password_ValidateButton);
        validateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                nextActivity();
            }
        });
        this.clearButton = (Button) this.findViewById(R.id.password_ClearButton);
        clearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                /** Erase all numbers in the password field.*/
                clear();
            }
        });
    }

    public void clear() {
        this.passEdit.setText("");
    }

    public void hidePassOnCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            /** Show password.*/
            passEdit.setTransformationMethod(null);
            passEdit.setSelection(passEdit.getText().length());
        } else {
            /** Hide password.*/
            passEdit.setTransformationMethod(new PasswordTransformationMethod());
            passEdit.setSelection(passEdit.getText().length());
        }
    }

    /** Goes to the next activity if user has entered good password.*/
    public void nextActivity() {
        if (hash.authenticate(passEdit.getText().toString(), dbPass) == true) {
            people = HAS_PASSWORD;
        } else {
            people= HAS_WRONG_PASSWORD;
        }
        if (people == HAS_PASSWORD) {
            /** Goes to next activity if password is good.*/
            goToTicket();
        } else if (people == HAS_WRONG_PASSWORD) {
            /** Stays here if password is wrong.*/
            Toast.makeText(getApplicationContext(), R.string.password_WrongPass,
                            Toast.LENGTH_SHORT).show();
            clear();
        }
    }

    /** Loads config for the next activity. */
    public void goToTicket () {
        SessionData.currentSession.setUser(user);
        if (SessionData.currentSession.getCurrentTicket() == null) {
            // Create a ticket if there isn't anyone
            Ticket t = SessionData.currentSession.newTicket();
        }
        Cash c = CashData.currentCash;
        if (c != null && !c.isOpened()) {
            // Cash is not opened
            Intent i = new Intent(Password.this, OpenCash.class);
            Password.this.startActivity(i);
            this.finish();
        } else if (c != null && c.isOpened() && !c.isClosed()) {
            // Cash is opened
            TicketInput.setup(CatalogData.catalog,
                              SessionData.currentSession.getCurrentTicket());
            Intent i = new Intent(Password.this, TicketInput.class);
            Password.this.startActivity(i);
            this.finish();
        } else if (c != null && c.isClosed()) {
            // Cash is closed
            Intent i = new Intent(Password.this, OpenCash.class);
            Password.this.startActivity(i);
            this.finish();
        } else {
            // Where is it?
            Log.e(LOG_TAG, "No cash while openning session. Cash is "
                  + c);
            Error.showError(R.string.err_no_cash, Password.this);
        }
    }
}
