package fr.pasteque.client.drivers.mpop;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.printing.BasePrinter;
import fr.pasteque.client.utils.StringUtils;

import java.io.IOException;

/**
 * Created by svirch_n on 23/12/15.
 */
public class MPopPrinter extends BasePrinter {

    protected MPopCommandDataList mPopCommand = new MPopCommandDataList();
    protected String textToPrint = "";

    public MPopPrinter(Handler callback) {
        super(Pasteque.getAppContext(), null, callback);
    }

    @Override
    public void connect() throws IOException {
        this.connected = true;
    }

    @Override
    public void disconnect() throws IOException {
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
    }

    @Override
    protected void printBitmap(Bitmap bitmap) {
        this.mPopCommand.add(MPopFunction.Printer.image(bitmap));
    }

    @Override
    protected void cut() {
        this.mPopCommand.add(MPopFunction.Printer.cut());
        MPopManager.sendCommand(mPopCommand.getByteArray());
    }
}
