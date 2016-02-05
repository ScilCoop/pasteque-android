package fr.pasteque.client.drivers.mpop;

import com.starmicronics.starioextension.starioextmanager.StarIoExtManager;
import com.starmicronics.starioextension.starioextmanager.StarIoExtManagerListener;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.activities.POSConnectedTrackedActivity;
import fr.pasteque.client.drivers.POSDeviceManager;
import fr.pasteque.client.drivers.utils.DeviceManagerEvent;
import fr.pasteque.client.models.CashRegister;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.models.ZTicket;

/**
 *
 * Created by svirch_n on 23/12/15.
 */
public class MPopDeviceManager extends POSDeviceManager {

    public static final int TIMEOUT = 10000;
    MPopPrinter mPopPrinter;
    StarIoExtManager manager;
    private final MPopPrinterCommand printerCommand = new PrinterCommandClassical();

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

    public MPopPrinterCommand getPrinterCommand() {
        return printerCommand;
    }

    public class PrinterCommandWithManager implements MPopPrinterCommand {
        public MPopCommunication.Result sendCommand(byte[] data) {
            return MPopCommunication.sendCommands(data, manager.getPort());
        }
    }

    public class PrinterCommandClassical implements MPopPrinterCommand {
        public MPopCommunication.Result sendCommand(byte[] data) {
            return MPopCommunication.sendCommands(data, Pasteque.getConfiguration().getPrinterModel(), "",TIMEOUT);
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

    private class MPopInnerListener extends StarIoExtManagerListener {

        @Override
        public void didBarcodeDataReceive(byte[] bytes) {
            super.didBarcodeDataReceive(bytes);
            String grossString = new String(bytes);
            String formatedString = grossString.replaceAll("[\r\n]+$", "");
            notifyEvent(new DeviceManagerEvent(DeviceManagerEvent.ScannerReader, formatedString));
        }
    }
}
