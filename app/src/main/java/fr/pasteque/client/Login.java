package fr.pasteque.client;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import fr.pasteque.client.utils.*;
import fr.pasteque.client.utils.Error;

/**
 * Created by nsvir on 05/08/15.
 * n.svirchevsky@gmail.com
 */

/**
 * Login Screen.
 * This Activity set the user and password account with Configuration.setUser Configuration.setPassword
 * Directly Calls Start activity if Configuration.isAccount == true
 * Leave if receive Login.LEAVE from the StartActivity
 */
public class Login extends TrackedActivity {

    private static final int MENU_ABOUT_ID = 1;
    public static final int START = 2;
    public static final int LEAVE = 3;
    public static final int PROCEED = 4;
    public static final int ERROR_LOGIN = 5;
    private EditText mLogin;
    private EditText mPassword;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.login);
        findViewById(R.id.signin).setOnClickListener(new SignInClickListener());
        findViewById(R.id.signup).setOnClickListener(new SignUpClickListener());
        findViewById(R.id.demo).setOnClickListener(new DemoClickListener());
        mLogin = (EditText) findViewById(R.id.login);
        mPassword = (EditText) findViewById(R.id.password);
        mPassword.setOnEditorActionListener(new PasswordEditorAction());
        findViewById(R.id.show_password).setOnClickListener(new ShowPasswordClickListener());
        if (this.hasDefaultAccount()) {
            this.setDefaultAccount();
        }
        if (!Configure.isDemo(this)) {
            this.mLogin.setText(Configure.getUser(this));
            this.mPassword.setText(Configure.getPassword(this));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Configure.isDemo(this)) {
            startActivity(Start.class);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem about = menu.add(Menu.NONE, MENU_ABOUT_ID, 0,
                this.getString(R.string.menu_about));
        about.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case MENU_ABOUT_ID:
                About.showAbout(this);
                return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Login.START)
            switch (resultCode) {
                case Login.LEAVE:
                    finish();
                    break;
                case Login.ERROR_LOGIN:
                    this.invalidateAccount();
                    Error.showError(R.string.err_not_logged, this);
                    break;
            }
    }

    private void setDefaultAccount() {
        Configure.setUser(this, getString(R.string.default_user));
        Configure.setPassword(this, getString(R.string.default_password));
        Configure.setCashRegister(this, getString(R.string.default_cash));
    }

    private boolean hasDefaultAccount() {
        return getResources().getBoolean(R.bool.hasDefaultAccount) == true;
    }

    private String getLogin() {
        return mLogin.getText().toString();
    }

    private String getPassword() {
        return mPassword.getText().toString();
    }

    private void setAccount() {
        Configure.setUser(this, getLogin());
        Configure.setPassword(this, getPassword());
    }

    private void setDemo() {
        Configure.setDemo(this);
    }

    private void invalidateAccount() {
        Configure.invalidateAccount(this);
    }

    private void signIn() {
        this.setAccount();
        this.startActivity(Start.class);
    }

    private void startActivity(Class<?> tclass) {
        startActivityForResult(new Intent(this, tclass), Login.START);
    }

    private boolean checkInput() {
        return mPassword.getText().length() != 0 && mLogin.getText().length() != 0;
    }

    protected class DemoClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Login.this.setDemo();
            Login.this.startActivity(Start.class);
        }
    }

    protected class SignUpClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Uri uri = Uri.parse(Login.this.getString(R.string.app_create_account_url));
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }
    }

    protected class SignInClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (Login.this.checkInput()) {
                Login.this.signIn();
            } else {
                Toast.makeText(Login.this, getString(R.string.err_login_empty), Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected class ShowPasswordClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (((CheckBox) view).isChecked()) {
                mPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                mPassword.setSelection(mPassword.getText().length());
            } else {
                mPassword.setInputType(129);
                mPassword.setSelection(mPassword.getText().length());
            }
        }
    }

    private class PasswordEditorAction implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                Login.this.signIn();
                return true;
            }
            return false;
        }
    }
}
