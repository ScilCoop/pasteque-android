package fr.pasteque.client.viewer.api;

import fr.pasteque.client.viewer.Pasteque;

/**
 * Created by svirch_n on 07/04/16
 * Last edited at 18:06.
 */
abstract class BaseAPI {

    String url;

    BaseAPI() {
        String login = Pasteque.getConf().getLogin();
        String password = Pasteque.getConf().getPassword();
        String host = Pasteque.getConf().getHost();
        url= "https://" + host + "/api.php?p=" + getAPIName();
        url += appendToUrl("login", login, "password", password);
    }

    protected String appendToUrl(String ... elements) {
        int i = 0;
        String result = "";
        while (i < elements.length) {
            result += "&" + elements[i] + "=" + elements[i+1];
            i += 2;
        }
        return result;
    }

    abstract String getAPIName();

    protected String getUrl(String action) {
        return url + "&action=" + action;
    }
}
