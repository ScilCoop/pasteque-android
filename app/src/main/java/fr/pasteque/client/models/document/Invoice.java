package fr.pasteque.client.models.document;

import fr.pasteque.client.models.Company;

import java.io.Serializable;

/**
 * Created by svirch_n on 25/04/16
 * This class is made from the NF-525 convention
 * Last edited at 15:38.
 */
public class Invoice extends Ticket implements Serializable {

    public String id;
    public String contactName;
    public Company company;
}
