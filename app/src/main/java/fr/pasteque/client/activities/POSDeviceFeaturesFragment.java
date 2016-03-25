package fr.pasteque.client.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.ArrayMap;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Map;

import static fr.pasteque.client.activities.POSConnectedTrackedActivity.DeviceManagerInThread;

/**
 * Created by svirch_n on 12/02/16
 * Last edited at 09:28.
 */
public class POSDeviceFeaturesFragment extends DialogFragment implements POSConnectedTrackedActivity.POSHandler, View.OnClickListener {

    public static final String BARCODE_VALUE = "4931036717968";
    public static final Integer SCAN_NUMBER_SUCCESS = R.id.scan_number_success;
    public static final Integer SCAN_NUMBER_FAILURE = R.id.scan_number_failure;
    public static final Integer PRINT_NUMBER_SUCCESS = R.id.printer_success_number;
    public static final Integer PRINT_NUMBER_FAILURE = R.id.printer_failure_number;
    public static final Integer PRINT_NUMBER_PENDING = R.id.printer_pending_number;

    private Map<Integer, Integer> counters = new ArrayMap<>();
    private Map<Integer, Boolean> connected = new ArrayMap<>();
    private String logsText = "";

    private TextView logs;
    private View view;
    private POSConnectedTrackedActivity activity;

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            counters.put(SCAN_NUMBER_SUCCESS, savedInstanceState.getInt("scan_number_success"));
            counters.put(SCAN_NUMBER_FAILURE, savedInstanceState.getInt("scan_number_failure"));
            counters.put(PRINT_NUMBER_SUCCESS, savedInstanceState.getInt("print_number_success"));
            counters.put(PRINT_NUMBER_FAILURE, savedInstanceState.getInt("print_number_failure"));
            counters.put(PRINT_NUMBER_PENDING, savedInstanceState.getInt("print_number_pending"));
            connected.put(R.id.pos_switch, savedInstanceState.getBoolean("pos_switch"));
            connected.put(R.id.printer_switch, savedInstanceState.getBoolean("printer_switch"));
            connected.put(R.id.scanner_switch, savedInstanceState.getBoolean("scanner_switch"));
            logsText = savedInstanceState.getString("logs");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("scan_number_success", (Integer) getValue(counters, SCAN_NUMBER_SUCCESS, 0));
        outState.putInt("scan_number_failure", (Integer) getValue(counters, SCAN_NUMBER_FAILURE, 0));
        outState.putInt("print_number_success", (Integer) getValue(counters, PRINT_NUMBER_SUCCESS, 0));
        outState.putInt("print_number_failure", (Integer) getValue(counters, PRINT_NUMBER_FAILURE, 0));
        outState.putInt("print_number_pending", (Integer) getValue(counters, PRINT_NUMBER_PENDING, 0));
        outState.putBoolean("pos_switch", (Boolean) getValue(connected, R.id.pos_switch, false));
        outState.putBoolean("printer_switch", (Boolean) getValue(connected, R.id.printer_switch, false));
        outState.putBoolean("scanner_switch", (Boolean) getValue(connected, R.id.scanner_switch, false));
        outState.putString("logs", logsText);
    }

    private Object getValue(Map<Integer, ?> map, Integer scanNumberSuccess, Object defaultValue) {
        Object result = map.get(scanNumberSuccess);
        return result == null ? defaultValue : result;
    }

    public static POSDeviceFeaturesFragment newInstance() {
        POSDeviceFeaturesFragment result = new POSDeviceFeaturesFragment();
        return result;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        onViewStateRestored(savedInstanceState);
        this.activity = (POSConnectedTrackedActivity) getActivity();
        this.activity.setFragment(this);
        this.view = inflater.inflate(R.layout.pos_device_features, container, false);
        logs = (TextView) this.view.findViewById(R.id.logs);
        logs.setMovementMethod(new ScrollingMovementMethod());
        ((TextView) this.view.findViewById(R.id.driver)).setText("Device driver: " + Pasteque.getConfiguration().getPrinterDriver());
        ((TextView) this.view.findViewById(R.id.model)).setText("Device model: " + Pasteque.getConfiguration().getPrinterModel());
        ((TextView) this.view.findViewById(R.id.address)).setText("Device address: " + Pasteque.getConfiguration().getPrinterAddress());
        this.view.findViewById(R.id.onOpenCashClick).setEnabled(activity.deviceManagerHasCashDrawer());
        this.view.findViewById(R.id.onOpenCashClick).setOnClickListener(this);
        this.view.findViewById(R.id.onPrinterClick).setOnClickListener(this);
        this.view.findViewById(R.id.onPOSDeviceSwitchClick).setOnClickListener(this);
        this.view.findViewById(R.id.onPrinterSwitchClick).setOnClickListener(this);
        this.view.findViewById(R.id.onScannerSwitchClick).setOnClickListener(this);
        updateSwitchStatus(connected);
        updateNumbers(counters);
        updateLogs(logsText);
        return this.view;
    }

    public void onDeviceManagerEvent(DeviceManagerEvent event) {
        switch (event.what) {
            case DeviceManagerEvent.ScannerReader:
                printScannerStatus(event);
                break;
            case DeviceManagerEvent.ScannerFailure:
                inc(SCAN_NUMBER_FAILURE);
                addLog("Scanner Failed");
                break;
            case DeviceManagerEvent.PrintDone:
                inc(PRINT_NUMBER_SUCCESS);
                dec(PRINT_NUMBER_PENDING);
                addLog("Printing Done");
                break;
            case DeviceManagerEvent.PrintError:
                inc(PRINT_NUMBER_FAILURE);
                dec(PRINT_NUMBER_PENDING);
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
                addLog("Log not managed n°" + event.what);
                break;
        }
    }

    private void updateSwitchStatus(int id, boolean value) {
        connected.put(id, value);
        if (view != null) {
            ((Switch) this.view.findViewById(id)).setChecked(value);
        }
    }

    private void updateNumbers(Map<Integer, Integer> counters) {
        for (Map.Entry<Integer, Integer> each : counters.entrySet()) {
            ((TextView) this.view.findViewById(each.getKey())).setText(String.valueOf(each.getValue()));
        }
    }

    private void updateSwitchStatus(Map<Integer, Boolean> connected) {
        for (Map.Entry<Integer, Boolean> each : connected.entrySet()) {
            ((Switch) this.view.findViewById(each.getKey())).setChecked(each.getValue());
        }
    }

    private void printScannerStatus(DeviceManagerEvent event) {
        addLog("Scanner readed: " + event.getString());
        addLog("Scanner Succeed");
        inc(SCAN_NUMBER_SUCCESS);
    }

    private void inc(Integer key) {
        Integer value = this.counters.get(key);
        if (value == null) {
            value = 0;
        }
        this.counters.put(key, ++value);
        if (this.view != null) {
            ((TextView) this.view.findViewById(key)).setText(String.valueOf(value));
        }
    }

    private void dec(Integer key) {
        Integer value = this.counters.get(key);
        if (value == null) {
            value = 0;
        }
        this.counters.put(key, --value);
        if (this.view != null) {
            ((TextView) this.view.findViewById(key)).setText(String.valueOf(value));
        }
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
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    addLog(action + ": No issues in thread");
                }
            });
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
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
        return ((Switch) ((LinearLayout) view).getChildAt(1)).isChecked();
    }

    private void disconnectPOS() {
        addLog(" - Disconnecting Device..");
        activity.getDeviceManagerInThread().execute(new DeviceManagerInThread.Task() {
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
        activity.getDeviceManagerInThread().execute(new DeviceManagerInThread.Task() {
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
        activity.getDeviceManagerInThread().execute(new DeviceManagerInThread.Task() {
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
        activity.getDeviceManagerInThread().execute(new DeviceManagerInThread.Task() {
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
        inc(PRINT_NUMBER_PENDING);
        addLog(" - Printing Test..");
        activity.getDeviceManagerInThread().execute(new DeviceManagerInThread.Task() {
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
        activity.getDeviceManagerInThread().execute(new DeviceManagerInThread.Task() {
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

    public void updateLogs(String logText) {
        if (this.logs != null) {
            this.logs.setText(logText);
        }
    }

    public void addLog(String newLog) {
        this.logsText = getTime() + ": " + newLog + "\n" + logsText;
        updateLogs(this.logsText);
    }

    public String getTime() {
        GregorianCalendar calendar = new GregorianCalendar();
        Date date = calendar.getTime();
        return new SimpleDateFormat("HH:mm:ss").format(date);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.onOpenCashClick:
                onOpenCashClick(v);
                break;
            case R.id.onPOSDeviceSwitchClick:
                onPOSDeviceSwitchClick(v);
                break;
            case R.id.onPrinterClick:
                onPrinterClick(v);
                break;
            case R.id.onPrinterSwitchClick:
                onPrinterSwitchClick(v);
                break;
            case R.id.onScannerSwitchClick:
                onScannerSwitchClick(v);
                break;

        }
    }
}
