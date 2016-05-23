package fr.pasteque.client.widgets;

import android.content.Context;
import android.widget.Button;
import fr.pasteque.client.Pasteque;

/**
 * Created by svirch_n on 23/05/16
 * Last edited at 17:09.
 */
public class PlaceButton extends Button {

    private int placeX;
    private int placeY;

    public PlaceButton(Context context) {
        super(context);
    }

    public void setPlaceX(int placeX) {
        this.placeX = placeX;
    }

    public void setPlaceY(int placeY) {
        this.placeY = placeY;
    }

    public void rate(int width, int height) {
        float widthRate = width / Pasteque.getRestaurantMapWidth();
        float heightRate = height / Pasteque.getRestaurantMapHeight();
        setX(placeX * widthRate);
        setY(placeY * heightRate);
    }
}
