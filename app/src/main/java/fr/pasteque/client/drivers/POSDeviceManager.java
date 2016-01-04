package fr.pasteque.client.drivers;

import android.os.Handler;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.activities.POSConnectedTrackedActivity;
import fr.pasteque.client.drivers.mpop.MPopDeviceManager;
import fr.pasteque.client.models.CashRegister;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.models.ZTicket;
import fr.pasteque.client.utils.PastequeConfiguration;

/**
 * Class used to manager multiple devices
 * Created by svirch_n on 23/12/15.
 */
public abstract class POSDeviceManager extends Handler {

    public static POSDeviceManager createPosConnection() {
        switch (Pasteque.getConfiguration().getPrinterDriver()) {
            case PastequeConfiguration.PrinterDriver.STARMPOP:
                return new MPopDeviceManager();
            default:
                return new DefaultDeviceManager();
        }
    }

    public abstract boolean connect();

    public abstract boolean disconnect();

    public abstract void printReceipt(Receipt receipt);

    public abstract void printZTicket(ZTicket zTicket, CashRegister cashRegister);

    public abstract boolean shouldDisconnect(POSConnectedTrackedActivity.State state);

    public abstract boolean shouldConnect(POSConnectedTrackedActivity.State state);
}
