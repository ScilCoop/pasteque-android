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

import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;

import com.mpowa.android.sdk.common.base.PowaEnums.ConnectionState;
import com.mpowa.android.sdk.powapos.core.PowaPOSEnums;

import fr.pasteque.client.utils.BitmapManipulation;
import fr.pasteque.client.utils.PastequePowaPos;
import fr.pasteque.client.utils.StringUtils;
import fr.pasteque.client.utils.exception.CouldNotConnectException;
import fr.pasteque.client.utils.exception.CouldNotDisconnectException;

public class PowaPrinter extends BasePrinter {

    private static final String TAG = "PowaPrinter";
    private String receipt;
    private boolean bManualDisconnect;
    private boolean connected;

    public PowaPrinter(Handler handler) {
        super(handler);
        this.bManualDisconnect = false;
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    @Override
    public void connect() throws CouldNotConnectException {
        // Start Powa printer
        this.bManualDisconnect = false;
        this.receipt = "";
        if (PastequePowaPos.getSingleton().getMCU().getConnectionState()
                .equals(ConnectionState.DISCONNECTED)) {
            this.connected = false;
            throw new CouldNotConnectException("Can not print with Powa Printer, MCU Disconnected");
        }
        this.connected = true;
    }

    @Override
    public void disconnect() throws CouldNotDisconnectException {
        PastequePowaPos.getSingleton().removeCallback(TAG);
        this.receipt = "";
        this.connected = false;
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
            PastequePowaPos.getSingleton().printText("        " + sub + "        ");
            //Remove the useless space at the beginning if exists
            if (ascii.charAt(index) == ' ')
                index++;
            ascii = ascii.substring(index);
        }
        PastequePowaPos.getSingleton().printText("        " + ascii + "        ");
    }

    @Override
    protected void printLine() {
        PastequePowaPos.getSingleton().printText(" ");
    }

    @Override
    protected void flush() {
        PastequePowaPos.getSingleton().printReceipt();
    }

    @Override
    protected void printBitmap(Bitmap bitmap) {
        super.printBitmap(bitmap);

        PastequePowaPos.getSingleton().printImage(BitmapManipulation.centeredBitmap(bitmap, 572));
    }

    @Override
    protected void cut() {
    }

    @Override
    protected void initPrint() {
        PastequePowaPos.getSingleton().startReceipt();
    }

    protected void printDone() {
        // Handled in PowaCallback
    }
}
