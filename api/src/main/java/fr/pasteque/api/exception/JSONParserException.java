package fr.pasteque.api.exception;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by svirch_n on 13/04/16
 * Last edited at 19:06.
 */
public class JSONParserException extends ParserException {

    public final JSONObject object;

    public JSONParserException(JSONObject object) {
        this.object = object;
    }

    public JSONParserException(JSONObject object, JSONException exception) {
        super(exception);
        this.object = object;
    }
}
