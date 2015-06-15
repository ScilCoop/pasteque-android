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

public class Customer implements Serializable {

    private String id;
    private String name;
    private String card;
    private String firstName;
    private String lastName;
    private String address1;
    private String address2;
    private String zipCode;
    private String city;
    private String department;
    private String country;
    private String mail;
    private String phone1;
    private String phone2;
    private String fax;
    private double prepaid;
    private double maxDebt;
    private double currDebt;
    private String tariffAreaId;

    public Customer(String id, String name, String card, String firstName, String lastName, String address1,
                    String address2, String zipCode, String city, String department, String country,
                    String mail, String phone1, String phone2, String fax,
                    double prepaid, double maxDebt, double currDebt, String area) {
        this.id = id;
        this.name = name;
        this.card = card;
        this.prepaid = prepaid;
        this.maxDebt = maxDebt;
        this.currDebt = currDebt;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address1 = address1;
        this.address2 = address2;
        this.zipCode = zipCode;
        this.city = city;
        this.department = department;
        this.country = country;
        this.mail = mail;
        this.phone1 = phone1;
        this.phone2 = phone2;
        this.fax = fax;
        this.tariffAreaId = area;
    }

    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getCard() {
        return this.card;
    }

    public double getCurrDebt() {
        return this.currDebt;
    }

    public double getMaxDebt() {
        return this.maxDebt;
    }

    public void addDebt(double amount) {
        this.currDebt += amount;
    }

    public double getPrepaid() {
        return this.prepaid;
    }
    public void setPrepaid(double value) {
        this.prepaid = value;
    }
    public void addPrepaid(double amount) {
        this.prepaid += amount;
    }

    public String getTariffAreaId() {
        return this.tariffAreaId;
    }

    public static Customer fromJSON(JSONObject o) throws JSONException {
        String id = o.getString("id");
        String name = o.getString("dispName");
        String card = o.getString("card");
        String firstName = o.getString("firstName");
        String lastName = o.getString("lastName");
        String address1 = o.getString("addr1");
        String address2 = o.getString("addr2");
        String zipCode = o.getString("zipCode");
        String city = o.getString("city");
        String department = o.getString("region");
        String country = o.getString("country");
        String mail = o.getString("email");
        String phone1 = o.getString("phone1");
        String phone2 = o.getString("phone2");
        String fax = o.getString("fax");
        double maxDebt = 0.0;
        if (!o.isNull("maxDebt")) {
            maxDebt = o.getDouble("maxDebt");
        }
        double currDebt = 0.0;
        if (!o.isNull("currDebt")) {
            currDebt = o.getDouble("currDebt");
        }
        double prepaid = o.getDouble("prepaid");
        String tariffAreaId = o.getString("tariffAreaId");
        return new Customer(id, name, card, firstName,lastName, address1,address2, zipCode, city, department,
                country,mail, phone1, phone2, fax, prepaid, maxDebt, currDebt, tariffAreaId);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("dispName", this.name);
        o.put("card", this.card);
        o.put("firstName", this.firstName);
        o.put("lastName", this.lastName);
        o.put("addr1", this.address1);
        o.put("addr2", this.address2);
        o.put("zipCode", this.zipCode);
        o.put("city", this.city);
        o.put("region", this.department);
        o.put("country", this.country);
        o.put("email", this.mail);
        o.put("phone1", this.phone1);
        o.put("phone2", this.phone2);
        o.put("fax", this.fax);
        o.put("prepaid", this.prepaid);
        o.put("maxDebt", this.maxDebt);
        o.put("currDebt", this.currDebt);
        return o;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Customer) && this.id.equals(((Customer)o).id);
    }
}
