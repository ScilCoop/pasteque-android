package fr.pasteque.client.models.document;

import fr.pasteque.client.models.Company;

import java.util.List;

/**
 * Created by svirch_n on 25/04/16
 * This class is made from the NF-525 convention
 * Last edited at 12:17.
 */
public class Ticket {

    public String id;
    public String documentId;
    public String softwareVersion;
    public int impressionNumber;
    public Company innerCompanyName;
    public String cashierCode;
    public String cashierName;
    public String operatorCode;
    public String operatorName;
    public String cashRegisterCode;
    public int clientNumber;
    //AAAAMMJJHHmmss
    public String date;
    public String operationType;
    public String documentType;
    public String ticketLineNumber;

    protected List<TicketLine> ticketLines;

    public List<Cashing> cashings;

    public int htSum;
    public int ttcSum;

    public Customer customer;

    public List<Payment> payments;
}
