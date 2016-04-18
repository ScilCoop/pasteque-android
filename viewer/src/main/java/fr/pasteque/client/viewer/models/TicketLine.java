package fr.pasteque.client.viewer.models;

import android.graphics.Bitmap;
import fr.pasteque.api.models.ProductModel;
import fr.pasteque.api.models.TicketLineModel;

/**
 * Created by svirch_n on 14/04/16
 * Last edited at 17:03.
 */
public class TicketLine {

    public Product product;
    public String quantity;
    public long id;

    public TicketLine(TicketLineModel ticketLineModel) {
        this.copy(ticketLineModel);
    }

    private void copy(TicketLineModel ticketLineModel) {
        product = new Product(ticketLineModel.product);
        quantity = ticketLineModel.quantity;
    }

    @Override
    public String toString() {
        return product.label + " x" + quantity;
    }
}
