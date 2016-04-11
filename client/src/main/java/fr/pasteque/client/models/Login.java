package fr.pasteque.client.models;

import java.io.Serializable;

/**
 * Created by nsvir on 14/08/15.
 * n.svirchevsky@gmail.com
 */
public class Login implements Serializable {

    private String username;
    private String password;
    private String cashRegister;

    public Login() {
    }

    public Login(String username, String password, String cashRegister) {
        this.username = username;
        this.password = password;
        this.cashRegister = cashRegister;
    }

    public void setAccount(String username, String password, String cashRegister) {
        this.username = username;
        this.password = password;
        this.cashRegister = cashRegister;
    }

    @Override
    public boolean equals(Object o) {
        if (this.username == null
                || this.password == null
                || this.cashRegister == null)
            return false;
        if (o != null && o instanceof Login) {
            Login obj = (Login) o;
            return this.username.equals(obj.username)
                    && this.password.equals(obj.password)
                    && this.cashRegister.equals(obj.cashRegister);
        }
        return false;
    }
}
