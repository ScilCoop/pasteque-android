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


import fr.pasteque.client.data.CashData;
import fr.pasteque.client.data.CatalogData;
import fr.pasteque.client.data.DataLoader;
import fr.pasteque.client.data.ReceiptData;
import fr.pasteque.client.data.SessionData;
import fr.pasteque.client.data.UserData;
import fr.pasteque.client.models.Cash;
import fr.pasteque.client.models.Catalog;
import fr.pasteque.client.models.User;
import fr.pasteque.client.models.Session;
import fr.pasteque.client.models.Ticket;
import fr.pasteque.client.utils.Hashcypher;
import fr.pasteque.client.utils.StringUtils;
import fr.pasteque.client.utils.TrackedActivity;

public class Password extends TrackedActivity {

    public static final int CODE_PASSWORD = 402;

    private static final String LOG_TAG = "Pasteque/Password";
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
            // Password OK, send back the user to calling activity
            Intent i = new Intent();
            i.putExtra("User", user);
            this.setResult(Activity.RESULT_OK, i);
            this.finish();
        } else if (people == HAS_WRONG_PASSWORD) {
            /** Stays here if password is wrong.*/
            Toast.makeText(getApplicationContext(), R.string.password_WrongPass,
                            Toast.LENGTH_SHORT).show();
            clear();
        }
    }

}
