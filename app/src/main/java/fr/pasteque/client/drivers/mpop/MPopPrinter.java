package fr.pasteque.client.drivers.mpop;

import android.graphics.Bitmap;
import android.os.Handler;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.drivers.printer.BasePrinter;
import fr.pasteque.client.drivers.utils.DeviceManagerEvent;
import fr.pasteque.client.utils.StringUtils;
import fr.pasteque.client.utils.exception.CouldNotConnectException;
import fr.pasteque.client.utils.exception.CouldNotDisconnectException;

/**
 * Created by svirch_n on 23/12/15.
 */
public class MPopPrinter extends BasePrinter {

    protected MPopCommandDataList mPopCommand = new MPopCommandDataList();
    protected String textToPrint = "";
    protected MPopPrinterCommand printerCommand;

    public MPopPrinter(MPopPrinterCommand printerCommand, Handler handler) {
        super(handler);
        this.printerCommand = printerCommand;
    }

    @Override
    public void connect() throws CouldNotConnectException {
    }

    @Override
    public void disconnect() throws CouldNotDisconnectException {
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
            notifyPrinterConnectionEvent(PRINT_CTX_FAILED);
        } else {
            notifyPrinterConnectionEvent(PRINT_DONE);
        }
    }

    public boolean isConnected() {
        return printerCommand.isConnected();
    }
}
