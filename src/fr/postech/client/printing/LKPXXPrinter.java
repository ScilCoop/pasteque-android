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

import fr.postech.client.models.Catalog;
import fr.postech.client.models.Customer;
import fr.postech.client.models.Payment;
import fr.postech.client.models.Product;
import fr.postech.client.models.Receipt;
import fr.postech.client.models.TicketLine;
import fr.postech.client.data.CatalogData;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import com.sewoo.jpos.command.ESCPOS;
import com.sewoo.jpos.command.ESCPOSConst;
import com.sewoo.jpos.printer.ESCPOSPrinter;
import com.sewoo.port.android.BluetoothPort;
import com.sewoo.request.android.RequestHandler;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;

public class LKPXXPrinter implements Printer {

    public static final int PRINT_DONE = 8654;
    public static final int PRINT_CTX_ERROR = 8655;

    private static final char ESC = ESCPOS.ESC;
    private static final char LF = ESCPOS.LF;
    private static final String CUT = ESC + "|#fP";

    private String address;
    private Context ctx;
    private ESCPOSPrinter printer;
    private BluetoothSocket sock;
    private BluetoothPort port;
    private Thread hThread;
    private Receipt queued;
    private boolean connected;
    private Handler callback;

    public LKPXXPrinter(Context ctx, String address, Handler callback) {
        this.address = address;
        this.ctx = ctx;
        this.port = BluetoothPort.getInstance();
        this.printer = new ESCPOSPrinter();
        this.queued = null;
        this.connected = false;
        this.callback = callback;
    }

    public void connect() throws IOException {
        BluetoothAdapter btadapt = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice dev = btadapt.getRemoteDevice(this.address);
        new ConnTask().execute(dev);
    }

    public void disconnect() throws IOException {
        try {
            port.disconnect();
            if ((hThread != null) && (hThread.isAlive())) {
                hThread.interrupt();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void printLine(String data) {
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
        ascii = ascii.replace("€", "E");
        try {
            this.printer.printNormal(ascii + LF);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printLine() {
        this.printer.lineFeed(1);
    }

    public void printReceipt(Receipt r) {
        if (this.connected == false) {
            this.queued = r;
            return;
        }
        DecimalFormat priceFormat = new DecimalFormat("#0.00");
        Customer c = r.getTicket().getCustomer();
        // Title
        DateFormat df = DateFormat.getDateTimeInstance();
        String date = df.format(new Date(r.getPaymentTime() * 1000));
        this.printLine(padAfter("Date : ", 7) + padBefore(date, 25));
        if (c != null) {
            this.printLine(padAfter("Client : ", 9)
                    + padBefore(c.getName(), 23));
        }
        this.printLine();
        // Content
        this.printLine(padAfter("Article", 10) + padBefore("Prix", 7)
                + padBefore("", 5) + padBefore("Total", 10));
        this.printLine();
        this.printLine("--------------------------------");
        for (TicketLine line : r.getTicket().getLines()) {
            this.printLine(padAfter(line.getProduct().getLabel(), 32)
                    + padBefore(priceFormat.format(line.getProduct().getTaxedPrice()), 17)
                    + padBefore("x" + line.getQuantity(), 5)
                    + padBefore(priceFormat.format(line.getTotalPrice()), 10));
        }
        this.printLine("--------------------------------");
        // Taxes
        // Total
        this.printLine(padAfter("Total", 15)
                + padBefore(priceFormat.format(r.getTicket().getTotalPrice()) + "€", 17));
        this.printLine(padAfter("Dont TVA", 15)
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
                if (cat.getProducts(cat.getPrepaidCategory()).contains(p)) {
                    refill += p.getTaxedPrice() * l.getQuantity();
                }
            }
            this.printLine();
            if (refill > 0.0) {
                this.printLine(padAfter("Recharge carte :", 16)
                        + padBefore(priceFormat.format(refill) + "€", 16));
            }
            this.printLine(padAfter("Solde carte pré-payée :", 32));
            this.printLine(padBefore(priceFormat.format(c.getPrepaid()) + "€", 32));
        }
        // Cut
        this.printLine();
        this.printLine();
        this.printLine();
        // End
        this.queued = null;
        if (this.callback != null) {
            Message m = this.callback.obtainMessage();
            m.what = PRINT_DONE;
            m.sendToTarget();
        }
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

	// Bluetooth Connection Task.
	class ConnTask extends AsyncTask<BluetoothDevice, Void, Integer> {
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
		}
		
		@Override
		protected Integer doInBackground(BluetoothDevice... params)
		{
			Integer retVal = null;
			try
			{
				port.connect(params[0]);
				retVal = new Integer(0);
			}
			catch (IOException e) {
			e.printStackTrace();
				retVal = new Integer(-1);
			}
			return retVal;
		}
		
		@Override
		protected void onPostExecute(Integer result)
		{
			if(result.intValue() == 0)	// Connection success.
			{
				RequestHandler rh = new RequestHandler();				
				hThread = new Thread(rh);
				hThread.start();
				connected = true;
				if (queued != null) {
					printReceipt(queued);
				}
			}
			else	// Connection failed.
			{
				if (callback != null) {
					Message m = callback.obtainMessage();
					m.what = PRINT_CTX_ERROR;
					m.sendToTarget();
				}
			}
			super.onPostExecute(result);
		}
	}
}
