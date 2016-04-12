package fr.pasteque.api.gatherer;

import jdk.nashorn.api.scripting.JSObject;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;

/**
 * Created by svirch_n on 12/04/16
 * Last edited at 15:56.
 */
public class JsonGatherer extends StringGatherer<JSONObject> {

    public JsonGatherer(Handler<JSONObject> handler) {
        super(handler);
    }

    @Override
    protected JSONObject parse(String string) throws IOException {
        return getJson(string);
    }

    public static JSONObject getJson(String content) throws IOException {
        return new JSONObject(content);
    }
}
