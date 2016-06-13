package fr.pasteque.client.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * Created by svirch_n on 09/06/16
 * Used to test a JSONAble interface
 * Last edited at 16:41.
 */
public class JSONability {

    public void assertJSON(String model, JSONable jsoNable) throws JSONException {
        JSONAssert.assertEquals(model, jsoNable.toJSON(), false);
    }
}
