/*
    Pasteque Android client
    Copyright (C) Pasteque contributors, see the COPYRIGHT file

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package fr.pasteque.client.drivers.printer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import com.sewoo.jpos.command.ESCPOS;
import com.sewoo.jpos.command.ESCPOSConst;
import com.sewoo.jpos.printer.ESCPOSPrinter;
import com.sewoo.port.android.BluetoothPort;
import com.sewoo.request.android.RequestHandler;
import fr.pasteque.client.Configure;
import fr.pasteque.client.utils.exception.CouldNotConnectException;
import fr.pasteque.client.utils.exception.CouldNotDisconnectException;

import java.io.IOException;

public class LKPXXPrinter extends BasePrinter {

    private static final char ESC = ESCPOS.ESC;
    private static final char LF = ESCPOS.LF;
    private static final String CUT = ESC + "|#fP";

    private ESCPOSPrinter printer;
    private BluetoothSocket sock;
    private BluetoothPort port;
    private Thread sewooHandlerThread;

    public LKPXXPrinter(Handler handler, String address) {
        super(handler, address);
        this.port = BluetoothPort.getInstance();
        this.printer = new ESCPOSPrinter();
    }

    @Override
	public void connect() throws CouldNotConnectException {
        this.printConnectTries = 0;
        this.maxConnectTries = Configure.getPrinterConnectTry();
        try {
            connectPrinter(this.address);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new CouldNotConnectException();
        }
    }

    private void connectPrinter(String address) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice dev = bluetoothAdapter.getRemoteDevice(address);
        try {
            port.connect(dev);
            if (port.isConnected()) {
                createHandler();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createHandler() {
        RequestHandler sewooHandler = new RequestHandler();
        sewooHandlerThread = new Thread(sewooHandler);
        sewooHandlerThread.start();
    }


    private void removeHandler() {
        if ((sewooHandlerThread != null) && (sewooHandlerThread.isAlive())) {
            sewooHandlerThread.interrupt();
        }
    }

    @Override
	public void disconnect() throws CouldNotDisconnectException {
        try {
            port.disconnect();
            removeHandler();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
	protected void printLine(String data) {
        String ascii = data.replace("é", "e");
        ascii = ascii.replace("è", "e");
        ascii = ascii.replace("ê", "e");
        ascii = ascii.replace("ë", "e");
        ascii = ascii.replace("à", "a");
        ascii = ascii.replace("ï", "i");
        ascii = ascii.replace("ô", "o");
        ascii = ascii.replace("ç", "c");
        ascii = ascii.replace("ù", "u");
        ascii = ascii.replace("É", "E");
        ascii = ascii.replace("È", "E");
        ascii = ascii.replace("Ê", "E");
        ascii = ascii.replace("Ë", "E");
        ascii = ascii.replace("À", "A");
        ascii = ascii.replace("Ï", "I");
        ascii = ascii.replace("Ô", "O");
        ascii = ascii.replace("Ç", "c");
        ascii = ascii.replace("Ù", "u");
        ascii = ascii.replace("€", "E");
        try {
            this.printer.printNormal(ascii + LF);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void printBitmap(Bitmap bitmap) {
        super.printBitmap(bitmap);
        try {
            this.printer.printBitmap(bitmap, ESCPOSConst.LK_ALIGNMENT_CENTER, ESCPOSConst.LK_BITMAP_NORMAL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
	protected void printLine() {
        this.printer.lineFeed(1);
    }

    @Override
    protected void cut() {

    }

    @Override
    protected void flush() {
        super.flush();
        printer.lineFeed(2);
    }

    @Override
    public boolean isConnected() {
        return port.isConnected();
    }
}
