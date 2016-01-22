package fr.pasteque.client.drivers.printer;

import android.os.Handler;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.models.CashRegister;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.models.ZTicket;
import fr.pasteque.client.utils.exception.CouldNotConnectException;

import java.io.IOException;

/**
 * Created by nanosvir on 04 Jan 16.
 */
public class EmptyPrinter extends PrinterConnection {

    public EmptyPrinter(Handler handler) {
        super(handler);
    }

    @Override
    public void connect() throws CouldNotConnectException {

    }

    @Override
    public void disconnect() throws IOException {

    }

    @Override
    public void printReceipt(Receipt r) {
        Pasteque.Log.d("No printer implemented");
    }

    @Override
    public void printZTicket(ZTicket z, CashRegister cr) {
        Pasteque.Log.d("No printer implemented");
    }
}
