package fr.pasteque.client.models.document;

/**
 * Created by svirch_n on 25/04/16
 * Last edited at 12:29.
 */
public class TicketLine {

    public String id;
    public String documentId;
    public int lineNumber;
    //public String dataSource;
    public String productName;
    public Quantity quantity;
    public String tvaCode;
    public int tvaRate;
    public int htValue;
    public int ttcValue;
    public String discountCode;
    public int discountRate;
    public int discountValue;
    public int discountSum;
    public int htSum;
    public int ttcSum;
    public String operationType;
    //public String userCode;
    public String date;
    //public String profitCenter;
    public String cashRegisterCode;
    public String cashierCode;
    public String operatorCode;
}
