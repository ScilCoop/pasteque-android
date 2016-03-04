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


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;


import fr.pasteque.client.drivers.PowaDeviceManager;
import fr.pasteque.client.utils.BitmapManipulation;
import fr.pasteque.client.utils.StringUtils;
import fr.pasteque.client.utils.exception.CouldNotConnectException;
import fr.pasteque.client.utils.exception.CouldNotDisconnectException;

public class PowaPrinter extends BasePrinter {

    private static final String TAG = "PowaPrinter";
    private String receipt;
    private boolean bManualDisconnect;
    private PowaDeviceManager.PowaPrinterCommand command;

    public PowaPrinter(PowaDeviceManager.PowaPrinterCommand command, Handler handler) {
        super(handler);
        this.command = command;
    }

    @Override
    public boolean isConnected() {
        return command.isConnected();
    }

    @Override
    public void connect() throws CouldNotConnectException {
        // Start Powa printer
        this.bManualDisconnect = false;
        this.receipt = "";
        if (command.isConnected()) {
            throw new CouldNotConnectException("Can not print with Powa Printer, MCU Disconnected");
        }
    }

    @Override
    public void disconnect() throws CouldNotDisconnectException {
        this.receipt = "";
        this.bManualDisconnect = true;
    }

    @Override
    protected void printLine(String data) {
        String ascii = StringUtils.formatAscii(data);
        while (ascii.length() > 32) {
            //Get the last word that fit
            //If no such word exist just cut the 32th character
            int index = (ascii.substring(0, 32)).lastIndexOf(" ");
            if (index == -1 || ascii.charAt(32) == ' ') {
                index = 32;
            }
            String sub = ascii.substring(0, index);
            command.printText("        " + sub + "        ");
            //Remove the useless space at the beginning if exists
            if (ascii.charAt(index) == ' ')
                index++;
            ascii = ascii.substring(index);
        }
        command.printText("        " + ascii + "        ");
    }

    @Override
    protected void printLine() {
        command.printText(" ");
    }

    @Override
    protected void flush() {
        command.printReceipt();
    }

    @Override
    protected void printBitmap(Bitmap bitmap) {
        super.printBitmap(bitmap);
        command.printImage(BitmapManipulation.centeredBitmap(bitmap, 572));
    }

    @Override
    protected void cut() {
    }

    @Override
    protected void initPrint() {
        command.startReceipt();
    }

    protected void printDone() {
        // Handled in PowaCallback
    }
}
