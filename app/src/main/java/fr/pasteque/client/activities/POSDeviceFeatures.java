package fr.pasteque.client.activities;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.R;
import fr.pasteque.client.drivers.POSDeviceManager;
import fr.pasteque.client.drivers.utils.DeviceManagerEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by svirch_n on 12/02/16
 * Last edited at 09:28.
 */
public class POSDeviceFeatures extends POSConnectedTrackedActivity {

    public static final String BARCODE_VALUE = "4532428152338";
    private int scan_number_success;
    private int print_number_success;
    private int print_number_failure;
    private int print_number_pending;
    private int scan_number_failure;

    private TextView logs;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.pos_device_features);
        logs = (TextView) findViewById(R.id.logs);
        logs.setMovementMethod(new ScrollingMovementMethod());
        ((TextView)findViewById(R.id.driver)).setText("Device driver: " + Pasteque.getConfiguration().getPrinterDriver());
        ((TextView)findViewById(R.id.model)).setText("Device model: " + Pasteque.getConfiguration().getPrinterModel());
        ((TextView)findViewById(R.id.address)).setText("Device address: " + Pasteque.getConfiguration().getPrinterAddress());
        findViewById(R.id.cash_button).setEnabled(deviceManagerHasCashDrawer());
    }

    @Override
    public boolean onDeviceManagerEvent(DeviceManagerEvent event) {
        switch (event.what) {
            case DeviceManagerEvent.ScannerReader:
                printScannerStatus(event);
                incScanSuccess();
                break;
            case DeviceManagerEvent.ScannerFailure:
                incScanFailure();
                addLog("Scanner Failed");
                break;
            case DeviceManagerEvent.PrintDone:
                incPrintSuccess();
                decPrintPending();
                addLog("Printing Done");
                break;
            case DeviceManagerEvent.PrintError:
                incPrintFailure();
                decPrintPending();
                addLog("Printing Error");
                break;
            case DeviceManagerEvent.DeviceConnected:
                updateSwitchStatus(R.id.pos_switch, true);
                addLog("Device Connected");
                break;
            case DeviceManagerEvent.DeviceConnectFailure:
                addLog("Could not connect device");
                break;
            case DeviceManagerEvent.PrinterConnectFailure:
                addLog("Could not connect printer");
                break;
            case DeviceManagerEvent.PrinterConnected:
                updateSwitchStatus(R.id.printer_switch, true);
                addLog("Printer Connected");
                break;
            case DeviceManagerEvent.ScannerConnected:
                updateSwitchStatus(R.id.scanner_switch, true);
                addLog("Scanner Connected");
                break;
            case DeviceManagerEvent.DeviceDisconnected:
                addLog("Device Disconnected");
                updateSwitchStatus(R.id.pos_switch, false);
                break;
            case DeviceManagerEvent.PrinterDisconnected:
                addLog("Printer Disconnected");
                updateSwitchStatus(R.id.printer_switch, false);
                break;
            case DeviceManagerEvent.ScannerDisconnected:
                addLog("Scanner Disconnected");
                updateSwitchStatus(R.id.scanner_switch, false);
                break;
            case DeviceManagerEvent.CashDrawerOpened:
                addLog("Cash Drawer Opened");
                break;
            case DeviceManagerEvent.CashDrawerClosed:
                addLog("Cash Drawer Closed");
                break;
            case DeviceManagerEvent.PrintQueued:
                addLog("Print queued");
                break;
            default:
                addLog("Log not managed");
                break;
        }
        return false;
    }

    private void updateSwitchStatus(int id, boolean value) {
        ((Switch)findViewById(id)).setChecked(value);
    }

    private void printScannerStatus(DeviceManagerEvent event) {
        addLog("Scanner readed: " + event.getString());
        if (event.getString().equals(BARCODE_VALUE)) {
            addLog("Scanner Succeed");
            incScanSuccess();
        } else {
            addLog("Scanner Failed");
            incScanFailure();
            Pasteque.Toast.show(R.string.barcode_failure);
        }

    }

    private void incPrintSuccess() {
        this.print_number_success++;
        ((TextView) findViewById(R.id.printer_success_number)).setText(String.valueOf(this.print_number_success));
    }

    private void incPrintFailure() {
        this.print_number_failure++;
        ((TextView) findViewById(R.id.printer_failure_number)).setText(String.valueOf(this.print_number_failure));
    }

    private void incPrintPending() {
        this.print_number_pending++;
        ((TextView) findViewById(R.id.printer_pending_number)).setText(String.valueOf(this.print_number_pending));
    }

    private void decPrintPending() {
        this.print_number_pending--;
        ((TextView) findViewById(R.id.printer_pending_number)).setText(String.valueOf(this.print_number_pending));
    }


    private void incScanSuccess() {
        this.scan_number_success++;
        ((TextView) findViewById(R.id.scan_number_success)).setText(String.valueOf(this.scan_number_success));
    }

    private void incScanFailure() {
        this.scan_number_failure++;
        ((TextView) findViewById(R.id.scan_number_failure)).setText(String.valueOf(this.scan_number_failure));
    }

    public void onPOSDeviceSwitchClick(View view) {
        if (!isSwitchEnabled(view)) {
            connectPOS();
        } else {
            disconnectPOS();
        }
    }


    public void onPrinterSwitchClick(View view) {
        if (!isSwitchEnabled(view)) {
            connectPrinter();
        } else {
            disconnectPrinter();
        }
    }

    private void addLogResult(boolean isSuccess, final String action, final Exception exception) {
        if (isSuccess) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addLog(action + ": No issues in thread");
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addLog(action + ": " + exception.toString());
                }
            });
        }
    }

    public void onScannerSwitchClick(View view) {
    }

    private boolean isSwitchEnabled(View view) {
        return ((Switch)((LinearLayout)view).getChildAt(1)).isChecked();
    }

    private void disconnectPOS()
    {
        addLog(" - Disconnecting Device..");
        getDeviceManagerInThread().execute(new DeviceManagerInThread.Task() {
            @Override
            public void execute(POSDeviceManager manager) throws Exception {
                manager.disconnectDevice();
            }

            @Override
            public void result(boolean isSuccess, Exception exceptionRaised) {
                addLogResult(isSuccess, "\t..Disconnecting Device", exceptionRaised);
            }
        });
    }

    private void connectPOS() {
        addLog(" - Connecting Device..");
        getDeviceManagerInThread().execute(new DeviceManagerInThread.Task() {
            @Override
            public void execute(POSDeviceManager manager) throws Exception {
                manager.connectDevice();
            }

            @Override
            public void result(boolean isSuccess, Exception exceptionRaised) {
                addLogResult(isSuccess, "\t..Connecting Device", exceptionRaised);
            }
        });
    }

    private void disconnectPrinter() {
        addLog(" - Disconnecting printer..");
        getDeviceManagerInThread().execute(new DeviceManagerInThread.Task() {
            @Override
            public void execute(POSDeviceManager manager) throws Exception {
                manager.disconnectPrinter();
            }

            @Override
            public void result(boolean isSuccess, Exception exceptionRaised) {
                addLogResult(isSuccess, "\t..Disconnecting printer", exceptionRaised);
            }
        });
    }

    private void connectPrinter() {
        addLog(" - Connecting printer..");
        getDeviceManagerInThread().execute(new DeviceManagerInThread.Task() {
            @Override
            public void execute(POSDeviceManager manager) throws Exception {
                manager.connectPrinter();
            }

            @Override
            public void result(boolean isSuccess, Exception exceptionRaised) {
                addLogResult(isSuccess, "\t..Connecting printer", exceptionRaised);
            }
        });
    }

    public void onPrinterClick(View view) {
        incPrintPending();
        addLog(" - Printing Test..");
        getDeviceManagerInThread().execute(new DeviceManagerInThread.Task() {
            @Override
            public void execute(POSDeviceManager manager) throws Exception {
                manager.printTest();
            }

            @Override
            public void result(boolean isSuccess, Exception exceptionRaised) {
                addLogResult(isSuccess, "\t..Printing Test", exceptionRaised);
            }
        });
    }

    public void onOpenCashClick(View view) {
        addLog(" - Opening Cash..");
        getDeviceManagerInThread().execute(new DeviceManagerInThread.Task() {
            @Override
            public void execute(POSDeviceManager manager) throws Exception {
                manager.openCashDrawer();
            }

            @Override
            public void result(boolean isSuccess, Exception exceptionRaised) {
                addLogResult(isSuccess, "\t..Opening Cash", exceptionRaised);
            }
        });
    }

    public void addLog(String log) {
        logs.setText(getTime() + ": " + log + "\n" + logs.getText());
    }

    public String getTime() {
        GregorianCalendar calendar = new GregorianCalendar();
        Date date = calendar.getTime();
        return new SimpleDateFormat("HH:mm:ss").format(date);
    }
}
