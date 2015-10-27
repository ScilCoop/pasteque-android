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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

public class ZTicket {

    private Cash cash;
    private int ticketCount;
    private double total;
    private double subtotal;
    private double taxAmount;
    private List<Receipt> receipts;
    private Map<PaymentMode, PaymentDetail> payments;
    private Map<Double, Double> taxBases;

    /** Build current Z ticket */
    public ZTicket(Context ctx) {
        this.cash = Data.Cash.currentCash(ctx);
        this.receipts = Data.Receipt.getReceipts(ctx);
        this.ticketCount = receipts.size();
        this.total = 0.0;
        this.subtotal = 0.0;
        this.taxAmount = 0.0;
        this.payments = new HashMap<>();
        this.taxBases = new HashMap<Double, Double>();
        for (Receipt r : this.receipts) {
            // Payments
            for (Payment p : r.getPayments()) {

                getOrCreatePaymentModeDetail(p.getMode()).add(p.getGiven());
                this.total += p.getGiven();

                // Check for give back
                Payment back = p.getBackPayment(ctx);
                if (back != null) {
                    // Same process
                    getOrCreatePaymentModeDetail(back.getMode()).add(back.getGiven());
                    this.total += back.getGiven();
                }
            }
            // Taxes
            Ticket t = r.getTicket();
            for (TicketLine l : t.getLines()) {
                double taxRate = l.getProduct().getTaxRate();
                Double base = taxBases.get(taxRate);
                double newBase = 0.0;
                if (base == null) {
                    newBase = l.getTotalDiscPExcTax();
                } else {
                    newBase = base + l.getTotalDiscPExcTax();
                }
                this.subtotal += l.getTotalDiscPExcTax();
                this.taxAmount += l.getTotalTaxCost(t.getDiscountRate());
                this.taxBases.put(taxRate, newBase);
            }
        }
    }

    /**
     * Get the PaymentDetail of a PaymentMode
     * Create and add it to the payments map if it doesn't exist.
     * @param paymentMode the paymentMode concerned
     * @return th paymentDetail of the paymentMode
     */
    private PaymentDetail getOrCreatePaymentModeDetail(PaymentMode paymentMode) {
        PaymentDetail result = this.payments.get(paymentMode);
        if (result == null) {
            result = new PaymentDetail();
            this.payments.put(paymentMode, result);
        }
        return result;
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

    public Map<PaymentMode, PaymentDetail> getPayments() {
        return this.payments;
    }

    public Map<Double, Double> getTaxBases() {
        return this.taxBases;
    }

    public Cash getCash() {
        return this.cash;
    }
}