package fr.pasteque.api;

/**
 * Created by svirch_n on 07/04/16
 * Last edited at 18:06.
 */
public class API {

    String url;

    public Tickets Tickets = new Tickets(this);

    public API(Configuration configuration) {
        String login =configuration.getLogin();
        String password =configuration.getPassword();
        String host =configuration.getHostname();
        url= "https://" + host + "/api.php?";
        url += createUrlArgument("login", login, "password", password);
    }

    /**
     * Formats an url argument like &<x1>=<x2>
     * @param elements paired <x1> <x2> <y1> <y2> ..
     * @return the formatted string
     */
    private String createUrlArgument(String... elements) {
        int i = 0;
        String result = "";
        while (i < elements.length) {
            result += "&" + elements[i] + "=" + elements[i+1];
            i += 2;
        }
        return result;
    }

    /**
     * Retrieve the base url for the subApi
     * @param subAPI the subApi
     * @return the base url
     */
    protected String getUrl(SubAPI subAPI) {
        return url + createUrlArgument("p", subAPI.getApiName());
    }
}
