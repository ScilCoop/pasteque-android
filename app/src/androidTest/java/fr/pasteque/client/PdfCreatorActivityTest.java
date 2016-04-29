package fr.pasteque.client;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import fr.pasteque.client.activities.PdfCreatorActivity;
import fr.pasteque.client.data.Data;
import fr.pasteque.client.models.Receipt;
import fr.pasteque.client.utils.file.InternalFile;
import fr.pasteque.client.widgets.pdf.Pdf;

import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by svirch_n on 25/04/16
 * Last edited at 16:48.
 */
public class PdfCreatorActivityTest extends ActivityInstrumentationTestCase2<PdfCreatorActivity> {

    public PdfCreatorActivityTest() {
        super(PdfCreatorActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        List<Receipt> receiptList = Data.Receipt.getReceipts(null);
        Intent intent = new Intent().putExtra(PdfCreatorActivity.TICKET_TAG, receiptList.get(1).getTicket());
        setActivityIntent(intent);
    }

    public void testPdf() throws Throwable {
        final Pdf pdf = getActivity().getPdf();
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    pdf.build(new FileOutputStream(new InternalFile("aPdf.pdf")));
                } catch (Exception e) {
                    fail();
                }
            }
        });
    }
}
