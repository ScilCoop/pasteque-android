package fr.pasteque.api.exception;

import org.json.JSONObject;

/**
 * Created by svirch_n on 13/04/16
 * Last edited at 19:12.
 */
public class WrongRequestResponse extends ParserException {

    public final JSONObject object;

    public WrongRequestResponse(JSONObject object) {
        this.object = object;
    }
}
