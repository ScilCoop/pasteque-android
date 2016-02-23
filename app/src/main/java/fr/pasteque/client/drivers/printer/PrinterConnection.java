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
package fr.pasteque.client.drivers.printer;

import fr.pasteque.client.Pasteque;
import fr.pasteque.client.drivers.mpop.MPopDeviceManager;
import fr.pasteque.client.drivers.mpop.MPopPrinter;

import android.os.Handler;
import android.os.Message;
import fr.pasteque.client.drivers.utils.DeviceManagerEvent;
import fr.pasteque.client.utils.PastequeConfiguration;
import fr.pasteque.client.utils.exception.CouldNotConnectException;

import java.io.IOException;

public abstract class PrinterConnection implements Printer {

    /**
     * Connection successfully established.
     */
    public static final int PRINT_DONE = 8654;
    /**
     * Could not establish connection due to IO error
     */
    public static final int PRINT_CTX_ERROR = 8655;
    /**
     * Connection failed after multiple attempts (timeout or such)
     */
    public static final int PRINT_CTX_FAILED = 8656;
    public static final int PRINTING_QUEUED = 8657;


    protected int printConnectTries;
    protected int maxConnectTries;
    private Handler handler;

    public PrinterConnection(Handler handler) {
        this.handler = handler;
    }

    /**
     * Connect to the printer.
     *
     * @return True if connection is running, false if there is no printer
     * @throws IOException If an error occurs while connecting to the printer
     */
    public static PrinterConnection getPrinterConnection(Handler handler) {
        PrinterConnection printer = getPrinter(handler);
        printer.handler = handler;
        return printer;
    }

    private static PrinterConnection getPrinter(Handler handler) {
        String prDriver = Pasteque.getConf().getPrinterDriver();
        if (!prDriver.equals("None")) {
            switch (prDriver) {
                case "LK-PXX":
                    return new LKPXXPrinter(handler, Pasteque.getConf().getPrinterAddress());
                case "Woosim":
                    return new WoosimPrinter(handler, Pasteque.getConf().getPrinterAddress());
            }
        }
        return new EmptyPrinter(handler);
    }

    public abstract boolean isConnected();

    public boolean notifyPrinterConnectionEvent(int what) {

        switch (what) {
            case PrinterConnection.PRINT_CTX_FAILED:
            case PrinterConnection.PRINT_CTX_ERROR:
                this.printConnectTries++;
                if (this.printConnectTries < this.maxConnectTries) {
                    // Retry silently
                    try {
                        connect();
                    } catch (CouldNotConnectException couldNotConnectException) {
                        // Fatal error
                        if (this.handler != null) {
                            Message m2 = handler.obtainMessage();
                            m2.what = PRINT_CTX_ERROR;
                            m2.obj = couldNotConnectException;
                            m2.sendToTarget();
                        }
                    }
                } else {
                    // Give up
                    sendMessage(PRINT_CTX_FAILED);
                    this.printConnectTries = 0;
                }
                break;
            case PrinterConnection.PRINT_DONE:
                this.printConnectTries = 0;
            default:
                sendMessage(what);
                break;
        }
        return true;
    }

    private void sendMessage(int what) {
        if (handler != null) {
            Message message = handler.obtainMessage();
            message.what = what;
            message.sendToTarget();
        }
    }

    public abstract void flushQueue();
}
