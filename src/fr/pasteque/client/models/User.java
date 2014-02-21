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
package fr.pasteque.client.models;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

public class User implements Serializable {

    private String id;
    private String name;
    private String password;
    private String permissions;

    public User(String id, String name, String password, String permissions) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.permissions = permissions;
    }

    public String getName() {
        return this.name;
    }

    public void setName() {
        this.name = name;
    }

    public String getId() {
        return this.id;
    }

    public String getPassword() {
        return this.password;
    }

    public boolean hasPassword() {
        return (this.password != null && !this.password.equals("")
                && !this.password.startsWith("empty:"));
    }

    public boolean hasPermission(String permission) {
        return this.permissions.indexOf(permission) != -1;
    }

    public static User fromJSON(JSONObject o, String permissions)
        throws JSONException {
        String id = o.getString("id");
        String name = o.getString("name");
        String password = null;
        if (!o.isNull("password")) {
            password = o.getString("password");
        }
        return new User(id, name, password, permissions);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", this.id);
        o.put("name", this.name);
        return o;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
