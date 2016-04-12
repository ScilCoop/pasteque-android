package fr.pasteque.api.gatherer;

import org.json.JSONException;

import java.io.IOException;
import java.net.URLConnection;

/**
 * Created by svirch_n on 12/04/16
 * Last edited at 15:53.
 */
public abstract class Gatherer<T1, T2> {

    Gatherer.Handler<T2> handler;

    protected Gatherer(Gatherer.Handler<T2> handler) {
        this.handler = handler;
    }

    /**
     * method applying the gatherer routine
     * @param urlConnection
     * @throws IOException
     */
    public final void apply(URLConnection urlConnection) throws IOException {
        this.handler.result(parse(extract(urlConnection)));
    }

    /**
     * Method parsing the url connection
     * @param urlConnection
     * @return
     * @throws IOException
     */
    protected abstract T2 parse(T1 urlConnection) throws IOException;

    protected abstract T1 extract(URLConnection urlConnection) throws IOException;

    public interface Handler<T2> {
        void result(T2 object) throws JSONException;
    }
}
