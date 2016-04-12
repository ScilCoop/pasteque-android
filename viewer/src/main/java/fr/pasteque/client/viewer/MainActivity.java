package fr.pasteque.client.viewer;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import fr.pasteque.api.API;
import fr.pasteque.api.connection.Connection;
import fr.pasteque.api.gatherer.Gatherer;
import fr.pasteque.api.gatherer.JsonGatherer;
import fr.pasteque.api.gatherer.smart.JsonArrayContentGatherer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by svirch_n on 07/04/16
 * Last edited at 17:01.
 */
public class MainActivity extends FragmentActivity {

    private static final int MENU_CONFIG_ID = 1;
    private List<String> content = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem config = menu.add(Menu.NONE, MENU_CONFIG_ID, 1,
                this.getString(R.string.menu_config));
        config.setIcon(R.drawable.ico_reglage);
        config.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_CONFIG_ID) {
            Intent i = new Intent(this, Configure.class);
            this.startActivity(i);
            return true;
        }
        return false;
    }

    public void download(View view) {
        API api = new API(Pasteque.getConfiguration());
        api.Tickets.getAllSharedTicket(new JsonArrayContentGatherer(new Gatherer.Handler<JSONArray>() {
            @Override
            public void result(JSONArray array) throws JSONException {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject json = array.getJSONObject(i);
                    JSONArray lines = json.getJSONArray("lines");
                    for (int j = 0; j < lines.length(); j++) {
                        MainActivity.this.content.add(lines.getJSONObject(j).getString("productId"));
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.text)).setText(MainActivity.this.content.toString());
                    }
                });

            }
        }));
        Log.d("Pasteque", api.Images.getProduct("9b20697aa3cef428249df60ff00c5963"));
    }
}
