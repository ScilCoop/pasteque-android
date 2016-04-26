package fr.pasteque.client;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.view.View;
import fr.pasteque.client.activities.PdfCreatorActivity;
import fr.pasteque.client.utils.PdfCreator;
import fr.pasteque.client.utils.file.InternalFile;
import fr.pasteque.client.widgets.Pdf;

import java.io.FileOutputStream;

/**
 * Created by svirch_n on 25/04/16
 * Last edited at 16:48.
 */
public class PdfCreatorActivityTest extends ActivityInstrumentationTestCase2<PdfCreatorActivity> {
    public PdfCreatorActivityTest() {
        super(PdfCreatorActivity.class);
    }

    @UiThreadTest
    public void testPdf() throws Exception {
        final View view = getActivity().getView();
        PdfCreator.build((Pdf) view, new FileOutputStream(new InternalFile("aPdf.pdf")));
    }
}
