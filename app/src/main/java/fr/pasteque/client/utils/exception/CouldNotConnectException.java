package fr.pasteque.client.utils.exception;

import java.io.IOException;

/**
 * Created by svirch_n on 22/01/16.
 */
public class CouldNotConnectException extends IOException {

    public CouldNotConnectException(Exception e) {
        super(e);
    }

    public CouldNotConnectException() {

    }

    public CouldNotConnectException(String s) {
        super(s);
    }
}
