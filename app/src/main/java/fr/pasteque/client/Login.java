package fr.pasteque.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import fr.pasteque.client.utils.TrackedActivity;

/**
 * Created by nsvir on 05/08/15.
 * n.svirchevsky@gmail.com
 */
public class Login extends TrackedActivity {

    public static final String EXTRA_LOGIN = "Login/login";
    public static final String EXTRA_PASSWORD = "Login/password";

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
    }

    private String getLogin() {
        return mLogin.getText().toString();
    }

    private String getPassword() {
        return mPassword.getText().toString();
    }

    protected void startActivity(Class<?> tClass) {
        Intent intent = new Intent(this, tClass);
        intent.putExtra(EXTRA_LOGIN, getLogin());
        intent.putExtra(EXTRA_PASSWORD, getPassword());
        startActivity(intent);
    }

    protected class DemoClickListener implements  View.OnClickListener {

        @Override
        public void onClick(View view) {

        }
    }

    protected class SignUpClickListener implements  View.OnClickListener {

        @Override
        public void onClick(View view) {

        }
    }

    protected class SignInClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            Login.this.startActivity(Start.class);
        }
    }
}
