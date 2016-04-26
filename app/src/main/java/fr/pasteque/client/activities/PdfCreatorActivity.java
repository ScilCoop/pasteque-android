package fr.pasteque.client.activities;

import android.os.Bundle;
import android.view.View;
import fr.pasteque.client.R;

/**
 * Created by svirch_n on 25/04/16
 * Last edited at 16:50.
 */
public class PdfCreatorActivity extends TrackedActivity {

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.pdf_invoice_viewer);
    }

    public View getView() {
        return findViewById(R.id.pdf);
    }
}
