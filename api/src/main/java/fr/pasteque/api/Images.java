package fr.pasteque.api;

import fr.pasteque.api.connection.Connection;
import fr.pasteque.api.gatherer.*;
import fr.pasteque.api.models.ProductModel;
import fr.pasteque.api.utils.Url;

/**
 * Created by svirch_n on 11/04/16
 * Last edited at 16:37.
 */
public class Images extends SubAPIHelper {
    Images(API api) {
        super(api);
    }

    public void getProduct(String id, Gatherer<?, ?> gatherer) {
        Url url = getUrl("getPrd").concat("id", id);
        new Connection(url).request(gatherer);
    }

    @Override
    protected String getApiName() {
        return "ImagesAPI";
    }

    public void getProduct(final String id, final API.Handler<byte[]> handler) {
        getProduct(id, new ByteGatherer(new Gatherer.Handler<byte[]>() {
            @Override
            protected void result(byte[] object) throws HandlerException {
                handler.result(object);
            }
        }));
    }
}
