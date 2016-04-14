package fr.pasteque.api.exception;

import java.io.IOException;

/**
 * Created by svirch_n on 13/04/16
 * Last edited at 18:57.
 */
public class ParserException extends Exception {

    public ParserException() {
        super();
    }

    public ParserException(Exception exception) {
        super(exception);
    }
}
