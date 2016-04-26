package fr.pasteque.client.utils;

import android.annotation.TargetApi;
import android.graphics.pdf.PdfDocument;

import static android.graphics.pdf.PdfDocument.Page;
import static android.graphics.pdf.PdfDocument.PageInfo;

import android.os.Build;
import android.view.View;
import fr.pasteque.client.widgets.Pdf;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by svirch_n on 25/04/16
 * Creates a pdf from a view
 * Last edited at 16:42.
 */
public class PdfCreator {

    public static final int PDF_WIDTH_DP = 1323;
    public static final int PDF_HEIGHT_DP = 1870;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static PdfDocument build(Pdf view, OutputStream stream) throws CouldNotCreatePdfException {
        // create a new document
        PdfDocument document = new PdfDocument();

        // crate a page description
        PageInfo pageInfo = new PageInfo.Builder(PDF_WIDTH_DP, PDF_HEIGHT_DP, 1).create();

        // start a page
        Page page = document.startPage(pageInfo);

        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.forceLayout();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        view.requestLayout();

        view.draw(page.getCanvas());

        // finish the page
        document.finishPage(page);

        // write the document content
        try {
            document.writeTo(stream);
        } catch (IOException e) {
            throw new CouldNotCreatePdfException(e);
        }

        // close the document
        document.close();

        return new PdfDocument();
    }
}
