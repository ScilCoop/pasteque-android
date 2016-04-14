package fr.pasteque.api.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by svirch_n on 14/04/16
 * Last edited at 11:55.
 */
public class ApiJsonUtils {

    private JSONObject json;

    public ApiJsonUtils(JSONObject object) {
        this.json = object;
    }

    public Object get(String key) {
        return ApiJsonUtils.get(this.json, key);
    }

    public String getString(String key) {
        return ApiJsonUtils.getString(this.json, key);
    }

    public static Object get(JSONObject object, String key) {
        try {
            return object.get(key);
        } catch (JSONException e) {
            return null;
        }
    }

    public static String getString(JSONObject object, String key) {
        try {
            return object.getString(key);
        } catch (JSONException e) {
            return null;
        }
    }

    public JSONArray getJSONArray(String lines) {
        try {
            return this.json.getJSONArray(lines);
        } catch (JSONException e) {
            return null;
        }
    }
}
