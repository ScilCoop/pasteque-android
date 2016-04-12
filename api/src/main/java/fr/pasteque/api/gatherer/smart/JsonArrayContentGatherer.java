package fr.pasteque.api.gatherer.smart;

import fr.pasteque.api.gatherer.Gatherer;
import fr.pasteque.api.gatherer.JsonGatherer;
import fr.pasteque.api.gatherer.StringGatherer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLConnection;

/**
 * Created by svirch_n on 12/04/16
 * Last edited at 16:50.
 */
public class JsonArrayContentGatherer extends StringGatherer<JSONArray> {

    public JsonArrayContentGatherer(Handler<JSONArray> handler) {
        super(handler);
    }

    @Override
    protected JSONArray parse(String content) throws IOException {
        JSONObject object = JsonGatherer.getJson(content);
        if ("ok".equals(object.getString("status"))) {
            return object.getJSONArray("content");
        } else {
            //TODO error
            return null;
        }
    }
}
