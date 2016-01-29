package fr.pasteque.client.drivers.mpop;

import android.graphics.Bitmap;
import android.os.Handler;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.drivers.printer.BasePrinter;
import fr.pasteque.client.utils.StringUtils;
import fr.pasteque.client.utils.exception.CouldNotConnectException;
import fr.pasteque.client.utils.exception.CouldNotDisconnectException;

/**
 * Created by svirch_n on 23/12/15.
 */
public class MPopPrinter extends BasePrinter {

    protected MPopCommandDataList mPopCommand = new MPopCommandDataList();
    protected String textToPrint = "";
    protected MPopDeviceManager.PrinterCommand printerCommand;

    public MPopPrinter(MPopDeviceManager.PrinterCommand printerCommand, Handler handler) {
        super(handler);
        this.printerCommand = printerCommand;
        this.connected = true;
    }

    @Override
    public void connect() throws CouldNotConnectException {
        this.connected = true;
    }

    @Override
    public void disconnect() throws CouldNotDisconnectException {
        this.connected = false;
    }

    @Override
    protected void printLine(String data) {
        data = StringUtils.formatAscii(data);
        this.textToPrint += data + "\r\n";
    }

    @Override
    protected void printLine() {
        this.textToPrint += "\r\n";
    }

    @Override
    protected void flush() {
        this.mPopCommand.add(MPopFunction.Printer.data(this.textToPrint));
        this.textToPrint = "";
    }

    @Override
    protected void printBitmap(Bitmap bitmap) {
        this.mPopCommand.add(MPopFunction.Printer.image(bitmap));
    }

    @Override
    protected void cut() {
        this.mPopCommand.add(MPopFunction.Printer.cut());
        byte[] bytes = this.mPopCommand.getByteArray();
        this.mPopCommand.clear();
        MPopCommunication.Result result = printerCommand.sendCommand(bytes);
        Pasteque.Log.d(result.getAsText());
        if (result != MPopCommunication.Result.Success) {
            handleMessage(PRINT_CTX_FAILED);
        }
    }
}
