package fr.pasteque.client.drivers.mpop;

import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.starioextension.starioextmanager.StarIoExtManager;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.activities.POSConnectedTrackedActivity;
import fr.pasteque.client.drivers.POSDeviceManager;
import fr.pasteque.client.models.CashRegister;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.models.ZTicket;

/**
 *
 * Created by svirch_n on 23/12/15.
 */
public class MPopDeviceManager extends POSDeviceManager {

    private final MPopPrinter mPopPrinter;
    StarIoExtManager manager;
    private final MPopDeviceManager.Printer printer = new Printer();

    public MPopDeviceManager() {
        manager = new StarIoExtManager(StarIoExtManager.Type.Standard, Pasteque.getConfiguration().getPrinterModel(), "", 10000, Pasteque.getAppContext());
        mPopPrinter = new MPopPrinter(printer, this);
    }

    @Override
    public boolean connect() {
        return this.manager.connect();
    }

    @Override
    public boolean disconnect() {
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

    public Printer getPrinter() {
        return printer;
    }

    public class Printer {
        public void sendCommand(byte[] data) {
            try {
                MPopCommunication.sendCommands(data, manager.getPort());
            } catch (StarIOPortException e) {
                e.printStackTrace();
            }
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
}
