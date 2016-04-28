package fr.pasteque.client.widgets.pdf;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import fr.pasteque.client.utils.CouldNotCreatePdfException;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by svirch_n on 26/04/16
 * Last edited at 10:02.
 */
public class Pdf extends RelativeLayout {

    /**
     * 160 * 8.3
     * 160 * 11.7
     */
    public static final int PDF_WIDTH_DP = 1323;
    public static final int PDF_HEIGHT_DP = 1870;

    public Pdf(Context context) {
        super(context);
    }

    public Pdf(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Pdf(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Pdf(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(PDF_WIDTH_DP, MeasureSpec.EXACTLY);
        heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(PDF_HEIGHT_DP, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void build(OutputStream stream) throws CouldNotCreatePdfException {
        prepareView();
        PdfDocument document = prepareDocument();
        writeDocument(stream, document);
    }

    private void prepareView() {
        this.layout(0, 0, this.getMeasuredWidth(), this.getMeasuredHeight());
        this.forceLayout();
        this.setDrawingCacheEnabled(true);
        this.buildDrawingCache(true);
        this.requestLayout();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private PdfDocument prepareDocument() {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(this.getMeasuredWidth(), this.getMeasuredHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        this.draw(page.getCanvas());
        document.finishPage(page);
        return document;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void writeDocument(OutputStream stream, PdfDocument document) throws CouldNotCreatePdfException {
        // write the document content
        try {
            document.writeTo(stream);
        } catch (IOException e) {
            throw new CouldNotCreatePdfException(e);
        }

        // close the document
        document.close();
    }
}
