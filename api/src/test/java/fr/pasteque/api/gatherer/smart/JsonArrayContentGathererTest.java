package fr.pasteque.api.gatherer.smart;

import fr.pasteque.api.gatherer.Gatherer;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by svirch_n on 12/04/16
 * Last edited at 17:05.
 */
public class JsonArrayContentGathererTest {
    @org.junit.Test
    public void parseSuccess() throws Exception {
        JsonArrayContentGatherer gatherer = new JsonArrayContentGatherer(null);
        gatherer.parse("{\"status\": \"ok\",\"content\":[]}");
    }

    @Test
    public void parseMalformed() throws Exception {
        JsonArrayContentGatherer gatherer = new JsonArrayContentGatherer(null);
        gatherer.parse("Malformed");
    }

    @Test
    public void parseFailure() throws Exception {
        JsonArrayContentGatherer gatherer = new JsonArrayContentGatherer(null);
        gatherer.parse("{\n" +
                "    \"status\": \"rej\",\n" +
                "    \"content\": {\n" +
                "        \"code\": \"Wrong parameters\",\n" +
                "        \"params\": null\n" +
                "    }\n" +
                "}\n");
    }

}