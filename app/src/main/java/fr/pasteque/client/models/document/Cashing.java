package fr.pasteque.client.models.document;

/**
 * Created by svirch_n on 25/04/16
 * This class is made from the NF-525 convention
 * A cashing is a tickeline sum of TVA
 * Last edited at 15:03.
 */
public class Cashing {

    public String id;
    public String documentId;
    public int htSum;
    public String tvaCode;
    public int tvaRate;
    public int tvaSum;
    public int discountSum;
    public int ttcSum;

}
