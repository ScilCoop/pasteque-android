package fr.pasteque.client.drivers;

import android.os.Handler;
import android.os.Looper;
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
import fr.pasteque.client.utils.exception.CouldNotConnectException;
import fr.pasteque.client.utils.exception.CouldNotDisconnectException;

import java.io.Serializable;

/**
 * Class used to manager multiple devices
 * Synchronized Class
 * Make sure it remains so
 * Created by svirch_n on 23/12/15.
 */
public abstract class POSDeviceManager extends Handler implements Serializable {

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

    public void connectDevice() throws CouldNotConnectException {
        disconnect();
        this.connected = connect();
        if (!this.connected) {
            notifyEvent(DeviceManagerEvent.DeviceConnectFailure);
            throw new CouldNotConnectException();
        }
        notifyEvent(DeviceManagerEvent.DeviceConnected);
    }

    public void disconnectDevice() throws CouldNotDisconnectException {
        this.connected = false;
        if (!this.disconnect()) {
            throw new CouldNotDisconnectException();
        }
        notifyEvent(DeviceManagerEvent.DeviceDisconnected);
    }

    public abstract void connectPrinter() throws CouldNotConnectException;

    public abstract void disconnectPrinter() throws CouldNotDisconnectException;

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
            this.eventListener.onThreadedDeviceManagerEvent(event);
        }
    }

    protected void notifyEvent(int eventNumber) {
        if (this.eventListener != null) {
            this.eventListener.onThreadedDeviceManagerEvent(new DeviceManagerEvent(eventNumber));
        }
    }

    public abstract boolean isPrinterConnected();

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case PrinterConnection.PRINT_DONE:
                notifyEvent(DeviceManagerEvent.PrintDone);
                break;
            case PrinterConnection.PRINT_CTX_ERROR:
            case PrinterConnection.PRINT_CTX_FAILED:
                notifyEvent(DeviceManagerEvent.PrintError);
                break;
            case PrinterConnection.PRINTING_QUEUED:
                notifyEvent(DeviceManagerEvent.PrintQueued);
                break;
        }
    }

    public boolean hasCashDrawer() {
        return false;
    }

    public abstract void printTest();

    public abstract void printQueued();

    public boolean reconnect() {
        disconnect();
        return connect();
    }
}
