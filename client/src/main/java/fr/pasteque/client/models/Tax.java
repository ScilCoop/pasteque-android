package fr.pasteque.client.models;

import java.io.Serializable;

/**
 * Created by nsvir on 27/08/15.
 * n.svirchevsky@gmail.com
 */
public class Tax implements Serializable {

    private String label;
    private double value;

    public Tax(String label, double value) {
        this.label = label;
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public String getLabel() {

        return label;
    }

    public String getPercent() {
        return (value * 100) + "";
    }
}
