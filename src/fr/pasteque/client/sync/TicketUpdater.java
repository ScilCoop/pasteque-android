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
	static private Context callBackContext;
	static private Handler endHandler;

	static public final int TICKETSERVICE_UPDATE = 1;
	static public final int TICKETSERVICE_SEND = 2;
	static public final int TICKETSERVICE_ONE = 4;
	static public final int TICKETSERVICE_ALL = 8;

	static public final int	UPDATE_ENDED = 1;
	static public final int	SEND_ENDED = 2;

	static public final String TAG = "TicketUpdater";
	static public final String SERVICE_TYPE = "fr.pasteque.client.ticketupdater.service_type";
	static public final String ID_TICKET = "fr.pasteque.client.ticketupdater.id_ticket";

	static private TicketUpdater instance = null;

	static public TicketUpdater getInstance() {
		if (instance == null)
			instance = new TicketUpdater();
		return instance;
	}

	public void updateTicketWithId(Context context, String id) {
		String baseUrl = SyncUtils.apiUrl(context);
		Map<String, String> ticketsParams = SyncUtils.initParams(context,
				"TicketsAPI", "getShared");
		ticketsParams.put("id", id);
		URLTextGetter.getText(baseUrl, ticketsParams, new DataHandler(
				TICKETSERVICE_UPDATE | TICKETSERVICE_ONE));
	}

	public void sendTicket(Context context, Ticket t) {
        try {
    		Map<String, String> postBody = SyncUtils.initParams(context,
                    "TicketsAPI", "share");
            postBody.put("ticket", t.toJSON(true).toString());
            Log.i(TAG, "Before URL TEXT GETTER");
            URLTextGetter.getText(SyncUtils.apiUrl(context), null, postBody,
                    new DataHandler(TICKETSERVICE_SEND | TICKETSERVICE_ONE));
        } catch (JSONException e) {
            Log.e(TAG, "FAILED TO SEND TICKET");
            e.printStackTrace();
            return;
        }
	}

	public void sendTicketWithId(Context context, String id) {
		Session currSession = SessionData.currentSession(context);

		if (currSession != null) {
			Ticket t = currSession.getCurrentTicket();
			if (t != null) {
				sendTicket(context, t);
			}
		}
	}

	public void updateAllTicket(Context context) {
		String baseUrl = SyncUtils.apiUrl(context);
		Map<String, String> ticketsParams = SyncUtils.initParams(context,
				"TicketsAPI", "getAllShared");
		URLTextGetter.getText(baseUrl, ticketsParams, new DataHandler(
				TICKETSERVICE_UPDATE | TICKETSERVICE_ALL));
	}

	public void execute(Context context, Handler datahandler, int serviteType, Ticket ticket) {
		if ((serviteType & TICKETSERVICE_SEND) != 0 && ticket != null) {
			callBackContext = context;
			endHandler = datahandler;
			sendTicket(context, ticket);
		}
	}

	public void execute(Context context, Handler datahandler, int serviceType) {
		execute(context, datahandler, serviceType, "none");
	}

	public void execute(Context context, Handler datahandler, int serviceType, String ticketNumber) {
		callBackContext = context;
		endHandler = datahandler;
		if ((serviceType & TICKETSERVICE_ONE) != 0) {
			if (ticketNumber == null || ticketNumber.equals("none"))
				return;
			if ((serviceType & TICKETSERVICE_UPDATE) != 0)
				updateTicketWithId(context, ticketNumber);
			else
				sendTicketWithId(context, ticketNumber);
		} else {
			if ((serviceType & TICKETSERVICE_UPDATE) != 0)
				updateAllTicket(context);
		}
	}

	private void parseAllTickets(JSONObject resp) {
		try {
			JSONArray respArray = resp.getJSONArray("content");
			for (int i = 0; i < respArray.length(); ++i) {
				parseOneTicket(respArray.getJSONObject(i));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private Ticket parseOneTicket(JSONObject resp) {
		Log.d(TAG, "Parsing one ticket");
		try {
			Ticket ticket = Ticket.fromJSON(callBackContext, resp);
			Session currSession = SessionData.currentSession(callBackContext);
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

	@SuppressLint("HandlerLeak")
	private class DataHandler extends Handler {
		private int type;
		private Object objToReturn;

		public DataHandler(int typeData) {
			this.type = typeData;
		}

		public DataHandler(int typeData, String ticketId) {
			this.type = typeData;
		}

		private void finish() {
			callBackContext = null;
	        TicketUpdater.notifyListener(endHandler, type, objToReturn);
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
						switch (type) {
						case TICKETSERVICE_UPDATE | TICKETSERVICE_ALL:
							parseAllTickets(result);
							break;
						case TICKETSERVICE_UPDATE | TICKETSERVICE_ONE:
							objToReturn = parseOneTicket(result.getJSONObject("content"));
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
				Log.e(TAG, "ERROR");
				((Exception) msg.obj).printStackTrace();
			case URLTextGetter.STATUS_NOK:
				Log.e(TAG, "STATUS_NOK");
				break;
			}
			finish();
		}
	}
}