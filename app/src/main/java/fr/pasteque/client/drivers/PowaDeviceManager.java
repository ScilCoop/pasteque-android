package fr.pasteque.client.drivers;

import android.content.Context;
import com.mpowa.android.sdk.common.base.PowaEnums;
import com.mpowa.android.sdk.common.dataobjects.PowaDeviceObject;
import com.mpowa.android.sdk.powapos.core.PowaPOSEnums;
import com.mpowa.android.sdk.powapos.core.abstracts.PowaScanner;
import com.mpowa.android.sdk.powapos.drivers.s10.PowaS10Scanner;
import com.mpowa.android.sdk.powapos.drivers.tseries.PowaTSeries;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.drivers.utils.DeviceManagerEvent;
import fr.pasteque.client.drivers.printer.BasePowaPOSCallback;
import fr.pasteque.client.drivers.printer.PowaPrinter;
import fr.pasteque.client.models.CashRegister;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.models.ZTicket;
import fr.pasteque.client.utils.PastequePowaPos;
import fr.pasteque.client.utils.exception.CouldNotConnectException;
import fr.pasteque.client.utils.exception.CouldNotDisconnectException;

import java.util.List;

/**
 * Created by svirch_n on 22/01/16.
 */
public class PowaDeviceManager extends SingletonPOSDeviceManager {

    private static final String CALLBACK_TAG = "PowaDeviceManager/Callback";
    PowaPrinter printer = new PowaPrinter(this);

    @Override
    protected boolean lastDisconnect(POSDeviceManager singleton) {
        PastequePowaPos.getSingleton().dispose();
        return true;

    }

    @Override
    protected boolean firstConnect(POSDeviceManager singleton) {
        Context context = Pasteque.getAppContext();
        PastequePowaPos.getSingleton().create(null, "null");
        PowaTSeries pos = new PowaTSeries(context, false);
        PastequePowaPos.getSingleton().addPeripheral(pos);

        PowaScanner scanner = new PowaS10Scanner(context);
        PastequePowaPos.getSingleton().addPeripheral(scanner);

        List<PowaDeviceObject> scanners = PastequePowaPos.getSingleton().getAvailableScanners();
        if (scanners.size() > 0) {
            PastequePowaPos.getSingleton().selectScanner(scanners.get(0));
        } else {
            Pasteque.Log.w("Scanner not found");
        }
        return true;
    }

    @Override
    public void connectPrinter() throws CouldNotConnectException {
        printer.connect();
        notifyEvent(DeviceManagerEvent.PrinterConnected);
    }

    @Override
    public void disconnectPrinter() throws CouldNotDisconnectException {
        printer.disconnect();
        notifyEvent(DeviceManagerEvent.PrinterDisconnected);
    }

    @Override
    public boolean connect() {
        super.connect();
        boolean result = true;
        try {
            connectPrinter();
        } catch (CouldNotConnectException e) {
            e.printStackTrace();
            result = false;
        }
        PastequePowaPos.getSingleton().addCallback(CALLBACK_TAG, new TransPowaCallback());
        return result;
    }

    @Override
    public boolean disconnect() {
        super.disconnect();
        boolean result = true;
        try {
            disconnectPrinter();
        } catch (CouldNotDisconnectException e) {
            e.printStackTrace();
            result = false;
        }
        PastequePowaPos.getSingleton().removeCallback(CALLBACK_TAG);
        return result;
    }

    @Override
    public void printReceipt(Receipt receipt) {
        printer.printReceipt(receipt);
    }

    @Override
    public void printZTicket(ZTicket zTicket, CashRegister cashRegister) {
        printer.printZTicket(zTicket, cashRegister);
    }

    @Override
    public boolean hasCashDrawer() {
        return true;
    }

    @Override
    public void printTest() {
        printer.printTest();
    }

    @Override
    public void printQueued() {
        printer.flushQueue();
    }

    @Override
    public void openCashDrawer() {
        PastequePowaPos.getSingleton().openCashDrawer();
    }

    @Override
    public boolean isPrinterConnected() {
        return printer.isConnected();
    }

    private class TransPowaCallback extends BasePowaPOSCallback {

        @Override
        public void onPrintJobResult(PowaPOSEnums.PrintJobResult result) {
            notifyEvent(new DeviceManagerEvent(DeviceManagerEvent.PrintDone, result));
        }

        @Override
        public void onRotationSensorStatus(PowaPOSEnums.RotationSensorStatus rotationSensorStatus) {
            notifyEvent(new DeviceManagerEvent(DeviceManagerEvent.BaseRotation, rotationSensorStatus));
        }

        @Override
        public void onScannerRead(String barcode) {
            notifyEvent(new DeviceManagerEvent(DeviceManagerEvent.ScannerReader, barcode));
        }

        @Override
        public void onMCUInitialized(final PowaPOSEnums.InitializedResult result) {
            if (result.equals(PowaPOSEnums.InitializedResult.SUCCESSFUL)) {
                notifyEvent(DeviceManagerEvent.PrinterConnected);
            }
        }

        @Override
        public void onMCUConnectionStateChanged(PowaEnums.ConnectionState state) {
            if (state.equals(PowaEnums.ConnectionState.CONNECTED)) {
                notifyEvent(DeviceManagerEvent.PrinterConnected);
            } else if (state.equals(PowaEnums.ConnectionState.DISCONNECTED)) {
                notifyEvent(DeviceManagerEvent.PrinterDisconnected);
            }
        }
    }
}
