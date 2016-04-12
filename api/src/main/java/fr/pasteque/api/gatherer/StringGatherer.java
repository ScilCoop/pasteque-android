package fr.pasteque.api.gatherer;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;

/**
 * Created by svirch_n on 12/04/16
 * Last edited at 15:57.
 */
public abstract class StringGatherer<T1> extends Gatherer<String, T1> {

    protected StringGatherer(Handler<T1> handler) {
        super(handler);
    }

    @Override
    protected String extract(URLConnection urlConnection) throws IOException {
        return getString(urlConnection.getInputStream());
    }


    static String getString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String json = "";
        String buff;
        while ((buff = bufferedReader.readLine()) != null) {
            json += buff;
        }
        return json;
    }
}
