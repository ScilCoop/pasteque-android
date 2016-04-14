package fr.pasteque.api.gatherer.smart;

import fr.pasteque.api.exception.JSONParserException;
import fr.pasteque.api.exception.ParserException;
import fr.pasteque.api.gatherer.Gatherer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by svirch_n on 12/04/16
 * Last edited at 16:50.
 */
public class JsonArraySmartGatherer extends SmartGatherer<JSONArray> {

    public JsonArraySmartGatherer(Gatherer.Handler<JSONArray> handler) {
        super(handler);
    }

    @Override
    protected JSONArray getSpecificJsonObject(JSONObject object, String key) throws ParserException {
        try {
            return object.getJSONArray(key);
        } catch (JSONException exception) {
            throw new JSONParserException(object, exception);
        }
    }


}
