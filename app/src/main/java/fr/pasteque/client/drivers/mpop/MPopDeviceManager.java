package fr.pasteque.client.drivers.mpop;

import com.starmicronics.starioextension.starioextmanager.StarIoExtManager;
import com.starmicronics.starioextension.starioextmanager.*;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.activities.POSConnectedTrackedActivity;
import fr.pasteque.client.drivers.POSDeviceManager;
import fr.pasteque.client.drivers.utils.DeviceManagerEvent;
import fr.pasteque.client.models.CashRegister;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.models.ZTicket;
import fr.pasteque.client.utils.exception.CouldNotConnectException;
import fr.pasteque.client.utils.exception.CouldNotDisconnectException;

/**
 * Created by svirch_n on 23/12/15.
 */
public class MPopDeviceManager extends POSDeviceManager {

    public static final int TIMEOUT = 10000;
    MPopPrinter mPopPrinter;
    StarIoExtManager manager;
    private final MPopPrinterCommand printerCommand = new PrinterCommandWithManager();

    public MPopDeviceManager() {
        manager = new StarIoExtManager(StarIoExtManager.Type.WithBarcodeReader, Pasteque.getConfiguration().getPrinterModel(), "", TIMEOUT, Pasteque.getAppContext());
        mPopPrinter = new MPopPrinter(printerCommand, this);
        this.manager.setListener(new MPopInnerListener());
    }

    @Override
    public boolean isPrinterConnected() {
        return mPopPrinter.isConnected();
    }

    @Override
    public void connectPrinter() throws CouldNotConnectException {
        notifyEvent(DeviceManagerEvent.PrinterConnectFailure);
    }

    @Override
    public void disconnectPrinter() throws CouldNotDisconnectException {
    }

    @Override
    protected boolean connect() {
        return this.manager.connect();
    }

    @Override
    protected boolean disconnect() {
        return this.manager.disconnect();
    }

    @Override
    public void printReceipt(Receipt receipt) {
        mPopPrinter.printReceipt(receipt);
    }

    @Override
    public void printZTicket(ZTicket zTicket, CashRegister cashRegister) {
        mPopPrinter.printZTicket(zTicket, cashRegister);
    }

    @Override
    public void openCashDrawer() {
        MPopManager.openDrawer();
    }

    public class PrinterCommandWithManager implements MPopPrinterCommand {
        public MPopCommunication.Result sendCommand(byte[] data) {
            return MPopCommunication.sendCommands(data, manager.getPort());
        }

        @Override
        public boolean isConnected() {
            return manager.getPrinterOnlineStatus().equals(StarIoExtManager.Status.PrinterOnline);
        }
    }

    public class PrinterCommandClassical implements MPopPrinterCommand {
        public MPopCommunication.Result sendCommand(byte[] data) {
            return MPopCommunication.sendCommands(data, Pasteque.getConfiguration().getPrinterModel(), "", TIMEOUT);
        }

        @Override
        public boolean isConnected() {
            return true;
        }
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

    @Override
    public boolean hasCashDrawer() {
        return true;
    }

    @Override
    public void printTest() {
        mPopPrinter.printTest();
    }

    @Override
    public void printQueued() {
        mPopPrinter.flushQueue();
    }

    private class MPopInnerListener extends StarIoExtManagerListener {

        @Override
        public void didCashDrawerOpen() {
            super.didCashDrawerOpen();
            notifyEvent(DeviceManagerEvent.CashDrawerOpened);
        }

        @Override
        public void didCashDrawerClose() {
            super.didCashDrawerClose();
            notifyEvent(DeviceManagerEvent.CashDrawerClosed);
        }

        @Override
        public void didBarcodeReaderConnect() {
            super.didBarcodeReaderConnect();
            notifyEvent(DeviceManagerEvent.ScannerConnected);
        }

        @Override
        public void didBarcodeReaderDisconnect() {
            super.didBarcodeReaderDisconnect();
            notifyEvent(DeviceManagerEvent.ScannerDisconnected);
        }

        @Override
        public void didPrinterOnline() {
            super.didPrinterOnline();
            notifyEvent(DeviceManagerEvent.PrinterConnected);
        }

        @Override
        public void didPrinterOffline() {
            super.didPrinterOffline();
            notifyEvent(DeviceManagerEvent.PrinterDisconnected);
        }

        @Override
        public void didBarcodeReaderImpossible() {
            super.didBarcodeReaderImpossible();
            notifyEvent(DeviceManagerEvent.ScannerFailure);
        }

        @Override
        public void didBarcodeDataReceive(byte[] bytes) {
            super.didBarcodeDataReceive(bytes);
            String grossString = new String(bytes);
            String formatedString = grossString.replaceAll("[\r\n]+$", "");
            notifyEvent(new DeviceManagerEvent(DeviceManagerEvent.ScannerReader, formatedString));
        }
    }
}
