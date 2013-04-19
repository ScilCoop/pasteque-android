/*
    POS-Tech Android
    Copyright (C) 2012 SARL SCOP Scil (contact@scil.coop)

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
package fr.postech.client.printing;

import fr.postech.client.models.Payment;
import fr.postech.client.models.Receipt;
import fr.postech.client.models.TicketLine;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;

public class LKPXXPrinter implements Printer {

    private static String CUT = "27|#fP";
    private static String LF = "\n";//"10";

    private String address;
    private Context ctx;
    private BluetoothSocket sock;
    private OutputStream stream;

    public LKPXXPrinter(Context ctx, String address) {
        this.address = address;
        this.ctx = ctx;
    }

    public void connect() throws IOException {
        BluetoothAdapter btadapt = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice dev = btadapt.getRemoteDevice(this.address);
        try {
            Method m = dev.getClass().getMethod("createRfcommSocket",
                    new Class[] {int.class});
            if (this.sock == null) {
                this.sock = (BluetoothSocket)m.invoke(dev,
                        Integer.valueOf(1));
            }
            this.sock.connect();
            if (this.stream == null) {
                this.stream = sock.getOutputStream();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() throws IOException {
        if (this.sock != null) {
            this.sock.close();
            this.sock = null;
            this.stream = null;
        }
    }

    private void print(String data) throws IOException {
        if (this.stream != null) {
            String ascii = data.replace("é", "e");
            ascii = ascii.replace("è", "e");
            ascii = ascii.replace("ê", "e");
            ascii = ascii.replace("ë", "e");
            ascii = ascii.replace("à", "a");
            ascii = ascii.replace("ï", "i");
            ascii = ascii.replace("ô", "o");
            ascii = ascii.replace("ç", "c");
            ascii = ascii.replace("ù", "u");
            ascii = ascii.replace("É", "E");
            ascii = ascii.replace("È", "E");
            ascii = ascii.replace("Ê", "E");
            ascii = ascii.replace("Ë", "E");
            ascii = ascii.replace("À", "A");
            ascii = ascii.replace("Ï", "I");
            ascii = ascii.replace("Ô", "O");
            ascii = ascii.replace("Ç", "c");
            ascii = ascii.replace("Ù", "u");
            this.stream.write(ascii.getBytes("ASCII"));
            this.stream.flush();
        }
    }

    public void printReceipt(Receipt r) throws IOException {
        DecimalFormat priceFormat = new DecimalFormat("#0.00");
        // Title
        DateFormat df = DateFormat.getDateTimeInstance();
        String date = df.format(new Date(r.getPaymentTime() * 1000));
        this.print(padAfter("Date : ", 7) + padBefore(date, 25) + LF);
        this.print(LF);
        // Content
        this.print(padAfter("Article", 10));
        this.print(padBefore("Prix", 7));
        this.print(padBefore("", 5));
        this.print(padBefore("Total", 10) + LF);
        this.print(LF);
        this.print("--------------------------------" + LF);
        for (TicketLine line : r.getTicket().getLines()) {
            this.print(padAfter(line.getProduct().getLabel(), 32));
            this.print(padBefore(priceFormat.format(line.getProduct().getTaxedPrice()), 17));
            this.print(padBefore("x" + line.getQuantity(), 5));
            this.print(padBefore(priceFormat.format(line.getTotalPrice()), 10));
            this.print(LF);
        }
        this.print("--------------------------------" + LF);
        // Taxes
        // Total
        this.print(padAfter("Total", 15));
        this.print(padBefore(priceFormat.format(r.getTicket().getTotalPrice()), 17));
        this.print(padAfter("Dont TVA", 15));
        this.print(padBefore(priceFormat.format(r.getTicket().getTaxPrice()), 17));
        // Payments
        this.print(LF + LF);
        for (Payment pmt : r.getPayments()) {
            this.print(padAfter(pmt.getMode().getLabel(this.ctx), 18));
            this.print(padBefore(priceFormat.format(pmt.getAmount()), 32));
            this.print(LF);
        }
        // Cut
        this.print(LF + LF + LF);
    }

    private static String padBefore(String text, int size) {
        String ret = "";
        for (int i = 0; i < size - text.length(); i++) {
            ret += " ";
        }
        ret += text;
        return ret;
    }
    
    private static String padAfter(String text, int size) {
        String ret = text;
        for (int i = 0; i < size - text.length(); i++) {
            ret += " ";
        }
        return ret;
    }
}
