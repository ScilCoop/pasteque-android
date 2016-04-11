package fr.pasteque.api;

/**
 * Created by svirch_n on 11/04/16
 * Last edited at 15:34.
 */
abstract class SubAPIHelper implements SubAPI {

    API api;

    SubAPIHelper(API api) {
        this.api = api;
    }

    String getUrl(String action) {
        return api.getUrl(this) + getAction(action);
    }

    private String getAction(String action) {
        return "&action=" + action;
    }
}
