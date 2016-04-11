package fr.pasteque.api;


/**
 * Created by svirch_n on 07/04/16
 * Last edited at 18:06.
 */
public class Tickets extends SubAPIHelper {

    protected Tickets(API api) {
        super(api);
    }

    public String getAllSharedTicket() {
        return getUrl("getAllShared");
    }

    @Override
    public String getApiName() {
        return "TicketsAPI";
    }
}
