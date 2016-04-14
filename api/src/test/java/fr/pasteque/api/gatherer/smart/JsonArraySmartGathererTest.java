package fr.pasteque.api.gatherer.smart;

import org.junit.Test;

/**
 * Created by svirch_n on 12/04/16
 * Last edited at 17:05.
 */
public class JsonArraySmartGathererTest {
    @org.junit.Test
    public void parseSuccess() throws Exception {
        JsonArraySmartGatherer gatherer = new JsonArraySmartGatherer(null);
        gatherer.parse("{\"status\": \"ok\",\"content\":[]}");
    }

    @Test
    public void parseMalformed() throws Exception {
        JsonArraySmartGatherer gatherer = new JsonArraySmartGatherer(null);
        gatherer.parse("Malformed");
    }

    @Test
    public void parseFailure() throws Exception {
        JsonArraySmartGatherer gatherer = new JsonArraySmartGatherer(null);
        gatherer.parse("{\n" +
                "    \"status\": \"rej\",\n" +
                "    \"content\": {\n" +
                "        \"code\": \"Wrong parameters\",\n" +
                "        \"params\": null\n" +
                "    }\n" +
                "}\n");
    }

}