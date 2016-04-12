package fr.pasteque.api;

import fr.pasteque.api.utils.Configuration;
import fr.pasteque.api.utils.Url;

/**
 * Created by svirch_n on 07/04/16
 * Last edited at 18:06.
 */
public class API {

    Url url;
    private final String charset;

    public Tickets Tickets = new Tickets(this);
    public Images Images = new Images(this);

    public API(Configuration configuration) {
        String login = configuration.getLogin();
        String password = configuration.getPassword();
        String host = configuration.getHostname();
        charset = configuration.getCharset();
        url = new Url(charset)
                .base("https://" + host + "/api.php?")
                .concat("login", login, "password", password);
    }

    /**
     * Retrieve the base url for the subApi
     *
     * @param subAPI the subApi
     * @return the base url
     */
    protected Url getUrl(SubAPI subAPI) {
        return url.concat("p", subAPI.getApiName());
    }
}
