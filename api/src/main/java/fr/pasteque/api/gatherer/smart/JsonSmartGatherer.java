package fr.pasteque.api.gatherer.smart;

import fr.pasteque.api.exception.ParserException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by svirch_n on 14/04/16
 * Last edited at 16:08.
 */
public class JsonSmartGatherer extends SmartGatherer<JSONObject> {

    public JsonSmartGatherer(Handler<JSONObject> handler) {
        super(handler);
    }

    @Override
    protected JSONObject getSpecificJsonObject(JSONObject object, String key) throws ParserException {
        try {
            return object.getJSONObject(key);
        } catch (JSONException e) {
            throw new ParserException(e);
        }
    }
}
