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
package fr.pasteque.client.printing;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import com.woosim.printer.WoosimCmd;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class WoosimPrinter extends BasePrinter {

    // Unique UUID for this application
	private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    private BluetoothSocket sock;
    private OutputStream printerStream;

    public WoosimPrinter(Context ctx, String address, Handler callback) {
        super(ctx, address, callback);
    }

    @Override
	public void connect() throws IOException {
        BluetoothAdapter btadapt = BluetoothAdapter.getDefaultAdapter();
        try {
            BluetoothDevice dev = btadapt.getRemoteDevice(this.address);
            // Get a BluetoothSocket
            this.sock = dev.createRfcommSocketToServiceRecord(SPP_UUID);
            new ConnTask().execute(dev);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            connected = false;
        }
    }

    @Override
	public void disconnect() throws IOException {
        try {
            this.sock.close();
            if (this.printerStream != null) {
                this.printerStream.close();
            }
        } catch (IOException e) {
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
            this.printerStream.write(ascii.getBytes());
            this.printerStream.write(WoosimCmd.printData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
	protected void printLine() {
        try {
            this.printerStream.write(WoosimCmd.printData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
	protected void cut() {
        try {
            this.printerStream.write(WoosimCmd.cutPaper(WoosimCmd.CUT_PARTIAL));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	// Bluetooth Connection Task.
	class ConnTask extends AsyncTask<BluetoothDevice, Void, Integer> {

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(BluetoothDevice... params)
		{
			Integer retVal = null;
			try
			{
                sock.connect();
                printerStream = sock.getOutputStream();
                printerStream.write(WoosimCmd.initPrinter());
				retVal = new Integer(0);
			}
			catch (IOException e) {
                e.printStackTrace();
				retVal = new Integer(-1);
			}
			return retVal;
		}

		@Override
		protected void onPostExecute(Integer result)
		{
			if(result == 0)	// Connection success.
			{
				connected = true;
				if (queued != null) {
					printReceipt(queued);
				}
                if (zQueued != null) {
                    printZTicket(zQueued, crQueued);
                }
			}
			else	// Connection failed.
			{
				WoosimPrinter.super.printDoneWithError();
			}
			super.onPostExecute(result);
		}
	}
}
