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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import java.io.IOException;

import fr.postech.client.data.ReceiptData;
import fr.postech.client.models.Receipt;
import fr.postech.client.widgets.ReceiptsAdapter;

public class ReceiptSelect extends Activity
implements AdapterView.OnItemClickListener {

    private static final String LOG_TAG = "POS-TECH/ReceiptSelect";

    private ListView list;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        // Set views
        setContentView(R.layout.receipt_select);
        this.list = (ListView) this.findViewById(R.id.receipts_list);
        this.list.setAdapter(new ReceiptsAdapter(ReceiptData.getReceipts()));
        this.list.setOnItemClickListener(this);
    }

    public void onItemClick(AdapterView parent, View v,
            int position, long id) {
        final Receipt r = ReceiptData.getReceipts().get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String label = this.getString(R.string.ticket_label,
                r.getTicket().getLabel());
        builder.setTitle(label);
        String[] items = new String[] { this.getString(R.string.delete) };
        builder.setItems(items, new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int which) {
               ReceiptData.getReceipts().remove(r);
               try {
                   ReceiptData.save(ReceiptSelect.this);
               } catch(IOException e) {
                   Log.e(LOG_TAG, "Unable to save receipts", e);
                   Error.showError(R.string.err_save_receipts, ReceiptSelect.this);
               }
               if (ReceiptData.hasReceipts()) {
                   ReceiptSelect.this.list.setAdapter(new ReceiptsAdapter(ReceiptData.getReceipts()));
               } else {
                   ReceiptSelect.this.finish();
               }
           }
        });
        builder.show();
    }

}
