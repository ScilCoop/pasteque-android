package fr.pasteque.api;


import fr.pasteque.api.connection.Connection;
import fr.pasteque.api.gatherer.Gatherer;
import fr.pasteque.api.gatherer.StringGatherer;

import java.io.IOException;

/**
 * Created by svirch_n on 07/04/16
 * Last edited at 18:06.
 */
public class Tickets extends SubAPIHelper {

    Tickets(API api) {
        super(api);
    }

    public void getAllSharedTicket(StringGatherer<?> gatherer) {
        try {
            new Connection(getUrl("getAllShared")).request(gatherer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getApiName() {
        return "TicketsAPI";
    }
}
