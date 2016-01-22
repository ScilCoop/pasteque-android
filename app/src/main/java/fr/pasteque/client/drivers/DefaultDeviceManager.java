package fr.pasteque.client.drivers;

import fr.pasteque.client.Pasteque;
import fr.pasteque.client.activities.POSConnectedTrackedActivity;
import fr.pasteque.client.models.CashRegister;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.models.ZTicket;
import fr.pasteque.client.drivers.printer.PrinterConnection;

import java.io.IOException;

/**
 * Classed used for compatibility with PrinterConnection old behavior
 * Created by svirch_n on 23/12/15.
 */
public class DefaultDeviceManager extends POSDeviceManager {


    PrinterConnection printerConnection;
    boolean connected = false;

    DefaultDeviceManager() {
        printerConnection = PrinterConnection.getPrinterConnection(this);
    }

    @Override
    public boolean connect() {
        try {
            printerConnection.connect();
            connected = true;
            return true;
        } catch (IOException e) {
            Pasteque.Log.d(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean disconnect() {
        try {
            if (connected) {
                printerConnection.disconnect();
            }
            connected = false;
            return true;
        } catch (IOException e) {
            Pasteque.Log.d(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void printReceipt(Receipt receipt) {
        if (!connected) {
            Pasteque.Log.d("No printer connected");
        } else {
            printerConnection.printReceipt(receipt);
        }
    }

    @Override
    public void printZTicket(ZTicket zTicket, CashRegister cashRegister) {
        if (!connected) {
            Pasteque.Log.d("No printer connected");
        } else {
            printerConnection.printZTicket(zTicket, cashRegister);
        }
    }

    @Override
    public void openCashDrawer() {

    }

    @Override
    public boolean shouldDisconnect(POSConnectedTrackedActivity.State state) {
        switch (state) {
            case OnPause:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean shouldConnect(POSConnectedTrackedActivity.State state) {
        switch (state) {
            case OnResume:
                return true;
            default:
                return false;
        }
    }
}
