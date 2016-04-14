package fr.pasteque.api.gatherer;

import fr.pasteque.api.exception.ParserException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by svirch_n on 12/04/16
 * Last edited at 15:56.
 */
public class JsonGatherer extends StringHelperGatherer<JSONObject> {

    public JsonGatherer(Handler<JSONObject> handler) {
        super(handler);
    }

    @Override
    protected JSONObject parse(String data) throws ParserException {
        try {
            return getJson(data);
        } catch (JSONException e){
            throw new ParserException(e);
        }
    }

    public static JSONObject getJson(String content) throws JSONException {
        return new JSONObject(content);
    }
}
