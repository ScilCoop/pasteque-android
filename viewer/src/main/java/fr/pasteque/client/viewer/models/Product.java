package fr.pasteque.client.viewer.models;

import android.graphics.Bitmap;
import fr.pasteque.api.models.ProductModel;

/**
 * Created by svirch_n on 14/04/16
 * Last edited at 17:06.
 */
public class Product {

    public String id;
    public Bitmap bitmap;
    public byte[] image;
    public String label;

    public Product(ProductModel product) {
        this.copy(product);
    }

    public void copy(ProductModel data) {
        this.id = data.id;
        this.image = data.image;
        this.label = data.label;
    }
}
