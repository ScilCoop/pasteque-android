package fr.pasteque.api;

import fr.pasteque.api.connection.Connection;
import fr.pasteque.api.gatherer.Gatherer;
import fr.pasteque.api.gatherer.HandlerException;
import fr.pasteque.api.gatherer.smart.JsonSmartGatherer;
import fr.pasteque.api.models.ProductModel;
import fr.pasteque.api.parser.ProductParser;
import fr.pasteque.api.utils.Url;
import org.json.JSONObject;

/**
 * Created by svirch_n on 14/04/16
 * Last edited at 15:35.
 */
public class Products extends SubAPIHelper {
    Products(API api) {
        super(api);
    }

    public void getProduct(String id, final API.Handler<ProductModel> productModelHandler) {
        Url url = getUrl("get").concat("id", id);
        new Connection(url).request(new JsonSmartGatherer(new Gatherer.Handler<JSONObject>() {
            @Override
            protected void result(JSONObject object) throws HandlerException {
                new ProductParser(productModelHandler).apply(object);
            }
        }));
    }

    @Override
    protected String getApiName() {
        return "ProductsAPI";
    }
}
