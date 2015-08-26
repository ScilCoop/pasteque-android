/*
    Pasteque Android client
    Copyright (C) Pasteque contributors, see the COPYRIGHT file

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package fr.pasteque.client.models;

import fr.pasteque.client.data.Data;
import fr.pasteque.client.data.DataSavable.ReceiptData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

public class ZTicket {

    private Cash cash;
    private int ticketCount;
    private int paymentCount;
    private double total;
    private double subtotal;
    private double taxAmount;
    private List<Receipt> receipts;
    private Map<PaymentMode, Double> payments;
    private Map<Double, Double> taxBases;

    /** Build current Z ticket */
    public ZTicket(Context ctx) {
        this.cash = Data.Cash.currentCash(ctx);
        this.receipts = Data.Receipt.getReceipts(ctx);
        this.ticketCount = receipts.size();
        this.paymentCount = 0;
        this.total = 0.0;
        this.subtotal = 0.0;
        this.taxAmount = 0.0;
        this.payments = new HashMap<PaymentMode, Double>();
        this.taxBases = new HashMap<Double, Double>();
        for (Receipt r : this.receipts) {
            // Payments
            for (Payment p : r.getPayments()) {
                double newAmount = 0.0;
                Double amount = this.payments.get(p.getMode());
                if (amount == null) {
                    newAmount = p.getGiven();
                } else {
                    newAmount = amount + p.getGiven();
                }
                this.paymentCount++;
                this.total += p.getGiven();
                this.payments.put(p.getMode(), newAmount);
                // Check for give back
                Payment back = p.getBackPayment(ctx);
                if (back != null) {
                    // Same process
                    double newAmountBack = 0.0;
                    Double amountBack = this.payments.get(back.getMode());
                    if (amountBack == null) {
                        newAmountBack = back.getGiven();
                    } else {
                        newAmountBack = amountBack + back.getGiven();
                    }
                    // this.paymentCount++; Do not count it as a payment
                    this.total += back.getGiven();
                    this.payments.put(back.getMode(), newAmountBack);
                }
            }
            // Taxes
            Ticket t = r.getTicket();
            for (TicketLine l : t.getLines()) {
                double taxRate = l.getProduct().getTaxRate();
                Double base = taxBases.get(taxRate);
                double newBase = 0.0;
                if (base == null) {
                    newBase = l.getTotalDiscExcTax(t.getTariffArea());
                } else {
                    newBase = base + l.getTotalDiscExcTax(t.getTariffArea());
                }
                this.subtotal += l.getTotalDiscExcTax(t.getTariffArea());
                this.taxAmount += l.getTaxCost(t.getTariffArea(), t.getDiscountRate());
                this.taxBases.put(taxRate, newBase);
            }
        }
    }

    public double getTotal() {
        return this.total;
    }

    public double getSubtotal() {
        return this.subtotal;
    }

    public double getTaxAmount() {
        return this.taxAmount;
    }

    public int getTicketCount() {
        return this.ticketCount;
    }

    public Map<PaymentMode, Double> getPayments() {
        return this.payments;
    }

    public Map<Double, Double> getTaxBases() {
        return this.taxBases;
    }

    public Cash getCash() {
        return this.cash;
    }
}