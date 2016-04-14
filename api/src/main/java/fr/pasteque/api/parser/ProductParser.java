package fr.pasteque.api.parser;

import fr.pasteque.api.API;
import fr.pasteque.api.models.ProductModel;
import fr.pasteque.api.utils.ApiJsonUtils;
import org.json.JSONObject;

/**
 * Created by svirch_n on 14/04/16
 * Last edited at 16:10.
 */
public class ProductParser extends Parser<JSONObject, ProductModel> {

    public ProductParser(API.Handler<ProductModel> handler) {
        super(handler);
    }

    @Override
    protected ProductModel parse(JSONObject data) {
        ProductModel result = new ProductModel();
        ApiJsonUtils json = new ApiJsonUtils(data);
        result.id = json.getString("id");
        result.label = json.getString("label");
        return result;
    }
}
