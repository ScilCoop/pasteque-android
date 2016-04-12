package fr.pasteque.api.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by svirch_n on 11/04/16
 * Last edited at 16:39.
 */
public class Url {

    public final String charset;
    public final String url;

    public Url(String charset) {
        this.charset = charset;
        this.url = "";
    }

    public Url(String charset, String url) {
        this.charset = charset;
        this.url = url;
    }

    public Url concat(String ... elements) {
        int i = 0;
        String resultUrl = url;
        try {
            while (i < elements.length) {
                resultUrl += String.format("&%s=%s",
                        URLEncoder.encode(elements[i], charset),
                        URLEncoder.encode(elements[i + 1], charset));
                i += 2;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return this.clone(resultUrl);
    }

    protected Url clone(String url) {
        Url result = new Url(this.charset, url);
        return result;
    }

    /**
     * Erase the stored url and base the new one
     * @param url
     */
    public Url base(String url) {
        Url result = this.clone(url);
        return result;
    }

    public String toString() {
        return this.url;
    }

}
