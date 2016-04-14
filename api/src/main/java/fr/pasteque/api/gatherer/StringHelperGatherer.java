package fr.pasteque.api.gatherer;

import fr.pasteque.api.exception.ParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;

/**
 * Created by svirch_n on 12/04/16
 * Last edited at 15:57.
 */
public abstract class StringHelperGatherer<T1> extends Gatherer<String, T1> {

    protected StringHelperGatherer(Handler<T1> handler) {
        super(handler);
    }

    @Override
    protected String extract(URLConnection urlConnection) throws ParserException {
        try {
            return getString(urlConnection.getInputStream());
        } catch (IOException e) {
            throw new ParserException(e);
        }
    }


    static String getString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String result = "";
        String buff;
        while ((buff = bufferedReader.readLine()) != null) {
            result += buff;
        }
        return result;
    }
}
