package fr.pasteque.api.models;

/**
 * Created by svirch_n on 14/04/16
 * Last edited at 12:26.
 */
public class ProductModel {

    public String id;
    public byte[] image;
    public String label;

    public ProductModel() {
    }

    public void copy(ProductModel data) {
        this.id = data.id;
        this.image = data.image;
        this.label = data.label;
    }
}
