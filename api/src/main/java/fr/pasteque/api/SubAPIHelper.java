package fr.pasteque.api;

import fr.pasteque.api.utils.Url;

/**
 * Created by svirch_n on 11/04/16
 * Last edited at 15:34.
 */
abstract class SubAPIHelper extends SubAPI {

    API api;

    SubAPIHelper(API api) {
        this.api = api;
    }

    Url getUrl(String action) {
        return api.getUrl(this).concat("action", action);
    }
}
