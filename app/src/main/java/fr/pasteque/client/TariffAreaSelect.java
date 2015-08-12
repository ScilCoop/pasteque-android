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
package fr.pasteque.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

import fr.pasteque.client.data.Data;
import fr.pasteque.client.data.DataSavable.TariffAreaData;
import fr.pasteque.client.models.TariffArea;
import fr.pasteque.client.widgets.TariffAreasAdapter;

public class TariffAreaSelect extends Activity
implements AdapterView.OnItemClickListener {

    private static final String LOG_TAG = "Pasteque/TariffAreaSelect";

    private ListView list;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        // Set views
        setContentView(R.layout.tariff_area_select);
        this.list = (ListView) this.findViewById(R.id.tariff_areas_list);
        List<TariffArea> data = new ArrayList<TariffArea>();
        data.add(null);
        data.addAll(Data.TariffArea.areas);
        this.list.setAdapter(new TariffAreasAdapter(data));
        this.list.setOnItemClickListener(this);
    }

    @Override
	public void onItemClick(AdapterView<?> parent, View v, int position,
                            long id) {
        TariffArea a = (TariffArea) this.list.getAdapter().getItem(position);
        Intent i = new Intent();
        i.putExtra("tariffArea", a);
        this.setResult(Activity.RESULT_OK, i);
        this.finish();
    }
}
