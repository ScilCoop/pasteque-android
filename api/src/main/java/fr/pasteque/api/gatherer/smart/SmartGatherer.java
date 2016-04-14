package fr.pasteque.api.gatherer.smart;

import fr.pasteque.api.exception.ParserException;
import fr.pasteque.api.exception.WrongRequestResponse;
import fr.pasteque.api.gatherer.Gatherer;
import fr.pasteque.api.gatherer.JsonGatherer;
import fr.pasteque.api.gatherer.StringHelperGatherer;
import org.json.JSONObject;

/**
 * Created by svirch_n on 12/04/16
 * Last edited at 18:54.
 */
public abstract class SmartGatherer<T1> extends StringHelperGatherer<T1> {

    public SmartGatherer(Gatherer.Handler<T1> handler) {
        super(handler);
    }

    @Override
    protected T1 parse(String data) throws ParserException {
            JSONObject object = JsonGatherer.getJson(data);
            if ("ok".equals(object.getString("status"))) {
                return getSpecificJsonObject(object, "content");
            } else {
                throw new WrongRequestResponse(object);
            }
        }

    protected abstract T1 getSpecificJsonObject(JSONObject object, String key) throws ParserException;
}
