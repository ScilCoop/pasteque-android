package fr.pasteque.client.models;

import java.io.Serializable;

/**
 * Created by svirch_n on 25/04/16
 * Last edited at 15:39.
 */
public class Address implements Serializable{

    public String address = "";
    public String postCode = "";
    public String city = "";
    public String country = "";

    public Address() {

    }

    public Address(String address, String postCode, String city, String country) {
        this.address = address;
        this.postCode = postCode;
        this.country = country;
        this.city = city;
    }
}
