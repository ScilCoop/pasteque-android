package fr.pasteque.client.drivers.printer;

import fr.pasteque.client.Pasteque;
import fr.pasteque.client.models.CashRegister;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.models.ZTicket;

import java.io.IOException;

/**
 * Created by nanosvir on 04 Jan 16.
 */
public class EmptyPrinter implements Printer {
    @Override
    public void connect() throws IOException {

    }

    @Override
    public void disconnect() throws IOException {

    }

    @Override
    public void printReceipt(Receipt r) {
        Pasteque.log("No printer implemented");
    }

    @Override
    public void printZTicket(ZTicket z, CashRegister cr) {
        Pasteque.log("No printer implemented");
    }
}
