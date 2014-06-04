package fr.pasteque.client.sync;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.pasteque.client.data.SessionData;
import fr.pasteque.client.models.Session;
import fr.pasteque.client.models.Ticket;
import fr.pasteque.client.utils.URLTextGetter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class TicketUpdater {
    // TODO: Thread safety anyone?
    static private Context callBackContext;
    static private Handler endHandler;

    static public final int TICKETSERVICE_UPDATE = 1;
    static public final int TICKETSERVICE_SEND = 2;
    static public final int TICKETSERVICE_ONE = 4;
    static public final int TICKETSERVICE_ALL = 8;

    static public final int UPDATE_ENDED = 1;
    static public final int SEND_ENDED = 2;

    static public final String TAG = "TicketUpdater";

    static private TicketUpdater instance = null;

    static public TicketUpdater getInstance() {
        if (instance == null)
            instance = new TicketUpdater();
        return instance;
    }

    private void getSharedTicket(Context context, String id) {
        String baseUrl = SyncUtils.apiUrl(context);
        Map<String, String> ticketsParams = SyncUtils.initParams(context,
                "TicketsAPI", "getShared");
        ticketsParams.put("id", id);
        URLTextGetter.getText(baseUrl, ticketsParams, new DataHandler(
                TICKETSERVICE_UPDATE | TICKETSERVICE_ONE));
    }

    private void sendSharedTicket(Context context, Ticket t) {
        try {
            Map<String, String> postBody = SyncUtils.initParams(context,
                    "TicketsAPI", "share");
            postBody.put("ticket", t.toJSON(true).toString());
            URLTextGetter.getText(SyncUtils.apiUrl(context), null, postBody,
                    new DataHandler(TICKETSERVICE_SEND | TICKETSERVICE_ONE));
        } catch (JSONException e) {
            Log.e(TAG, "Unable to send ticket");
            e.printStackTrace();
            return;
        }
    }

    private void getAllSharedTickets(Context context) {
        String baseUrl = SyncUtils.apiUrl(context);
        Map<String, String> ticketsParams = SyncUtils.initParams(context,
                "TicketsAPI", "getAllShared");
        URLTextGetter.getText(baseUrl, ticketsParams,
                new DataHandler(TICKETSERVICE_UPDATE | TICKETSERVICE_ALL));
    }

    public void execute(Context context, Handler datahandler, int serviteType,
            Ticket ticket) {
        // Screwed up function to call sendTicket(context, ticket)
        // serviteType must have flag TICKETSERVICE_SEND
        if ((serviteType & TICKETSERVICE_SEND) != 0 && ticket != null) {
            this.callBackContext = context;
            this.endHandler = datahandler;
            this.sendSharedTicket(context, ticket);
        }
    }

    public void execute(Context context, Handler datahandler, int serviceType) {
        // Screwed up call for getAllSharedTickets
        this.callBackContext = context;
        this.endHandler = datahandler;
        this.getAllSharedTickets(context);
    }

    public void execute(Context context, Handler datahandler, int serviceType,
            String ticketNumber) {
        callBackContext = context;
        endHandler = datahandler;
        if ((serviceType & TICKETSERVICE_ONE) != 0) {
            if (ticketNumber == null) {
                // Send one ticket without specifying which one
                return;
            }
            if ((serviceType & TICKETSERVICE_UPDATE) != 0) {
                this.getSharedTicket(context, ticketNumber);
            } else {
                Session currSession = SessionData.currentSession(context);
                for (Ticket t : currSession.getTickets()) {
                    if (t.getId().equals(ticketNumber)) {
                        this.sendSharedTicket(context, t);
                        break;
                    }
                }
            }
        } else if ((serviceType & TICKETSERVICE_UPDATE) != 0) {
            this.getAllSharedTickets(context);
        }
    }

    private void parseAllTickets(JSONObject resp) {
        try {
            // Clear all previous tickets
            // TODO: THIS.. IS.. SUICIDE!!!
            Session currSession = SessionData.currentSession(callBackContext);
            currSession.getTickets().clear();
            // Refresh with new content
            JSONArray respArray = resp.getJSONArray("content");
            for (int i = 0; i < respArray.length(); ++i) {
                parseOneTicket(respArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Ticket parseOneTicket(JSONObject resp) {
        try {
            Ticket ticket = Ticket.fromJSON(callBackContext, resp);
            Session currSession = SessionData.currentSession(callBackContext);
            // TODO: unlink session, use returned value instead
            currSession.updateTicket(ticket);
            return ticket;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void notifyListener(Handler listener, int what, Object obj) {
        if (listener != null) {
            Message m = listener.obtainMessage();
            m.what = what;
            m.obj = obj;
            m.sendToTarget();
        }
    }

    private class DataHandler extends Handler {
        private int type;
        private Object objToReturn;

        public DataHandler(int typeData) {
            this.type = typeData;
        }

        private void finish() {
            callBackContext = null;
            TicketUpdater.notifyListener(endHandler, this.type,
                    this.objToReturn);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case URLTextGetter.SUCCESS:
                String content = (String) msg.obj;
                Log.e(TAG, content);
                try {
                    JSONObject result = new JSONObject(content);
                    String status = result.getString("status");
                    if (!status.equals("ok")) {
                        JSONObject err = result.getJSONObject("content");
                        String error = err.getString("code");
                        Log.e(TAG, error);
                    } else {
                        switch (this.type) {
                        case TICKETSERVICE_UPDATE | TICKETSERVICE_ALL:
                            parseAllTickets(result);
                            break;
                        case TICKETSERVICE_UPDATE | TICKETSERVICE_ONE:
                            this.objToReturn = parseOneTicket(result.getJSONObject("content"));
                            break;
                        case TICKETSERVICE_SEND | TICKETSERVICE_ONE:
                            Log.e(TAG, content);
                            break;
                        }
                    }
                } catch (JSONException e) {

                }
                break;
            case URLTextGetter.ERROR:
                Log.e(TAG, "Error while updating ticket");
                ((Exception) msg.obj).printStackTrace();
                break;
            case URLTextGetter.STATUS_NOK:
                Log.e(TAG, "Server error while updating ticket");
                break;
            }
            this.finish();
        }
    }
}