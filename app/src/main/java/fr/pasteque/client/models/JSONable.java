package fr.pasteque.client.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by svirch_n on 09/06/16
 * Last edited at 16:18.
 */
public interface JSONable {

    JSONObject toJSON() throws JSONException;
}
