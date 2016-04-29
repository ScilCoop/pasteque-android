package fr.pasteque.client.utils;

import fr.pasteque.client.Pasteque;

import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * Created by svirch_n on 29/04/16
 * Last edited at 10:43.
 */
public class Date implements Serializable{

    private final double date;
    private static final String TIME_FORMAT = "HH:mm:ss";
    private static final String DATE_FORMAT = "MM/dd/yyyy";
    private static final String DATE_TIME_FORMAT = DATE_FORMAT + " " + TIME_FORMAT;


    public Date(double date) {
        this.date = date;
    }

    public Date() {
        this.date = new java.util.Date().getTime();
    }

    public String format(String formatDate) {
        return new SimpleDateFormat(formatDate).format(date);
    }

    public String formatTime() {
        return format(TIME_FORMAT);
    }

    public String formatDate() {
        return format(DATE_FORMAT);
    }

    public String formatDateTime() {
        return format(DATE_TIME_FORMAT);
    }



}
