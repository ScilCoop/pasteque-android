package fr.pasteque.api;

/**
 * Created by svirch_n on 11/04/16
 * Last edited at 16:37.
 */
public class Images extends SubAPIHelper {
    Images(API api) {
        super(api);
    }

    public String getProduct(String id) {
        return getUrl("getPrd").concat("id", id).toString();
    }

    @Override
    protected String getApiName() {
        return "ImagesAPI";
    }
}
