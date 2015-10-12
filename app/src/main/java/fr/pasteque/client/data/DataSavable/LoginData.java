package fr.pasteque.client.data.DataSavable;


import android.content.Context;
import fr.pasteque.client.Configure;
import fr.pasteque.client.models.Login;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nsvir on 14/08/15.
 * n.svirchevsky@gmail.com
 */
public class LoginData extends AbstractJsonDataSavable {

    public static final String FILENAME = "login.data";

    private Login login = null;

    public void setLogin(Login login) {
        this.login = login;
    }

    public Login getLogin(Context ctx) {
        if (this.login == null) {
            this.loadNoMatterWhat(ctx);
        }
        return this.login;
    }

    public boolean equalsConfiguredAccount(Context ctx) {
        return new Login(Configure.getUser(ctx),
                Configure.getPassword(ctx),
                Configure.getMachineName(ctx)).equals(this.login);
    }

    @Override
    protected String getFileName() {
        return LoginData.FILENAME;
    }

    @Override
    protected List<Object> getObjectList() {
        List<Object> result = new ArrayList<>();
        result.add(this.login);
        return result;
    }

    @Override
    protected List<Type> getClassList() {
        List<Type> result = new ArrayList<>();
        result.add(Login.class);
        return result;
    }

    @Override
    protected int getNumberOfObjects() {
        return 1;
    }

    @Override
    protected void recoverObjects(List<Object> objs) {
        this.login = (Login) objs.get(0);
    }
}
