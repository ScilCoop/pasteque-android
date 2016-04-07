package fr.pasteque.client.viewer.api;

import android.util.Log;

/**
 * Created by svirch_n on 07/04/16
 * Last edited at 18:06.
 */
public class Tickets extends BaseAPI {

    public static Tickets API = new Tickets();

    private Tickets() {
        super();
    }

    @Override
    String getAPIName() {
        return "Tickets";
    }

    public void getAllSharedTicket() {
        Log.d("Pasteque", getUrl("getAllShared"));
    }
}
