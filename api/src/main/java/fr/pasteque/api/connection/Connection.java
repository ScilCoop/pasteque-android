package fr.pasteque.api.connection;

import fr.pasteque.api.gatherer.Gatherer;
import fr.pasteque.api.utils.Url;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by svirch_n on 11/04/16
 * Last edited at 17:29.
 */
public class Connection {

    private final Url url;
    private URLConnection connection;


    public Connection(Url url) {
        this.url = url;
    }

    public void request(Gatherer<?,?> gatherer) {
        try {
            connection = new URL(url.url).openConnection();
        } catch (IOException e) {
            gatherer.thrower(e);
            return;
        }
        connection.setRequestProperty("Accept-Charset", url.charset);
        new ConnectionThread(connection, gatherer).start();
    }

    public class ConnectionThread extends Thread {

        private URLConnection connection;
        private Gatherer<?,?> gatherer;

        public ConnectionThread(URLConnection connection, Gatherer<?, ?> gatherer) {
            this.connection = connection;
            this.gatherer = gatherer;
        }

        @Override
        public void run() {
            super.run();
            this.gatherer.apply(this.connection);
        }
    }
}
