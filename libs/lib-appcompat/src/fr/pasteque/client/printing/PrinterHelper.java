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
package fr.pasteque.client.printing;

import fr.pasteque.client.R;
import fr.pasteque.client.models.Cash;
import fr.pasteque.client.models.Catalog;
import fr.pasteque.client.models.Category;
import fr.pasteque.client.models.CashRegister;
import fr.pasteque.client.models.Customer;
import fr.pasteque.client.models.Payment;
import fr.pasteque.client.models.PaymentMode;
import fr.pasteque.client.models.Product;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.models.TicketLine;
import fr.pasteque.client.models.ZTicket;
import fr.pasteque.client.data.CatalogData;
import fr.pasteque.client.data.ResourceData;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;

/** Basic class for printing */
public abstract class PrinterHelper implements Printer {

    public static final int PRINT_DONE = 8654;
    public static final int PRINT_CTX_ERROR = 8655;

    protected String address;
    protected Context ctx;
    protected Receipt queued;
    protected ZTicket zQueued;
    /** Cash register queued along zQueued */
    protected CashRegister crQueued;
    protected boolean connected;
    protected Handler callback;

    public PrinterHelper(Context ctx, String address, Handler callback) {
        this.address = address;
        this.ctx = ctx;
        this.queued = null;
        this.connected = false;
        this.callback = callback;
    }

    public abstract void connect() throws IOException;
    public abstract void disconnect() throws IOException;

    protected abstract void printLine(String data);
    protected abstract void printLine();
    protected abstract void cut();

    protected void printHeader() {
        try {
            String headerData = ResourceData.loadString(this.ctx,
                    "MobilePrinter.Header");
            if (headerData != null) {
                String[] lines = headerData.split("\n");
                for (String line : lines) {
                    this.printLine(line);
                }
                this.printLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void printFooter() {
        try {
            String footerData = ResourceData.loadString(this.ctx,
                    "MobilePrinter.Footer");
            if (footerData != null) {
                String[] lines = footerData.split("\n");
                for (String line : lines) {
                    this.printLine(line);
                }
                this.printLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printReceipt(Receipt r) {
        if (this.connected == false) {
            this.queued = r;
            return;
        }
        DecimalFormat priceFormat = new DecimalFormat("#0.00");
        Customer c = r.getTicket().getCustomer();
        this.printHeader();
        // Title
        DateFormat df = DateFormat.getDateTimeInstance();
        String date = df.format(new Date(r.getPaymentTime() * 1000));
        this.printLine(padAfter(this.ctx.getString(R.string.tkt_date), 7)
                        + padBefore(date, 25));
        if (c != null) {
            this.printLine(padAfter(this.ctx.getString(R.string.tkt_cust), 9)
                    + padBefore(c.getName(), 23));
        }
        this.printLine();
        // Content
        this.printLine(padAfter(this.ctx.getString(R.string.tkt_line_article), 10)
                + padBefore(this.ctx.getString(R.string.tkt_line_price), 7)
                + padBefore("", 5)
                + padBefore(this.ctx.getString(R.string.tkt_line_total), 10));
        this.printLine();
        this.printLine("--------------------------------");
        for (TicketLine line : r.getTicket().getLines()) {
            this.printLine(padAfter(line.getProduct().getLabel(),32));
            this.printLine(padBefore(priceFormat.format(line.getProduct().getTaxedPrice()), 17)
                    + padBefore("x" + line.getQuantity(), 5)
                    + padBefore(priceFormat.format(line.getTotalPrice()), 10));
        }
        this.printLine("--------------------------------");
        // Taxes
        this.printLine();
        DecimalFormat rateFormat = new DecimalFormat("#0.#");
        Map<Double, Double> taxes = r.getTicket().getTaxes();
        for (Double rate : taxes.keySet()) {
            double dispRate = rate * 100;
            this.printLine(padAfter(this.ctx.getString(R.string.tkt_tax)
                            + rateFormat.format(dispRate) + "%", 20)
                    + padBefore(priceFormat.format(taxes.get(rate)) + "€", 12));
        }
        this.printLine();
        // Total
        this.printLine(padAfter(this.ctx.getString(R.string.tkt_subtotal), 15)
                + padBefore(priceFormat.format(r.getTicket().getSubTotalPrice()) + "€", 17));
        this.printLine(padAfter(this.ctx.getString(R.string.tkt_total), 15)
                + padBefore(priceFormat.format(r.getTicket().getTotalPrice()) + "€", 17));
        this.printLine(padAfter(this.ctx.getString(R.string.tkt_inc_vat), 15)
                + padBefore(priceFormat.format(r.getTicket().getTaxPrice()) + "€", 17));
        // Payments
        this.printLine();
        this.printLine();
        for (Payment pmt : r.getPayments()) {
            this.printLine(padAfter(pmt.getMode().getLabel(this.ctx), 20)
                    + padBefore(priceFormat.format(pmt.getGiven()) + "€", 12));
            if (pmt.getGiveBack() > 0.005) {
                this.printLine(padAfter("  " + pmt.getMode().getGiveBackLabel(this.ctx), 20)
                    + padBefore(priceFormat.format(pmt.getGiveBack()) + "€", 12));
            }
        }
        if (c != null) {
            double refill = 0.0;
            for (TicketLine l : r.getTicket().getLines()) {
                Product p = l.getProduct();
                Catalog cat = CatalogData.catalog(this.ctx);
                Category prepaidCat = cat.getPrepaidCategory();
                if (prepaidCat != null
                        && cat.getProducts(prepaidCat).contains(p)) {
                    refill += p.getTaxedPrice() * l.getQuantity();
                }
            }
            this.printLine();
            if (refill > 0.0) {
                this.printLine(padAfter(this.ctx.getString(R.string.tkt_refill), 16)
                        + padBefore(priceFormat.format(refill) + "€", 16));
            }
            this.printLine(padAfter(this.ctx.getString(R.string.tkt_prepaid_amount), 32));
            this.printLine(padBefore(priceFormat.format(c.getPrepaid()) + "€", 32));
        }
        this.printFooter();
        // Cut
        this.printLine();
        this.printLine();
        this.printLine();
        this.cut();
        // End
        this.queued = null;
    }

    public void printZTicket(ZTicket z, CashRegister cr) {
        if (this.connected == false) {
            this.zQueued = z;
            this.crQueued = cr;
            return;
        }
        this.printHeader();
        // Title
        DecimalFormat priceFormat = new DecimalFormat("#0.00");
        DateFormat df = DateFormat.getDateTimeInstance();
        this.printLine(cr.getMachineName());
        String openDate = df.format(new Date(z.getCash().getOpenDate() * 1000));
        String closeDate = df.format(new Date(z.getCash().getCloseDate() * 1000));
        this.printLine(padAfter(this.ctx.getString(R.string.tkt_z_open), 10) + padBefore(openDate, 23));
        this.printLine(padAfter(this.ctx.getString(R.string.tkt_z_close), 10) + padBefore(closeDate, 23));
        this.printLine(padAfter(this.ctx.getString(R.string.tkt_z_tickets), 10) + padBefore(String.valueOf(z.getTicketCount()), 22));
        this.printLine(padAfter(this.ctx.getString(R.string.tkt_z_total), 10) + padBefore(priceFormat.format(z.getTotal()) + "€", 22));
        this.printLine(padAfter(this.ctx.getString(R.string.tkt_z_subtotal), 10) + padBefore(priceFormat.format(z.getSubtotal()) + "€", 22));
        this.printLine(padAfter(this.ctx.getString(R.string.tkt_z_taxes), 10) + padBefore(priceFormat.format(z.getTaxAmount()) + "€", 22));
        this.printLine("--------------------------------");
        // Payments
        this.printLine();
        this.printLine();
        Map<PaymentMode, Double> pmt = z.getPayments();
        for (PaymentMode mode : pmt.keySet()) {
            this.printLine(padAfter(mode.getLabel(this.ctx), 20)
                    + padBefore(priceFormat.format(pmt.get(mode)) + "€", 12));
        }
        this.printLine("--------------------------------");
        // Taxes
        DecimalFormat rateFormat = new DecimalFormat("#0.#");
        this.printLine();
        this.printLine();
        for (Double rate : z.getTaxBases().keySet()) {
            this.printLine(padAfter(rateFormat.format(rate * 100) + "%", 9) + padBefore(priceFormat.format(z.getTaxBases().get(rate)) + "€ / " + priceFormat.format(z.getTaxBases().get(rate) * rate) + "€", 23));
        }
        this.printFooter();
        // Cut
        this.printLine();
        this.printLine();
        this.printLine();
        this.cut();
        // End
        this.zQueued = null;
        this.crQueued = null;
        if (this.callback != null) {
            Message m = this.callback.obtainMessage();
            m.what = PRINT_DONE;
            m.sendToTarget();
        }
    }

    protected static String padBefore(String text, int size) {
        String ret = "";
        for (int i = 0; i < size - text.length(); i++) {
            ret += " ";
        }
        ret += text;
        return ret;
    }
    
    protected static String padAfter(String text, int size) {
        String ret = text;
        for (int i = 0; i < size - text.length(); i++) {
            ret += " ";
        }
        return ret;
    }
}
