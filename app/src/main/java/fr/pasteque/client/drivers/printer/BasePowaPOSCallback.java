package fr.pasteque.client.drivers.printer;

import com.mpowa.android.sdk.powapos.common.base.PowaEnums;
import com.mpowa.android.sdk.powapos.core.PowaPOSEnums;
import com.mpowa.android.sdk.powapos.core.callbacks.PowaPOSCallback;

import java.util.Map;

/**
 * Created by nsvir on 28/07/15.
 * n.svirchevsky@gmail.com
 */
public class BasePowaPOSCallback extends PowaPOSCallback {

    protected BasePowaPOSCallback() {

    }

    @Override
    public void onMCUInitialized(PowaPOSEnums.InitializedResult initializedResult) {

    }

    @Override
    public void onMCUConnectionStateChanged(PowaEnums.ConnectionState connectionState) {

    }

    @Override
    public void onMCUFirmwareUpdateStarted() {

    }

    @Override
    public void onMCUFirmwareUpdateProgress(int i) {

    }

    @Override
    public void onMCUFirmwareUpdateFinished() {

    }

    @Override
    public void onMCUBootloaderUpdateStarted() {

    }

    @Override
    public void onMCUBootloaderUpdateProgress(int i) {

    }

    @Override
    public void onMCUBootloaderUpdateFinished() {

    }

    @Override
    public void onMCUBootloaderUpdateFailed(PowaPOSEnums.BootloaderUpdateError bootloaderUpdateError) {

    }

    @Override
    public void onMCUSystemConfiguration(Map<String, String> map) {

    }

    @Override
    public void onUSBDeviceAttached(PowaPOSEnums.PowaUSBCOMPort powaUSBCOMPort) {

    }

    @Override
    public void onUSBDeviceDetached(PowaPOSEnums.PowaUSBCOMPort powaUSBCOMPort) {

    }

    @Override
    public void onCashDrawerStatus(PowaPOSEnums.CashDrawerStatus cashDrawerStatus) {

    }

    @Override
    public void onRotationSensorStatus(PowaPOSEnums.RotationSensorStatus rotationSensorStatus) {

    }

    @Override
    public void onScannerInitialized(PowaPOSEnums.InitializedResult initializedResult) {

    }

    @Override
    public void onScannerRead(String s) {

    }

    @Override
    public void onPrintJobResult(PowaPOSEnums.PrintJobResult printJobResult) {

    }

    @Override
    public void onPrinterOutOfPaper() {

    }

    @Override
    public void onUSBReceivedData(PowaPOSEnums.PowaUSBCOMPort powaUSBCOMPort, byte[] bytes) {

    }
}
