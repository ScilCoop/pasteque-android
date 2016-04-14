package fr.pasteque.api.parser;


import fr.pasteque.api.API;

/**
 * Created by svirch_n on 14/04/16
 * Last edited at 11:35.
 */
public abstract class Parser<T1, T2> {

    protected API.Handler<T2> handler;

    public Parser(API.Handler<T2> handler) {
        this.handler = handler;
    }

    public void apply(T1 data) {
        this.handler.result(this.parse(data));
    }

    protected abstract T2 parse(T1 data);

}
