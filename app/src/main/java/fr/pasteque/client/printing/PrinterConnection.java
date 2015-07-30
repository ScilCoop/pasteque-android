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

import fr.pasteque.client.Configure;
import fr.pasteque.client.models.CashRegister;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.models.ZTicket;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import java.io.IOException;

public class PrinterConnection implements Handler.Callback {

    private static final String LOG_TAG = "Pasteque/PrinterConnection";

    /** Connection successfully established. */
    public static final int PRINT_DONE = 8654;
    /** Could not establish connection due to IO error */
    public static final int PRINT_CTX_ERROR = 8655;
    /** Connection failed after multiple attempts (timeout or such) */
    public static final int PRINT_CTX_FAILED = 8656;


    private Printer printer;
    private int printConnectTries;
    private int maxConnectTries;
    private Handler callback;

    public PrinterConnection(Handler callback) {
        this.callback = callback;
    }

    /** Connect to the printer.
     * @return True if connection is running, false if there is no printer
     * @throws IOException If an error occurs while connecting to the printer
     */
    public boolean connect(Context ctx) throws IOException {
        this.printConnectTries = 0;
        String prDriver = Configure.getPrinterDriver(ctx);
        if (!prDriver.equals("None")) {
            switch (prDriver) {
                case "LK-PXX":
                    this.printer = new LKPXXPrinter(ctx,
                            Configure.getPrinterAddress(ctx), new Handler(this));
                    printer.connect();
                    this.printConnectTries = 0;
                    this.maxConnectTries = Configure.getPrinterConnectTry(ctx);
                    return true;
                case "Woosim":
                    this.printer = new WoosimPrinter(ctx,
                            Configure.getPrinterAddress(ctx), new Handler(this));
                    printer.connect();
                    this.printConnectTries = 0;
                    this.maxConnectTries = Configure.getPrinterConnectTry(ctx);
                    return true;
                case "PowaPOS":
                    this.printer = new PowaPrinter(ctx, new Handler(this));
                    this.printer.connect();
                    return true;
            }
        }
        return false;
    }

    public void disconnect() throws IOException {
        this.printer.disconnect();
    }

    public void printReceipt(Receipt r) {
        this.printer.printReceipt(r);
    }

    public void printZTicket(ZTicket z, CashRegister cr) {
        this.printer.printZTicket(z, cr);
    }

    @Override
	public boolean handleMessage(Message m) {
        
        switch (m.what) {
        case BasePrinter.PRINT_DONE:
            if (this.callback != null) {
                Message m2 = callback.obtainMessage();
                m2.what = PRINT_DONE;
                m2.sendToTarget();
            }
            this.printConnectTries = 0;
            break;
        case BasePrinter.PRINT_CTX_ERROR:
            this.printConnectTries++;
            if (this.printConnectTries < this.maxConnectTries) {
                // Retry silently
                try {
                    if (this.printer != null) { // only if not destroyed
                        this.printer.connect();
                    }
                } catch (IOException e) {
                    // Fatal error
                    if (this.callback != null) {
                        Message m2 = callback.obtainMessage();
                        m2.what = PRINT_CTX_ERROR;
                        m2.obj = e;
                        m2.sendToTarget();
                    }
                }
            } else {
                // Give up
                if (this.callback != null) {
                    Message m2 = callback.obtainMessage();
                    m2.what = PRINT_CTX_FAILED;
                    m2.sendToTarget();
                }
                this.printConnectTries = 0;
            }
            break;
        }
        return true;
    }

}
