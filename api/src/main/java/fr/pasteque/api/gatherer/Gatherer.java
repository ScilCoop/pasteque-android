package fr.pasteque.api.gatherer;

import fr.pasteque.api.exception.ParserException;

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
    public final void apply(URLConnection urlConnection) {
        try {
            this.handler.result(parse(extract(urlConnection)));
        } catch (IOException e) {
            catcher(new ParserException(e));
        } catch (Exception e) {
            catcher(e);
        }
    }

    private void catcher(Exception e) {
        this._catcher(e);
    }

    public void thrower(Exception e) {
        this._catcher(e);
    }

    private void _catcher(Exception e) {
        e.printStackTrace();
        this.handler.catcher(e);
    }

    /**
     * Method parsing the url connection
     * @param data
     * @return
     * @throws IOException
     */
    protected abstract T2 parse(T1 data) throws ParserException;

    protected abstract T1 extract(URLConnection urlConnection) throws IOException, ParserException;

    public static abstract class Handler<T2> {

        protected abstract void result(T2 object) throws HandlerException;

        protected void catcher(Exception e) {};
    }
}
