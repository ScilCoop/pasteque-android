package fr.pasteque.api.gatherer;

import fr.pasteque.api.exception.ParserException;

import java.io.IOException;

/**
 * Created by svirch_n on 12/04/16
 * Last edited at 18:53.
 */
public class StringGatherer extends StringHelperGatherer<String> {

    public StringGatherer(Handler<String> handler) {
        super(handler);
    }

    @Override
    protected String parse(String data) throws ParserException {
        return data;
    }
}
