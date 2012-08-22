/*
    POS-Tech Android
    Copyright (C) 2012 SARL SCOP Scil (contact@scil.coop)

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
package fr.postech.client;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;

import fr.postech.client.data.CashData;
import fr.postech.client.data.ReceiptData;
import fr.postech.client.models.Cash;
import fr.postech.client.models.Receipt;

public class Debug extends Activity {

    private static final String LOG_TAG = "POS-TECH/Debug";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug);
        this.refresh();
    }

    public void refresh() {
        TextView cash = (TextView) this.findViewById(R.id.dbg_current_cash);
        if (CashData.currentCash == null) {
            cash.setText("Null");
        } else {
            Cash c = CashData.currentCash;
            String strCash = "Id: " + c.getId() + "\n";
            strCash += "Host: " + c.getMachineName() + "\n";
            strCash += "Open date: ";
            if (c.wasOpened()) {
                Date d = new Date(c.getOpenDate() * 1000);
                Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                strCash += cal.get(Calendar.YEAR) + "-"
                    + (cal.get(Calendar.MONTH) + 1) + "-"
                    + cal.get(Calendar.DAY_OF_MONTH) + " "
                    + cal.get(Calendar.HOUR_OF_DAY) + ":"
                    + cal.get(Calendar.MINUTE) + "\n";
            } else {
                strCash += "not opened\n";
            }
            strCash += "Close date: ";
            if (c.isClosed()) {
                Date d = new Date(c.getCloseDate() * 1000);
                Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                strCash += cal.get(Calendar.YEAR) + "-"
                    + (cal.get(Calendar.MONTH) + 1) + "-"
                    + cal.get(Calendar.DAY_OF_MONTH) + " "
                    + cal.get(Calendar.HOUR_OF_DAY) + ":"
                    + cal.get(Calendar.MINUTE) + "\n";
            } else {
                strCash += "not closed\n";
            }
            strCash += "Dirty: " + CashData.dirty;
            cash.setText(strCash);
        }

        TextView rcpts = (TextView) this.findViewById(R.id.dbg_receipts);
        String strrcpts = ReceiptData.getReceipts().size() + " tickets\n";
        for (Receipt r : ReceiptData.getReceipts()) {
            try {
                strrcpts += r.toJSON().toString(2) + "\n";
            } catch (Exception e) {
                e.printStackTrace();
                StringWriter w = new StringWriter();
                e.printStackTrace(new PrintWriter(w));
                strrcpts += w.toString();
            }
        }
        rcpts.setText(strrcpts);
    }

    public void deleteCash(View v) {
        CashData.clear(this);
        this.refresh();
    }

    public void deleteReceipts(View v) {
        ReceiptData.clear(this);
        this.refresh();
    }
}