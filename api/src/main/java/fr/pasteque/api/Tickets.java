package fr.pasteque.api;


import fr.pasteque.api.connection.Connection;
import fr.pasteque.api.gatherer.Gatherer;
import fr.pasteque.api.gatherer.HandlerException;
import fr.pasteque.api.gatherer.smart.JsonArraySmartGatherer;
import fr.pasteque.api.models.TicketModel;
import fr.pasteque.api.parser.SharedTicketParser;
import org.json.JSONArray;

import java.util.List;

/**
 * Created by svirch_n on 07/04/16
 * Last edited at 18:06.
 */
public class Tickets extends SubAPIHelper {

    Tickets(API api) {
        super(api);
    }

    public void getAllSharedTicket(final API.Handler<List<TicketModel>> handler) {
        new Connection(getUrl("getAllShared")).request(new JsonArraySmartGatherer(new Gatherer.Handler<JSONArray>() {
            @Override
            protected void result(JSONArray array) throws HandlerException {
                new SharedTicketParser(handler).apply(array);
            }
        }));
    }

    @Override
    protected String getApiName() {
        return "TicketsAPI";
    }


}
