package fr.pasteque.client.drivers;

import fr.pasteque.client.Pasteque;
import fr.pasteque.client.activities.POSConnectedTrackedActivity;
import fr.pasteque.client.drivers.printer.BasePrinter;
import fr.pasteque.client.drivers.utils.DeviceManagerEvent;
import fr.pasteque.client.models.CashRegister;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.models.ZTicket;
import fr.pasteque.client.drivers.printer.PrinterConnection;
import fr.pasteque.client.utils.exception.CouldNotConnectException;
import fr.pasteque.client.utils.exception.CouldNotDisconnectException;

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
    public void connectBluetooth() {

    }

    @Override
    public void connectPrinter() throws CouldNotConnectException {
        try {
            printerConnection.connect();
        } catch (CouldNotConnectException e) {
            notifyEvent(DeviceManagerEvent.PrinterConnectFailure);
            throw e;
        }
        notifyEvent(DeviceManagerEvent.PrinterConnected);
    }

    @Override
    public void disconnectPrinter() throws CouldNotDisconnectException {
        if (printerConnection.isConnected()) {
            printerConnection.disconnect();
            notifyEvent(DeviceManagerEvent.PrinterDisconnected);
        }
    }

    @Override
    public boolean connect() {
        try {
            connectPrinter();
        } catch (IOException e) {
            Pasteque.Log.d(e.getMessage(), e);
        }
        connected = true;
        return true;
    }

    @Override
    public boolean disconnect() {
        try {
            disconnectPrinter();
        } catch (IOException e) {
            Pasteque.Log.d(e.getMessage(), e);
        }
        connected = false;
        return true;
    }

    @Override
    public void printReceipt(Receipt receipt) {
        if (!printerConnection.isConnected()) {
            Pasteque.Log.d("No printer connected");
        } else {
            printerConnection.printReceipt(receipt);
        }
    }

    @Override
    public void printZTicket(ZTicket zTicket, CashRegister cashRegister) {
        if (!printerConnection.isConnected()) {
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

    public void printTest() {
        printerConnection.printTest();
    }

    @Override
    public void printQueued() {
        printerConnection.flushQueue();
    }
}
