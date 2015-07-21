package fr.pasteque.client;

import android.content.Intent;
import android.view.MenuItem;

public class BnpTransaction extends Transaction {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // if the item is not catch in the super
        if (!super.onOptionsItemSelected(item)) {
            switch (item.getItemId()) {
                case R.id.ab_menu_open_bnp:
                    String url = "https://www.secure.bnpparibas.net/banque/portail/particulier/HomePage?type=site";
                    Intent accessBnp = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
                    startActivity(accessBnp);
                    return true;
            }
        }
        return false;
    }

}
