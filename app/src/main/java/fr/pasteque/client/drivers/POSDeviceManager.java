package fr.pasteque.client.drivers;

import android.os.Handler;
import android.os.Message;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.drivers.printer.PrinterConnection;
import fr.pasteque.client.drivers.utils.DeviceManagerEvent;
import fr.pasteque.client.activities.POSConnectedTrackedActivity;
import fr.pasteque.client.drivers.mpop.MPopDeviceManager;
import fr.pasteque.client.drivers.utils.DeviceManagerEventListener;
import fr.pasteque.client.models.CashRegister;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.models.ZTicket;
import fr.pasteque.client.utils.PastequeConfiguration;

/**
 * Class used to manager multiple devices
 * Synchronized Class
 * Make sure it remains so
 * Created by svirch_n on 23/12/15.
 */
public abstract class POSDeviceManager extends Handler {

    private DeviceManagerEventListener eventListener;
    private boolean connected;

    public static POSDeviceManager createPosConnection() {
        switch (Pasteque.getConfiguration().getPrinterDriver()) {
            case PastequeConfiguration.PrinterDriver.POWAPOS:
                return new PowaDeviceManager();
            case PastequeConfiguration.PrinterDriver.STARMPOP:
                return new MPopDeviceManager();
            default:
                return new DefaultDeviceManager();
        }
    }

    public boolean connectDevice() {
        this.connected = connect();
        return this.connected;
    }

    public boolean disconnectDevice() {
        this.connected = !this.disconnect();
        return !this.connected;
    }

    protected abstract boolean connect();

    protected abstract boolean disconnect();

    public abstract void printReceipt(Receipt receipt);

    public abstract void printZTicket(ZTicket zTicket, CashRegister cashRegister);

    public abstract void openCashDrawer();

    public abstract boolean shouldDisconnect(POSConnectedTrackedActivity.State state);

    public abstract boolean shouldConnect(POSConnectedTrackedActivity.State state);

    public void setEventListener(DeviceManagerEventListener eventListener) {
        this.eventListener = eventListener;
    }

    protected void notifyEvent(DeviceManagerEvent event) {
        if (this.eventListener != null) {
            this.eventListener.onDeviceManagerEvent(event);
        }
    }

    public boolean isPrinterConnected() {
        return connected;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case PrinterConnection.PRINT_DONE:
                notifyEvent(new DeviceManagerEvent(DeviceManagerEvent.PrintDone));
                break;
            case PrinterConnection.PRINT_CTX_ERROR:
            case PrinterConnection.PRINT_CTX_FAILED:
                notifyEvent(new DeviceManagerEvent(DeviceManagerEvent.PrintError));
                break;
        }
    }
}
