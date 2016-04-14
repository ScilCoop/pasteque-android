package fr.pasteque.api.gatherer;

import fr.pasteque.api.exception.ParserException;
import fr.pasteque.api.gatherer.Gatherer;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

/**
 * Created by svirch_n on 14/04/16
 * Last edited at 15:07.
 */
public class ByteGatherer extends Gatherer<InputStream, byte[]>{

    public ByteGatherer(Handler<byte[]> handler) {
        super(handler);
    }

    @Override
    protected byte[] parse(InputStream data) throws ParserException {
        try {
            return IOUtils.toByteArray(data);
        } catch (IOException e) {
            throw new ParserException(e);
        }
    }

    @Override
    protected InputStream extract(URLConnection urlConnection) throws IOException, ParserException {
        return urlConnection.getInputStream();
    }

}
