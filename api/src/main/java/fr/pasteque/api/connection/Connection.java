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

    URLConnection connection;


    public Connection(Url url) throws IOException {
        connection = new URL(url.url).openConnection();
        connection.setRequestProperty("Accept-Charset", url.charset);
    }

    public void request(Gatherer<?,?> gatherer) {
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
            try {
                this.gatherer.apply(this.connection);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
