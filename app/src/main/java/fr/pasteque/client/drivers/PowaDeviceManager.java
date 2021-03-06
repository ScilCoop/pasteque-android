package fr.pasteque.client.drivers;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import com.mpowa.android.sdk.powapos.PowaPOS;
import com.mpowa.android.sdk.powapos.common.base.PowaEnums;
import com.mpowa.android.sdk.powapos.common.base.PowaException;
import com.mpowa.android.sdk.powapos.common.dataobjects.PowaDeviceObject;
import com.mpowa.android.sdk.powapos.core.PowaPOSEnums;
import com.mpowa.android.sdk.powapos.core.abstracts.PowaScanner;
import com.mpowa.android.sdk.powapos.drivers.s10.PowaS10Scanner;
import com.mpowa.android.sdk.powapos.drivers.tseries.PowaTSeries;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.activities.POSConnectedTrackedActivity;
import fr.pasteque.client.drivers.utils.DeviceManagerEvent;
import fr.pasteque.client.drivers.printer.BasePowaPOSCallback;
import fr.pasteque.client.drivers.printer.PowaPrinter;
import fr.pasteque.client.models.CashRegister;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.models.ZTicket;
import fr.pasteque.client.utils.IntegerHolder;
import fr.pasteque.client.utils.exception.CouldNotConnectException;
import fr.pasteque.client.utils.exception.CouldNotDisconnectException;
import fr.pasteque.client.utils.exception.ScannerNotFoundException;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by svirch_n on 22/01/16.
 */
public class PowaDeviceManager extends POSDeviceManager {

    private static final String CALLBACK_TAG = "PowaDeviceManager/Callback";
    protected PowaPrinterCommand command = new PowaPrinterCommand();
    PowaPrinter printer = new PowaPrinter(command, this);
    PowaPOS powa;
    PowaTSeries printerDevice;

    @Override
    public void connectPrinter() throws CouldNotConnectException {
        printer.connect();
    }

    @Override
    public void disconnectPrinter() throws CouldNotDisconnectException {
        printer.disconnect();
    }

    @Override
    protected boolean connect() {
        Context context = Pasteque.getAppContext();
        final PowaPOS powa = new PowaPOS(context, new TransPowaCallback());
        this.powa = powa;
        printerDevice = new PowaTSeries(context, false);
        final PowaScanner scanner = new PowaS10Scanner(context);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                powa.addPeripheral(printerDevice);
                powa.addPeripheral(scanner);
                connectBluetooth(powa);
                powa.requestMCURotationSensorStatus();
            }
        });
        return true;
    }

    public void connectBluetooth() {
        if (powa != null) {
            connectBluetooth(powa);
        }
    }

    /**
     * Connect the first bluetooth paired POWA scan
     * only if no scans are currently connected
     * @param powa the POS
     */
    private void connectBluetooth(PowaPOS powa) {
        if (powa != null) {
            PowaScanner scanner = powa.getScanner();
            if (scanner != null && scanner.getSelectedScanner() == null) {
                //If no scanner is selected
                try {
                    scanner.selectScanner(getScanner(powa));
                    scanner.setScannerAutoScan(Pasteque.getConf().scannerIsAutoScan());
                } catch (ScannerNotFoundException ignore) {
                }
            }
        }
    }

    private PowaDeviceObject getScanner(final PowaPOS powa) throws ScannerNotFoundException {
        PowaDeviceObject scannerSelected = null;
        List<PowaDeviceObject> scanners = powa.getAvailableScanners();
        if (scanners.size() > 0) {
            scannerSelected = scanners.get(0);
        } else {
            throw new ScannerNotFoundException();
        }
        return scannerSelected;
    }

    @Override
    protected boolean disconnect() {
        if (powa != null) {
            powa.dispose();
            //No handler disconnected with dispose
            notifyEvent(DeviceManagerEvent.PrinterDisconnected);
            notifyEvent(DeviceManagerEvent.ScannerDisconnected);
        }
        return true;
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
        powa.openCashDrawer();
    }

    @Override
    public boolean shouldConnect(POSConnectedTrackedActivity.State state) {
        if (state == POSConnectedTrackedActivity.State.OnResume) {
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldDisconnect(POSConnectedTrackedActivity.State state) {
        if (state == POSConnectedTrackedActivity.State.OnPause) {
            return true;
        }
        return false;
    }

    private class TransPowaCallback extends BasePowaPOSCallback {

        @Override
        public void onScannerConnectionStateChanged(PowaEnums.ConnectionState newState) {
            switch (newState) {
                case CONNECTED:
                    notifyEvent(new DeviceManagerEvent(DeviceManagerEvent.ScannerConnected));
                    break;
                case DISCONNECTED:
                    notifyEvent(new DeviceManagerEvent(DeviceManagerEvent.ScannerDisconnected));
                    break;
            }
        }

        @Override
        public void onPrintJobResult(PowaPOSEnums.PrintJobResult result) {
            PowaDeviceManager.this.command.printingDone(new DeviceManagerEvent(DeviceManagerEvent.PrintDone, result));
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
        public void onMCUConnectionStateChanged(PowaEnums.ConnectionState state) {
            if (state.equals(PowaEnums.ConnectionState.CONNECTED)) {
                powa.requestMCUSystemConfiguration();
                notifyEvent(DeviceManagerEvent.PrinterConnected);
            } else if (state.equals(PowaEnums.ConnectionState.DISCONNECTED)) {
                notifyEvent(DeviceManagerEvent.PrinterDisconnected);
            }
        }

        @Override
        public void onMCUSystemConfiguration(Map<String, String> map) {
            super.onMCUSystemConfiguration(map);
            notifyEvent(100);
        }
    }

    public class PowaPrinterCommand {

        LinkedList<IntegerHolder> pendingPrints = new LinkedList<>();
        IntegerHolder current = new IntegerHolder();

        public void printingDone(DeviceManagerEvent printingDoneEvent) {
            if (pendingPrints.size() > 0) {
                IntegerHolder first = pendingPrints.getFirst();
                first.decrease();
                if (first.isEmpty()) {
                    notifyEvent(printingDoneEvent);
                    pendingPrints.removeFirst();
                }
            }
        }

        public boolean isConnected() {
            return printerDevice.isDriverConnected();
        }

        public void printText(String string) {
            printerDevice.printText(string);
        }

        public void startReceipt() {
            current.increase();
            printerDevice.startReceipt();
        }

        public void printImage(Bitmap bitmap) {
            current.increase();
            printerDevice.printImage(bitmap);
        }

        public void printReceipt() {
            printerDevice.printReceipt();
            pendingPrints.add(current);
            current = new IntegerHolder();
        }

        public void connect() {
        }

        public void disconnect() {
        }
    }
}
