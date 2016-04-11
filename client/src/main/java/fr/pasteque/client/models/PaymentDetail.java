package fr.pasteque.client.models;

/**
 * Created by nsvir on 27/10/15.
 * n.svirchevsky@gmail.com
 */

/**
 * Keep the movement details.
 * Sum the income and the outcome
 */
public class PaymentDetail {

    private Double income = 0.0;
    private Double outcome = 0.0;

    public PaymentDetail() {
    }

    public Double getIncome() {
        return income;
    }

    public Double getOutcome() {
        return outcome;
    }

    public Double getTotal() {
        return income - outcome;
    }

    public void add(Double value) {
        if (value > 0) {
            income += value;
        } else {
            outcome += -value;
        }
    }
}
