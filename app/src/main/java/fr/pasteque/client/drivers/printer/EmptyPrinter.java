package fr.pasteque.client.drivers.printer;

import android.os.Handler;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.models.CashRegister;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.models.ZTicket;
import fr.pasteque.client.utils.exception.CouldNotConnectException;
import fr.pasteque.client.utils.exception.CouldNotDisconnectException;

import java.io.IOException;

/**
 * Created by nanosvir on 04 Jan 16.
 */
public class EmptyPrinter extends PrinterConnection {

    private boolean connected;

    public EmptyPrinter(Handler handler) {
        super(handler);
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void connect() throws CouldNotConnectException {
        connected = true;
    }

    @Override
    public void disconnect() throws CouldNotDisconnectException {
        connected = false;
    }

    @Override
    public void printReceipt(Receipt r) {
        if (isConnected()) {
            notifyPrinterConnectionEvent(PRINT_DONE);
            Pasteque.Log.d("No printer implemented");
        } else {
            notifyPrinterConnectionEvent(PRINT_CTX_ERROR);
        }
    }

    @Override
    public void printZTicket(ZTicket z, CashRegister cr) {
        if (isConnected()) {
            notifyPrinterConnectionEvent(PRINT_DONE);
            Pasteque.Log.d("No printer implemented");
        } else {
            notifyPrinterConnectionEvent(PRINT_CTX_ERROR);
        }
    }

    @Override
    public void printTest() {
        if (isConnected()) {
            notifyPrinterConnectionEvent(PRINT_DONE);
            Pasteque.Log.d("No printer implemented");
        } else {
            notifyPrinterConnectionEvent(PRINT_CTX_ERROR);
        }
    }

    @Override
    public void flushQueue() {
        // Empty because of a bad implementation between
        // printerconnection, which is some kind of a factory
        // and baseprinter, which is the base content of a printer but with some none generic features
    }
}
