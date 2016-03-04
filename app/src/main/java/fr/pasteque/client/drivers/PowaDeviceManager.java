package fr.pasteque.client.drivers;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import com.mpowa.android.sdk.powapos.PowaPOS;
import com.mpowa.android.sdk.powapos.common.base.PowaEnums;
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

import java.util.LinkedList;

/**
 * Created by svirch_n on 22/01/16.
 */
public class PowaDeviceManager extends POSDeviceManager {

    private static final String CALLBACK_TAG = "PowaDeviceManager/Callback";
    protected PowaPrinterCommand command = new PowaPrinterCommand();
    PowaPrinter printer = new PowaPrinter(command, this);
    PowaPOS powa;

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
        final PowaTSeries pos = new PowaTSeries(context, false);
        final PowaScanner scanner = new PowaS10Scanner(context);

        /*List<PowaDeviceObject> scanners = powa.getAvailableScanners();
        if (scanners.size() > 0) {
            powa.selectScanner(scanners.get(0));
        } else {
            Pasteque.Log.w("Scanner not found");
        }*/
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                powa.addPeripheral(pos);
                powa.addPeripheral(scanner);
            }
        });
        powa.requestMCURotationSensorStatus();
        return true;
    }

    @Override
    protected boolean disconnect() {
        if (powa != null) {
            powa.dispose();
            //No handler disconnected with dispose
            notifyEvent(DeviceManagerEvent.PrinterDisconnected);
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

    @Override
    public boolean isPrinterConnected() {
        return printer.isConnected();
    }

    private class TransPowaCallback extends BasePowaPOSCallback {

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
                notifyEvent(DeviceManagerEvent.PrinterConnected);
            } else if (state.equals(PowaEnums.ConnectionState.DISCONNECTED)) {
                notifyEvent(DeviceManagerEvent.PrinterDisconnected);
            }
        }
    }

    public class PowaPrinterCommand {

        LinkedList<IntegerHolder> pendingPrints = new LinkedList<>();
        IntegerHolder current = new IntegerHolder();
        boolean printedImage = false;

        public void printingDone(DeviceManagerEvent printingDoneEvent) {
            IntegerHolder first = pendingPrints.getFirst();
            first.decrease();
            if (first.isEmpty()) {
                notifyEvent(printingDoneEvent);
                pendingPrints.removeFirst();
            }
        }

        public boolean isConnected() {
            if (powa.getMCU() == null) {
                return false;
            }
            return powa.getMCU().getConnectionState()
                    .equals(PowaEnums.ConnectionState.CONNECTED);
        }

        public void printText(String string) {
            if (printedImage) {
                current.increase();
                printedImage = false;
            }
            powa.printText(string);
        }

        public void startReceipt() {
            powa.startReceipt();
        }

        public void printImage(Bitmap bitmap) {
            if (!printedImage) {
                current.increase();
                printedImage = true;
            }
            powa.printImage(bitmap);
        }

        public void printReceipt() {
            powa.printReceipt();
            pendingPrints.add(current);
            current = new IntegerHolder();
        }
    }
}
